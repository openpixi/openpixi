package org.openpixi.pixi.physics.grid;

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
		// Not implemented yet.
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		// Not implemented yet.
	}

	public void interpolateToParticle(IParticle p, Grid g) {
		/*
		Usually this method would tell the particles what gauge links are currently acting on them. In the case of
		the optimized super particle classes, parallel transport is taken care of by interpolateToGrid().
		 */
	}
}
