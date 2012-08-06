package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.List;

/**
 * Interface for single threaded and multi threaded particle movers.
 */
public abstract class ParticleMover {

	/** Solver for the particle equations of motion. */
	protected Solver psolver;

	protected ParticleBoundaries boundaries;


	public ParticleBoundaryType getBoundaryType() {
		return boundaries.getType();
	}

	public Solver getSolver() {
		return psolver;
	}

	public void setSolver(Solver psolver) {
		this.psolver = psolver;
	}


	public ParticleMover(Solver psolver, ParticleBoundaries boundaries) {
		this.psolver = psolver;
		this.boundaries = boundaries;
	}


	public void changeBoundaryType(ParticleBoundaryType type) {
		boundaries.changeType(type);
	}


	public abstract void push(List<Particle> particles, Force force, double tstep);

	public abstract void prepare(List<Particle> particles, Force force, double tstep);

	public abstract void complete(List<Particle> particles, Force force, double tstep);
}
