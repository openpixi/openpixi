package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.physics.Settings;

/**
 * Wraps up the difference between master and ordinary worker and just runs a node.
 */
public class Node implements Runnable {

	private Settings settings;
	private IbisRegistry registry;
	private Master master;

	/**
	 * If this node is not master returns null.
	 */
	public Master getMaster() {
		return master;
	}

	public Node(Settings settings) {
		this.settings = settings;
	}

	public void run() {
		try {
			registry = new IbisRegistry(settings.getNumOfNodes());
			Thread workerThread = new Thread(new Worker(registry, settings));
			workerThread.start();
			if (registry.isMaster()) {
				master = new Master(registry, settings);
				master.distributeProblem();
				master.collectResults();
			}

			workerThread.join();
			// Wait for everybody to receive the last message before closing ibis.
			Thread.sleep(1000);
			registry.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isMaster() {
		return registry.isMaster();
	}
}
