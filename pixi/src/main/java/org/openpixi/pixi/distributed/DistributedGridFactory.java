package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

/**
 * Creates grid with boundaries to neighboring nodes.
 */
public class DistributedGridFactory {

	private Settings settings;
	private Cell[][] cellsFromMaster;
	private NeighborMap neighbors;

	/** Partition of this worker in global coordinates. */
	private IntBox myPartGlobal;
	/** Partition of this worker in local coordinates. */
	private IntBox myPartLocal;

	private IntBox simArea;

	public DistributedGridFactory(
			Settings settings,
			IntBox myPartGlobal,
			Cell[][] cellsFromMaster,
			NeighborMap neighbors) {
		this.settings = settings;
		this.myPartGlobal = myPartGlobal;
		this.cellsFromMaster = cellsFromMaster;
		this.neighbors = neighbors;

		this.myPartLocal = new IntBox(0, myPartGlobal.xsize() - 1, 0, myPartGlobal.ysize() - 1);
		this.simArea = new IntBox(0, settings.getGridCellsX() - 1, 0, settings.getGridCellsY() - 1);
	}


	public Grid create() {

		Cell[][] myCells = setUpCellValues();

		// TODO set border and ghost cells

		double localSimAreaWidth = myPartGlobal.xsize() * settings.getCellWidth();
		double localSimAreaHeight = myPartGlobal.ysize() * settings.getCellHeight();
		return new Grid(localSimAreaWidth, localSimAreaHeight, myCells, settings.getGridSolver());
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
				if (myPartLocal.contains(x, y)) {
					myCells[realIndex(x)][realIndex(y)] = cellsFromMaster[masterIndexX(x)][masterIndexY(y)];
				} else {
					myCells[realIndex(x)][realIndex(y)] = setOutsideCell(x, y);
				}
			}
		}
		return myCells;
	}


	private Cell setOutsideCell(int x, int y) {
		if (simArea.contains(x,y)) {
			return new Cell();
		} else {
			return cellsFromMaster[masterIndexX(x)][masterIndexY(y)];
		}
	}


	/**
	 * Converts grid indexing
	 * from local simulation area index
	 * to global simulation area index.
	 */
	private int worldIndexX(int localIndex) {
		return localIndex + myPartGlobal.xmin();
	}


	/**
	 * Converts grid indexing
	 * from local simulation area index
	 * to global simulation area index.
	 */
	private int worldIndexY(int localIndex) {
		return localIndex + myPartGlobal.ymin();
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
	 * Only left partitions are distributed with the extra cells.
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
	 * Only top partitions are distributed with the extra cells.
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
