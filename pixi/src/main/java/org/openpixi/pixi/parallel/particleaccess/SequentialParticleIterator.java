package org.openpixi.pixi.parallel.particleaccess;

import org.openpixi.pixi.physics.particles.IParticle;

import java.util.List;

/**
 * Executes the action upon particles in sequential order.
 */
public class SequentialParticleIterator implements ParticleIterator {

	public void execute(List<IParticle> particles, ParticleAction action) {
		for (IParticle particle: particles) {
			action.execute(particle);
		}
	}
}
