package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.parallel.cellaccess.CellIterator;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.grid.YMField;

public class Grid {

	/**
	 * Solver algorithm for the field equations
	 */
	private FieldSolver fsolver;

	/**
	 * Instance of the CellIterator which iterates over all cells in the grid (either sequentially or in parallel)
	 */
	private CellIterator cellIterator;

	/*
	 *      Cell actions
	 */
	private ResetChargeAction resetCharge = new ResetChargeAction();
	private ResetCurrentAction resetCurrent = new ResetCurrentAction();
	private StoreFieldsAction storeFields = new StoreFieldsAction();
	/**
	 * Cell array. This one dimensional array is used to represent the d-dimensional grid. The cells are indexed by
	 * their cell ids. Cell ids can be computed from lattice coordinates with the index() method.
	 */
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
	 * Number of cells (size of the grid) in each direction
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

	/**
	 * Returns the FieldSolver instance currently used.
	 * @return  Instance of the FieldSolver
	 */
	public FieldSolver getFsolver() {
		return fsolver;
	}

	/**
	 * Sets the FieldSolver used for grid updates.
	 * @param fsolver   Instance of the FieldSolver which should be used.
	 */
	public void setFsolver(FieldSolver fsolver) {
		this.fsolver = fsolver;
	}

	/**
	 * Returns the YMField instance of the (dir)-component of the current.
	 * @param coor      Lattice coordinate of the current
	 * @param dir       Index of the component
	 * @return          YMField instance of the (dir)-component of the current.
	 */
	public YMField getJ(int[] coor, int dir) {
		return cells[getCellIndex(coor)].getJ(dir);
	}

	/**
	 * Adds a YMField instance to the (dir)-component of the current.
	 * @param coor      Lattice coordinate of the current
	 * @param dir       Index of the component
	 * @param field     YMField to be added to the (dir)-component of the current.
	 */
	public void addJ(int[] coor, int dir, YMField field) {
		cells[getCellIndex(coor)].addJ(dir, field);
	}

	/**
	 * Returns the YMField instance of the charge density.
	 * @param coor      Lattice coordinate of the charge density
	 * @return          YMField instance of the charge density
	 */
	public YMField getRho(int[] coor) {
		return cells[getCellIndex(coor)].getRho();
	}

	/**
	 * Sets the YMField instance of the charge density.
	 * @param coor      Lattice coordinate of the charge density
	 * @param field     YMField instance which the electric field should be set to.
	 */
	public void setRho(int[] coor, YMField field) {
		cells[getCellIndex(coor)].setRho(field);
	}

	/**
	 * Adds a YMField to the charge density.
	 * @param coor      Lattice coordinate of the charge density
	 * @param field     YMField instance which should be added.
	 */
	public void addRho(int[] coor, YMField field) {
		cells[getCellIndex(coor)].addRho(field);
	}

	/**
	 * Returns the YMField instance of the (dir)-component of the electric field.
	 * @param coor      Lattice coordinate of the electric field
	 * @param dir       Index of the component
	 * @return          YMField instance of the (dir)-component
	 */
	public YMField getE(int[] coor, int dir) {
		return cells[getCellIndex(coor)].getE(dir);
	}

	/**
	 * Sets the YMField instance of the (dir)-component of the electric field.
	 * @param coor      Lattice coordinate of the electric field
	 * @param dir       Index of the component
	 * @param field     YMField instance which the electric field should be set to.
	 */
	public void setE(int[] coor, int dir, YMField field) {
		cells[getCellIndex(coor)].setE(dir, field);
	}

	/**
	 * Adds a YMField to the (dir)-component of the electric field.
	 * @param coor      Lattice coordinate of the electric field
	 * @param dir       Index of the component
	 * @param field     YMField instance which should be added.
	 */
	public void addE(int[] coor, int dir, YMField field) {
		cells[getCellIndex(coor)].addE(dir, field);
	}

	/**
	 * Returns the (dir1, dir2)-component of the field strength tensor at a certain lattice coordinate.
	 * @param coor      Lattice coordinate of the field strength tensor
	 * @param dir1      First spatial component (0 - (numberOfDimensions-1))
	 * @param dir2      Second spatial component (0 - (numberOfDimensions-1))
	 * @return          YMField instance of the (dir1, dir2)-component
	 */
	public YMField getFTensor(int[] coor, int dir1, int dir2) {
		return cells[getCellIndex(coor)].getFieldStrength(dir1, dir2);
	}

