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
public class MasterCommunicator {

	/**
	 * Receives incoming results.
	 */
	private class CollectPortUpcall implements MessageUpcall {
		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {
			//To change body of implemented methods use File | Settings | File Templates.
		}
	}

	private IbisRegistry registry;
	/** For multicast to all slaves. */
	private SendPort scatterPort;
	/** For receiving messages from all slaves. */
	private ReceivePort gatherPort;


	/**
	 * Creates ports for communication.
	 * To avoid deadlock the receive ports have to be created first.
	 */
	public MasterCommunicator(IbisRegistry registry) throws Exception {
		this.registry = registry;

		gatherPort = registry.getIbis().createReceivePort(
				PixiPorts.GATHER_PORT,
				PixiPorts.GATHER_PORT_ID,
				new CollectPortUpcall());
		gatherPort.enableConnections();
		gatherPort.enableMessageUpcalls();

		Map<IbisIdentifier, String> portMap = new HashMap<IbisIdentifier, String>();
		for (IbisIdentifier slaveIbisID: registry.getWorkers()) {
			portMap.put(slaveIbisID, PixiPorts.SCATTER_PORT_ID);
		}
		scatterPort = registry.getIbis().createSendPort(PixiPorts.SCATTER_PORT);
		scatterPort.connect(portMap);
	}


	public void distribute(Box[] partitions, int[] assignment) throws IOException {
		IbisIdentifier[] ibisAssignment = convertAssignment(assignment);

		for (IbisIdentifier slaveID: registry.getWorkers()) {
			// Create send port; connect it; send problem, disconnect port
		}
	}


	/**
	 * Converts the partition assignment table from integers to ibis identifiers.
	 * The ibis identifiers are needed to set up ports to neighbors.
	 */
	private IbisIdentifier[] convertAssignment(int[] assignment) {
		assert assignment.length == registry.getWorkers().size() + 1;

		IbisIdentifier[] ibisAssignment = new IbisIdentifier[assignment.length];
		for (int i = 0; i < assignment.length; i++) {
			ibisAssignment[i] = registry.getWorkers().get(assignment[i]);
		}
		return ibisAssignment;
	}


	public void collectResults() {
		// TODO body
	}
}
