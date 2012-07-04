package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.MessageUpcall;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.SendPort;

import java.io.IOException;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Slave.
 */
public class IbisSlaveCommunication {

	/**
	 * Receives the problem distribution.
	 */
	private class DistributePortUpcall implements MessageUpcall {

		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {
			//To change body of implemented methods use File | Settings | File Templates.
		}
	}

	private IbisRegistry registry;
	/** For receiving the problem. */
	private ReceivePort distributePort;
	/** For sending the result. */
	private SendPort collectPort;

	/**
	 * Creates the ports.
	 * Receive ports have to be created first to avoid deadlock.
	 */
	public IbisSlaveCommunication(IbisRegistry registry) throws Exception {
		this.registry = registry;

		distributePort = registry.getIbis().createReceivePort(
				IbisRegistry.DISTRIBUTE_PORT,
				IbisRegistry.DISTRIBUTE_PORT_ID,
				new DistributePortUpcall());

		collectPort = registry.getIbis().createSendPort(IbisRegistry.COLLECT_PORT);
		collectPort.connect(registry.getMaster(), IbisRegistry.COLLECT_PORT_ID);
	}
}
