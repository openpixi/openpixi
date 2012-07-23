package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 * Determines how are the particles iterated when interpolation happens.
 */
public abstract class InterpolationIterator {

	protected Interpolator interpolator;

	public InterpolationIterator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	public abstract void interpolateToGrid(List<Particle> particles, Grid grid, double tstep);

	public abstract void interpolateToParticle(List<Particle> particles, Grid grid);

	public abstract void interpolateChargedensity(List<Particle> particles, Grid grid);
}
