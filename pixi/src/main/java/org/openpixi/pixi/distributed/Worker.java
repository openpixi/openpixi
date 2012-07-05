package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerCommunicator;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	WorkerCommunicator communicator;

	public Worker(IbisRegistry registry) throws Exception {
		communicator = new WorkerCommunicator(registry);
	}

	public void run() {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
