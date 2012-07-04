package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.assigning.PartitionAssigner;
import org.openpixi.pixi.distributed.assigning.SimplePartitionAssigner;
import org.openpixi.pixi.distributed.ibis.IbisMasterCommunication;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.partitioning.Box;
import org.openpixi.pixi.distributed.partitioning.Partitioner;
import org.openpixi.pixi.distributed.partitioning.SimplePartitioner;

/**
 * Represents the logic of the simulation on the master side.
 * Has the ability to distribute the problem, run the simulation and collect the results.
 */
public class Master implements Node {

	private IbisMasterCommunication communicator;
	private int numCellsX;
	private int numCellsY;
	private int numNodes;

	// TODO replace parameters with settings class
	public Master(IbisRegistry registry, int numCellsX, int numCellsY, int numNodes) throws Exception {
		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;

		communicator = new IbisMasterCommunication(registry);
	}

	public void problemDistribution() {
		Partitioner partitioner = new SimplePartitioner();
		Box[] partitions = partitioner.partition(numCellsX, numCellsY, numNodes);

		PartitionAssigner assigner = new SimplePartitionAssigner();
		int[] assignment = assigner.assign(partitions, numNodes);

		//communicator.distribute(partitions, assignment);
	}

	public void step() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void resultCollection() {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
