package org.openpixi.pixi.distributed;

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

	private int neighborID;

	/** Ghost cells of this node - cells to be received from neighbors. */
	private List<Cell> boundaryCells = new ArrayList<Cell>();
	/** Ghost cells of neighbors - cells to be send to neighbors. */
	private List<Cell> borderCells = new ArrayList<Cell>();

	/** Ghost particles of neighbors - particles to be send to neighbors. */
	private List<Particle> borderParticles = new ArrayList<Particle>();
	/** Particles leaving this node - particles to be send to neighbors. */
	private List<Particle> leavingParticles = new ArrayList<Particle>();
	/** Ghost particles of this node - particles to be received from neighbors. */
	private List<Particle> boundaryParticles = new ArrayList<Particle>();
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


	public SharedData(int neighborID) {
		this.neighborID = neighborID;
	}


	public void registerBorderCell(Cell cell) {
		borderCells.add(cell);
	}


	public void registerBoundaryCell(Cell cell) {
		boundaryCells.add(cell);
	}


	public void registerBorderParticle(Particle p) {
		borderParticles.add(p);
	}


	public void registerLeavingParticle(Particle p) {
		leavingParticles.add(p);
	}


	public void sendLeavingParticles() {
		// TODO implement
	}


	public void sendBorderParticles() {
		// TODO implement
	}


	public void sendBorderCells() {
		// TODO implement
	}


	public void waitForArrivingParticles() {
		// TODO implement
	}


	private void waitForGhostParticles() {
		// TODO implement
	}


	public void waitForGhostCells() {
		// TODO implement
	}


	public List<Particle> getArrivingParticles() {
		return null;  // TODO implement
	}


	public List<Particle> getGhostParticles() {
		return null;  // TODO implement
	}


	public List<Particle> getLeavingParticles() {
		return null;  // TODO implement
	}
}
