package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.distributed.partitioning.Box;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Master.
 */
public class IbisMasterCommunication {

	/**
	 * Receives incoming results.
	 */
	private class CollectPortUpcall implements MessageUpcall {
		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {
			//To change body of implemented methods use File | Settings | File Templates.
		}
	}

	private IbisRegistry registry;
	private SendPort distributePort;
	private ReceivePort collectPort;

	/**
	 * Creates ports for communication.
	 * To avoid deadlock the receive ports have to be created first.
	 */
	public IbisMasterCommunication(IbisRegistry registry) throws Exception {
		this.registry = registry;

		collectPort = registry.getIbis().createReceivePort(
				IbisRegistry.COLLECT_PORT,
				IbisRegistry.COLLECT_PORT_ID,
				new CollectPortUpcall());

		Map<IbisIdentifier, String> portMap = new HashMap<IbisIdentifier, String>();
		for (IbisIdentifier slaveIbisID: registry.getSlaves()) {
			portMap.put(slaveIbisID, IbisRegistry.DISTRIBUTE_PORT_ID);
		}

		distributePort = registry.getIbis().createSendPort(IbisRegistry.DISTRIBUTE_PORT);
		distributePort.connect(portMap);
	}


	public void distribute(Box[] partitions, int[] assignment) throws IOException {
		IbisIdentifier[] ibisAssignment = convertAssignment(assignment);
		WriteMessage wm = distributePort.newMessage();
		wm.writeObject(partitions);
		wm.writeObject(assignment);
		wm.finish();
	}


	/**
	 * Converts the partition assignment table from integers to ibis identifiers.
	 * The ibis identifiers are needed to set up ports to neighbors.
	 */
	private IbisIdentifier[] convertAssignment(int[] assignment) {
		assert assignment.length == registry.getSlaves().size();

		IbisIdentifier[] ibisAssignment = new IbisIdentifier[assignment.length];
		for (int i = 0; i < assignment.length; i++) {
			ibisAssignment[i] = registry.getSlaves().get(i);
		}
		return ibisAssignment;
	}


	public void collectResults() {
		// TODO body
	}
}
