package org.openpixi.pixi.distributed.movement.boundary;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundary;
import org.openpixi.pixi.physics.solver.Solver;

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
	public void apply(Solver solver, Force force, Particle particle, double timeStep) {

		// We need to translate the position of the particle to the coordinates
		// valid at the neighbor.
		// However, we want the particle to keep its position at the local node.
		// Thus, we need a copy of the particle.

		Particle copy = new Particle(particle);
		copy.addX(-xoffset);
		copy.addPrevX(-xoffset);
		copy.addY(-yoffset);
		copy.addPrevY(-yoffset);
		sharedData.registerBorderParticle(copy);
	}
}
