package org.openpixi.pixi.parallel.cellaccess;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Interface for iterating over the cells of grid.
 * Enables parallel access to cells.
 */
public abstract class CellIterator {

	protected IntBox dimensions;

	public abstract void execute(Grid grid, CellAction action);
	
	public abstract double calculate(Grid grid, CellAction action);

	/**
	 * In this mode the iterator does not calculate the extra cells.
	 */
	public void setNormalMode(int[] numCells) {
		
		int length = numCells.length;
		int[] min = new int[length];
		int[] max = new int[length];
		for (int i=0;i<length;i++) {
			min[i] = 0;
			max[i] = numCells[i] - 1;
		}
		dimensions = new IntBox(length, min, max);
	}

}
