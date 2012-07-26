package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundary;

/**
 *  Registers the border particles (particles which belong to this node
 *  but need to be send to neighbors as they influence their interpolation).
 */
public class BorderGate extends ParticleBoundary {

	private SharedData sharedData;


	public BorderGate(double xoffset, double yoffset, SharedData sharedData) {
		super(xoffset, yoffset);
		this.sharedData = sharedData;
	}


	@Override
	public void apply(Particle p) {
		sharedData.registerBorderParticle(p);
	}
}