	/**
	 * Sets the (dir1, dir2)-component of the field strength tensor at a certain lattice coordinate.
	 * @param coor      Lattice coordinate of the field strength tensor
	 * @param dir1      First space component (0 - (numberOfDimensions-1))
	 * @param dir2      Second space component (0 - (numberOfDimensions-1))
	 * @param field     YMField instance which the field strength tensor should be set to.
	 */
	public void setFTensor(int[] coor, int dir1, int dir2, YMField field) {
		cells[getCellIndex(coor)].setFieldStrength(dir1, dir2, field);
	}

	/**
	 * Returns the gauge link at time (t) at a given lattice coordinate in a given direction.
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @return      Instance of the gauge link
	 */
	public LinkMatrix getU(int[] coor, int dir) {
		return cells[getCellIndex(coor)].getU(dir);
	}

	/**
	 * Sets the gauge link at time (t) at given lattice coordinate in given direction to a new value.
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @param mat   LinkMatrix instance
	 */
	public void setU(int[] coor, int dir, LinkMatrix mat) {
		cells[getCellIndex(coor)].setU(dir, mat);
	}

	/**
	 * Returns the gauge link at time (t+dt) at a given lattice coordinate in a given direction.
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @return      Instance of the gauge link
	 */
	public LinkMatrix getUnext(int[] coor, int dir) {
		return cells[getCellIndex(coor)].getUnext(dir);
	}

	/**
	 * Sets the gauge link at time (t+dt) at given lattice coordinate in given direction to a new value.
	 * @param coor  Lattice coordinate of the gauge link
	 * @param dir   Direction of the gauge link
	 * @param mat   LinkMatrix instance
	 */
	public void setUnext(int[] coor, int dir, LinkMatrix mat) {
		cells[getCellIndex(coor)].setUnext(dir, mat);
	}

	/**
	 * Resets charge in a cell at a given lattice coordinate.
	 * @param coor  Lattice coordinate of the cell
	 */
	public void resetCharge(int[] coor) {
		cells[getCellIndex(coor)].resetCharge();
	}

