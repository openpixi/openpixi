package org.openpixi.pixi.parallel.particleaccess;

import org.openpixi.pixi.physics.particles.Particle;

/**
 * Required in particle iterator, so that we are able to use the particle iterator
 * for different operations upon particles.
 */
public interface ParticleAction {
	void execute(Particle particle);
}
