package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.movement.BoundingBox;

/**
 * TODO add possibility to instantiate any grid boundary +
 * TODO   for the time being unify with particle boundary
 * TODO create simple tests for grid boundaries
 */
public class Grid {

	/**
	 * The purpose of the extra cells is twofold.
	 * 1) They assure that we always have a cell to interpolate to.
	 *    For example, the hardwall particle boundaries allow the particle to be outside
	 *    of the simulation area.
	 *    That means that the particle can be in a cell [numCellsX, numCellsY].
	 *    If the particle is in this cell, it will be interpolated
	 *    to the four surrounding cells from [numCellsX, numCellsY] to [numCellsX+1, numCellsY+1].
	 *    Hence, the two extra cells after the grid's end.
	 *    However, at the beginning we only need one extra cell as the particle in cell
	 *    [-1,-1] interpolates to cells from [-1,-1] to [0,0].
	 *
	 * 2) In the field solver we usually have to use left, right, top and bottom neighboring cell
	 *    to update the value of the current cell.
	 *    Without the extra cells we would have to treat each side separately in order not to get
	 *    IndexOutOfBounds exception.
	 *    With the extra cells we can comfortably iterate through the entire grid in a uniform way
	 *    using the 0 values of extra cells when calculating the fields at the sides.
	 */
	private static final int EXTRA_CELLS_BEFORE_GRID = 1;
	private static final int EXTRA_CELLS_AFTER_GRID = 2;

	/**solver algorithm for the maxwell equations*/
	private FieldSolver fsolver;
	/**solver for the electrostatic poisson equation*/
	private PoissonSolver poisolver;

	private GridBoundaryType boundaryType;

	private Cell[][] cells;

	/**number of cells in x direction*/
	private int numCellsX;
	/**number of cells in x direction*/
	private int numCellsY;
	/**width of each cell*/
	private double cellWidth;
	/**height of each cell*/
	private double cellHeight;

	public FieldSolver getFsolver() {
		return fsolver;
	}

	public void setFsolver(FieldSolver fsolver) {
		this.fsolver = fsolver;
	}

	public double getJx(int x, int y) {
		return cells[idx(x)][idx(y)].jx;
	}

	public void setJx(int x, int y, double value) {
		cells[idx(x)][idx(y)].jx = value;
	}

	public void addJx(int x, int y, double value) {
		cells[idx(x)][idx(y)].jx += value;
	}

	public double getJy(int x, int y) {
		return cells[idx(x)][idx(y)].jy;
	}

	public void setJy(int x, int y, double value) {
		cells[idx(x)][idx(y)].jy = value;
	}

	public void addJy(int x, int y, double value) {
		cells[idx(x)][idx(y)].jy += value;
	}

	public double getRho(int x, int y) {
		return cells[idx(x)][idx(y)].rho;
	}

	public void setRho(int x, int y, double value) {
		cells[idx(x)][idx(y)].rho = value;
	}
	
	public void addRho(int x, int y, double value) {
		cells[idx(x)][idx(y)].rho += value;
	}

	public double getPhi(int x, int y) {
		return cells[idx(x)][idx(y)].phi;
	}

	public void setPhi(int x, int y, double value) {
		cells[idx(x)][idx(y)].phi = value;
	}

	public double getEx(int x, int y) {
		return cells[idx(x)][idx(y)].Ex;
	}

	public void setEx(int x, int y, double value) {
		cells[idx(x)][idx(y)].Ex = value;
	}

	public void addEx(int x, int y, double value) {
		cells[idx(x)][idx(y)].Ex += value;
	}

	public double getEy(int x, int y) {
		return cells[idx(x)][idx(y)].Ey;
	}

	public void setEy(int x, int y, double value) {
		cells[idx(x)][idx(y)].Ey = value;
	}

	public void addEy(int x, int y, double value) {
		cells[idx(x)][idx(y)].Ey += value;
	}

	public double getBz(int x, int y) {
		return cells[idx(x)][idx(y)].Bz;
	}

	public void setBz(int x, int y, double value) {
		cells[idx(x)][idx(y)].Bz = value;
	}

	public void addBz(int x, int y, double value) {
		cells[idx(x)][idx(y)].Bz += value;
	}

	public double getExo(int x, int y) {
		return cells[idx(x)][idx(y)].Exo;
	}

	public void setExo(int x, int y, double value) {
		cells[idx(x)][idx(y)].Exo = value;
	}

	public double getEyo(int x, int y) {
		return cells[idx(x)][idx(y)].Eyo;
	}

	public void setEyo(int x, int y, double value) {
		cells[idx(x)][idx(y)].Eyo = value;
	}

	public double getBzo(int x, int y) {
		return cells[idx(x)][idx(y)].Bzo;
	}

