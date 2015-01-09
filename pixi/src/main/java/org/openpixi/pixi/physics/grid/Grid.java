package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.parallel.cellaccess.CellIterator;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.FieldSolver;

public class Grid {

	/*
	 * TODO remove the accessors for individual cell fields and call directly the accessors on the cell
	 * TODO extract field solver to simulation
	 *      - makes grid simpler
	 *      - makes all the important initialization to happen at one place (in simulation)
	 */
	/**
	 * The purpose of the extra cells is twofold. 1) They represent the
	 * boundaries around the simulation area cells. The boundaries assure that
	 * we always have a cell to interpolate to. For example, the hardwall
	 * particle boundaries allow the particle to be outside of the simulation
	 * area. That means that the particle can be in a cell [numCellsX,
	 * numCellsY]. If the particle is in this cell, it will be interpolated to
	 * the four surrounding cells from [numCellsX, numCellsY] to [numCellsX+1,
	 * numCellsY+1]. Hence, the two extra cells after the grid's end. However,
	 * at the beginning we only need one extra cell as the particle in cell
	 * [-1,-1] interpolates to cells from [-1,-1] to [0,0].
	 *
	 * 2) In the field solver we usually have to use left, right, top and bottom
	 * neighboring cell to update the value of the current cell. Without the
	 * extra cells we would have to treat each side separately in order not to
	 * get IndexOutOfBounds exception. With the extra cells we can comfortably
	 * iterate through the entire grid in a uniform way using the 0 values of
	 * extra cells when calculating the fields at the sides.
	 */
	public static final int INTERPOLATION_RADIUS = 1;
	public static final int HARDWALL_SAFETY_CELLS = 1;
	//Check comment above, but due to the CIC to particle interpolator
	//we actually need two cells at the beginning. But for an other reason than at the end.
	public static final int EXTRA_CELLS_BEFORE_GRID =
			INTERPOLATION_RADIUS + HARDWALL_SAFETY_CELLS - 1 + 1;
	public static final int EXTRA_CELLS_AFTER_GRID =
			INTERPOLATION_RADIUS + HARDWALL_SAFETY_CELLS;
	/**
	 * solver algorithm for the maxwell equations
	 */
	private FieldSolver fsolver;
	private GridBoundaryType boundaryType;
	private CellIterator cellIterator;
	private ResetChargeAction resetCharge = new ResetChargeAction();
	private ResetCurrentAction resetCurrent = new ResetCurrentAction();
	private StoreFieldsAction storeFields = new StoreFieldsAction();
	private Cell[][][] cells;
	/**
	 * number of cells in x direction
	 */
	private int numCellsX;
	/**
	 * number of cells in y direction
	 */
	private int numCellsY;
	/**
	 * number of cells in z direction
	 */
	private int numCellsZ;
	/**
	 * width of each cell
	 */
	private double cellWidth;
	/**
	 * height of each cell
	 */
	private double cellHeight;
	/**
	 * depth of each cell
	 */
	private double cellDepth;

	public FieldSolver getFsolver() {
		return fsolver;
	}

	public void setFsolver(FieldSolver fsolver) {
		this.fsolver = fsolver;
	}

	public double getJx(int x, int y) {
		return cells[index(x)][index(y)][0].getJx();
	}

	public void addJx(int x, int y, double value) {
		cells[index(x)][index(y)][0].addJx(value);
	}
	
	public double getJx(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getJx();
	}

