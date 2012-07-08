package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerCommunicator;
import org.openpixi.pixi.physics.Settings;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	private WorkerCommunicator communicator;
	private Settings settings;
	/** ID of this worker. */
	private int workerID;


	public Worker(IbisRegistry registry, Settings settings) throws Exception {
		communicator = new WorkerCommunicator(registry);
		workerID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
	}


	public void run() {
		try {
			communicator.receiveProblem();

			// TODO build the simulation together with boundaries
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
