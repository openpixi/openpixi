package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.grid.Cell;

import java.util.List;

public interface IncomingResultHandler {
	void handle(int workerID, List<IParticle> particlePartitions, Cell[][] gridPartitions);
}
