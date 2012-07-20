package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;

/**
 *
 */
public interface ParticleBoundaries {
	void changeType(ParticleBoundaryType boundaryType);

	void apply(Particle p);

	ParticleBoundaryType getType();
}
