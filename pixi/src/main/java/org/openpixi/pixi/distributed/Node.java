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
	private Worker worker;

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
			distribute();
			worker.run();
			collect();
			close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * There is waiting for other nodes in IbisRegistry creation
	 * => If more nodes exists in a single jvm, they have to call distribute in separate threads!
	 */
	public void distribute() {
		registry = new IbisRegistry(settings.getNumOfNodes());
		worker = new Worker(registry, settings);
		if (registry.isMaster()) {
			master = new Master(registry, settings);
			master.distributeProblem();
		}
		worker.receiveProblem();
	}


	public void step() {
		worker.step();
	}


	public void collect() {
		worker.sendResults();
		if (master != null) {
			master.collectResults();
		}
	}


	public void close() {
		try {
			// Wait for everybody to receive the last message before closing ibis.
			Thread.sleep(1000);
			registry.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public boolean isMaster() {
		return registry.isMaster();
	}
}
