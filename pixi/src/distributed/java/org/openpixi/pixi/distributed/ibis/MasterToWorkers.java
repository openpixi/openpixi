package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.distributed.util.IncomingResultHandler;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.List;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Master.
 */
public class MasterToWorkers {

	private IbisRegistry registry;
	private ReceivePort recvResultsPort;
	private IncomingResultHandler resultHandler;


	public MasterToWorkers(IbisRegistry registry, IncomingResultHandler resultHandler) throws Exception {
		this.registry = registry;
		this.resultHandler = resultHandler;

		// Initialize ports
		recvResultsPort = registry.getIbis().createReceivePort(
				PixiPorts.GATHER_PORT, PixiPorts.GATHER_PORT_ID, new IncomingResults());
		recvResultsPort.enableConnections();
		recvResultsPort.enableMessageUpcalls();
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


	public void close() {
		try {
			recvResultsPort.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	private class IncomingResults implements MessageUpcall {
		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {
			int workerID = readMessage.readInt();
			List<Particle> particles = (List<Particle>)readMessage.readObject();
			Cell[][] cells = (Cell[][])readMessage.readObject();
			resultHandler.handle(workerID, particles, cells);
		}
	}
}
