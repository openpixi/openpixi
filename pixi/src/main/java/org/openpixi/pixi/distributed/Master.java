package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisMasterCommunication;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;

/**
 * Represents the logic of the simulation on the master side.
 * Has the ability to distribute the problem, run the simulation and collect the results.
 */
public class Master implements Node {

	IbisMasterCommunication communicator;

	public Master(IbisRegistry registry) throws Exception {
		communicator = new IbisMasterCommunication(registry);
	}

	public void problemDistribution() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void step() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void resultCollection() {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
