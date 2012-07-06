package org.openpixi.pixi.distributed.partitioning;

import org.openpixi.pixi.physics.util.IntBox;

import java.util.ArrayList;
import java.util.List;

/**
 *  Basic simulation area partitioner.
 *  Works only under following conditions.
 *  1) numCellsX, numCellsY and numPartitions are all powers of 2.
 *  2) numPartitions <= numCellsX * numCellsY
 *
 *  The algorithm works as follows.
 *  We have list of areas (boxes) which we need to split (initially 1 area/box).
 *  - In each iteration we take all the boxes and split them into half at the larger side.
 *  - We repeat the previous step until we have the specified number of boxes.
 */
public class SimplePartitioner implements Partitioner {

	public IntBox[] partition(int numCellsX, int numCellsY, int numPartitions) {
		assert numCellsX > 0;
		assert numCellsY > 0;

		if (!isPower2(numCellsX) || !isPower2(numCellsY) || !isPower2(numPartitions)) {
			throw new RuntimeException("Number of cells in x and y direction " +
					"as well as the number of nodes must be power of 2!");
		}
		if (numPartitions > numCellsX * numCellsY) {
			throw new RuntimeException(
					"Number of nodes must be less or equal to the number of cells!");
		}

		List<IntBox> partitions = new ArrayList<IntBox>();
		partitions.add(new IntBox(0, numCellsX, 0, numCellsY));

		while (partitions.size() < numPartitions) {
			partitions = splitBoxes(partitions);
		}

		return partitions.toArray(new IntBox[0]);
	}


	private List<IntBox> splitBoxes(List<IntBox> partitions) {
		List<IntBox> newPartitions = new ArrayList<IntBox>();
		for (IntBox b: partitions) {
			if (b.xsize() > b.ysize()) {
				// Split along x axis
				int xmid = b.xmin() + b.xsize() / 2;
				newPartitions.add(new IntBox(b.xmin(), xmid, b.ymin(), b.ymax()));
				newPartitions.add(new IntBox(xmid, b.xmax(), b.ymin(), b.ymax()));
			}
			else {
				// Split along y axis
				int ymid = b.ymin() + b.ysize() / 2;
				newPartitions.add(new IntBox(b.xmin(), b.xmax(), b.ymin(), ymid));
				newPartitions.add(new IntBox(b.xmin(), b.xmax(), ymid, b.ymax()));
			}
		}
		return newPartitions;
	}


	private boolean isPower2(int number) {
		if ((number & (number - 1)) == 0) {
			return true;
		}
		else {
			return false;
		}
	}
}
