package org.openpixi.pixi.distributed.assigning;

import org.openpixi.pixi.distributed.partitioning.Box;

/**
 * Basic assignment of partitions to nodes.
 * Assigns one partition to each node =>
 * expects the number of nodes to be equal to number of partitions.
 */
public class SimplePartitionAssigner implements PartitionAssigner {

	public int[] assign(Box[] partitions, int numOfNodes) {
		if (partitions.length != numOfNodes) {
			throw new RuntimeException("Number of nodes and partitions must be equal!");
		}

		int[] retval = new int[partitions.length];
		for (int i = 0; i < numOfNodes; ++i) {
			retval[i] = i;
		}
		return retval;
	}
}
