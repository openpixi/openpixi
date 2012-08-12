package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

/**
 * Adds ID to each cell for easier debugging of cell transfer.
 */
public privileged aspect CellID {

	String Cell.id;


	@AdviceName("setCellID")
	after(Grid grid): execution(*..Grid.new(*..Settings)) && this(grid) &&
			cflow(within(*..Master)) {
		for (int x = 0; x < grid.getNumCellsX(); ++x) {
			for (int y = 0; y < grid.getNumCellsY(); ++y) {
				grid.getCell(x,y).id = String.format("[%d,%d]", x, y);
			}
		}
	}


	@AdviceName("copyCellID")
	after(Cell from, Cell to):
			execution(* *..Cell.copyFrom(Cell)) && args(from) && this(to) {
		from.id = to.id;
	}

}