	/**
	 * Returns an array of the number of cells in the grid
	 * in a given direction.
	 * @return      Array of number of cells in given direction.
	 */
	public int[] getNumCells() {
		return numCells;
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
	 * Calculate total number of cells in the grid.
	 * @return Total number of cells in the grid.
	 */
	public int getTotalNumberOfCells() {
		int numberOfCells = 1;
		for (int i = 0; i < numDim; i++) {
			numberOfCells *= getNumCells(i);
		}
		return numberOfCells;
	}

	/**
	 * Returns the lattice spacing of the grid.
	 * @return  Lattice spacing of the grid.
	 */
	public double getLatticeSpacing() {
		return as;
	}
	
	/**
	 * Returns the gauge coupling.
	 * @return  Gauge coupling.
	 */
	public double getGaugeCoupling() {
		return gaugeCoupling;
	}
	
	/**
	 * Returns the cell iterator.
	 * @return  Cell iterator.
	 */
	public CellIterator getCellIterator() {
		return cellIterator;
	}

	/**
	 * Returns the Cell instance at given lattice coordinates with respect to periodic boundary conditions.
	 *
	 * @param coor  Lattice coordinate of the cell
	 * @return      Cell instance at lattice coordinates with respect to periodic boundary conditions
	 */
	public Cell getCell(int[] coor) {
		return cells[getCellIndex(coor)];
	}

	/**
	 * Returns full Cell array of the grid. Note that the cells in this array are indexed by cell ids.
	 * @return  Full array with all cells
	 */
	public Cell[] getCells() {
		return cells;
	}

	/**
	 * Returns number of dimensions of the grid.
	 * @return	Number of dimensions
	 */
	public int getNumberOfDimensions()
	{
		return numDim;
	}

	public int getNumberOfColors() {
		return numCol;
	}

	/**
	 * Main constructor for the Grid class. Given a settings file it initializes the lattice and sets up the FieldSolver
	 * and the CellIterator.
	 * @param settings  Settings instance
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
	 * Constructor for the Grid class.
	 * It creates a grid of the same size and deep copies U and E fields.
	 * @param grid  Grid from which to copy dimensions
	 */
	public Grid(Grid grid) {
		gaugeCoupling = grid.gaugeCoupling;
		as = grid.as;
		numCol = grid.numCol;
		numDim = grid.numDim;
		numCells = new int[numDim];

		for(int i = 0; i < numDim; i++) {
			numCells[i] = grid.numCells[i];
		}

		createGrid();

		copyValuesFrom(grid);

		// TODO: Share iterators is ok?
		this.fsolver = grid.fsolver;
		this.cellIterator = grid.cellIterator;
	}

	/**
	 * Copy U and E field values from one grid to another.
	 * @param grid
	 */
	private void copyValuesFrom(Grid grid) {
		int numberOfCells = grid.getTotalNumberOfCells();
		for (int ci = 0; ci < numberOfCells; ci++) {
			int[] cellPosition = grid.getCellPos(ci);
			for (int d = 0; d < numDim; d++) {
				LinkMatrix U = grid.getU(cellPosition, d);
				this.setU(cellPosition, d, U);
				YMField E = grid.getE(cellPosition, d);
				this.setE(cellPosition, d, E);
				// TODO: if desired: Copy other fields as well.
			}
		}
	}

	/**
	 * This methods initializes each cell in the grid.
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
	 * It stores the fields, i.e. sets (t+dt) fields from the last grid update to the old ones at time (t) and calls the
	 * FieldSolver to solve the equations of motion for one time step.
	 *
	 * @param tstep size of the time step
	 */
	public void updateGrid(double tstep) {
		getFsolver().step(this, tstep);
		storeFields();
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
	 *      U_{x, ij} = U_{x, i} U_{x+i, j} U_{x+i+j, -i} U_{x+j, -j}
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

		return U1.mult(U2.mult(U3.mult(U4)));
	}

	/**
	 * Getter for gauge links. Returns a link starting from a certain lattice coordinate with the right direction and
	 * orientation.
	 *
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
	 * This method translates a cell index to the corresponding lattice position with respect to periodic boundary
	 * donitions.
	 *
	 * @param index	Cell index
	 * @return		Lattice positions of the cell
	 */
	public int[] getCellPos(int index)
	{
		int[] pos = new int[this.numDim];

		for(int i = this.numDim-1; i >= 0; i--)
		{
			pos[i] = index % this.numCells[i];
			index -= pos[i];
			index /= this.numCells[i];
		}

		return pos;
	}

	/**
	 * This method translates a lattice coordinate vector to the corresponding cell id with respect to periodic boundary
	 * conditions.
	 *
	 * @param coor  lattice coordinate vector
	 * @return      cell id
	 */
	public int getCellIndex(int[] coor)
	{
		//ensure periodicity
		int[] periodicCoor = periodic(coor);

		int cellIndex = periodicCoor[0];

		for(int i = 1; i < coor.length; i++)
		{
			cellIndex *= numCells[i];
			cellIndex += periodicCoor[i];
		}
		return  cellIndex;
	}

	/**
	 * This method translates a lattice coordinate vector to the corresponding cell id with respect to periodic boundary
	 * conditions.
	 *
	 * @param coor  lattice coordinate vector
	 * @return      cell id
	 */
	public int index(int[] coor) {
		
		int[] modCoor = periodic(coor);
		int res = modCoor[0];
		int dim = 1;
		
		for (int i = 1; i < numDim; ++i) {
			dim *= numCells[i-1];
			res += modCoor[i]*dim;
		}
		return res;
	}

	/**
	 * This method implements periodic boundary conditions on the lattice. If the lattice coordinate vector is outside
	 * the bounds of the simulation box it gets shifted appropriately.
	 *
	 * @param coor  lattice coordinate vector
	 * @return      shifted lattice coordinate vector
	 */
	public int[] periodic(int[] coor) {
		
		int[] res = new int[numDim];
		for (int i = 0; i < numDim; ++i) {
			res[i] = (coor[i] + numCells[i]) % numCells[i];
			//if(coor[i] != res[i]) {System.out.println(coor[i]);System.out.println(numCells[i]);System.out.println(res[i]);System.out.println("/");}
		}
		return res;
	}

	/**
	 * Shifts a lattice coorindate vector by one unit step in a certain direction. The direction is passed as an integer
	 * for the direction and an orientation.
	 *
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
	public int[] shift(int[] coor, int dir, int orientation)
	{
		int[] shiftedCoordinate = coor.clone();

		for(int i = 0; i < numDim; i++)
		{
			shiftedCoordinate[i] += orientation * unitVectors[dir][i];
		}


		return periodic(shiftedCoordinate);
	}

	/*
		Cell actions
	 */

	/**
	 * ResetCurrentAction is used by the CellIterator to reset all currents in the grid.
	 */
	private class ResetCurrentAction implements CellAction {

		public void execute(Grid grid, int[] coor) {
			grid.getCell(coor).resetCurrent();
		}
	}

	/**
	 * ResetChargeAction is used by the CellIterator to reset all charges on the grid.
	 */
	private class ResetChargeAction implements CellAction {

		public void execute(Grid grid, int[] coor) {
			grid.resetCharge(coor);
		}
	}

	/**
	 * StoreFieldsAction is used by the CellIterator to save the new (t+dt) values of the fields to the variables of the
	 * old (t) fields. Or more simple: The new fields become the old fields.
	 */
	private class StoreFieldsAction implements CellAction {

		public void execute(Grid grid, int[] coor) {
			grid.getCell(coor).reassignLinks();
		}
	}
	
	/**
	 * Calculates the field from the forward plaquette starting at lattice coordinate coor in the directions j and k.
	 * The matrix multiplication is done in the concrete field class.
	 * The forward plaquette is defined as follows:
	 *      U_{x, jk} = U_{x, j} U_{x+j, k} U^adj_{x+k, j} U^adj_{x, k}
	 *
	 * @param coor  Lattice coordinate from where the plaquette starts
	 * @param j    Index of the first direction
	 * @param k    Index of the second direction
	 * @return      Field from the forward plaquette
	 */
	public YMField FieldFromForwardPlaquette(int[] coor, int j, int k) {
		
		YMField res = cells[getCellIndex(coor)].getEmptyField(numCol);
		int[] coor1 = new int[numDim];
		int[] coor2 = new int[numDim];
		System.arraycopy(coor, 0, coor1, 0, coor.length);
		System.arraycopy(coor, 0, coor2, 0, coor.length);
		coor1[j]++;
		coor2[k]++;

		res.FieldFromForwardPlaquette(cells[getCellIndex(coor)].getU(j), cells[getCellIndex(coor1)].getU(k), cells[getCellIndex(coor2)].getU(j), cells[getCellIndex(coor)].getU(k));

		return res;
	}
	
	/**
	 * Calculates the field from the backward plaquette starting at lattice coordinate coor in the directions j and k.
	 * The matrix multiplication is done in the concrete field class.
	 * The backward plaquette is defined as follows:
	 *      U_{x, jk} = U_{x, j} U^adj_{x+j-k, k} U^adj_{x-k, j} U_{x, k}
	 *
	 * @param coor  Lattice coordinate from where the plaquette starts
	 * @param j    Index of the first direction
	 * @param k    Index of the second direction
	 * @return      Field from the backward plaquette
	 */
	public YMField FieldFromBackwardPlaquette(int[] coor, int j, int k) {
		
		YMField res = cells[getCellIndex(coor)].getEmptyField(numCol);
		int[] coor1 = new int[numDim];
		int[] coor2 = new int[numDim];
		System.arraycopy(coor, 0, coor1, 0, coor.length);
		System.arraycopy(coor, 0, coor2, 0, coor.length);
		coor1[j]++;
		coor1[k]--;
		coor2[k]--;

		res.FieldFromBackwardPlaquette(cells[getCellIndex(coor)].getU(j), cells[getCellIndex(coor1)].getU(k), cells[getCellIndex(coor2)].getU(j), cells[getCellIndex(coor2)].getU(k));

		return res;
	}

}
