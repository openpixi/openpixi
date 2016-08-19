package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.particles.CGCSuperParticle;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * This interpolation algorithm is used for the optimized version of CGC simulations in the lab frame. The particles
 * in this type of simulation merely act as 'static' sources for the current. It is assumed that this kind of particle
 * moves along a grid axis such that there is no ambiguity in defining parallel transport for the color charges of the
 * particles. The super particle classes encapsulate larger collections of particles whose relative positions are fixed
 * during the simulation and whose charges are updated at the same time when they cross into other cells.
 */
public class CGCSuperParticleInterpolationNGP implements  InterpolatorAlgorithm {
	public void interpolateToGrid(IParticle p, Grid g) {
		double at = g.getTemporalSpacing();
		double as = g.getLatticeSpacing();

		CGCSuperParticle P = (CGCSuperParticle) p;
		if(P.needsUpdate(g.getSimulationSteps())) {
			int indexOffset = P.getCurrentOffset(g.getSimulationSteps());
			if(P.orientation > 0) {
				for (int i = 0; i < P.numberOfParticles; i++) {
					int index = i + indexOffset;
					AlgebraElement J = P.Q[i].mult(as / at);
					g.addJ(index, 0, J); // Optimizations only work for x-direction!
					GroupElement U = g.getUnext(index, 0);
					P.Q[i].actAssign(U.adj());
				}
			} else {
				// Other orientation not yet implemented.
			}
		}
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCSuperParticle P = (CGCSuperParticle) p;
		int indexOffset = P.getCurrentNGPOffset(g.getSimulationSteps());
		for (int i = 0; i < P.numberOfParticles; i++) {
			int index = i + indexOffset;
			g.addRho(index, P.Q[i]);
		}
	}

	public void interpolateToParticle(IParticle p, Grid g) {
		/*
		Usually this method would tell the particles what gauge links are currently acting on them. In the case of
		the optimized super particle classes, parallel transport is taken care of by interpolateToGrid().
		 */
	}
}
