package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.distributed.util.IncomingProblemHandler;
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

	private ReceivePort recvProblemPort;
	private SendPort sendResultPort;

	private IncomingProblemHandler problemHandler;


	public IbisRegistry getRegistry() {
		return registry;
	}


	public WorkerToMaster(IbisRegistry registry, IncomingProblemHandler problemHandler) throws Exception {
		this.registry = registry;
		this.problemHandler = problemHandler;

		recvProblemPort = registry.getIbis().createReceivePort(
				PixiPorts.ONE_TO_ONE_PORT, PixiPorts.DISTRIBUTE_PORT_ID, new IncomingProblem());
		recvProblemPort.enableConnections();
		recvProblemPort.enableMessageUpcalls();

		sendResultPort = registry.getIbis().createSendPort(PixiPorts.GATHER_PORT);
	}


	public void sendResults(int workerID,
	                        List<Particle> particles,
	                        Cell[][] cells) throws IOException {
		if (sendResultPort.connectedTo().length == 0) {
			sendResultPort.connect(registry.getMaster(), PixiPorts.GATHER_PORT_ID);
		}

		WriteMessage wm = sendResultPort.newMessage();
		wm.writeInt(workerID);
		wm.writeObject(particles);
		wm.writeObject(cells);
		wm.finish();
	}


	public void close() {
		try {
			sendResultPort.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	private class IncomingProblem implements MessageUpcall {
		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {

			IntBox[] partitions = (IntBox[])readMessage.readObject();
			List<Particle> particles = (List<Particle>)readMessage.readObject();
			Cell[][] cells = (Cell[][])readMessage.readObject();

			problemHandler.handle(partitions, particles, cells);

			readMessage.finish();
			recvProblemPort.close();
		}
	}
}
