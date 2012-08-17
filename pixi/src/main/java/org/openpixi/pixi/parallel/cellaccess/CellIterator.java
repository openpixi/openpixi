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

	/**
	 * In this mode the iterator does not calculate the extra cells.
	 */
	public void setNormalMode(int numCellsX, int numCellsY) {
		dimensions = new IntBox(
				0, numCellsX - 1,
				0, numCellsY - 1);
	}


	/**
	 * In this mode the iterator does also calculate the extra cells.
	 */
	public void setExtraCellsMode(int numCellsX, int numCellsY) {
		dimensions = new IntBox(
				-Grid.EXTRA_CELLS_BEFORE_GRID,
				numCellsX + Grid.EXTRA_CELLS_AFTER_GRID - 1,
				-Grid.EXTRA_CELLS_BEFORE_GRID,
				numCellsY + Grid.EXTRA_CELLS_AFTER_GRID - 1);
	}

}
