package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;

public interface ParticleBoundaries {
	void changeType(ParticleBoundaryType boundaryType);

	/**
	 * Uses bounding box around the particle based on particle's radius to determine the region
	 * of the particle. In another words, it enables to reflect the particle based on particle's
	 * radius being outside of simulation area.
	 *
	 * This method is not safe and should be use only when really required!
	 *
	 * !!! IMPORTANT !!!
	 * - Can not be used in distributed simulation because crossing particles
	 *   would be detected too early!
	 * - Can not be used in parallel simulation because the usage of common particle bounding box
	 *   is not thread safe. On the other hand, to create a new bounding box (call constructor)
	 *   for each particle would be thread safe but also slow.
	 */
	void applyOnParticleBoundingBox(Particle p);

	void applyOnParticleCenter(Particle p);

	ParticleBoundaryType getType();
}
