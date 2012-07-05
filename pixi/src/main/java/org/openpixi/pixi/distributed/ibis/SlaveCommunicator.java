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
public class SlaveCommunicator {

	/**
	 * Receives the problem distribution.
	 */
	private class DistributePortUpcall implements MessageUpcall {

		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {
			//To change body of implemented methods use File | Settings | File Templates.
		}
	}

	private IbisRegistry registry;
	/** For receiving collective messages from master. */
	private ReceivePort scatterPort;
	/** For sending messages back to master. */
	private SendPort gatherPort;

	/**
	 * Creates the ports.
	 * Receive ports have to be created first to avoid deadlock.
	 */
	public SlaveCommunicator(IbisRegistry registry) throws Exception {
		this.registry = registry;

		scatterPort = registry.getIbis().createReceivePort(
				PixiPorts.SCATTER_PORT,
				PixiPorts.SCATTER_PORT_ID,
				new DistributePortUpcall());

		gatherPort = registry.getIbis().createSendPort(PixiPorts.GATHER_PORT);
		gatherPort.connect(registry.getMaster(), PixiPorts.GATHER_PORT_ID);
	}
}
