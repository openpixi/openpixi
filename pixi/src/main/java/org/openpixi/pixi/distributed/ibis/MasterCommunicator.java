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
public class MasterCommunicator {

	private IbisRegistry registry;

	// Received data
	private List<List<Particle>> particlePartitions = new ArrayList<List<Particle>>();
	private Cell[][][] gridPartitions;


	public List<List<Particle>> getParticlePartitions() {
		return particlePartitions;
	}

	public Cell[][][] getGridPartitions() {
		return gridPartitions;
	}


	public MasterCommunicator(IbisRegistry registry) throws Exception {
		this.registry = registry;

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

		SendPort sendPort = registry.getIbis().createSendPort(PixiPorts.ONE_TO_ONE_PORT);
		sendPort.connect(
				registry.convertWorkerIDToIbisID(workerID),
				PixiPorts.DISTRIBUTE_PORT_ID);

		WriteMessage wm = sendPort.newMessage();
		wm.writeObject(partitions);
		wm.writeObject(particles);
		wm.writeObject(cells);
		wm.finish();

		sendPort.close();
	}


	public void collectResults() throws Exception {
		ReceivePort recvPort = registry.getIbis().createReceivePort(
				PixiPorts.GATHER_PORT, PixiPorts.GATHER_PORT_ID);
		recvPort.enableConnections();

		for (int i = 0; i < registry.getWorkers().size(); ++i) {

			ReadMessage rm = recvPort.receive();
			int workerID = rm.readInt();
			List<Particle> particles = (List<Particle>)rm.readObject();
			Cell[][] cells = (Cell[][])rm.readObject();
			rm.finish();

			gridPartitions[workerID] = cells;
			particlePartitions.set(workerID, particles);
		}

		recvPort.close();
	}
}
