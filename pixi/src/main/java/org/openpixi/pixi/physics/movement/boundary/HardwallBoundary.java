package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

/**
 *  Reflects particle off the wall.
 *  Allows particle to be outside of the simulation area!
 */
public class HardwallBoundary extends ParticleBoundary {

	public HardwallBoundary(double xoffset, double yoffset) {
		super(xoffset, yoffset);
	}

	/**
	 * Since we are modifying the velocity we need to bring it from the half step to whole step.
	 */
	@Override
	public void apply(Solver solver, Force force, Particle particle, double timeStep) {
		solver.complete(particle, force, timeStep);

		if (xoffset < 0) {
			particle.setVx(Math.abs(particle.getVx()));
		}
		else if (xoffset > 0) {
			particle.setVx(-Math.abs(particle.getVx()));
		}
		if (yoffset < 0) {
			particle.setVy(Math.abs(particle.getVy()));
		}
		else if (yoffset > 0) {
			particle.setVy(-Math.abs(particle.getVy()));
		}

		solver.prepare(particle, force, timeStep);
	}
}
