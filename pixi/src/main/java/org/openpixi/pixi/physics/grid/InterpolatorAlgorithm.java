package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.particles.Particle;

public interface InterpolatorAlgorithm {

	public void interpolateToGrid(Particle p, Grid g, double tstep);
	
	public void interpolateChargedensity(Particle p, Grid g);

	public void interpolateToParticle(Particle p, Grid g);

}
