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


	/**
	 * Distributed the problem, runs the simulation and then collects the results.
	 */
	public void run() {
		distribute();
		for (int i = 0; i < settings.getIterations(); ++i) {
			step();
		}
		collect();
		close();
	}


	/**
	 * There is waiting for other nodes in IbisRegistry creation
	 * => If more nodes exists in a single jvm, they have to call distribute in separate threads!
	 */
	public void distribute() {
		registry = new IbisRegistry(
				settings.getNumOfNodes(), settings.getIplServer(), settings.getIplPool());
		worker = new Worker(registry, settings);
		if (registry.isMaster()) {
			master = new Master(registry, settings);
			master.distributeProblem();
		}
		worker.receiveProblem();
	}


	/**
	 * Executes one step of the simulation.
	 */
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
			Thread.sleep(100);
			worker.close();
			if (master != null) {
				master.close();
			}
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
