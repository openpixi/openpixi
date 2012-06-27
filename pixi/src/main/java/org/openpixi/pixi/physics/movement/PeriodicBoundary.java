package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;

/**
 * If the particle leaves at one side, it reappears at the
 * other opposite side of the simulation area.
 */
public class PeriodicBoundary extends ParticleBoundary {

	public PeriodicBoundary(double xoffset, double yoffset) {
		super(xoffset, yoffset);
	}

	@Override
	public void apply(Particle p) {
		p.addX(xoffset);
		p.addPrevX(xoffset);
		p.addY(yoffset);
		p.addPrevY(yoffset);
	}
}
