package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerCommunicator;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	private WorkerCommunicator communicator;
	private Settings settings;
	/** ID of this worker. */
	private int workerID;

	private SharedDataManager sharedDataManager;


	public Worker(IbisRegistry registry, Settings settings) throws Exception {
		this.settings = settings;
		communicator = new WorkerCommunicator(registry);
		workerID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
	}


	public void run() {
		try {
			communicator.receiveProblem();

			IntBox simulationAreaInCellDimensions =
					new IntBox(0, settings.getGridCellsX() - 1, 0, settings.getGridCellsY() - 1);
			sharedDataManager = new SharedDataManager(
					workerID, communicator.getPartitions(),
					simulationAreaInCellDimensions, settings.getBoundaryType());

			DistributedGridFactory gridFactory = new DistributedGridFactory(
					settings, communicator.getPartitions()[workerID],
					communicator.getCells(), sharedDataManager);
			Grid grid = gridFactory.create();

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
}
