package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.assigning.PartitionAssigner;
import org.openpixi.pixi.distributed.assigning.SimplePartitionAssigner;
import org.openpixi.pixi.distributed.ibis.MasterCommunicator;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.partitioning.Partitioner;
import org.openpixi.pixi.distributed.partitioning.SimplePartitioner;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Distributes the problem and collects the results.
 */
public class Master {

	private MasterCommunicator communicator;
	private Settings settings;

	private Grid initialGrid;
	private List<Particle> initialParticles;

	private Grid finalGrid;
	private List<Particle> finalParticles;

	/** Problem decomposition table. */
	IntBox[] partitions;
	/** Assigns each problem to a node.
	 * i-th element is node responsible for calculating i-th partition. */
	int[] assignment;


	public Master(IbisRegistry registry, Settings settings) throws Exception {
		this.settings = settings;
		communicator = new MasterCommunicator(registry);

		initialGrid = new Grid(settings);
		initialParticles = settings.getParticles();
	}


	public void distributeProblem() throws IOException {
		Partitioner partitioner = new SimplePartitioner();
		partitions = partitioner.partition(
				settings.getGridCellsX(), settings.getGridCellsY(), settings.getNumOfNodes());

		PartitionAssigner assigner = new SimplePartitionAssigner();
		assignment = assigner.assign(partitions, settings.getNumOfNodes());

		List<List<Particle>> particlePartitions = partitionParticles(
				partitions, initialParticles);
		Cell[][][] gridPartitions = partitionGrid(partitions, initialGrid);

		communicator.distributeProblem(partitions, assignment, particlePartitions, gridPartitions);
	}


	public void collectResults() throws Exception {
		ResultsHolder results = communicator.collectResults();
		assembleParticles(results.particlePartitions);
		assembleGrid(results.nodeIDs, results.gridPartitions);
	}


	/**
	 * Divides cells according to partitions.
	 */
	private Cell[][][] partitionGrid(IntBox[] partitions, Grid grid) {
		Cell[][][] gridPartitions = new Cell[partitions.length][][];
		for (int i = 0; i < partitions.length; ++i) {
			gridPartitions[i] = getSubgrid(partitions[i], grid);
		}
		return gridPartitions;
	}


	/**
	 * At the borders returns also the extra cells!
	 * Since there is some initialization done on the grid
	 * (density charge interpolation + poisson solver)
	 * we need to distribute also the extra cells.
	 */
	private Cell[][] getSubgrid(IntBox partition, Grid grid) {
		int startX = partition.xmin();
		int endX = partition.xmax();
		int startY = partition.ymin();
		int endY = partition.ymax();
		if (startX == 0) {
			startX -= Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endX == settings.getGridCellsX() - 1) {
			endX += Grid.EXTRA_CELLS_AFTER_GRID;
		}
		if (startY == 0) {
			startY -= Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endY == settings.getGridCellsY() - 1) {
			endY += Grid.EXTRA_CELLS_AFTER_GRID;
		}

		Cell[][] subGrid = new Cell[endX - startX + 1][endY - startY + 1];
		for (int x = startX; x <= endX ; x++) {
			for (int y = startY; y < endY; y++) {
				subGrid[x - startX][y - startY] = grid.getCell(x,y);
			}
		}
		return  subGrid;
	}


	/**
	 * Divides particles according to partitions they belong to.
	 */
	private List<List<Particle>> partitionParticles(IntBox[] partitions, List<Particle> particles) {
		List<List<Particle>> particlePartitions = new ArrayList<List<Particle>>();
		for (int i = 0; i < partitions.length; ++i) {
			particlePartitions.add(new ArrayList<Particle>());
		}

		for (Particle p: particles) {
			int partitionIndex = -1;
			for  (int i = 0; i < partitions.length; ++i) {
				int cellX = (int)p.getX() / initialGrid.getNumCellsX();
				int cellY = (int)p.getY() / initialGrid.getNumCellsY();

				if (partitions[i].contains(cellX, cellY)) {
					partitionIndex = i;
				}
			}
			assert partitionIndex != -1;
			particlePartitions.get(partitionIndex).add(p);
		}
		return particlePartitions;
	}


	/**
	 * Puts together the particles coming from workers.
	 */
	private void assembleParticles(List<List<Particle>> particlePartitions) {
		for (List<Particle> particles: particlePartitions) {
			finalParticles.addAll(particles);
		}
	}


	/**
	 * Puts together the subgrids coming from workers.
	 */
	private void assembleGrid(int[] nodeIDs, Cell[][][] gridPartitions) {
		int totalXCells = Grid.EXTRA_CELLS_BEFORE_GRID +
				settings.getGridCellsX() +
				Grid.EXTRA_CELLS_AFTER_GRID;
		int totalYCells = Grid.EXTRA_CELLS_BEFORE_GRID +
				settings.getGridCellsY() +
				Grid.EXTRA_CELLS_AFTER_GRID;

		Cell[][] cells = new Cell[totalXCells][totalYCells];
		for (int i = 0; i < partitions.length; ++i) {
			int subgridIndex = findSubgridIndex(assignment[i], nodeIDs);
			fillSubgrid(partitions[i], gridPartitions[subgridIndex], cells);
		}

		finalGrid = new Grid(cells, settings.getSimulationWidth(), settings.getSimulationHeight());
	}


	private int findSubgridIndex(int nodeID, int[] nodeIDs) {
		for (int i = 0; i < nodeIDs.length; i++) {
			if (nodeIDs[i] == nodeID) {
				return i;
			}
		}
		throw new RuntimeException("Could not find index of the subgrid!");
	}


	/**
	 * We fill also the extra cells.
	 * In case of hardwall boundary we have additional information in them.
	 */
	private void fillSubgrid(IntBox partition, Cell[][] subgrid, Cell[][] cells) {
		int startX = partition.xmin();
		int endX = partition.xmax();
		int startY = partition.ymin();
		int endY = partition.ymax();
		if (startX == 0) {
			startX -= Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endX == settings.getGridCellsX() - 1) {
			endX += Grid.EXTRA_CELLS_AFTER_GRID;
		}
		if (startY == 0) {
			startY -= Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endY == settings.getGridCellsY() - 1) {
			endY += Grid.EXTRA_CELLS_AFTER_GRID;
		}

		for (int x = startX; x <= endX ; x++) {
			for (int y = startY; y < endY; y++) {
				cells[x][y] = subgrid[x - startX][y - startY];
			}
		}
	}
}
