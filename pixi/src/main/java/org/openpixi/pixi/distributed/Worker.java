package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.grid.DistributedGridFactory;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerCommunicator;
import org.openpixi.pixi.distributed.movement.boundary.DistributedParticleBoundaries;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	private WorkerCommunicator communicator;
	private Settings settings;
	/** ID of this worker. */
	private int workerID;


	public Worker(IbisRegistry registry, Settings settings) throws Exception {
		this.settings = settings;
		communicator = new WorkerCommunicator(registry);
		workerID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
	}


	public void run() {
		try {
			communicator.receiveProblem();

			SharedDataManager sharedDataManager =  createSharedDataManager();
			Grid grid = createGrid(sharedDataManager);
			ParticleBoundaries  particleBoundaries = createParticleBoundaries(sharedDataManager);


			// TODO create simulation
			// TODO run the simulation


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


	private SharedDataManager createSharedDataManager() {
		IntBox simulationAreaInCellDimensions = new IntBox(
				0, settings.getGridCellsX() - 1, 0, settings.getGridCellsY() - 1);
		return new SharedDataManager(
				workerID, communicator.getPartitions(),
				simulationAreaInCellDimensions, settings.getBoundaryType());
	}


	private Grid createGrid(SharedDataManager sharedDataManager) {
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
