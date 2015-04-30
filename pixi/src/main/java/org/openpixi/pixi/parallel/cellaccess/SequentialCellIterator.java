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
            int[] pos = convertCellIndexToPosition(cellIdx, dimensions);
			action.execute(grid, pos);
		}
	}
	//TODO This should happen only once to avoid code duplication
	private int[] convertCellIndexToPosition(int ci, IntBox dimensions)
    {
        int dim = dimensions.getDim();
        int[] pos = new int[dim];

        for(int i = 0; i < dim; i++)
        {
            pos[i] = ci % dimensions.getSize(i) + dimensions.getMin(i);
            ci -= pos[i];
            ci /= dimensions.getSize(i);
        }

        return pos;
    }
	
}
