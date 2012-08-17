package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 * Base class for local and distributed interpolation.
 */
public abstract class Interpolation {

	protected InterpolatorAlgorithm interpolator;

	public Interpolation(InterpolatorAlgorithm interpolator) {
		this.interpolator = interpolator;
	}

	public abstract void interpolateToGrid(List<Particle> particles, Grid grid, double tstep);

	public abstract void interpolateToParticle(List<Particle> particles, Grid grid);

	public abstract void interpolateChargedensity(List<Particle> particles, Grid grid);
}
