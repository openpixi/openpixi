package org.openpixi.pixi.distributed;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
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
	private Grid grid;


	public Master(IbisRegistry registry, Settings settings) throws Exception {
		this.settings = settings;
		communicator = new MasterCommunicator(registry);
		grid = new Grid(settings);
	}


	public void distributeProblem() throws IOException {
		Partitioner partitioner = new SimplePartitioner();
		IntBox[] partitions = partitioner.partition(
				settings.getGridCellsX(), settings.getGridCellsY(), settings.getNumOfNodes());

		PartitionAssigner assigner = new SimplePartitionAssigner();
		int[] assignment = assigner.assign(partitions, settings.getNumOfNodes());

		List<List<Particle>> particlePartitions = partitionParticles(
				partitions, settings.getParticles());
		Cell[][][] gridPartitions = partitionGrid(partitions, grid);

		communicator.distributeProblem(partitions, assignment, particlePartitions, gridPartitions);
	}


	public void collectResults() {
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
			startX -= grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endX == settings.getGridCellsX() - 1) {
			endX += grid.EXTRA_CELLS_AFTER_GRID;
		}
		if (startY == 0) {
			startY -= grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (endY == settings.getGridCellsY() - 1) {
			endY += grid.EXTRA_CELLS_AFTER_GRID;
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
				int cellX = (int)p.getX() / grid.getNumCellsX();
				int cellY = (int)p.getY() / grid.getNumCellsY();

				if (partitions[i].contains(cellX, cellY)) {
					partitionIndex = i;
				}
			}
			assert partitionIndex != -1;
			particlePartitions.get(partitionIndex).add(p);
		}
		return particlePartitions;
	}
}
