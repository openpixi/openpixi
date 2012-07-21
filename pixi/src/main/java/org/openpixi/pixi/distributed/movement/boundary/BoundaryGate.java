package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundary;

/**
 *  Registers the leaving particles (particles which are crossing to neighbors).
 */
public class BoundaryGate extends ParticleBoundary {

	private SharedData sharedData;


	public BoundaryGate(double xoffset, double yoffset, SharedData sharedData) {
		super(xoffset, yoffset);
		this.sharedData = sharedData;
	}


	@Override
	public void apply(Particle p) {
		sharedData.registerLeavingParticle(p);
	}
}
