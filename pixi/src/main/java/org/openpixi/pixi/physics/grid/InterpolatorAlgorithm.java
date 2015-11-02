package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.particles.IParticle;

public interface InterpolatorAlgorithm {

	public void interpolateToGrid(IParticle p, Grid g, double tstep);

	public void interpolateChargedensity(IParticle p, Grid g);

	public void interpolateToParticle(IParticle p, Grid g);

}