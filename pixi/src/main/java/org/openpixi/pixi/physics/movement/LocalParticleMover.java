package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.movement.boundary.RegionBoundaryMap2D;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.List;

/**
 * Non-distributed version of particle movement.
 * Moves and checks the boundary of the particle.
 */
public class LocalParticleMover implements ParticleMover {

	/** Solver for the particle equations of motion */
	private Solver psolver;
	RegionBoundaryMap2D boundaries;

	public LocalParticleMover(
			Solver psolver,
			BoundingBox simulationArea,
			ParticleBoundaryType boundaryType) {

		this.psolver = psolver;
		boundaries = new RegionBoundaryMap2D(simulationArea, boundaryType);
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
