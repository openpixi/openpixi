package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.particles.IParticle;

public class EmptyInterpolator implements InterpolatorAlgorithm {

	@Override
	public void interpolateToGrid(IParticle p, Grid g, double tstep) {
		// DO NOTHING
	}

	@Override
	public void interpolateToParticle(IParticle p, Grid g) {
		// DO NOTHING
	}

	@Override
	public void interpolateChargedensity(IParticle p, Grid g) {
		// DO NOTHING
	}
}
