package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

/**
 * Determines how the particle behaves when it leaves the simulation area.
 */
public abstract class ParticleBoundary {

	/**
	 * Distance of the boundary region xmin from simulation's xmin.
	 * Can have three values:
	 * -simulation_width: signalizes that the particle is left of the simulation area
	 * 0: signalizes that the particle's x coordinate is within the simulation area
	 * +simulation_width: signalizes that the particle is right of the simulation area
	 * */
	protected double xoffset;
	/**
	 * Distance of the boundary region ymin from simulation's ymin.
	 * Can have three values (-simulation_height,0,+simulation_height) similarly as xoffset.
	 * */
	protected double yoffset;

	public ParticleBoundary(double xoffset, double yoffset) {
		this.xoffset = xoffset;
		this.yoffset = yoffset;
	}

	public abstract void apply(Solver solver, Force force, Particle particle, double timeStep);
}
