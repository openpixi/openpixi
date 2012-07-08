package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;
import org.openpixi.pixi.distributed.ResultsHolder;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.List;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Master.
 */
public class MasterCommunicator {

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
	public void sendProblem(int nodeID, IntBox[] partitions,
	                        List<Particle> particles,
	                        Cell[][] cells) throws IOException {

		SendPort sendPort = registry.getIbis().createSendPort(PixiPorts.ONE_TO_ONE_PORT);
		sendPort.connect(
				registry.convertNodeIDToIbisID(nodeID),
				PixiPorts.DISTRIBUTE_PORT_ID);

		WriteMessage wm = sendPort.newMessage();
		wm.writeObject(partitions);
		wm.writeObject(particles);
		wm.writeObject(cells);
		wm.finish();

		sendPort.close();
	}


	public ResultsHolder collectResults() throws Exception {
		ReceivePort recvPort = registry.getIbis().createReceivePort(
				PixiPorts.GATHER_PORT, PixiPorts.GATHER_PORT_ID);
		recvPort.enableConnections();

		ResultsHolder resultsHolder = new ResultsHolder(registry.getWorkers().size());
		for (int i = 0; i < registry.getWorkers().size(); ++i) {

			ReadMessage rm = recvPort.receive();
			int nodeID = rm.readInt();
			List<Particle> particles = (List<Particle>)rm.readObject();
			Cell[][] cells = (Cell[][])rm.readObject();
			rm.finish();

			resultsHolder.gridPartitions[nodeID] = cells;
			resultsHolder.particlePartitions.set(nodeID, particles);
		}

		recvPort.close();
		return  resultsHolder;
	}
}
