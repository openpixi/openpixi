package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;

/**
 * Applied to particles which requires no action (e.g. particles within simulation area).
 */
public class EmptyBoundary extends ParticleBoundary {

	public EmptyBoundary(double xoffset, double yoffset) {
		super(xoffset, yoffset);
	}

	@Override
	public void apply(Particle p) {
		// DO NOTHING
	}
}
