package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.grid.DistributedGridFactory;
import org.openpixi.pixi.distributed.grid.DistributedInterpolation;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerToMaster;
import org.openpixi.pixi.distributed.movement.boundary.DistributedParticleBoundaries;
import org.openpixi.pixi.distributed.util.BooleanLock;
import org.openpixi.pixi.distributed.util.IncomingProblemHandler;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.util.ClassCopier;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.List;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker {

	private WorkerToMaster communicator;

	/* Local and global settings differ only in settings connected to simulation size. */
	private Settings localSettings;
	private Settings globalSettings;

	/** ID of this worker. */
	private int workerID;

	private Simulation simulation;

	private SharedDataManager sharedDataManager;

	/* Received problem */
	private IntBox[] partitions;
	private List<Particle> particles;
	private Cell[][] cells;

	private BooleanLock recvProblemLock = new BooleanLock();


	public Worker(IbisRegistry registry, Settings settings) {
		this.globalSettings = settings;
		try {
			communicator = new WorkerToMaster(registry, new ProblemHandler());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		workerID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
	}


	public void step() {
		simulation.step();
	}


	public void receiveProblem() {
			recvProblemLock.waitForTrue();
			recvProblemLock.reset();
			createSimulation();
	}


	public void sendResults() {
		Cell[][] finalCells = getFinalCells(simulation.grid);

		// The results can come in arbitrary order; thus,
		// we have to send also the id of the node which is sending the result.
		try {
			communicator.sendResults(
					workerID,
					simulation.particles,
					finalCells);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	/**
	 * If the local simulation is at the edge of global simulation,
	 * we also need to include the extra cells to the master
	 * (because of hardwall boundaries).
	 */
	private Cell[][] getFinalCells(Grid grid) {
		IntBox mypart = partitions[workerID];

		int xstart = 0;
		int ystart = 0;
		int xend = mypart.xsize() - 1;
		int yend = mypart.ysize() - 1;
		if (mypart.xmin() == 0) {
			xstart -= Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (mypart.xmax() == globalSettings.getGridCellsX() - 1) {
			xend += Grid.EXTRA_CELLS_AFTER_GRID;
		}
		if (mypart.ymin() == 0) {
			ystart -= Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (mypart.ymax() == globalSettings.getGridCellsY() - 1) {
			yend += Grid.EXTRA_CELLS_AFTER_GRID;
		}

		Cell[][] finalCells = new Cell[xend - xstart + 1][yend - ystart + 1];
		for (int x = xstart; x <= xend ; ++x) {
			for (int y = ystart; y <= yend; ++y) {
				finalCells[x - xstart][y - ystart] = grid.getCell(x,y);
			}
		}
		return  finalCells;
	}


	private void createSimulation() {
		createLocalSettings();

		sharedDataManager =  createSharedDataManager();
		ParticleBoundaries particleBoundaries = createParticleBoundaries(sharedDataManager);
		sharedDataManager.setParticleBoundaries(particleBoundaries);

		Grid grid = createGrid(sharedDataManager);
		Interpolation interpolation = createInterpolationIterator(sharedDataManager);
		sharedDataManager.setGrid(grid);

		sharedDataManager.initializeCommunication();

		this.simulation = new Simulation(
				localSettings, grid, particles,
				particleBoundaries, interpolation);
	}


	/**
	 * Some settings (e.g. simulation width and height) pertain to the global simulation
	 * and are incorrect for the local simulation.
	 * Thus, we need to correct them, so that they correspond to the local simulation.
	 */
	private void createLocalSettings() {
		IntBox mypart = partitions[workerID];

		double cellWidth = globalSettings.getCellWidth();
		double cellHeight = globalSettings.getCellHeight();

		localSettings = ClassCopier.copy(globalSettings);

		localSettings.setGridCellsX(mypart.xsize());
		localSettings.setGridCellsY(mypart.ysize());
		localSettings.setSimulationWidth(cellWidth * mypart.xsize());
		localSettings.setSimulationHeight(cellHeight * mypart.ysize());
	}


	private Interpolation createInterpolationIterator(SharedDataManager sharedDataManager) {
		DoubleBox zoneOfLocalInfluence = new DoubleBox(
				(Grid.INTERPOLATION_RADIUS - 1) * localSettings.getCellWidth(),
				localSettings.getSimulationWidth() -
						Grid.INTERPOLATION_RADIUS * localSettings.getCellWidth(),
				(Grid.INTERPOLATION_RADIUS - 1) * localSettings.getCellHeight(),
				localSettings.getSimulationHeight() -
						Grid.INTERPOLATION_RADIUS * localSettings.getCellHeight());
		return new DistributedInterpolation(
				localSettings.getInterpolator(),
				sharedDataManager, zoneOfLocalInfluence, localSettings.getParticleIterator());
	}


	private SharedDataManager createSharedDataManager() {
		IntBox simulationAreaInCellDimensions = new IntBox(
				0, globalSettings.getGridCellsX() - 1, 0, globalSettings.getGridCellsY() - 1);
		return new SharedDataManager(
				workerID,
				partitions,
				simulationAreaInCellDimensions,
				localSettings.getBoundaryType(),
				communicator.getRegistry());
	}


	private Grid createGrid(SharedDataManager sharedDataManager) {
		DistributedGridFactory gridFactory = new DistributedGridFactory(
				localSettings, partitions[workerID],
				cells, sharedDataManager);
		return gridFactory.create();
	}


	private ParticleBoundaries createParticleBoundaries(SharedDataManager sharedDataManager) {
		DoubleBox simulationAreaInParticleDimensions = new DoubleBox(
				0, localSettings.getSimulationWidth(), 0, localSettings.getSimulationHeight());
		DoubleBox innerSimulationArea = new DoubleBox(
				0, localSettings.getSimulationWidth() - localSettings.getCellWidth(),
				0, localSettings.getSimulationHeight() - localSettings.getCellHeight());
		return new DistributedParticleBoundaries(
				simulationAreaInParticleDimensions, innerSimulationArea,
				localSettings.getParticleBoundary(), sharedDataManager);
	}


	public void close() {
		communicator.close();
		sharedDataManager.close();
	}


	private class ProblemHandler implements IncomingProblemHandler {
		public void handle(IntBox[] partitions, List<Particle> particles, Cell[][] cells) {
			Worker.this.partitions = partitions;
			Worker.this.particles = particles;
			Worker.this.cells = cells;
			recvProblemLock.setToTrue();
		}
	}
}
