package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates distributed environment on local host.
 */
public class SimulatedDistributedEnvironment {

	private Settings settings;
	private Master master;


	/**
	 * Should be called after run as the master is set in the run method.
	 */
	public Master getMaster() {
		return master;
	}


	public SimulatedDistributedEnvironment(Settings settings) {
		this.settings = settings;
	}


	public void run() throws InterruptedException {
		Thread server = startIplServer();
		setRequiredSystemProperties();

		List<Thread> threads = new ArrayList<Thread>();
		List<Node> nodes = new ArrayList<Node>();

		// Start all the nodes
		for (int i = 0; i < settings.getNumOfNodes(); i++) {
			Node node = new Node(settings);
			Thread thread = new Thread(node);

			thread.start();

			threads.add(thread);
			nodes.add(node);
		}

		// Wait for all the nodes
		for (Thread node: threads) {
			node.join();
		}
		// Stop the server
		server.interrupt();

		// Set the master
		for (Node n: nodes) {
			if (n.isMaster()) {
				this.master = n.getMaster();
			}
		}
	}


	private void setRequiredSystemProperties() {
		System.setProperty("ibis.server.address", "localhost");
		System.setProperty("ibis.pool.name", "test");
	}


	private Thread startIplServer() {
		Thread server = new Thread(new Runnable() {
			public void run() {
				ibis.ipl.server.Server.main(new String[] {"--events"});
			}
		});
		server.start();
		return server;
	}
}
