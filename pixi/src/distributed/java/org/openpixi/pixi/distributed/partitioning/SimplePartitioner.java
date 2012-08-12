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

	private IntBox[] partitions;
	private int numCellsX;
	private int numCellsY;


	public IntBox[] partition(int numCellsX, int numCellsY, int numPartitions) {
		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;

		assert numCellsX > 0;
		assert numCellsY > 0;

		if (!isPower2(numCellsX) || !isPower2(numCellsY) || !isPower2(numPartitions)) {
			throw new RuntimeException("Number of cells in x and y direction " +
					"as well as the number of partitions must be power of 2!");
		}
		if (numPartitions > numCellsX * numCellsY) {
			throw new RuntimeException(
					"Number of partitions must be less or equal to the number of cells!");
		}

		List<IntBox> partitions = new ArrayList<IntBox>();
		partitions.add(new IntBox(0, numCellsX - 1, 0, numCellsY - 1));

		while (partitions.size() < numPartitions) {
			partitions = splitBoxes(partitions);
		}

		this.partitions = partitions.toArray(new IntBox[0]);
		return this.partitions;
	}


	private List<IntBox> splitBoxes(List<IntBox> partitions) {
		List<IntBox> newPartitions = new ArrayList<IntBox>();
		for (IntBox b: partitions) {
			if (b.xsize() > b.ysize()) {
				// Split along x axis
				int xmid = (b.xmin() + b.xsize() / 2);
				newPartitions.add(new IntBox(b.xmin(), xmid - 1, b.ymin(), b.ymax()));
				newPartitions.add(new IntBox(xmid, b.xmax(), b.ymin(), b.ymax()));
			}
			else {
				// Split along y axis
				int ymid = (b.ymin() + b.ysize() / 2);
				newPartitions.add(new IntBox(b.xmin(), b.xmax(), b.ymin(), ymid - 1));
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


	@Override
	public String toString() {
		StringBuilder retval = new StringBuilder();
		IntBox first = findFirst();
		IntBox nextY = first;
		while (nextY != null) {
			IntBox nextX = nextY;
			while (nextX != null) {
				int index = getIndex(nextX);
				retval.append(index + " ");
				nextX = findNextX(nextX);
			}
			retval.append("\n");
			nextY = findNextY(nextY);
		}
		return retval.toString().trim();
	}


	private int getIndex(IntBox partition) {
		for (int i = 0; i < partitions.length; ++i) {
			if (partitions[i] == partition) {
				return i;
			}
		}
		throw new RuntimeException("Partition was not found!");
	}


	private IntBox findNextX(IntBox current) {
		for (IntBox part: partitions) {
			if (part.xmin() == current.xmax() + 1 && part.ymin() == current.ymin()) {
				return part;
			}
		}
		return null;
	}


	private IntBox findNextY(IntBox current) {
		for (IntBox part: partitions) {
			if (part.ymin() == current.ymax() + 1 && part.xmin() == current.xmin()) {
				return part;
			}
		}
		return null;
	}


	private IntBox findFirst() {
		for (IntBox part: partitions) {
			if (part.xmin() == 0 && part.ymin() == 0) {
				return  part;
			}
		}
		throw new RuntimeException("Could not find the first partition!");
	}
}
