package org.openpixi.pixi.distributed.grid;

import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.distributed.movement.boundary.BorderRegions;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.IntBox;
import org.openpixi.pixi.physics.util.Point;

import java.util.List;

/**
 * Creates grid with boundaries to neighboring nodes.
 */
public class DistributedGridFactory {

	private Settings settings;
	private Cell[][] cellsFromMaster;
	/** Partition of this worker in global coordinates. */
	private IntBox myPartGlobal;
	private SharedDataManager sharedDataManager;


	public DistributedGridFactory(
			Settings settings,
			IntBox myPartGlobal,
			Cell[][] cellsFromMaster,
			SharedDataManager sharedDataManager) {
		this.settings = settings;
		this.myPartGlobal = myPartGlobal;
		this.cellsFromMaster = cellsFromMaster;
		this.sharedDataManager = sharedDataManager;
	}


	public Grid create() {

		Cell[][] myCells = setUpCellValues();
		setUpBorderCells(myCells);

		return new Grid(settings, myCells);
	}


	/**
	 * Determines the border cells and the indices of the corresponding ghost cells at remote nodes.
	 * Since the corresponding border and ghost cells must have the same order,
	 * the ghost cells are registered once the ghost cell indices (border cells map) is exchanged.
	 */
	private void setUpBorderCells(Cell[][] myCells) {
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

		BorderRegions borders = new BorderRegions(simAreaDouble, innerSimAreaDouble);

		int xmin = -Grid.INTERPOLATION_RADIUS;
		int xmax = myPartGlobal.xsize() + Grid.INTERPOLATION_RADIUS - 1;
		int ymin = -Grid.INTERPOLATION_RADIUS;
		int ymax = myPartGlobal.ysize() + Grid.INTERPOLATION_RADIUS - 1;

		for (int x = xmin; x <= xmax; x++) {
			for (int y = ymin; y <= ymax; y++) {
				double simX = x * settings.getCellWidth() + settings.getCellWidth() / 2;
				double simY = y * settings.getCellHeight() + settings.getCellHeight() / 2;

				int region = borders.getRegion(simX, simY);

				List<SharedData> sharedDatas = sharedDataManager.getBorderSharedData(region);
				List<Point> directions = sharedDataManager.getBorderDirections(region);
				assert sharedDatas.size() == directions.size();

				for (int i = 0; i < sharedDatas.size(); ++i) {
					Point remoteGhostCellIndex = getRemoteGhostCellIndex(x, y, directions.get(i));

					sharedDatas.get(i).registerBorderCell(
							myCells[realIndex(x)][realIndex(y)],
							remoteGhostCellIndex);
				}
			}
		}
	}


	/**
	 * Translates the local border cell index to remote ghost cell index.
	 */
	private Point getRemoteGhostCellIndex(int x, int y, Point direction) {
		int xoffset = direction.x * settings.getGridCellsX();
		int yoffset = direction.y * settings.getGridCellsY();
		return new Point(x - xoffset, y - yoffset);
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
