package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;

import java.util.List;

public interface IncomingParticlesHandler {
	void handle(List<Particle> particles);
}
