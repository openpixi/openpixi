package org.openpixi.pixi.ui;

import org.openpixi.pixi.distributed.Node;
import org.openpixi.pixi.physics.Settings;

/**
 * Runs distributed simulation.
 */
public class MainDistributedBatch {

	public static void main(String[] args) throws Exception {
		System.setProperty("ibis.server.address", "localhost");
		System.setProperty("ibis.pool.name", "test");

		Settings settings = new Settings();
		settings.setNumOfNodes(2);
		settings.setGridCellsX(32);
		settings.setGridCellsY(32);

		Node node = new Node(settings);
		node.run();
	}
}
