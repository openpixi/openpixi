package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.MasterToWorkers;
import org.openpixi.pixi.distributed.partitioning.Partitioner;
import org.openpixi.pixi.distributed.partitioning.SimplePartitioner;
import org.openpixi.pixi.distributed.util.CountLock;
import org.openpixi.pixi.distributed.util.IncomingResultHandler;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.ParticleGridInitializer;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LocalInterpolation;
import org.openpixi.pixi.physics.util.ClassCopier;
import org.openpixi.pixi.physics.util.IntBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Distributes the problem and collects the results.
 * To each worker we assign exactly one problem.
 */
public class Master {

	private MasterToWorkers communicator;
	private Settings settings;

	private Grid initialGrid;
	private List<Particle> initialParticles;

	private Grid finalGrid;
	private List<Particle> finalParticles;

	/**
	 * Problem decomposition table.
	 * N-th element in the table belongs to the n-th worker.
	 */
	private IntBox[] partitions;

	/* Results received from workers */
	private Cell[][][] gridPartitions;
	private List<List<Particle>> particlePartitions = new ArrayList<List<Particle>>();

	private CountLock resultsLock;


	public Grid getFinalGrid() {
		return finalGrid;
	}

	public List<Particle> getFinalParticles() {
		return finalParticles;
	}


	public Master(IbisRegistry registry, Settings settings) {
		this.settings = settings;
		try {
			communicator = new MasterToWorkers(registry, new ResultHandler());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		// Initialize the grid

		initialGrid = new Grid(settings);
		initialParticles = settings.getParticles();

		ParticleGridInitializer pgi = new ParticleGridInitializer();
		pgi.initialize(
				new LocalInterpolation(
						settings.getInterpolator(), settings.getParticleIterator()),
				settings.getPoissonSolver(),
				initialParticles,
				initialGrid);

		// Initialize the classes holding the result
		gridPartitions = new Cell[settings.getNumOfNodes()][][];
		for (int i = 0; i < settings.getNumOfNodes(); i++) {
			particlePartitions.add(new ArrayList<Particle>());
		}

		resultsLock = new CountLock(settings.getNumOfNodes());
	}


	public void distributeProblem() {
		// Partition the problem
		Partitioner partitioner = new SimplePartitioner();
		partitions = partitioner.partition(
				settings.getGridCellsX(), settings.getGridCellsY(), settings.getNumOfNodes());

		// Log the partitioning scheme
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.debug("Problem partitioning:\n{}", partitioner);

		// Partition the data
		List<Particle> initialParticlesCopy = copyInitialParticles();
		List<List<Particle>> particlePartitions = partitionParticles(
				partitions, initialParticlesCopy);
		Cell[][][] gridPartitions = partitionGrid(partitions, initialGrid);

		// Send to each worker
		for (int workerID = 0; workerID < partitions.length; workerID++) {
			try {
				communicator.sendProblem(
						workerID, partitions,
						particlePartitions.get(workerID), gridPartitions[workerID]);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}


	private List<Particle> copyInitialParticles() {
		List<Particle> copy = new ArrayList<Particle>();
		for (Particle p: initialParticles) {
			copy.add(ClassCopier.copy(p));
		}
		return copy;
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
	 * At the global simulation edges we also the extra cells!
	 * Since there is some initialization done on the grid
	 * (density charge interpolation + poisson solver)
	 * we need to distribute also the extra cells.
	 *
	 * At the inner partitions of global simulation we still need the ghost cells
	 * for correct field solving.
	 */
	private Cell[][] getSubgrid(IntBox partition, Grid grid) {
		int startX = partition.xmin() - Grid.EXTRA_CELLS_BEFORE_GRID;
		int endX = partition.xmax() + Grid.EXTRA_CELLS_AFTER_GRID;
		int startY = partition.ymin() - Grid.EXTRA_CELLS_BEFORE_GRID;
		int endY = partition.ymax() + Grid.EXTRA_CELLS_AFTER_GRID;

		Cell[][] subGrid = new Cell[endX - startX + 1][endY - startY + 1];
		for (int x = startX; x <= endX ; x++) {
			for (int y = startY; y <= endY; y++) {
				// We create a new copy of the cell so that each cell is unique.
				// In another words we want to avoid shared references
				// present in periodic boundaries.
				subGrid[x - startX][y - startY] = ClassCopier.copy(grid.getCell(x,y));
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
			int cellX = (int)Math.floor(p.getX() / initialGrid.getCellWidth());
			int cellY = (int)Math.floor(p.getY() / initialGrid.getCellHeight());

			for  (int i = 0; i < partitions.length; ++i) {
				if (partitions[i].contains(cellX, cellY)) {
					partitionIndex = i;
				}
			}

			assert partitionIndex != -1;

			// Translate particle's position
			double xmin = partitions[partitionIndex].xmin() * settings.getCellWidth();
			double ymin = partitions[partitionIndex].ymin() * settings.getCellHeight();
			p.setX(p.getX() - xmin);
			p.setY(p.getY() - ymin);

			particlePartitions.get(partitionIndex).add(p);
		}
		return particlePartitions;
	}


	public void collectResults() {
		resultsLock.waitForCount();
		resultsLock.reset();
		finalParticles = assembleParticles(particlePartitions);
		finalGrid = assembleGrid(gridPartitions);
	}


	/**
	 * Puts together the particle lists coming from workers.
	 */
	private List<Particle> assembleParticles(List<List<Particle>> particlePartitions) {
		List<Particle> assembledParticles = new ArrayList<Particle>();
		for (int i = 0; i < particlePartitions.size(); ++i) {

			double xmin = partitions[i].xmin() * settings.getCellWidth();
			double ymin = partitions[i].ymin() * settings.getCellHeight();

			for (Particle p: particlePartitions.get(i)) {

				// Translate particles position
				p.setX(p.getX() + xmin);
				p.setY(p.getY() + ymin);

				assembledParticles.add(p);
			}
		}
		return assembledParticles;
	}


	/**
	 * Puts together the subgrids coming from workers.
	 */
	private Grid assembleGrid(Cell[][][] gridPartitions) {
		int totalXCells =
				Grid.EXTRA_CELLS_BEFORE_GRID +
				settings.getGridCellsX() +
				Grid.EXTRA_CELLS_AFTER_GRID;
		int totalYCells =
				Grid.EXTRA_CELLS_BEFORE_GRID +
				settings.getGridCellsY() +
				Grid.EXTRA_CELLS_AFTER_GRID;

		Cell[][] cells = new Cell[totalXCells][totalYCells];
		for (int workerID = 0; workerID < partitions.length; ++workerID) {
			fillSubgrid(partitions[workerID], gridPartitions[workerID], cells);
		}

		return new Grid(settings, cells);
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
			for (int y = startY; y <= endY; y++) {
				cells[x + Grid.EXTRA_CELLS_BEFORE_GRID][y + Grid.EXTRA_CELLS_BEFORE_GRID] =
						subgrid[x - startX][y - startY];
			}
		}
	}


	public void close() {
		communicator.close();
	}


	private class ResultHandler implements IncomingResultHandler {
		public void handle(int workerID, List<Particle> particles, Cell[][] cells) {
			gridPartitions[workerID] = cells;
			particlePartitions.set(workerID, particles);
			resultsLock.increase();
		}
	}
}
