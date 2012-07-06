package org.openpixi.pixi.distributed.assigning;

import org.openpixi.pixi.physics.util.IntBox;

/**
 * Interface for classes assigning partitions to nodes.
 * FUTURE: implement more complex assigners
 */
public interface PartitionAssigner {
	int[] assign(IntBox[] partitions, int numOfNodes);
}
