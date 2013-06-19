package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.particles.Particle;

public class EmptyInterpolator implements InterpolatorAlgorithm {

	@Override
	public void interpolateToGrid(Particle p, Grid g, double tstep) {
		// DO NOTHING
	}

	@Override
	public void interpolateToParticle(Particle p, Grid g) {
		// DO NOTHING
	}

	@Override
	public void interpolateChargedensity(Particle p, Grid g) {
		// DO NOTHING
	}
}
