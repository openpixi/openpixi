package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.particles.IParticle;

import java.util.List;

public interface IncomingParticlesHandler {
	void handle(List<IParticle> particles);
}
