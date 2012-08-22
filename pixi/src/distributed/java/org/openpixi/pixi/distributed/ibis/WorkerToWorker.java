package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.distributed.util.IncomingCellsHandler;
import org.openpixi.pixi.distributed.util.IncomingParticlesHandler;
import org.openpixi.pixi.distributed.util.IncomingPointsHandler;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.Point;

import java.io.IOException;
import java.util.List;

/**
 * Handles the exchange of shared data during the simulation.
 */
public class WorkerToWorker {

	/* The following constants identify the incoming message. */
	private static final int ARRIVING_PARTICLES_MSG = 0;
	private static final int GHOST_PARTICLES_MSG = 1;
	private static final int GHOST_CELLS_MSG = 2;
	private static final int GHOST_CELLS_INDEXES_MSG = 3;

	private IbisRegistry registry;

	private ReceivePort recvPort;
	private SendPort sendPort;

	/** ID of the neighbor with whom this communicator communicates. */
	private int neighborID;

	/* Handlers of upcalls for higher level classes. */
	private IncomingPointsHandler ghostCellsIndexesHandler;
	private IncomingCellsHandler ghostCellsHandler;
	private IncomingParticlesHandler ghostParticlesHandler;
	private IncomingParticlesHandler arrivingParticlesHandler;


	public void setGhostCellsHandler(IncomingCellsHandler ghostCellsHandler) {
		this.ghostCellsHandler = ghostCellsHandler;
	}

	public void setGhostParticlesHandler(IncomingParticlesHandler ghostParticlesHandler) {
		this.ghostParticlesHandler = ghostParticlesHandler;
	}

	public void setArrivingParticlesHandler(IncomingParticlesHandler arrivingParticlesHandler) {
		this.arrivingParticlesHandler = arrivingParticlesHandler;
	}

	public void setGhostCellsIndexesHandler(IncomingPointsHandler ghostCellsIndexesHandler) {
		this.ghostCellsIndexesHandler = ghostCellsIndexesHandler;
	}


	public WorkerToWorker(IbisRegistry registry, int neighborID) {
		this.registry = registry;
		this.neighborID = neighborID;

		// One ibis instance can not have 2 receive ports with the same ID; thus,
		// we need to number the receive ports according to the neighbors they are connected to.
		try {
			recvPort = registry.getIbis().createReceivePort(
					PixiPorts.ONE_TO_ONE_PORT,
					PixiPorts.EXCHANGE_PORT_ID + neighborID,
					new IncomingMessageHandler());
			recvPort.enableConnections();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	/**
	 * To avoid deadlock the send ports should be connected after all the receive ports are created.
	 * Furthermore, we enable upcalls here to avoid upcalls being called to early.
	 */
	public void initializeConnection() {
		recvPort.enableMessageUpcalls();
		try {
			sendPort = registry.getIbis().createSendPort(PixiPorts.ONE_TO_ONE_PORT);
			int myID = registry.convertIbisIDToWorkerID(registry.getIbis().identifier());
			sendPort.connect(
					registry.convertWorkerIDToIbisID(neighborID),
					PixiPorts.EXCHANGE_PORT_ID + myID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void sendLeavingParticles(List<Particle> leavingParticles) {
		try {
			WriteMessage wm = sendPort.newMessage();
			wm.writeInt(ARRIVING_PARTICLES_MSG);
			wm.writeObject(leavingParticles);
			wm.finish();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void sendBorderParticles(List<Particle> borderParticles) {
		try {
			WriteMessage wm = sendPort.newMessage();
			wm.writeInt(GHOST_PARTICLES_MSG);
			wm.writeObject(borderParticles);
			wm.finish();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void sendBorderCells(List<Cell> borderCells) {
		try {
			WriteMessage wm = sendPort.newMessage();
			wm.writeInt(GHOST_CELLS_MSG);
			wm.writeObject(borderCells);
			wm.finish();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void closeSendPorts() {
		try {
			sendPort.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void closeReceivePorts() {
		try {
			recvPort.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void sendBorderCellsMap(List<Point> borderCellsMap) {
		try {
			WriteMessage wm = sendPort.newMessage();
			wm.writeInt(GHOST_CELLS_INDEXES_MSG);
			wm.writeObject(borderCellsMap);
			wm.finish();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	private class IncomingMessageHandler implements MessageUpcall {
		public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {
			int msgType = readMessage.readInt();
			switch (msgType) {

				case GHOST_CELLS_INDEXES_MSG:
					List<Point> indexes = (List<Point>)readMessage.readObject();
					ghostCellsIndexesHandler.handle(indexes);
					return;

				case ARRIVING_PARTICLES_MSG:
					List<Particle> particles = (List<Particle>)readMessage.readObject();
					arrivingParticlesHandler.handle(particles);
					return;

				case GHOST_PARTICLES_MSG:
					particles = (List<Particle>)readMessage.readObject();
					ghostParticlesHandler.handle(particles);
					return;

				case GHOST_CELLS_MSG:
					List<Cell> cells = (List<Cell>)readMessage.readObject();
					ghostCellsHandler.handle(cells);
					return;

				default:
					assert false: "Unreachable code detected!";
			}
		}
	}
}
