package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.List;

/**
 * Moves and checks the boundary of the particle.
 */
public class SimpleParticleMover implements ParticleMover {

	/** Solver for the particle equations of motion. */
	public Solver psolver;

	private ParticleBoundaries boundaries;


	public SimpleParticleMover(Solver psolver, ParticleBoundaries boundaries) {
		this.psolver = psolver;
		this.boundaries = boundaries;
	}


	public ParticleBoundaryType getBoundaryType() {
		return boundaries.getType();
	}


	public void changeBoundaryType(ParticleBoundaryType type) {
		boundaries.changeType(type);
	}


	public void push(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			// Before we move the particle we store its position
			p.storePosition();
			psolver.step(p, force, tstep);
			psolver.complete(p, force, tstep);
			boundaries.applyOnParticleCenter(p);
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
