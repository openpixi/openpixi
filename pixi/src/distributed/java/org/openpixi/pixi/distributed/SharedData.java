package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.WorkerToWorker;
import org.openpixi.pixi.distributed.util.BooleanLock;
import org.openpixi.pixi.distributed.util.IncomingCellsHandler;
import org.openpixi.pixi.distributed.util.IncomingParticlesHandler;
import org.openpixi.pixi.distributed.util.IncomingPointsHandler;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.util.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the data which needs to be shared between two nodes.
 * Specifically, sends and receives ghost and border particles and cells.
 */
public class SharedData {

	/** Handles the network communication with neighbor. */
	private WorkerToWorker communicator;

	/* Locks to wait for the data to be received. */
	private BooleanLock arrivingParticlesLock = new BooleanLock();
	private BooleanLock ghostParticlesLock = new BooleanLock();
	private BooleanLock ghostCellsLock = new BooleanLock();
	private BooleanLock ghostCellsIndexesLock = new BooleanLock();

	/** Maps the list of outgoing border cells to remote cells (needs to be sent out at the beginning). */
	private List<Point> borderCellsMap = new ArrayList<Point>();

	/** Border cells which need to be send out to neighbor. */
	private List<Cell> borderCells = new ArrayList<Cell>();
	/** Ghost cells references which are shared with the grid class and thus are global. */
	private List<Cell> globalGhostCells = new ArrayList<Cell>();
	/** Ghost cells which are received from neighbor and local to this class,
	 * not visible from outside.
	 * It can happen that the ghost cells arrive too early (in the solve fields phase).
	 * In such case they can not be copied to the grid cells; hence, the local list. */
	private List<Cell> localGhostCells;

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

	/** Required for registering ghost cells. */
	private Grid grid;


	public void setParticleBoundaries(ParticleBoundaries particleBoundaries) {
		this.particleBoundaries = particleBoundaries;
	}


	public void setGrid(Grid grid) {
		this.grid = grid;
	}


	public SharedData(WorkerToWorker communicator) {
		this.communicator = communicator;

		communicator.setGhostCellsHandler(new GhostCellsHandler());
		communicator.setGhostParticlesHandler(new GhostParticlesHandler());
		communicator.setArrivingParticlesHandler(new ArrivingParticlesHandler());
		communicator.setGhostCellsIndexesHandler(new GhostCellsIndexesHandler());
	}


	/**
	 * Initializes the connection +
	 * exchanges the order of ghost cells.
	 */
	public void initializeCommunication() {
		communicator.initializeConnection();
		communicator.sendBorderCellsMap(borderCellsMap);
		ghostCellsIndexesLock.waitForTrue();
	}


	public void registerBorderCell(Cell borderCell, Point remoteCellIndex) {
		borderCells.add(borderCell);
		borderCellsMap.add(remoteCellIndex);
	}


	public void registerBorderParticle(Particle p) {
		borderParticles.add(p);
	}


	public void registerLeavingParticle(Particle p) {
		leavingParticles.add(p);
	}


	public void sendLeavingParticles() {
		communicator.sendLeavingParticles(leavingParticles);
	}


	public void sendBorderParticles() {
		communicator.sendBorderParticles(borderParticles);
	}


	public void sendBorderCells() {
		communicator.sendBorderCells(borderCells);
	}


	public void waitForArrivingParticles() {
		arrivingParticlesLock.waitForTrue();
	}


	/**
	 * Has a twofold function:
	 * 1) Waits for the ghost cells to arrive.
	 * 2) Makes the ghost cells visible to simulation and grid classes.
	 */
	public void waitForGhostCells() {
		ghostCellsLock.waitForTrue();
		for (int i = 0; i < localGhostCells.size(); ++i) {
			globalGhostCells.get(i).copyFrom(localGhostCells.get(i));
		}
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
		leavingParticles.clear();
		borderParticles.clear();
	}


	/**
	 * Resets the cell list waiting lock.
	 * => Should be called at the end of cell communication.
	 */
	public void cleanUpCellCommunication() {
		ghostCellsLock.reset();
	}


	public void closeSendPorts() {
		communicator.closeSendPorts();
	}


	public void closeReceivePorts() {
		communicator.closeReceivePorts();
	}


	private class GhostCellsIndexesHandler implements IncomingPointsHandler {

		/**
		 * Registers the ghost cells in the same order
		 * as the corresponding border cells at the neighbor.
		 */
		public void handle(List<Point> ghostCellsMap) {
			assert grid != null;
			for (Point cellIndex: ghostCellsMap) {
				globalGhostCells.add(grid.getCell(cellIndex.x, cellIndex.y));
			}
			ghostCellsIndexesLock.setToTrue();
		}
	}


	private class GhostCellsHandler implements IncomingCellsHandler {
		public void handle(List<Cell> cells) {
			assert cells.size() == globalGhostCells.size();
			localGhostCells = cells;
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
		 * All arriving particle become right away also border particles.
		 * Furthermore, they might also lie outside of the simulation area.
		 */
		public void handle(List<Particle> particles) {
			arrivingParticles = particles;
			for (Particle particle: particles) {
				/**
				 * The nulls won't work with hardwall boundaries!!!
				 * TODO inject the solver, force and time step into shared data from classes above
				 */
				particleBoundaries.applyOnParticleCenter(null, null, particle, 0);
			}
			arrivingParticlesLock.setToTrue();
		}
	}
}
