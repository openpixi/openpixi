package org.openpixi.pixi.ui;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import org.openpixi.pixi.distributed.Master;
import org.openpixi.pixi.distributed.Worker;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.physics.Settings;

/**
 * Runs distributed simulation.
 */
public class MainDistributedBatch {

	public static void main(String[] args) throws Exception {

		Settings settings = new Settings();
		settings.setNumOfNodes(2);
		settings.setGridCellsX(32);
		settings.setGridCellsY(32);

		IbisRegistry registry = new IbisRegistry(settings.getNumOfNodes());
		Thread workerThread = new Thread(new Worker(registry, settings));
		workerThread.start();
		if (registry.isMaster()) {
			Master master = new Master(registry, settings);
			master.distributeProblem();
			master.collectResults();
		}

		workerThread.join();
		// Wait for everybody to receive the last message before closing ibis.
		Thread.sleep(1000);
		registry.close();
	}
}
