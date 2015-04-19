package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.List;

public interface IncomingProblemHandler {
	void handle(IntBox[] partitions, List<IParticle> particles, Cell[][] cells);
}
