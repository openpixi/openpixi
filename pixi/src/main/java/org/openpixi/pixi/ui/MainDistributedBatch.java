package org.openpixi.pixi.ui;

import org.openpixi.pixi.distributed.Master;
import org.openpixi.pixi.distributed.Node;
import org.openpixi.pixi.distributed.Slave;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;

/**
 * Runs distributed simulation.
 */
public class MainDistributedBatch {

	public static void main(String[] args) throws Exception {

		// Read number of nodes
		if (args.length < 1) {
			System.out.println("Number of nodes was not specified!");
			System.exit(1);
		}
		int numOfNodes = 0;
		try {
			numOfNodes = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("Invalid parameter: " + args[0] + "!");
			System.exit(1);
		}

		IbisRegistry registry = new IbisRegistry();
		registry.waitForJoin(numOfNodes);
		Node node;
		if (registry.isMaster()) {
			node = new Master(registry);
		}
		else {
			node = new Slave(registry);
		}
	}
}
