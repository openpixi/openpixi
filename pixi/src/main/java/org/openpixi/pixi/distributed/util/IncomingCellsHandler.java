package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.grid.Cell;

import java.util.List;

public interface IncomingCellsHandler {
	void handle(List<Cell> cells);
}
