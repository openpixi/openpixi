package org.openpixi.pixi.parallel.cellaccess;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Iterates over all the cells in sequential order.
 */
public class SequentialCellIterator extends CellIterator {

	public void execute(Grid grid, CellAction action) {
		
		int numOfCells = dimensions.getNumCells();
		for (int cellIdx = 0; cellIdx < numOfCells; cellIdx++) {
            int[] pos = grid.getCellPos(cellIdx);
			action.execute(grid, pos);
		}
	}
}