	public void setBzo(int x, int y, double value) {
		cells[idx(x)][idx(y)].Bzo = value;
	}

	public int getNumCellsX() {
		return numCellsX;
	}

	public int getNumCellsY() {
		return numCellsY;
	}

	public double getCellWidth() {
		return cellWidth;
	}

	public double getCellHeight() {
		return cellHeight;
	}	

	Grid(
			int numCellsX, int numCellsY,
			double simWidth, double simHeight,
			GridBoundaryType boundaryType,
			FieldSolver fsolver,
			PoissonSolver poisolver) {

		this.boundaryType = boundaryType;
		this.fsolver = fsolver;
		this.poisolver = poisolver;

		set(numCellsX, numCellsY, simWidth, simHeight);
	}

	public void set(int numCellsX, int numCellsY,
			double simWidth, double simHeight) {

		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;
		this.cellWidth = simWidth/numCellsX;
		this.cellHeight = simHeight/numCellsY;

		createGridWithBoundaries();
		poisolver.solve(this);
	}

	private void createGridWithBoundaries() {
		cells = new Cell[getNumCellsXTotal()][getNumCellsYTotal()];

		// Create inner cells
		for (int x = 0; x < getNumCellsX(); x++) {
			for (int y = 0; y < getNumCellsY(); y++) {
				cells[idx(x)][idx(y)] = new Cell();
			}
		}

		createBoundaryCells();
	}

	private void createBoundaryCells() {
		// left boundary (with corner cells)
		for (int x = 0; x < EXTRA_CELLS_BEFORE_GRID; x++) {
			for (int y = 0; y < getNumCellsYTotal(); y++) {
				createBoundaryCell(x,y);
			}
		}
		// right boundary (with corner cells)
		for (int x = EXTRA_CELLS_BEFORE_GRID + numCellsX; x < getNumCellsXTotal(); x++) {
			for (int y = 0; y < getNumCellsYTotal(); y++) {
				createBoundaryCell(x,y);
			}
		}
		// top boundary (without corner cells)
		for (int x = EXTRA_CELLS_BEFORE_GRID; x < EXTRA_CELLS_BEFORE_GRID + numCellsX; x++) {
			for (int y = 0; y < EXTRA_CELLS_BEFORE_GRID; y++) {
				createBoundaryCell(x,y);
			}
		}
		// bottom boundary (without corner cells)
		for (int x = EXTRA_CELLS_BEFORE_GRID; x < EXTRA_CELLS_BEFORE_GRID + numCellsX; x++) {
			for (int y = EXTRA_CELLS_BEFORE_GRID + numCellsY; y < getNumCellsYTotal(); y++) {
				createBoundaryCell(x,y);
			}
		}
	}

	/**
	 * Based on the boundary type creates the boundary cell.
	 */
	private void createBoundaryCell(int x, int y) {
		if (boundaryType == GridBoundaryType.Hardwall) {
			cells[x][y] = new Cell();
		}
		else if (boundaryType == GridBoundaryType.Periodic) {
			BoundingBox innerGrid = new BoundingBox(
					EXTRA_CELLS_BEFORE_GRID, numCellsX + EXTRA_CELLS_BEFORE_GRID - 1,
					EXTRA_CELLS_BEFORE_GRID, numCellsY + EXTRA_CELLS_BEFORE_GRID - 1);
			int refX = x;
			int refY = y;

			if (x < innerGrid.xmin()) {
				refX += numCellsX;
			} else if (x > innerGrid.xmax()) {
				refX -= numCellsX;
			}
			if (y < innerGrid.ymin()) {
				refY += numCellsY;
			} else if (y > innerGrid.ymax()) {
				refY -= numCellsY;
			}

			cells[x][y] = cells[refX][refY];
		}
	}

	public void updateGrid(double tstep) {
		storeFields();
		getFsolver().step(this, tstep);
	}

	public void resetCurrentAndCharge() {
		for(int x = 0; x < getNumCellsXTotal(); x++) {
			for(int y = 0; y < getNumCellsYTotal(); y++) {
				cells[x][y].resetCharge();
				cells[x][y].resetCurrent();
			}
		}
	}

	public void storeFields() {
		for (int x = 0; x < getNumCellsXTotal(); x++) {
			for (int y = 0; y < getNumCellsYTotal(); y++) {
				cells[x][y].storeFields();
			}
		}
	}

	private int idx(int userIdx) {
		return EXTRA_CELLS_BEFORE_GRID + userIdx;
	}

	/** Includes the extra cells. */
	private int getNumCellsXTotal() {
		return numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
	}

	/** Includes the extra cells. */
	private int getNumCellsYTotal() {
		return numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
	}
}
