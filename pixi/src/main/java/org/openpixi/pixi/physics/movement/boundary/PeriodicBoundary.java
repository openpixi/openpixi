package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

/**
 * If the particle leaves at one side, it reappears at the
 * other opposite side of the simulation area.
 */
public class PeriodicBoundary extends ParticleBoundary {

	public PeriodicBoundary(double xoffset, double yoffset) {
		super(xoffset, yoffset);
	}

	@Override
	public void apply(Solver solver, Force force, Particle particle, double timeStep) {
		particle.addX(-xoffset);
		particle.addPrevX(-xoffset);
		particle.addY(-yoffset);
		particle.addPrevY(-yoffset);
	}
}
