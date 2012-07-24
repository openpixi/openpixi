package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.grid.DistributedGridFactory;
import org.openpixi.pixi.distributed.grid.DistributedInterpolationIterator;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerToMaster;
import org.openpixi.pixi.distributed.movement.boundary.DistributedParticleBoundaries;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.InterpolationIterator;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	private WorkerToMaster communicator;
	private Settings settings;
	/** ID of this worker. */
	private int workerID;


	public Worker(IbisRegistry registry, Settings settings) throws Exception {
		this.settings = settings;
		communicator = new WorkerToMaster(registry);
		workerID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
	}


	public void run() {
		try {
			communicator.receiveProblem();

			adjustSettingsToLocalNode();

			SharedDataManager sharedDataManager =  createSharedDataManager();
			ParticleBoundaries  particleBoundaries = createParticleBoundaries(sharedDataManager);
			sharedDataManager.setParticleBoundaries(particleBoundaries);

			Grid grid = createGrid(sharedDataManager);
			InterpolationIterator interpolation = createInterpolationIterator(sharedDataManager);

			Simulation simulation = new Simulation(
					settings, grid, communicator.getParticles(),
					particleBoundaries, interpolation);


			// TODO run the simulation
			// TODO get grid subpart which should be sent back to master

			// The results can come in arbitrary order; thus,
			// we have to send also the id of the node which is sending the result.
			communicator.sendResults(
					workerID,
					communicator.getParticles(),
					communicator.getCells());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Some settings (e.g. simulation width and height) pertain to the global simulation
	 * and are incorrect for the local simulation.
	 * Thus, we need to correct them to correspond to the local simulation.
	 */
	private void adjustSettingsToLocalNode() {
		IntBox mypart = communicator.getPartitions()[workerID];

		double cellWidth = settings.getCellWidth();
		double cellHeight = settings.getCellHeight();

		settings.setGridCellsX(mypart.xsize());
		settings.setGridCellsY(mypart.ysize());
		settings.setSimulationWidth(cellWidth * mypart.xsize());
		settings.setSimulationHeight(cellHeight * mypart.ysize());
	}


	private InterpolationIterator createInterpolationIterator(SharedDataManager sharedDataManager) {
		DoubleBox zoneOfLocalInfluence = new DoubleBox(
				(Grid.INTERPOLATION_RADIUS - 1) * settings.getCellWidth(),
				settings.getSimulationWidth() -
						Grid.INTERPOLATION_RADIUS * settings.getCellWidth(),
				(Grid.INTERPOLATION_RADIUS - 1) * settings.getCellHeight(),
				settings.getSimulationHeight() -
						Grid.INTERPOLATION_RADIUS * settings.getCellHeight());
		return new DistributedInterpolationIterator(
				settings.getInterpolator(),
				sharedDataManager, zoneOfLocalInfluence);
	}


	private SharedDataManager createSharedDataManager() {
		IntBox simulationAreaInCellDimensions = new IntBox(
				0, settings.getGridCellsX() - 1, 0, settings.getGridCellsY() - 1);
		return new SharedDataManager(
				workerID,
				communicator.getPartitions(),
				simulationAreaInCellDimensions,
				settings.getBoundaryType(),
				communicator.getRegistry());
	}


	private Grid createGrid(SharedDataManager sharedDataManager) throws IOException {
		DistributedGridFactory gridFactory = new DistributedGridFactory(
				settings, communicator.getPartitions()[workerID],
				communicator.getCells(), sharedDataManager);
		return gridFactory.create();
	}


	private ParticleBoundaries createParticleBoundaries(SharedDataManager sharedDataManager) {
		DoubleBox simulationAreaInParticleDimensions = new DoubleBox(
				0, settings.getSimulationWidth(), 0, settings.getSimulationHeight());
		DoubleBox innerSimulationArea = new DoubleBox(
				0, settings.getSimulationWidth() - settings.getCellWidth(),
				0, settings.getSimulationHeight() - settings.getCellHeight());
		return new DistributedParticleBoundaries(
				simulationAreaInParticleDimensions, innerSimulationArea,
				settings.getParticleBoundary(), sharedDataManager);
	}
}
