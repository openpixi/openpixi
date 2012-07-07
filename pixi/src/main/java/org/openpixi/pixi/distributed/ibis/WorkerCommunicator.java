package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.List;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the worker.
 */
public class WorkerCommunicator {

	private IbisRegistry registry;

	// Necessary data for building the simulation (all received in one message).
	private IntBox[] partitions;
	private int[] assignment;
	private List<Particle> particles;
	private Cell[][] cells;


	public IntBox[] getPartitions() {
		return partitions;
	}

	public int[] getAssignment() {
		return assignment;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public Cell[][] getCells() {
		return cells;
	}


	/**
	 * Creates the ports.
	 * Receive ports have to be created first to avoid deadlock.
	 */
	public WorkerCommunicator(IbisRegistry registry) throws Exception {
		this.registry = registry;
	}


	public void receiveProblem() throws IOException, ClassNotFoundException {
		ReceivePort distributePort = registry.getIbis().createReceivePort(
				PixiPorts.ONE_TO_ONE_PORT, PixiPorts.DISTRIBUTE_PORT_ID);
		distributePort.enableConnections();

		ReadMessage rm = distributePort.receive();
		partitions = (IntBox[])rm.readObject();
		assignment = (int[])rm.readObject();
		particles = (List<Particle>)rm.readObject();
		cells = (Cell[][])rm.readObject();
		rm.finish();

		distributePort.close();
	}


	public void sendResults(List<Particle> particles,
	                        Cell[][] cells) throws IOException {
		SendPort sendResultPort = registry.getIbis().createSendPort(PixiPorts.GATHER_PORT);
		sendResultPort.connect(registry.getMaster(), PixiPorts.GATHER_PORT_ID);

		WriteMessage wm = sendResultPort.newMessage();
		wm.writeObject(particles);
		wm.writeObject(cells);
		wm.finish();

		sendResultPort.close();
	}


	public int getWorkerID() {
		return registry.convertIbisIDToNodeID(registry.getIbis().identifier());
	}
}
