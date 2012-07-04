package org.openpixi.pixi.distributed.assigning;

import org.openpixi.pixi.distributed.partitioning.Box;

/**
 * Interface for classes assigning partitions to nodes.
 * FUTURE: implement more complex assigners
 */
public interface PartitionAssigner {
	int[] assign(Box[] partitions, int numOfNodes);
}