	public void addJx(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].addJx(value);
	}

	public double getJy(int x, int y) {
		return cells[index(x)][index(y)][0].getJy();
	}

	public void addJy(int x, int y, double value) {
		cells[index(x)][index(y)][0].addJy(value);
	}
	
	public double getJy(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getJy();
	}

	public void addJy(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].addJy(value);
	}
	
	public double getJz(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getJz();
	}

	public void addJz(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].addJz(value);
	}

	public double getRho(int x, int y) {
		return cells[index(x)][index(y)][0].getRho();
	}
	
	public double getRho(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getRho();
	}

	public void setRho(int x, int y, double value) {
		cells[index(x)][index(y)][0].setRho(value);
	}
	
	public void setRho(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setRho(value);
	}

	public void addRho(int x, int y, double value) {
		cells[index(x)][index(y)][0].addRho(value);
	}
	
	public void addRho(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].addRho(value);
	}

	public double getPhi(int x, int y) {
		return cells[index(x)][index(y)][0].getPhi();
	}

	public void setPhi(int x, int y, double value) {
		cells[index(x)][index(y)][0].setPhi(value);
	}
	
	public double getPhi(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getPhi();
	}

	public void setPhi(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setPhi(value);
	}

	public double getEx(int x, int y) {
		return cells[index(x)][index(y)][0].getEx();
	}

	public void setEx(int x, int y, double value) {
		cells[index(x)][index(y)][0].setEx(value);
	}

	public double getExo(int x, int y) {
		return cells[index(x)][index(y)][0].getExo();
	}

	public void setExo(int x, int y, double value) {
		cells[index(x)][index(y)][0].setExo(value);
	}
	
	public void addEx(int x, int y, double value) {
		cells[index(x)][index(y)][0].setEx(cells[index(x)][index(y)][0].getEx() + value);
	}
	
	public double getEx(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getEx();
	}

	public void setEx(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEx(value);
	}

	public double getExo(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getExo();
	}

	public void setExo(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setExo(value);
	}
	
	public void addEx(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEx(cells[index(x)][index(y)][index(z)].getEx() + value);
	}

	public double getEy(int x, int y) {
		return cells[index(x)][index(y)][0].getEy();
	}

	public void setEy(int x, int y, double value) {
		cells[index(x)][index(y)][0].setEy(value);
	}

	public double getEyo(int x, int y) {
		return cells[index(x)][index(y)][0].getEyo();
	}

	public void setEyo(int x, int y, double value) {
		cells[index(x)][index(y)][0].setEyo(value);
	}
	
	public void addEy(int x, int y, double value) {
		cells[index(x)][index(y)][0].setEy(cells[index(x)][index(y)][0].getEy() + value);
	}
	
	public double getEy(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getEy();
	}

	public void setEy(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEy(value);
	}

	public double getEyo(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getEyo();
	}

	public void setEyo(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEyo(value);
	}
	
	public void addEy(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEy(cells[index(x)][index(y)][index(z)].getEy() + value);
	}
	
	public double getEz(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getEz();
	}

	public void setEz(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEz(value);
	}

	public double getEzo(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getEzo();
	}

	public void setEzo(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEzo(value);
	}
	
	public void addEz(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setEz(cells[index(x)][index(y)][index(z)].getEz() + value);
	}

	public double getBz(int x, int y) {
		return cells[index(x)][index(y)][0].getBz();
	}

	public void setBz(int x, int y, double value) {
		cells[index(x)][index(y)][0].setBz(value);
	}

	public void addBz(int x, int y, double value) {
		cells[index(x)][index(y)][0].setBz(cells[index(x)][index(y)][0].getBz() + value);
	}

	public double getBzo(int x, int y) {
		return cells[index(x)][index(y)][0].getBzo();
	}

	public void setBzo(int x, int y, double value) {
		cells[index(x)][index(y)][0].setBzo(value);
	}
	
	public double getBz(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getBz();
	}

	public void setBz(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBz(value);
	}

	public void addBz(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBz(cells[index(x)][index(y)][index(z)].getBz() + value);
	}

	public double getBzo(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getBzo();
	}

	public void setBzo(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBzo(value);
	}
	
	public double getBx(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getBx();
	}

	public void setBx(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBx(value);
	}

	public void addBx(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBx(cells[index(x)][index(y)][index(z)].getBx() + value);
	}

	public double getBxo(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getBxo();
	}

	public void setBxo(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBxo(value);
	}
	
	public double getBy(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getBy();
	}

	public void setBy(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBy(value);
	}

	public void addBy(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setBy(cells[index(x)][index(y)][index(z)].getBy() + value);
	}

	public double getByo(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)].getByo();
	}

	public void setByo(int x, int y, int z, double value) {
		cells[index(x)][index(y)][index(z)].setByo(value);
	}
	
	public void resetCharge(int x, int y) {
		cells[index(x)][index(y)][0].resetCharge();
	}
	
	public void resetCharge(int x, int y, int z) {
		cells[index(x)][index(y)][index(z)].resetCharge();
	}

	public int getNumCellsX() {
		return numCellsX;
	}

	public int getNumCellsY() {
		return numCellsY;
	}
	
	public int getNumCellsZ() {
		return numCellsZ;
	}

	public double getCellWidth() {
		return cellWidth;
	}

	public double getCellHeight() {
		return cellHeight;
	}
	
	public double getCellDepth() {
		return cellDepth;
	}

	public Cell getCell(int x, int y) {
		return cells[index(x)][index(y)][0];
	}
	
	public Cell getCell(int x, int y, int z) {
		return cells[index(x)][index(y)][index(z)];
	}

	public Cell[][][] getCells() {
		return cells;
	}

	public Grid(Settings settings) {
		this.boundaryType = settings.getGridBoundary();

		set(settings.getGridCellsX(), settings.getGridCellsY(), settings.getGridCellsZ(),
				settings.getSimulationWidth(), settings.getSimulationHeight(), settings.getSimulationDepth());
		
		this.fsolver = settings.getGridSolver();
		this.fsolver.initializeIterator(settings.getCellIterator(), numCellsX, numCellsY, numCellsZ);

		this.cellIterator = settings.getCellIterator();
		this.cellIterator.setNormalMode(numCellsX, numCellsY, numCellsZ);//this.cellIterator.setExtraCellsMode(numCellsX, numCellsY);
	}

	/**
	 * In the distributed version we want to create the grid from cells which
	 * come from master; hence, this constructor. Creates grid from the given
	 * cells. The input cells have to contain also the boundary cells.
	 */
	public Grid(Settings settings, Cell[][][] cells) {
		this.numCellsX = cells.length;//this.numCellsX = cells.length - EXTRA_CELLS_BEFORE_GRID - EXTRA_CELLS_AFTER_GRID;
		this.numCellsY = cells[0].length;//this.numCellsY = cells[0].length - EXTRA_CELLS_BEFORE_GRID - EXTRA_CELLS_AFTER_GRID;
		this.numCellsZ = cells[0][0].length;
		this.cellWidth = settings.getGridStep();//this.cellWidth = settings.getSimulationWidth() / numCellsX;
		this.cellHeight = this.cellWidth;//this.cellHeight = settings.getSimulationHeight() / numCellsY;
		this.cellDepth = this.cellWidth;

		this.cells = cells;

		/*
		 * Grid and FieldSolver must have each its own cell iterator!
		 * They use different modes of iteration.
		 * While the grid iterates also over the extra cells the field solver does not.
		 */

		this.fsolver = settings.getGridSolver();
		fsolver.initializeIterator(settings.getCellIterator(), numCellsX, numCellsY, numCellsZ);

		this.cellIterator = settings.getCellIterator();
		this.cellIterator.setNormalMode(this.numCellsX, this.numCellsY, this.numCellsZ);//this.cellIterator.setExtraCellsMode(this.numCellsX, this.numCellsY);
	}

	/**
	 * Change the size of the field. TODO make sure the method can not be called
	 * in distributed version E.g. throw an exception if this is distributed
	 * version
	 */
	public void changeSize(int numCellsX, int numCellsY, int numCellsZ,
			double simWidth, double simHeight, double simDepth) {
		set(numCellsX, numCellsY, numCellsZ, simWidth, simHeight, simDepth);
		fsolver.changeSize(numCellsX, numCellsY, numCellsZ);
		cellIterator.setNormalMode(this.numCellsX, this.numCellsY, this.numCellsZ);//cellIterator.setExtraCellsMode(this.numCellsX, this.numCellsY);
	}

	/**
	 * This method is dangerous as it would not work in distributed version.
	 * TODO make sure the method can not be called in distributed version E.g.
	 * throw an exception if this is distributed version
	 */
	private void set(int numCellsX, int numCellsY, int numCellsZ,
			double simWidth, double simHeight, double simDepth) {

		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;
		this.numCellsZ = numCellsZ;
		this.cellWidth = simWidth / numCellsX;
		this.cellHeight = simHeight / numCellsY;
		this.cellDepth = simDepth / numCellsZ;

		createGridWithBoundaries();
	}

	private void createGridWithBoundaries() {
		cells = new Cell[getNumCellsX()][getNumCellsY()][getNumCellsZ()];
		// Create inner cells
		for (int x = 0; x < getNumCellsX(); x++) {
			for (int y = 0; y < getNumCellsY(); y++) {
				for (int z = 0; z < getNumCellsZ(); z++) {
					cells[index(x)][index(y)][index(z)] = new Cell();
				}
			}
		}
		//createBoundaryCells();
	}
