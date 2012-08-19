package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundary;
import org.openpixi.pixi.physics.solver.Solver;

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
	public void apply(Solver solver, Force force, Particle particle, double timeStep) {
		// Translate the position of the particle to match the position at the remote node.
		particle.addX(-xoffset);
		particle.addPrevX(-xoffset);
		particle.addY(-yoffset);
		particle.addPrevY(-yoffset);
		sharedData.registerLeavingParticle(particle);
	}
}
