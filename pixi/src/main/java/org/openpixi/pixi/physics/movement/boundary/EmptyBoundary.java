package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

/**
 * Applied to particles which requires no action (e.g. particles within simulation area).
 */
public class EmptyBoundary extends ParticleBoundary {

	public EmptyBoundary(double xoffset, double yoffset) {
		super(xoffset, yoffset);
	}

	@Override
	public void apply(Solver solver, Force force, Particle particle, double timeStep) {
		// DO NOTHING
	}
}
