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
		new Thread(new Worker(registry, settings)).start();
		if (registry.isMaster()) {
			Master master = new Master(registry, settings);
			master.distributeProblem();
			master.collectResults();
		}
	}
}
