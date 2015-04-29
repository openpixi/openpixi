package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.parallel.particleaccess.ParticleAction;
import org.openpixi.pixi.parallel.particleaccess.ParticleIterator;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.IParticleBoundaryConditions;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.solver.Solver;
import org.openpixi.pixi.physics.grid.Grid;

import java.util.List;

/**
 * Moves and checks the boundary of the particle.
 */
public class ParticleMover {

	/** Solver for the particle equations of motion. */
	private Solver solver;
	private IParticleBoundaryConditions boundaries;
	private ParticleIterator particleIterator;

	/* These are set in each iteration to enable the inner classes to read them. */
	private Force force;
	private double timeStep;

	private Push push = new Push();
	private Prepare prepare = new Prepare();
	private Complete complete = new Complete();


	public Solver getSolver() {
		return solver;
	}

	public void setSolver(Solver psolver) {
		this.solver = psolver;
	}


	public ParticleMover(
			Solver solver,
			IParticleBoundaryConditions boundaries,
			ParticleIterator particleIterator) {
		this.solver = solver;
		this.boundaries = boundaries;
		this.particleIterator = particleIterator;
	}


	public void push(List<IParticle> particles, Force force, Grid g, double timeStep) {
		this.force = force;
		this.timeStep = timeStep;
		particleIterator.execute(particles, push);

	}


	public void prepare(List<IParticle> particles, Force force, double timeStep) {
		this.force = force;
		this.timeStep = timeStep;
		particleIterator.execute(particles, prepare);
	}


	public void complete(List<IParticle> particles, Force force, double timeStep) {
		this.force = force;
		this.timeStep = timeStep;
		particleIterator.execute(particles, complete);
	}


	private class Push implements ParticleAction {
		public void execute(IParticle particle) {
			particle.storePosition();
			solver.step(particle, force, timeStep);
			boundaries.applyOnParticle(particle);
		}
	}


	private class Prepare implements ParticleAction {
		public void execute(IParticle particle) {
			solver.prepare(particle, force, timeStep);
		}
	}


	private class Complete implements ParticleAction {
		public void execute(IParticle particle) {
			solver.complete(particle, force, timeStep);
		}
	}
}
