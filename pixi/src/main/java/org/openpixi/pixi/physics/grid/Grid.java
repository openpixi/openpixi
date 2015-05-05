package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.parallel.cellaccess.CellIterator;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.FieldSolver;

public class Grid {

	/**
	 * solver algorithm for the field equations
	 */
	private FieldSolver fsolver;
	private CellIterator cellIterator;
	private ResetChargeAction resetCharge = new ResetChargeAction();
	private ResetCurrentAction resetCurrent = new ResetCurrentAction();
	private StoreFieldsAction storeFields = new StoreFieldsAction();
	private Cell[] cells;
	/**
	 * Number of dimensions
	 */
	private int numDim;
	/**
	 * Number of colors
	 */
	private int numCol;
	/**
	 * Number of cells in d dimension
	 */
	private int numCells[];
	/**
	 * Spatial lattice spacing
	 */
	private double as;
	/**
	 * Gauge coupling strength
	 */
	private double gaugeCoupling;

	/**
	 * Unit vectors to be used for the shift method.
	 */
	private int[][] unitVectors;

	public FieldSolver getFsolver() {
		return fsolver;
	}

	public void setFsolver(FieldSolver fsolver) {
		this.fsolver = fsolver;
	}

	public YMField getJ(int[] coor, int dir) {
		return cells[index(coor)].getJ(dir);
	}

	public void addJ(int[] coor, int dir, YMField field) {
		cells[index(coor)].addJ(dir, field);
	}
	
	public YMField getRho(int[] coor) {
		return cells[index(coor)].getRho();
	}
	
	public void setRho(int[] coor, YMField field) {
		cells[index(coor)].setRho(field);
	}
	
	public void addRho(int[] coor, YMField field) {
		cells[index(coor)].addRho(field);
	}

	public YMField getE(int[] coor, int dir) {
		return cells[index(coor)].getE(dir);
	}

	public void setE(int[] coor, int dir, YMField field) {
		cells[index(coor)].setE(dir, field);
	}
	
	public void addE(int[] coor, int dir, YMField field) {
		cells[index(coor)].addE(dir, field);
	}

	public YMField getFTensor(int[] coor, int dir1, int dir2) {
		return cells[index(coor)].getFieldStrength(dir1, dir2);
	}

	/**
	 * Sets value the field strength component (dir1, dir2) tensor at a certain lattice coordinate.
	 * @param coor      Lattice coordinate of the field strength tensor
	 * @param dir1      First space component (0 - (numberOfDimensions-1))
	 * @param dir2      Second space component (0 - (numberOfDimensions-1))
	 * @param field     YMField instance which the field strength tensor should be set to.
	 */
	public void setFTensor(int[] coor, int dir1, int dir2, YMField field) {
		cells[index(coor)].setFieldStrength(dir1, dir2, field);
	}

	/**
	 * Returns the gauge link at (t) at a given lattice coordinate in a given direction/
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @return      Instance of the gauge link
	 */
	public LinkMatrix getU(int[] coor, int dir) {
		return cells[index(coor)].getU(dir);
	}

	/**
	 * Sets the gauge link at (t) at given lattice coordinate in given direction to a new value.
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @param mat   LinkMatrix instance
	 */
	public void setU(int[] coor, int dir, LinkMatrix mat) {
		cells[index(coor)].setU(dir, mat);
	}

	/**
	 * Returns the gauge link at (t+dt) at a given lattice coordinate in a given direction/
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @return      Instance of the gauge link
	 */
	public LinkMatrix getUnext(int[] coor, int dir) {
		return cells[index(coor)].getUnext(dir);
	}

	/**
	 * Sets the gauge link at (t+dt) at given lattice coordinate in given direction to a new value.
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @param mat   LinkMatrix instance
	 */
	public void setUnext(int[] coor, int dir, LinkMatrix mat) {
		cells[index(coor)].setUnext(dir, mat);
	}

	/**
	 * Resets charge in a cell at a given lattice coordinate.
	 * @param coor  Lattice coordinate of the cell
	 */
	public void resetCharge(int[] coor) {
		cells[index(coor)].resetCharge();
	}

