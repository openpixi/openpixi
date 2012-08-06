package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;

import java.util.List;

/**
 * Interface for single threaded and multi threaded particle movers.
 */
public interface ParticleMover {
	ParticleBoundaryType getBoundaryType();

	void changeBoundaryType(ParticleBoundaryType type);

	void push(List<Particle> particles, Force force, double tstep);

	void prepare(List<Particle> particles, Force force, double tstep);

	void complete(List<Particle> particles, Force force, double tstep);
}