/*
	private void createBoundaryCells() {
		// left boundary (with corner cells)
		for (int x = 0; x < EXTRA_CELLS_BEFORE_GRID; x++) {
			for (int y = 0; y < getNumCellsYTotal(); y++) {
				createBoundaryCell(x, y);
			}
		}
		// right boundary (with corner cells)
		for (int x = EXTRA_CELLS_BEFORE_GRID + numCellsX; x < getNumCellsXTotal(); x++) {
			for (int y = 0; y < getNumCellsYTotal(); y++) {
				createBoundaryCell(x, y);
			}
		}
		// top boundary (without corner cells)
		for (int x = EXTRA_CELLS_BEFORE_GRID; x < EXTRA_CELLS_BEFORE_GRID + numCellsX; x++) {
			for (int y = 0; y < EXTRA_CELLS_BEFORE_GRID; y++) {
				createBoundaryCell(x, y);
			}
		}
		// bottom boundary (without corner cells)
		for (int x = EXTRA_CELLS_BEFORE_GRID; x < EXTRA_CELLS_BEFORE_GRID + numCellsX; x++) {
			for (int y = EXTRA_CELLS_BEFORE_GRID + numCellsY; y < getNumCellsYTotal(); y++) {
				createBoundaryCell(x, y);
			}
		}
	}
*/
	/**
	 * Based on the boundary type creates the boundary cell.
	 */