	/**
	 * Returns the number of cells in the grid in a given direction.
	 * @param dir   Index of the direction
	 * @return      Number of cells in given direction.
	 */
	public int getNumCells(int dir) {
		return numCells[dir];
	}

	/**
	 * Returns the lattice spacing of the grid.
	 * @return  Lattice spacing of the grid.
	 */
	public double getLatticeSpacing() {
		return as;
	}

	/**
	 * Returns the Cell instance at given lattice coorindates with respect to periodic boundary conditions.
	 *
	 * @param coor  Lattice coordinate of the cell
	 * @return      Cell instance at lattice coordinates with respect to periodic boundary conditions
	 */
	public Cell getCell(int[] coor) {
		return cells[index(coor)];
	}

	/**
	 * Returns full Cell array of the grid. Note that the cells in this array are index by cell indices.
	 * @return  Full array with all cells
	 */
	public Cell[] getCells() {
		return cells;
	}

	/**
	 * Main constructor for the Grid class. Given a settings file it initializes the lattice and sets up the FieldSolver and the CellIterator.
	 * @param settings
	 */
	public Grid(Settings settings) {

		gaugeCoupling = settings.getCouplingConstant();
		as = settings.getGridStep();
		numCol = settings.getNumberOfColors();
		numDim = settings.getNumberOfDimensions();
		numCells = new int[numDim];
		
		for(int i = 0; i < numDim; i++) {
			numCells[i] = settings.getGridCells(i);
		}

		createGrid();
				
		this.fsolver = settings.getGridSolver();
		this.fsolver.initializeIterator(settings.getCellIterator(), numCells);

		this.cellIterator = settings.getCellIterator();
		this.cellIterator.setNormalMode(numCells);
		
	}

	/**
	 * Change the size of the grid. TODO make sure the method can not be called
	 * in distributed version E.g. throw an exception if this is distributed
	 * version
	 */
	public void changeSize(int[] NewNumberOfCells) {
		numCells = NewNumberOfCells;
		createGrid();
		fsolver.changeSize(numCells);
		cellIterator.setNormalMode(numCells);
	}

	/**
	 * This methods initializes the each cell in the grid.
	 */
	private void createGrid() {

		unitVectors = new int[numDim][numDim];

		int length = 1;
		for(int i = 0; i < numDim; i++) {
			length *= numCells[i];

			/*
				Setup unit vectors.
			 */
			unitVectors[i][i] = 1;
		}
		cells = new Cell[length];

		for(int i = 0; i < length; i++) {
			cells[i] = new Cell(numDim, numCol);
		}
	
	}

	/**
	 * This method advances the grid by one time step:
	 * It stores the fields (i.e. sets (t+dt) fields from the last grid update to the old ones)
	 * and calls the FieldSolver to solve the equations of motion for one time step.
	 *
	 * @param tstep size of the time step
	 */
	public void updateGrid(double tstep) {
		storeFields();
		getFsolver().step(this, tstep);
	}

	/**
	 * Resets all currents in every cell to zero.
	 */
	public void resetCurrent() {
		cellIterator.execute(this, resetCurrent);
	}

	/**
	 * Resets all charges in every cell to zero.
	 */
	public void resetCharge() {
		cellIterator.execute(this, resetCharge);
	}

	/**
	 * Stores "new" fields which have been calculated in the last simulation step to the variables of the "old" fields.
	 */
	public void storeFields() {
		cellIterator.execute(this, storeFields);
	}

