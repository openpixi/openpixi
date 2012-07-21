package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;

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
}
