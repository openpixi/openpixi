package org.openpixi.pixi.parallel.cellaccess;

import org.openpixi.pixi.physics.grid.Grid;

/**
 * Iterates over all the cells in sequential order.
 */
public class SequentialCellIterator extends CellIterator {

	public void execute(Grid grid, CellAction action) {
		for (int x = dimensions.xmin(); x <= dimensions.xmax(); ++x) {
			for (int y = dimensions.ymin(); y <= dimensions.ymax(); ++y) {
				action.execute(grid, x, y);
			}
		}
	}
}
