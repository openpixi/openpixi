package org.openpixi.pixi.distributed.ibis;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Slave.
 */
public class IbisSlaveCommunication {

	private IbisRegistry registry;

	public IbisSlaveCommunication(IbisRegistry registry) throws Exception {
		this.registry = registry;
	}
}
