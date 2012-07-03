package org.openpixi.pixi.distributed.ibis;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Master.
 */
public class IbisMasterCommunication {

	private IbisRegistry registry;

	public IbisMasterCommunication(IbisRegistry registry) throws Exception {
		this.registry = registry;
	}
}
