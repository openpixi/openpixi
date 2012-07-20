package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.Solver;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.List;

/**
 * Non-distributed version of particle movement.
 * Moves and checks the boundary of the particle.
 */
public class ParticleMover {

	/** Solver for the particle equations of motion */
	public Solver psolver;
	private ParticleBoundaries boundaries;

	/** Remembers the boundary type. */
	private ParticleBoundaryType boundaryType;
	/** Remembers the simulation area. */
	private DoubleBox simulationArea;

	public ParticleMover(
			Solver psolver,
			DoubleBox simulationArea,
			ParticleBoundaryType boundaryType) {

		this.psolver = psolver;
		this.boundaryType = boundaryType;
		this.simulationArea = simulationArea;
		boundaries = new ParticleBoundaries(simulationArea, boundaryType);
	}


	public ParticleBoundaryType getBoundaryType() {
		return boundaryType;
	}


	public void setBoundaryType(ParticleBoundaryType type) {
		this.boundaryType = type;
		boundaries = new ParticleBoundaries(simulationArea, type);
	}


	public void push(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			// Before we move the particle we store its position
			p.storePosition();
			psolver.step(p, force, tstep);
			psolver.complete(p, force, tstep);
			boundaries.apply(p);
			psolver.prepare(p, force, tstep);
		}
	}


	public void prepare(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			psolver.prepare(p, force, tstep);
		}
	}


	public void complete(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			psolver.complete(p, force, tstep);
		}
	}
}
