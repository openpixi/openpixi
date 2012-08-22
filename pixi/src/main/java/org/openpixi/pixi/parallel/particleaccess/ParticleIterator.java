package org.openpixi.pixi.parallel.particleaccess;

import org.openpixi.pixi.physics.Particle;

import java.util.List;

/**
 *  Interface for iterating over particles.
 *  Any class using this interface will result in a more complex code.
 *  On the other hand, the class using this interface can make use of parallel particle iterator
 *  which can work upon the particles in parallel.
 *
 *  Each implementation calls the ParticleAction.execute() method on each particle.
 */
public interface ParticleIterator {
	void execute(List<Particle> particles, ParticleAction action);
}
