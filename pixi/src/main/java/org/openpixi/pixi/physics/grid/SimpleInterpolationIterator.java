package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 *
 */
public class SimpleInterpolationIterator extends InterpolationIterator {

	public SimpleInterpolationIterator(Interpolator interpolator) {
		super(interpolator);
	}

	@Override
	public void interpolateToGrid(List<Particle> particles, Grid grid, double tstep) {
		grid.resetCurrent();
		for (Particle p: particles) {
			interpolator.interpolateToGrid(p, grid, tstep);
		}
	}

	@Override
	public void interpolateToParticle(List<Particle> particles, Grid grid) {
		for (Particle p: particles) {
			interpolator.interpolateToParticle(p, grid);
		}
	}

	@Override
	public void interpolateChargedensity(List<Particle> particles, Grid grid) {
		grid.resetCharge();
		for (Particle p: particles) {
			interpolator.interpolateChargedensity(p, grid);
		}
	}
}
