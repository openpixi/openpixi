package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.List;

public interface IncomingProblemHandler {
	void handle(IntBox[] partitions, List<Particle> particles, Cell[][] cells);
}
