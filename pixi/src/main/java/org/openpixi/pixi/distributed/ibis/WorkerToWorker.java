package org.openpixi.pixi.distributed.ibis;

import org.openpixi.pixi.distributed.IncomingCellsHandler;
import org.openpixi.pixi.distributed.IncomingParticlesHandler;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;

import java.util.List;

/**
 * Handles the exchange of shared data during the simulation.
 */
public class WorkerToWorker {

	private IbisRegistry registry;

	/** ID of the neighbor with whom this communicator communicates. */
	private int neighborID;

	/* Handlers of upcalls for higher level classes. */
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


	public WorkerToWorker(IbisRegistry registry, int neighborID) {
		this.registry = registry;
		this.neighborID = neighborID;
	}


	public void sendLeavingParticles(List<Particle> leavingParticles) {
		// TODO implement
	}


	public void sendBorderParticles(List<Particle> borderParticles) {
		// TODO implement
	}


	public void sendBorderCells(List<Cell> borderCells) {
		// TODO implement
	}
}
