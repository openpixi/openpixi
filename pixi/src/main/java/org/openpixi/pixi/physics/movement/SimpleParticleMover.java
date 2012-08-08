package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.List;

/**
 * Moves and checks the boundary of the particle.
 */
public class SimpleParticleMover extends ParticleMover {

	public SimpleParticleMover(Solver psolver, ParticleBoundaries boundaries) {
		super(psolver, boundaries);
	}

	public void push(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			// Before we move the particle we store its position
			p.storePosition();
			solver.step(p, force, tstep);
			solver.complete(p, force, tstep);
			boundaries.applyOnParticleCenter(p);
			solver.prepare(p, force, tstep);
		}
	}


	public void prepare(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			solver.prepare(p, force, tstep);
		}
	}


	public void complete(List<Particle> particles, Force force, double tstep) {
		for (Particle p : particles) {
			solver.complete(p, force, tstep);
		}
	}
}
