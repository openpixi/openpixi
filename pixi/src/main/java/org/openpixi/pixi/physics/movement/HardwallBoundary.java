package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;

/**
 *  Reflects particle off the wall.
 *  Allows particle to be outside of the simulation area!
 */
public class HardwallBoundary extends ParticleBoundary {

	public HardwallBoundary(double xoffset, double yoffset) {
		super(xoffset, yoffset);
	}

	@Override
	public void apply(Particle p) {
		if (xoffset != 0) {
			p.setVx(-p.getVx());
		}
		if (yoffset != 0) {
			p.setVy(-p.getVy());
		}
	}
}
