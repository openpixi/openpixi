package org.openpixi.pixi.distributed.partitioning;

import org.openpixi.pixi.physics.util.IntBox;

/**
 * Interface for partitioning the simulation area to smaller rectangles.
 * FUTURE: implement more elaborate partitioners
 */
public interface Partitioner {

	public IntBox[] partition(int numCellsX, int numCellsY, int numPartitions);
}
