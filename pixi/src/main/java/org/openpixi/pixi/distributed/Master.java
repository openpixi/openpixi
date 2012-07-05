package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.assigning.PartitionAssigner;
import org.openpixi.pixi.distributed.assigning.SimplePartitionAssigner;
import org.openpixi.pixi.distributed.ibis.MasterCommunicator;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.partitioning.Box;
import org.openpixi.pixi.distributed.partitioning.Partitioner;
import org.openpixi.pixi.distributed.partitioning.SimplePartitioner;

/**
 * Distributes the problem and collects the results.
 */
public class Master {

	private MasterCommunicator communicator;
	private int numCellsX;
	private int numCellsY;
	private int numNodes;

	// TODO replace parameters with settings class
	public Master(IbisRegistry registry, int numCellsX, int numCellsY, int numNodes) throws Exception {
		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;

		communicator = new MasterCommunicator(registry);
	}

	public void distributeProblem() {
		Partitioner partitioner = new SimplePartitioner();
		Box[] partitions = partitioner.partition(numCellsX, numCellsY, numNodes);

		PartitionAssigner assigner = new SimplePartitionAssigner();
		int[] assignment = assigner.assign(partitions, numNodes);
	}

	public void collectResults() {
	}
}
