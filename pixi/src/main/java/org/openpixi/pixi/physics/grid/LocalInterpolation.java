package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.parallel.particleaccess.ParticleAction;
import org.openpixi.pixi.parallel.particleaccess.ParticleIterator;
import org.openpixi.pixi.physics.particles.IParticle;

import java.util.List;

/**
 * Calls the specific interpolation algorithm.
 * Implements the iteration over the particles using the ParticleIterator.
 */
public class LocalInterpolation extends Interpolation {

	private ParticleIterator particleIterator;

	private Grid grid;
	private double timeStep;

	private InterpolateToGrid interpolateToGrid = new InterpolateToGrid();
	private InterpolateToParticle interpolateToParticle = new InterpolateToParticle();
	private InterpolateChargedensity interpolateChargedensity = new InterpolateChargedensity();

	public LocalInterpolation(InterpolatorAlgorithm interpolator,
			ParticleIterator particleIterator) {
		super(interpolator);
		this.particleIterator = particleIterator;
	}

	@Override
	public void interpolateToGrid(List<IParticle> particles, Grid grid) {
		//grid.resetCurrent();
		this.grid = grid;
		this.timeStep = grid.getTemporalSpacing();
		particleIterator.execute(particles, interpolateToGrid);
	}

	@Override
	public void interpolateToParticle(List<IParticle> particles, Grid grid) {
		this.grid = grid;
		particleIterator.execute(particles, interpolateToParticle);
	}

	@Override
	public void interpolateChargedensity(List<IParticle> particles, Grid grid) {
		//grid.resetCharge();
		this.grid = grid;
		particleIterator.execute(particles, interpolateChargedensity);
	}

	private class InterpolateToGrid implements ParticleAction {
		public void execute(IParticle particle) {
			interpolator.interpolateToGrid(particle, grid);
		}
	}

	private class InterpolateToParticle implements ParticleAction {
		public void execute(IParticle particle) {
			interpolator.interpolateToParticle(particle, grid);
		}
	}

	private class InterpolateChargedensity implements ParticleAction {
		public void execute(IParticle particle) {
			interpolator.interpolateChargedensity(particle, grid);
		}
	}
}
