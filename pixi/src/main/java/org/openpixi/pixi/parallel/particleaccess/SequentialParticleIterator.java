package org.openpixi.pixi.parallel.particleaccess;

import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 * Executes the action upon particles in sequential order.
 */
public class SequentialParticleIterator implements ParticleIterator {

	public void execute(List<Particle> particles, ParticleAction action) {
		for (Particle particle: particles) {
			action.execute(particle);
		}
	}
}
