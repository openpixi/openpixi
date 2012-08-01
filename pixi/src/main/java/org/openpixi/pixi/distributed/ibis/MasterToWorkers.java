package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Master.
 */
public class MasterToWorkers {

	private IbisRegistry registry;
	private ReceivePort recvResultsPort;
	private SendPort[] sendPorts;

	// Received data
	private List<List<Particle>> particlePartitions = new ArrayList<List<Particle>>();
	private Cell[][][] gridPartitions;


	public List<List<Particle>> getParticlePartitions() {
		return particlePartitions;
	}

	public Cell[][][] getGridPartitions() {
		return gridPartitions;
	}


	public MasterToWorkers(IbisRegistry registry) throws Exception {
		this.registry = registry;

		// Initialize ports

		recvResultsPort = registry.getIbis().createReceivePort(
				PixiPorts.GATHER_PORT, PixiPorts.GATHER_PORT_ID);
		recvResultsPort.enableConnections();

		sendPorts = new SendPort[registry.getWorkers().size()];
		for (int i = 0; i < sendPorts.length; ++i) {
			sendPorts[i] = registry.getIbis().createSendPort(PixiPorts.ONE_TO_ONE_PORT);
		}

		// Initialize the holders for received data
		int numOfWorkers = registry.getWorkers().size();
		gridPartitions = new Cell[numOfWorkers][][];
		for (int i = 0; i < numOfWorkers; i++) {
			particlePartitions.add(new ArrayList<Particle>());
		}
	}


	/**
	 * The ports for problem distribution are closed right after they are used to minimize
	 * number of open connections.
	 */
	public void sendProblem(int workerID, IntBox[] partitions,
	                        List<Particle> particles,
	                        Cell[][] cells) throws IOException {

		SendPort sendPort = sendPorts[workerID];
		if (sendPort.connectedTo().length == 0) {
			sendPort.connect(
					registry.convertWorkerIDToIbisID(workerID),
					PixiPorts.DISTRIBUTE_PORT_ID);
		}

		WriteMessage wm = sendPort.newMessage();
		wm.writeObject(partitions);
		wm.writeObject(particles);
		wm.writeObject(cells);
		wm.finish();

		sendPort.close();
	}


	public void collectResults() throws Exception {
		for (int i = 0; i < registry.getWorkers().size(); ++i) {

			ReadMessage rm = recvResultsPort.receive();
			int workerID = rm.readInt();
			List<Particle> particles = (List<Particle>)rm.readObject();
			Cell[][] cells = (Cell[][])rm.readObject();
			rm.finish();

			gridPartitions[workerID] = cells;
			particlePartitions.set(workerID, particles);
		}
	}
}