	/**
	 * Calculates the plaquette starting at lattice coordinate coor in the plane of d1 and d2 with orientations o1, o2.
	 * This method implements the following definition of the plaquette:
	 *      U_{x, ij} = U_{x+j, -j} U_{x+i+j, -i} U_{x+i, j} U_{x, i}
	 *
	 *
	 * @param coor  Lattice coordinate from where the plaquette starts
	 * @param d1    Index of the first direction
	 * @param d2    Index of the second direction
	 * @param o1    Orientation of the first direction
	 * @param o2    Orientation of the second direction
	 * @return      Plaquette as LinkMatrix with correct orientation
	 */
	public LinkMatrix getPlaquette(int[] coor, int d1, int d2, int o1, int o2)
	{
		/*
			The four lattice coordinates associated with the plaquette.
		 */
		int[] x1 = coor.clone();
		int[] x2 = shift(x1, d1, o1);
		int[] x3 = shift(x2, d2, o2);
		int[] x4 = shift(x3, d1, -o1);

		/*
			The four gauge links associated with the plaquette.
		 */

		LinkMatrix U1 = getLink(x1, d1, o1);
		LinkMatrix U2 = getLink(x2, d2, o2);
		LinkMatrix U3 = getLink(x3, d1, -o1);
		LinkMatrix U4 = getLink(x4, d2, -o2);

		/*
			Plaquette calculation
		 */

		LinkMatrix P = U4.mult(U3);
		P = P.mult(U2);
		P = P.mult(U1);

		return P;
	}

	/**
	 * Getter for gauge links. Returns a link starting from a certain lattice coordinate with the right direction and orientation.
	 * Examples:
	 * Link starting at coor in positive x-direction: getLink(coor, 0, 1)
	 * Link starting at coor in negative x-direction: getLink(coor, 0, -1)
	 *
	 * @param coor          Lattice coordinate from which the link starts from
	 * @param dir           Direction of the link (0 - (numberOfDimensions-1))
	 * @param orientation   Orientation of the link (-1 or 1)
	 * @return              Gauge link in certain direction with correct orientation
	 */
	public LinkMatrix getLink(int[] coor, int dir, int orientation)
	{
		if(orientation < 0)
		{
			return getCell(shift(coor, dir, orientation)).getU(dir).adj();
		}
		return getCell(coor).getU(dir);
	}


	/**
	 * This method translates a lattice coordinate vector to the corresponding cell id with respect
	 * to periodic boundary conditions.
	 *
	 * @param coor  lattice coordinate vector
	 * @return      cell id
	 */
	private int index(int[] coor) {
		
		int[] modCoor = periodic(coor);
		int res = modCoor[0];
		
		for (int i = 1; i < numDim; ++i) {
			res += modCoor[i]*numCells[i-1];
		}
		return res;
	}

	/**
	 * This method implements periodic boundary conditions on the lattice.
	 * If the lattice coordinate vector is outside the bounds of the simulation box it gets shifted appropriately.
	 *
	 * @param coor  lattice coordinate vector
	 * @return      shifted lattice coordinate vector
	 */
	private int[] periodic(int[] coor) {
		
		int[] res = new int[numDim];
		for (int i = 0; i < numDim; ++i) {
			res[i] = (coor[i] + numCells[i]) % numCells[i];
		}
		return res;
	}

	/**
	 * Shifts a lattice coorindate vector by one unit step in a certain direction. The direction is passed as an integer for the direction and an orientation.
	 * Examples:
	 * Shift by one in negative x-direction: shift(coor, 0, -1)
	 * Shift by one in positive x-direction: shift(coor, 0, 1)
	 * Shift by one in positive z-direction: shift(coor, 2, 1)
	 *
	 * @param coor          Input lattice coordinate vector
	 * @param dir           Direction of the shift (0 - (numberOfDirections-1))
	 * @param orientation   Orientation of the direction (1 or -1)
	 * @return              Shifted coordinate with respect to periodic boundary conditions.
	 */
	private int[] shift(int[] coor, int dir, int orientation)
	{
		int[] shiftedCoordinate = coor.clone();

		for(int i = 0; i < numDim; i++)
		{
			shiftedCoordinate[i] =+ orientation * unitVectors[dir][i];
		}


		return periodic(shiftedCoordinate);
	}

	private class ResetCurrentAction implements CellAction {

		public void execute(Grid grid, int[] coor) {
			grid.getCell(coor).resetCurrent();
		}
	}

	private class ResetChargeAction implements CellAction {

		public void execute(Grid grid, int[] coor) {
			grid.resetCharge(coor);
		}
	}

	private class StoreFieldsAction implements CellAction {

		public void execute(Grid grid, int[] coor) {
			grid.getCell(coor).reassignLinks();
		}
	}
}
