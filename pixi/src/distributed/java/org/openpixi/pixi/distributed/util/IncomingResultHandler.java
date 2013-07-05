package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.grid.Cell;

import java.util.List;

public interface IncomingResultHandler {
	void handle(int workerID, List<Particle> particlePartitions, Cell[][] gridPartitions);
}
