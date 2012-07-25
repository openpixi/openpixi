package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.grid.DistributedGridFactory;
import org.openpixi.pixi.distributed.grid.DistributedInterpolationIterator;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerToMaster;
import org.openpixi.pixi.distributed.movement.boundary.DistributedParticleBoundaries;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.InterpolationIterator;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.util.ClassCopier;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	private WorkerToMaster communicator;

	/* Local and global settings differ only in settings connected to simulation size. */
	private Settings localSettings;
	private Settings globalSettings;

	/** ID of this worker. */
	private int workerID;


	public Worker(IbisRegistry registry, Settings settings) throws Exception {
		this.globalSettings = settings;
		communicator = new WorkerToMaster(registry);
		workerID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
	}


	public void run() {
		try {
			communicator.receiveProblem();
			createLocalSettings();

			SharedDataManager sharedDataManager =  createSharedDataManager();
			ParticleBoundaries  particleBoundaries = createParticleBoundaries(sharedDataManager);
			sharedDataManager.setParticleBoundaries(particleBoundaries);

			Grid grid = createGrid(sharedDataManager);
			InterpolationIterator interpolation = createInterpolationIterator(sharedDataManager);

			Simulation simulation = new Simulation(
					localSettings, grid, communicator.getParticles(),
					particleBoundaries, interpolation);

			for (int i = 0; i < localSettings.getIterations(); ++i) {
				simulation.step();
			}

			Cell[][] finalCells = getFinalCells(simulation.grid);

			// The results can come in arbitrary order; thus,
			// we have to send also the id of the node which is sending the result.
			communicator.sendResults(
					workerID,
					simulation.particles,
					finalCells);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * If the local simulation is at the edge of global simulation,
	 * we also need to include the extra cells to the master
	 * (because of hardwall boundaries).
	 */
	private Cell[][] getFinalCells(Grid grid) {
		IntBox mypart = communicator.getPartitions()[workerID];

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
			ystart += Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		if (mypart.ymax() == globalSettings.getGridCellsY() - 1) {
			yend += Grid.EXTRA_CELLS_AFTER_GRID;
		}

		Cell[][] finalCells = new Cell[xend][yend];
		for (int x = xstart; x <= xend ; ++x) {
			for (int y = ystart; y <= yend; ++y) {
				finalCells[x][y] = grid.getCell(x,y);
			}
		}
		return  finalCells;
	}


	/**
	 * Some settings (e.g. simulation width and height) pertain to the global simulation
	 * and are incorrect for the local simulation.
	 * Thus, we need to correct them, so that they correspond to the local simulation.
	 */
	private void createLocalSettings() {
		IntBox mypart = communicator.getPartitions()[workerID];

		double cellWidth = globalSettings.getCellWidth();
		double cellHeight = globalSettings.getCellHeight();

		localSettings = ClassCopier.copy(globalSettings);

		localSettings.setGridCellsX(mypart.xsize());
		localSettings.setGridCellsY(mypart.ysize());
		localSettings.setSimulationWidth(cellWidth * mypart.xsize());
		localSettings.setSimulationHeight(cellHeight * mypart.ysize());
	}


	private InterpolationIterator createInterpolationIterator(SharedDataManager sharedDataManager) {
		DoubleBox zoneOfLocalInfluence = new DoubleBox(
				(Grid.INTERPOLATION_RADIUS - 1) * localSettings.getCellWidth(),
				localSettings.getSimulationWidth() -
						Grid.INTERPOLATION_RADIUS * localSettings.getCellWidth(),
				(Grid.INTERPOLATION_RADIUS - 1) * localSettings.getCellHeight(),
				localSettings.getSimulationHeight() -
						Grid.INTERPOLATION_RADIUS * localSettings.getCellHeight());
		return new DistributedInterpolationIterator(
				localSettings.getInterpolator(),
				sharedDataManager, zoneOfLocalInfluence);
	}


	private SharedDataManager createSharedDataManager() {
		IntBox simulationAreaInCellDimensions = new IntBox(
				0, localSettings.getGridCellsX() - 1, 0, localSettings.getGridCellsY() - 1);
		return new SharedDataManager(
				workerID,
				communicator.getPartitions(),
				simulationAreaInCellDimensions,
				localSettings.getBoundaryType(),
				communicator.getRegistry());
	}


	private Grid createGrid(SharedDataManager sharedDataManager) throws IOException {
		DistributedGridFactory gridFactory = new DistributedGridFactory(
				localSettings, communicator.getPartitions()[workerID],
				communicator.getCells(), sharedDataManager);
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
}
