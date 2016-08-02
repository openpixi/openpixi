package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;

/**
 * Common interface for all CGC initial particles.
 */
public interface IParticleCreator {
	void initialize(Simulation s, int direction, int orientation);

	void setGaussConstraint(AlgebraElement[] gaussConstraint);
}
