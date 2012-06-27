package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;

/**
 * Determines how the particle behaves when it leaves the simulation area.
 */
public abstract class ParticleBoundary {

	/** Distance of the boundary region xmin from simulation's xmin. */
	protected double xoffset;
	/** Distance of the boundary region ymin from simulation's ymin. */
	protected double yoffset;

	public ParticleBoundary(double xoffset, double yoffset) {
		this.xoffset = xoffset;
		this.yoffset = yoffset;
	}

	public abstract void apply(Particle p);
}
