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
public class WorkerToMaster {

	private IbisRegistry registry;

	// Received data
	private IntBox[] partitions;
	private List<Particle> particles;
	private Cell[][] cells;


	public IntBox[] getPartitions() {
		return partitions;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public Cell[][] getCells() {
		return cells;
	}

	public IbisRegistry getRegistry() {
		return registry;
	}

	public WorkerToMaster(IbisRegistry registry) throws Exception {
		this.registry = registry;
	}


	public void receiveProblem() throws IOException, ClassNotFoundException {
		ReceivePort distributePort = registry.getIbis().createReceivePort(
				PixiPorts.ONE_TO_ONE_PORT, PixiPorts.DISTRIBUTE_PORT_ID);
		distributePort.enableConnections();

		ReadMessage rm = distributePort.receive();
		partitions = (IntBox[])rm.readObject();
		particles = (List<Particle>)rm.readObject();
		cells = (Cell[][])rm.readObject();
		rm.finish();

		distributePort.close();
	}


	public void sendResults(int workerID,
	                        List<Particle> particles,
	                        Cell[][] cells) throws IOException {
		SendPort sendResultPort = registry.getIbis().createSendPort(PixiPorts.GATHER_PORT);
		sendResultPort.connect(registry.getMaster(), PixiPorts.GATHER_PORT_ID);

		WriteMessage wm = sendResultPort.newMessage();
		wm.writeInt(workerID);
		wm.writeObject(particles);
		wm.writeObject(cells);
		wm.finish();

		sendResultPort.close();
	}
}
