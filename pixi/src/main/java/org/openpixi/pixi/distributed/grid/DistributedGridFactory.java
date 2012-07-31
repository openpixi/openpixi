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
	/** Partition of this worker in local coordinates. */
	private IntBox myPartLocal;
	/** Box specifying the indices of the cells coming from master. */
	private IntBox boxFromMaster;

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

		this.boxFromMaster = new IntBox(0, cellsFromMaster.length - 1, 0, cellsFromMaster[0].length - 1);
		this.myPartLocal = new IntBox(0, myPartGlobal.xsize() - 1, 0, myPartGlobal.ysize() - 1);
	}


	public Grid create() {

		Cell[][] myCells = setUpCellValues();
		setUpCellBoundaries(myCells);

		double localSimAreaWidth = myPartGlobal.xsize() * settings.getCellWidth();
		double localSimAreaHeight = myPartGlobal.ysize() * settings.getCellHeight();
		return new Grid(localSimAreaWidth, localSimAreaHeight, myCells, settings.getGridSolver());
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


	/**
	 * Most of the cells are coming from master.
	 * However, the shared cells have to be created.
	 */
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
				if (myPartLocal.contains(x, y)) {
					myCells[realIndex(x)][realIndex(y)] = cellsFromMaster[masterIndexX(x)][masterIndexY(y)];
				} else {
					myCells[realIndex(x)][realIndex(y)] = setOutsideCell(x, y);
				}
			}
		}
		return myCells;
	}


	/**
	 * If the master sent the outside cell, use the one from master.
	 * If it did not sent the cell, create a new one.
	 */
	private Cell setOutsideCell(int x, int y) {
		if (boxFromMaster.contains(masterIndexX(x),masterIndexY(y))) {
			return cellsFromMaster[masterIndexX(x)][masterIndexY(y)];
		} else {
			return new Cell();
		}
	}


	/**
	 * Converts grid indexing
	 * from (-1,0,..) -> user index
	 * to (0,1,..) -> real index
	 */
	private int realIndex(int userIndex) {
		return userIndex + Grid.EXTRA_CELLS_BEFORE_GRID;
	}


	/**
	 * Converts grid indexing.
	 * From user index (-1,0,..) to the index for cells coming from master.
	 * Only left partitions are distributed with the extra cells before grid.
	 */
	private int masterIndexX(int x) {
		if (myPartGlobal.xmin() == 0) {
			return x + Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		else {
			return x;
		}
	}


	/**
	 * Only top partitions are distributed with the extra cells before grid.
	 */
	private int masterIndexY(int y) {
		if (myPartGlobal.ymin() == 0) {
			return y + Grid.EXTRA_CELLS_BEFORE_GRID;
		}
		else {
			return y;
		}
	}


}
