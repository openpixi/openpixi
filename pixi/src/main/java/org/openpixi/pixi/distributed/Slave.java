package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.SlaveCommunicator;

/**
 * Represents the logic of the simulation on the side of the slave.
 */
public class Slave implements Node {

	SlaveCommunicator communicator;

	public Slave(IbisRegistry registry) throws Exception {
		communicator = new SlaveCommunicator(registry);
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
