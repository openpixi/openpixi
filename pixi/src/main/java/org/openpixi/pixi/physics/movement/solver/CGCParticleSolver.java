package org.openpixi.pixi.physics.movement.solver;

import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.CGCParticle;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Particle solver for CGC simulations. The particles stay on fixed trajectories moving at light speed. The charge has
 * to be parallel transporting along the trajectory. It is assumed that particles move on a grid line such that parallel
 * transport using gauge links is well defined.
 */
public class CGCParticleSolver implements ParticleSolver {

	public void step(IParticle p, Force f, double dt) {
		CGCParticle P = (CGCParticle) p;

		for (int i = 0; i < P.pos0.length; i++) {
			P.pos1[i] = P.pos0[i] + P.vel[i] * dt;
		}

		P.Q1 = P.Q0.act(P.U.adj());

	}

	public void prepare(IParticle p, Force f, double step) {
		// Not implemented.
	}

	public void complete(IParticle p, Force f, double step) {
		// Not implemented.
	}
}
