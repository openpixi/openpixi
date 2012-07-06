package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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


	/**
	 * Creates ports for communication.
	 * To avoid deadlock the receive ports have to be created first.
	 */
	public MasterCommunicator(IbisRegistry registry) throws Exception {
		this.registry = registry;
	}


	/**
	 * The ports for problem distribution are closed right after they are used to minimize
	 * number of open connections.
	 */
	public void distributeProblem(IntBox[] partitions, int[] assignment,
	                              List<List<Particle>> particlePartitions,
	                              Cell[][][] gridPartitions) throws IOException {
		assert assignment.length >= registry.getWorkers().size();

		for (int i = 0; i < assignment.length; ++i) {
			SendPort distributePort = registry.getIbis().createSendPort(PixiPorts.ONE_TO_ONE_PORT);
			distributePort.connect(convertNodeIDToIbisID(assignment[i]), PixiPorts.DISTRIBUTE_PORT_ID);

			WriteMessage wm = distributePort.newMessage();
			wm.writeObject(partitions);
			wm.writeObject(assignment);
			wm.writeObject(particlePartitions.get(assignment[i]));
			wm.writeObject(gridPartitions[assignment[i]]);
			wm.finish();

			distributePort.close();
		}
	}


	/**
	 * Relies heavily on the fact that the list of workers is the same on each pc!
	 */
	private IbisIdentifier convertNodeIDToIbisID(int nodeID) {
		return registry.getWorkers().get(nodeID);
	}


	public void collectResults() {
		// TODO body
	}
}
