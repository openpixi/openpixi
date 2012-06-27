package org.openpixi.pixi.physics.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;

import java.util.List;

/**
 *
 */
public interface ParticleMover {
	void push(List<Particle> particles, Force force, double tstep);

	void prepare(List<Particle> particles, Force force, double tstep);

	void complete(List<Particle> particles, Force force, double tstep);
}
