package org.openpixi.pixi.distributed.partitioning;

/**
 * Interface for partitioning the simulation area to smaller rectangles.
 * FUTURE: implement more elaborate partitioners
 */
public interface Partitioner {

	public Box[] partition(int numCellsX, int numCellsY, int numPartitions);
}
