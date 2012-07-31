package org.openpixi.pixi.distributed.grid;

import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.distributed.movement.boundary.BorderRegions;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.movement.boundary.BoundaryRegions;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Creates grid with boundaries to neighboring nodes.
 */
public class DistributedGridFactory {

	private Settings settings;
	private Cell[][] cellsFromMaster;
	/** Partition of this worker in global coordinates. */
	private IntBox myPartGlobal;
	private SharedDataManager sharedDataMan;


	public DistributedGridFactory(
			Settings settings,
			IntBox myPartGlobal,
			Cell[][] cellsFromMaster,
			SharedDataManager sharedDataMan) {
		this.settings = settings;
		this.myPartGlobal = myPartGlobal;
		this.cellsFromMaster = cellsFromMaster;
		this.sharedDataMan = sharedDataMan;
	}


	public Grid create() {

		Cell[][] myCells = setUpCellValues();
		setUpCellBoundaries(myCells);

		return new Grid(
				settings.getSimulationWidth(),
				settings.getSimulationHeight(),
				myCells,
				settings.getGridSolver());
	}


	/**
	 * We first determine the region of the cell
	 * and then register the cell as either a boundary or border cell.
	 *
	 * Note that the order in which the boundary and border cells are added matters!
	 * Each boundary cell has a corresponding border cell on the other node.
	 * The boundary cells and their corresponding border cells on other node should
	 * have the same order!
	 */
	private void setUpCellBoundaries(Cell[][] myCells) {
		DoubleBox simAreaDouble = new DoubleBox(
				0,
				settings.getSimulationWidth(),
				0,
				settings.getSimulationHeight());
		DoubleBox innerSimAreaDouble = new DoubleBox(
				settings.getCellWidth(),
				settings.getSimulationWidth() - settings.getCellWidth(),
				settings.getCellHeight(),
				settings.getSimulationHeight() - settings.getCellHeight());

		BoundaryRegions boundaries = new BoundaryRegions(simAreaDouble);
		BorderRegions borders = new BorderRegions(simAreaDouble, innerSimAreaDouble);

		int xmin = -Grid.INTERPOLATION_RADIUS;
		int xmax = myPartGlobal.xsize() + Grid.INTERPOLATION_RADIUS - 1;
		int ymin = -Grid.INTERPOLATION_RADIUS;
		int ymax = myPartGlobal.ysize() + Grid.INTERPOLATION_RADIUS - 1;

		for (int x = xmin; x <= xmax; x++) {
			for (int y = ymin; y <= ymax; y++) {
				double simX = x * settings.getCellWidth() + settings.getCellWidth() / 2;
				double simY = y * settings.getCellHeight() + settings.getCellHeight() / 2;

				int boundaryRegion = boundaries.getRegion(simX, simY);
				int borderRegion = borders.getRegion(simX, simY);

				sharedDataMan.registerBoundaryCell(boundaryRegion, myCells[realIndex(x)][realIndex(y)]);
				sharedDataMan.registerBorderCell(borderRegion, myCells[realIndex(x)][realIndex(y)]);
			}
		}
	}


	private Cell[][] setUpCellValues() {
		int xcells = myPartGlobal.xsize() + Grid.EXTRA_CELLS_BEFORE_GRID + Grid.EXTRA_CELLS_AFTER_GRID;
		int ycells = myPartGlobal.ysize() + Grid.EXTRA_CELLS_BEFORE_GRID + Grid.EXTRA_CELLS_AFTER_GRID;
		Cell[][] myCells = new Cell[xcells][ycells];

		int xmin = -Grid.EXTRA_CELLS_BEFORE_GRID;
		int xmax = myPartGlobal.xsize() + Grid.EXTRA_CELLS_AFTER_GRID - 1;
		int ymin = -Grid.EXTRA_CELLS_BEFORE_GRID;
		int ymax = myPartGlobal.ysize() + Grid.EXTRA_CELLS_AFTER_GRID - 1;

		for (int x = xmin; x <= xmax; x++) {
			for (int y = ymin; y <= ymax; y++) {
				myCells[realIndex(x)][realIndex(y)] = cellsFromMaster[realIndex(x)][realIndex(y)];
			}
		}
		return myCells;
	}


	/**
	 * Converts grid indexing
	 * from (-1,0,..) -> user index
	 * to (0,1,..) -> real index
	 */
	private int realIndex(int userIndex) {
		return userIndex + Grid.EXTRA_CELLS_BEFORE_GRID;
	}
}
