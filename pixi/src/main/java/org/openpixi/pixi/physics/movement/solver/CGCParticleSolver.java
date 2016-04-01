package org.openpixi.pixi.physics.movement.solver;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.CGCParticle;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Particle solver for CGC simulations. The particles stay on fixed trajectories moving at light speed. The charge has
 * to be parallel transporting along the trajectory. It is assumed that particles move on a grid line such that parallel
 * transport using gauge links is well defined.
 */
public class CGCParticleSolver implements ParticleSolver {

	public void updatePosition(IParticle p, Force f, double dt) {
		CGCParticle P = (CGCParticle) p;

		for (int i = 0; i < P.pos0.length; i++) {
			P.pos1[i] = P.pos0[i] + P.vel[i] * dt;
		}
	}


	public void updateCharge(IParticle p, Force f, double dt) {
		CGCParticle P = (CGCParticle) p;
		if(P.updateCharge) {
			// Charge has to be parallel transported.
			P.Q1 = P.Q0.act(P.U.adj());
			P.updateCharge = false;
		} else {
			// No update needed, just switch Q1 and Q0.
			AlgebraElement Q = P.Q1;
			P.Q1 = P.Q0;
			P.Q0 = Q;
		}
	}

	public void prepare(IParticle p, Force f, double step) {
		// Not implemented.
	}

	public void complete(IParticle p, Force f, double step) {
		// Not implemented.
	}
}
