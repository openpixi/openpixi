package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.particles.IParticle;

import java.util.List;

/**
 * Base class for local and distributed interpolation.
 */
public abstract class Interpolation {

	protected InterpolatorAlgorithm interpolator;

	public Interpolation(InterpolatorAlgorithm interpolator) {
		this.interpolator = interpolator;
	}

	public abstract void interpolateToGrid(List<IParticle> particles, Grid grid, double tstep);

	public abstract void interpolateToParticle(List<IParticle> particles, Grid grid);

	public abstract void interpolateChargedensity(List<IParticle> particles, Grid grid);
}