/*	
	private void createBoundaryCell(int x, int y) {
		if (boundaryType == GridBoundaryType.Hardwall) {
			cells[x][y] = new Cell();
		} else if (boundaryType == GridBoundaryType.Periodic) {
			int xmin = EXTRA_CELLS_BEFORE_GRID;
			int xmax = numCellsX + EXTRA_CELLS_BEFORE_GRID - 1;
			int ymin = EXTRA_CELLS_BEFORE_GRID;
			int ymax = numCellsY + EXTRA_CELLS_BEFORE_GRID - 1;

			int refX = x;
			int refY = y;
			if (x < xmin) {
				refX += numCellsX;
			} else if (x > xmax) {
				refX -= numCellsX;
			}
			if (y < ymin) {
				refY += numCellsY;
			} else if (y > ymax) {
				refY -= numCellsY;
			}

			cells[x][y] = cells[refX][refY];
		}
	}
*/
	public void updateGrid(double tstep) {
		storeFields();
		getFsolver().step(this, tstep);
	}

	public void resetCurrent() {
		cellIterator.execute(this, resetCurrent);
	}

	public void resetCharge() {
		cellIterator.execute(this, resetCharge);
	}

	public void storeFields() {
		cellIterator.execute(this, storeFields);
	}

	/**
	 * Maps the client index which can be negative to the real array index which
	 * has to be non-negative. The client index can be negative if the client is
	 * asking for a cell which is within the top or left boundary. (By client we
	 * mean any code which is using this class)
	 *
	 */
	private int index(int clientIdx) {
		//return EXTRA_CELLS_BEFORE_GRID + clientIdx;
		return clientIdx;
	}

	/**
	 * Includes the extra cells.
	 */
	public int getNumCellsXTotal() {
		return numCellsX;//return numCellsX + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
	}

	/**
	 * Includes the extra cells.
	 */
	public int getNumCellsYTotal() {
		return numCellsY;//return numCellsY + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID;
	}
	
	public int getNumCellsZTotal() {
		return numCellsZ;
	}

	private class ResetCurrentAction implements CellAction {

		public void execute(Grid grid, int x, int y, int z) {
			grid.getCell(x, y, z).resetCurrent();
		}
	}

	private class ResetChargeAction implements CellAction {

		public void execute(Grid grid, int x, int y, int z) {
			grid.resetCharge(x, y, z);
		}
	}

	private class StoreFieldsAction implements CellAction {

		public void execute(Grid grid, int x, int y, int z) {
			grid.getCell(x, y, z).storeFields();
		}
	}
}
