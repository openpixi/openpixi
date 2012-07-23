package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.WorkerToWorker;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the data which needs to be shared between two nodes.
 * Specifically, sends and receives ghost and border particles and cells.
 */
public class SharedData {

	/** Handles the network communication with neighbor. */
	private WorkerToWorker neighbor;

	/* Locks to wait for the data to arrive. */
	private BooleanLock arrivingParticlesLock = new BooleanLock();
	private BooleanLock ghostParticlesLock = new BooleanLock();
	private BooleanLock ghostCellsLock = new BooleanLock();

	/** Ghost cells of this node - cells to be received from neighbors. */
	private List<Cell> ghostCells = new ArrayList<Cell>();
	/** Ghost cells of neighbors - cells to be send to neighbors. */
	private List<Cell> borderCells = new ArrayList<Cell>();

	/** Ghost particles of neighbors - particles to be send to neighbors. */
	private List<Particle> borderParticles = new ArrayList<Particle>();
	/** Particles leaving this node - particles to be send to neighbors. */
	private List<Particle> leavingParticles = new ArrayList<Particle>();
	/** Ghost particles of this node - particles to be received from neighbors. */
	private List<Particle> ghostParticles = new ArrayList<Particle>();
	/** Particles arriving to this node - particles to be received from neighbors. */
	private List<Particle> arrivingParticles = new ArrayList<Particle>();

	/**
	 * The arriving particles need to be checked
	 * (they become border particles +
	 * they might need to be reflected in the case of hardwall boundaries)
	 */
	private ParticleBoundaries particleBoundaries;


	public void setParticleBoundaries(ParticleBoundaries particleBoundaries) {
		this.particleBoundaries = particleBoundaries;
	}


	public SharedData(WorkerToWorker neighbor) {
		this.neighbor = neighbor;
		neighbor.setGhostCellsHandler(new GhostCellsHandler());
		neighbor.setGhostParticlesHandler(new GhostParticlesHandler());
		neighbor.setArrivingParticlesHandler(new ArrivingParticlesHandler());
	}


	public void registerBorderCell(Cell cell) {
		borderCells.add(cell);
	}


	public void registerGhostCell(Cell cell) {
		ghostCells.add(cell);
	}


	public void registerBorderParticle(Particle p) {
		borderParticles.add(p);
	}


	public void registerLeavingParticle(Particle p) {
		leavingParticles.add(p);
	}


	public void sendLeavingParticles() {
		neighbor.sendLeavingParticles(leavingParticles);
	}


	public void sendBorderParticles() {
		neighbor.sendBorderParticles(borderParticles);
	}


	public void sendBorderCells() {
		neighbor.sendBorderCells(borderCells);
	}


	public void waitForArrivingParticles() {
		arrivingParticlesLock.waitForTrue();
	}


	public void waitForGhostCells() {
		ghostCellsLock.waitForTrue();
	}


	/**
	 * Blocks until the arriving particles are received.
	 */
	public List<Particle> getArrivingParticles() {
		waitForArrivingParticles();
		return arrivingParticles;
	}


	/**
	 * Blocks until the ghost particles are received.
	 */
	public List<Particle> getGhostParticles() {
		ghostParticlesLock.waitForTrue();
		return ghostParticles;
	}


	public List<Particle> getLeavingParticles() {
		return leavingParticles;
	}


	/**
	 * Resets the particle lists waiting locks.
	 * Resets the particle lists which are send to neighbors.
	 * => Should be called at the end of particle communication.
	 */
	public void cleanUpParticleCommunication() {
		ghostParticlesLock.reset();
		arrivingParticlesLock.reset();
		borderParticles.clear();
		leavingParticles.clear();
	}


	/**
	 * Resets the cell list waiting lock.
	 * => Should be called at the end of cell communication.
	 */
	public void cleanUpCellCommunication() {
		ghostCellsLock.reset();
	}


	private class GhostCellsHandler implements IncomingCellsHandler {

		/**
		 * The values of incoming ghost cells have to be copied to the existing cells.
		 * We can not simply use new reference to the cell
		 * since the very reference that we have is also used by the grid.
		 */
		public void handle(List<Cell> cells) {
			assert cells.size() == ghostCells.size();
			for (int i = 0; i < cells.size(); ++i) {
				ghostCells.get(i).copy(cells.get(i));
			}
			ghostCellsLock.setToTrue();
		}
	}


	private class GhostParticlesHandler implements IncomingParticlesHandler {
		public void handle(List<Particle> particles) {
			ghostParticles = particles;
			ghostParticlesLock.setToTrue();
		}
	}


	private class ArrivingParticlesHandler implements  IncomingParticlesHandler {

		/**
		 * All arriving particles have to be checked with boundary classes.
		 * There are for sure border particles.
		 * There might be particles outside of the simulation area.
		 */
		public void handle(List<Particle> particles) {
			arrivingParticles = particles;
			for (Particle particle: particles) {
				particleBoundaries.apply(particle);
			}
			arrivingParticlesLock.setToTrue();
		}
	}
}
