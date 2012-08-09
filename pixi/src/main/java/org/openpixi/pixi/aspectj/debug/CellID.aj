package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

/**
 * Adds ID to each cell for easier debugging of cell transfer.
 */
public privileged aspect CellID {

	int Cell.id;


	@AdviceName("setCellID")
	after(Grid grid): execution(*..Grid.new(*..Settings)) && this(grid) &&
			cflow(within(*..Master)) {
		int xmin = -Grid.EXTRA_CELLS_BEFORE_GRID;
		int ymin = -Grid.EXTRA_CELLS_BEFORE_GRID;
		int xmax = grid.getNumCellsX() + Grid.EXTRA_CELLS_AFTER_GRID - 1;
		int ymax = grid.getNumCellsY() + Grid.EXTRA_CELLS_AFTER_GRID - 1;

		int id = 0;
		for (int x = xmin; x <= xmax; ++x) {
			for (int y = ymin; y <= ymax; ++y) {
				grid.getCell(x,y).id = id;
				++id;
			}
		}
	}


	@AdviceName("copyCellID")
	after(Cell from, Cell to):
			execution(* *..Cell.copyFrom(Cell)) && args(from) && this(to) {
		from.id = to.id;
	}

}
