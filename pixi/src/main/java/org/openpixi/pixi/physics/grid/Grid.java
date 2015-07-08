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
	 * their cell ids. Cell ids can be computed from lattice coordinates with the {@link #getCellIndex(int[])} method.
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
	 * Temporal lattice spacing
	 */
	private double at;

	/**
	 * Gauge coupling strength
	 */
	private double gaugeCoupling;

	/**
	 * Unit vectors to be used for the shift method.
	 */
	@Deprecated
	private int[][] unitVectors;

	/**
	 * Holds the cummulated cell count. This array is used by {@link #shift(int, int, int)}
	 * to quickly calculate index shifts in various directions.
	 * <pre>
	 * cummulatedCellCount[numDim] = 1;
	 * cummulatedCellCount[i] = cummulatedCellCount[i + 1] * numCells[i];
	 * </pre>
	 */
	private int cummulatedCellCount[];

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
	 * @param index     Lattice index of the current
	 * @param dir       Index of the component
	 * @return          YMField instance of the (dir)-component of the current.
	 */
	public YMField getJ(int index, int dir) {
		return cells[index].getJ(dir);
	}

	/**
	 * Adds a YMField instance to the (dir)-component of the current.
	 * @param index     Lattice index of the current
	 * @param dir       Index of the component
	 * @param field     YMField to be added to the (dir)-component of the current.
	 */
	public void addJ(int index, int dir, YMField field) {
		cells[index].addJ(dir, field);
	}

	/**
	 * Returns the YMField instance of the charge density.
	 * @param index     Lattice index of the charge density
	 * @return          YMField instance of the charge density
	 */
	public YMField getRho(int index) {
		return cells[index].getRho();
	}

	/**
	 * Sets the YMField instance of the charge density.
	 * @param index     Lattice index of the charge density
	 * @param field     YMField instance which the electric field should be set to.
	 */
	public void setRho(int index, YMField field) {
		cells[index].setRho(field);
	}

	/**
	 * Adds a YMField to the charge density.
	 * @param index     Lattice index of the charge density
	 * @param field     YMField instance which should be added.
	 */
	public void addRho(int index, YMField field) {
		cells[index].addRho(field);
	}

	/**
	 * Returns the YMField instance of the (dir)-component of the electric field.
	 * @param index     Lattice index of the electric field
	 * @param dir       Index of the component
	 * @return          YMField instance of the (dir)-component
	 */
	public YMField getE(int index, int dir) {
		return cells[index].getE(dir);
	}

	/**
	 * Sets the YMField instance of the (dir)-component of the electric field.
	 * @param index     Lattice index of the electric field
	 * @param dir       Index of the component
	 * @param field     YMField instance which the electric field should be set to.
	 */
	public void setE(int index, int dir, YMField field) {
		cells[index].setE(dir, field);
	}

	/**
	 * Adds a YMField to the (dir)-component of the electric field.
	 * @param index     Lattice index of the electric field
	 * @param dir       Index of the component
	 * @param field     YMField instance which should be added.
	 */
	public void addE(int index, int dir, YMField field) {
		cells[index].addE(dir, field);
	}

	/**
	 * Returns the (dir1, dir2)-component of the field strength tensor at a certain lattice index.
	 * @param index     Lattice index of the field strength tensor
	 * @param dir1      First spatial component (0 - (numberOfDimensions-1))
	 * @param dir2      Second spatial component (0 - (numberOfDimensions-1))
	 * @return          YMField instance of the (dir1, dir2)-component
	 */
	public YMField getFTensor(int index, int dir1, int dir2) {
		return cells[index].getFieldStrength(dir1, dir2);
	}

	/**
	 * Sets the (dir1, dir2)-component of the field strength tensor at a certain lattice index.
	 * @param index     Lattice index of the field strength tensor
	 * @param dir1      First space component (0 - (numberOfDimensions-1))
	 * @param dir2      Second space component (0 - (numberOfDimensions-1))
	 * @param field     YMField instance which the field strength tensor should be set to.
	 */
	public void setFTensor(int index, int dir1, int dir2, YMField field) {
		cells[index].setFieldStrength(dir1, dir2, field);
	}

	/**
	 * Returns the gauge link at time (t) at a given lattice index in a given direction.
	 * @param index Lattice index of the gauge link
	 * @param dir   Direction of the gauge link
	 * @return      Instance of the gauge link
	 */
	public LinkMatrix getU(int index, int dir) {
		return cells[index].getU(dir);
	}

	/**
	 * Sets the gauge link at time (t) at given lattice index in given direction to a new value.
	 * @param index Lattice index of the gauge link
	 * @param dir   Direction of the gauge link
	 * @param mat   LinkMatrix instance
	 */
	public void setU(int index, int dir, LinkMatrix mat) {
		cells[index].setU(dir, mat);
	}

	/**
	 * Returns the gauge link at time (t+dt) at a given lattice index in a given direction.
	 * @param index Lattice index of the gauge link
	 * @param dir   Direction of the gauge link
	 * @return      Instance of the gauge link
	 */
	public LinkMatrix getUnext(int index, int dir) {
		return cells[index].getUnext(dir);
	}

	/**
	 * Sets the gauge link at time (t+dt) at given lattice index in given direction to a new value.
	 * @param index Lattice index of the gauge link
	 * @param dir   Direction of the gauge link
	 * @param mat   LinkMatrix instance
	 */
	public void setUnext(int index, int dir, LinkMatrix mat) {
		cells[index].setUnext(dir, mat);
	}

	/**
	 * Resets charge in a cell at a given lattice index.
	 * @param index  Lattice index of the cell
	 */
	public void resetCharge(int index) {
		cells[index].resetCharge();
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
	 * Returns the number of colors.
	 * @return  Number of colors.
	 */
	public int getNumberOfColors() {
		return numCol;
	}
	
	/**
	 * Returns the temporal lattice spacing.
	 * @return  Temporal lattice spacing.
	 */
	public double getTemporalSpacing() {
		return at;
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
	 * Returns the Cell instance at given lattice index.
	 *
	 * @param index  Index of the cell
	 * @return       Cell instance at lattice index
	 */
	public Cell getCell(int index) {
		return cells[index];
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

	/**
	 * Main constructor for the Grid class. Given a settings file it initializes the lattice and sets up the FieldSolver
	 * and the CellIterator.
	 * @param settings  Settings instance
	 */
	public Grid(Settings settings) {

		gaugeCoupling = settings.getCouplingConstant();
		as = settings.getGridStep();
		at = settings.getTimeStep();
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
			for (int d = 0; d < numDim; d++) {
				LinkMatrix U = grid.getU(ci, d);
				this.setU(ci, d, U);
				YMField E = grid.getE(ci, d);
				this.setE(ci, d, E);
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

		cummulatedCellCount = new int[numDim + 1];
		cummulatedCellCount[numDim] = 1;
		for (int i = numDim - 1; i >= 0; i--) {
			cummulatedCellCount[i] = cummulatedCellCount[i + 1] * numCells[i];
		}

		cells = new Cell[length];

		for(int i = 0; i < length; i++) {
			cells[i] = new Cell(numDim, numCol);
		}
	
	}

	/**
	 * This method advances the grid by one time step:
	 * It calls the FieldSolver to solve the equations of motion for one time step.
	 *
	 * @param tstep size of the time step
	 */
	public void updateGrid(double tstep) {
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
	 * Calculates the plaquette starting at lattice index in the plane of d1 and d2 with orientations o1, o2.
	 * This method implements the following definition of the plaquette:
	 * <pre>     U_{x, ij} = U_{x, i} U_{x+i, j} U_{x+i+j, -i} U_{x+j, -j}</pre>
	 *
	 *
	 * @param index Lattice index from where the plaquette starts
	 * @param d1    Index of the first direction
	 * @param d2    Index of the second direction
	 * @param o1    Orientation of the first direction
	 * @param o2    Orientation of the second direction
	 * @return      Plaquette as LinkMatrix with correct orientation
	 */
	public LinkMatrix getPlaquette(int index, int d1, int d2, int o1, int o2)
	{
		/*
			The four lattice indices associated with the plaquette.
		 */
		int x1 = index;
		int x2 = shift(x1, d1, o1);
		int x3 = shift(x2, d2, o2);
		int x4 = shift(x3, d1, -o1);

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
	 * Getter for gauge links. Returns a link starting from a certain lattice index with the right direction and
	 * orientation.
	 * <br>
	 * Examples:
	 * <ul><li>Link starting at index in positive x-direction: getLink(index, 0, 1)</li>
	 * <li>Link starting at index in negative x-direction: getLink(index, 0, -1)</li></ul>
	 *
	 * @param index         Lattice index from which the link starts from
	 * @param direction     Direction of the link (0 - (numberOfDimensions-1))
	 * @param orientation   Orientation of the link (-1 or 1)
	 * @return              Gauge link in certain direction with correct orientation
	 */
	public LinkMatrix getLink(int index, int direction, int orientation)
	{
		if(orientation < 0)
		{
			return getCell(shift(index, direction, orientation)).getU(direction).adj();
		}
		return getCell(index).getU(direction);
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
	 * @param coordinates  lattice coordinate vector
	 * @return             cell id
	 */
	public int getCellIndex(int[] coordinates)
	{
		//ensure periodicity
		int[] periodicCoordinates = periodic(coordinates);

		int cellIndex = periodicCoordinates[0];

		for(int i = 1; i < coordinates.length; i++)
		{
			cellIndex *= numCells[i];
			cellIndex += periodicCoordinates[i];
		}
		return  cellIndex;
	}

	/**
	 * This method implements periodic boundary conditions on the lattice. If the lattice coordinate vector is outside
	 * the bounds of the simulation box it gets shifted appropriately.
	 *
	 * @param coordinates  lattice coordinate vector
	 * @return             shifted lattice coordinate vector
	 */
	@Deprecated
	protected int[] periodic(int[] coordinates) {
		
		int[] res = new int[numDim];
		for (int i = 0; i < numDim; ++i) {
			res[i] = (coordinates[i] + numCells[i]) % numCells[i];
		}
		return res;
	}

	/**
	 * Shifts a lattice coordinate vector by one unit step in a certain direction. The direction is passed as an integer
	 * for the direction and an orientation.
	 * <br>
	 * Examples:
	 * <ul><li>Shift by one in negative x-direction: shift(coor, 0, -1)</li>
	 * <li>Shift by one in positive x-direction: shift(coor, 0, 1)</li>
	 * <li>Shift by one in positive z-direction: shift(coor, 2, 1)</li></ul>
	 *
	 * @param coordinates   Input lattice coordinate vector
	 * @param direction     Direction of the shift (0 - (numberOfDirections-1))
	 * @param orientation   Orientation of the direction (1 or -1)
	 * @return              Shifted coordinate with respect to periodic boundary conditions.
	 */
	@Deprecated
	protected int[] shift(int[] coordinates, int direction, int orientation)
	{
		int[] shiftedCoordinate = coordinates.clone();

		for(int i = 0; i < numDim; i++)
		{
			shiftedCoordinate[i] += orientation * unitVectors[direction][i];
		}


		return periodic(shiftedCoordinate);
	}

	/**
	 * Shifts a lattice index by one unit step in a certain direction. The direction is passed as an integer
	 * for the direction and an orientation.
	 * <br>
	 * Examples:
	 * <ul><li>Shift by one in negative x-direction: shift(index, 0, -1)</li>
	 * <li>Shift by one in positive x-direction: shift(index, 0, 1)</li>
	 * <li>Shift by one in positive z-direction: shift(index, 2, 1)</li></ul>
	 *
	 * @param index         Valid input index of lattice coordinate
	 * @param direction     Direction of the shift (0 - (numberOfDirections-1))
	 * @param orientation   Orientation of the direction (1 or -1)
	 * @return              Index of shifted coordinate with respect to periodic boundary conditions.
	 */
	public int shift(int index, int direction, int orientation)
	{
		int result = index;
		int directionIndex = index / cummulatedCellCount[direction + 1];
		int withinDirectionIndex = directionIndex % numCells[direction];
		if (orientation > 0) {
			if (withinDirectionIndex == numCells[direction] - 1) {
				// wrap around along positive direction
				result -= cummulatedCellCount[direction];
			}
			result += cummulatedCellCount[direction + 1];
		} else if (orientation < 0) {
			if (withinDirectionIndex == 0) {
				// wrap around along negative direction
				result += cummulatedCellCount[direction];
			}
			result -= cummulatedCellCount[direction + 1];
		} else {
			// do nothing if orientation == 0
		}
		return result;
	}

	/*
		Cell actions
	 */

	/**
	 * ResetCurrentAction is used by the CellIterator to reset all currents in the grid.
	 */
	private class ResetCurrentAction implements CellAction {

		public void execute(Grid grid, int index) {
			grid.getCell(index).resetCurrent();
		}
	}

	/**
	 * ResetChargeAction is used by the CellIterator to reset all charges on the grid.
	 */
	private class ResetChargeAction implements CellAction {

		public void execute(Grid grid, int index) {
			grid.resetCharge(index);
		}
	}

	/**
	 * StoreFieldsAction is used by the CellIterator to save the new (t+dt) values of the fields to the variables of the
	 * old (t) fields. Or more simple: The new fields become the old fields.
	 */
	private class StoreFieldsAction implements CellAction {

		public void execute(Grid grid, int index) {
			grid.getCell(index).reassignLinks();
		}
	}

	/**
	 * Calculates the field from the forward plaquette starting at a lattice index in the directions j and k.
	 * The matrix multiplication is done in the concrete field class.
	 * The forward plaquette is defined as follows:
	 * <pre>     U_{x, jk} = U_{x, j} U_{x+j, k} U^adj_{x+k, j} U^adj_{x, k}</pre>
	 *
	 * @param index  Lattice index from where the plaquette starts
	 * @param j      Index of the first direction
	 * @param k      Index of the second direction
	 * @return       Field from the forward plaquette
	 */
	public YMField FieldFromForwardPlaquette(int index, int j, int k) {
		
		YMField res = cells[index].getEmptyField(numCol);
		int id1 = shift(index, j, 1);
		int id2 = shift(index, k, 1);

		res.FieldFromForwardPlaquette(cells[index].getU(j), cells[id1].getU(k), cells[id2].getU(j), cells[index].getU(k));

		return res;
	}

	/**
	 * Calculates the field from the backward plaquette starting at a lattice index in the directions j and k.
	 * The matrix multiplication is done in the concrete field class.
	 * The backward plaquette is defined as follows:
	 * <pre>     U_{x, jk} = U_{x, j} U^adj_{x+j-k, k} U^adj_{x-k, j} U_{x, k}</pre>
	 *
	 * @param index  Lattice index from where the plaquette starts
	 * @param j      Index of the first direction
	 * @param k      Index of the second direction
	 * @return       Field from the backward plaquette
	 */
	public YMField FieldFromBackwardPlaquette(int index, int j, int k) {

		YMField res = cells[index].getEmptyField(numCol);
		int id1 = shift(shift(index, j, 1), k, -1);
		int id2 = shift(index, k, -1);

		res.FieldFromBackwardPlaquette(cells[index].getU(j), cells[id1].getU(k), cells[id2].getU(j), cells[id2].getU(k));

		return res;
	}

	/**
	 * Calculates the square of the electric field from the temporal plaquette starting at a lattice index in a direction.
	 *
	 * @param index      Lattice index from where the plaquette starts
	 * @param direction  Index of the direction
	 * @return           E^2 calculated from the temporal plaquette
	 */
	public double getEsquaredFromLinks(int index, int direction) {
		
		double norm = at*at;
		//double res = 1.0 - cells[index].getUnext(direction).mult(cells[index].getU(direction).adj()).getTrace()/numCol;
		double res = cells[index].getUnext(direction).mult(cells[index].getU(direction).adj()).getLinearizedAlgebraElement().square()/norm;

		return res;
	}

	/**
	 * Calculates the square of the magnetic field from the spatial plaquette starting at a lattice index in a direction.
	 *
	 * @param index      Lattice index from where the plaquette starts
	 * @param direction  Index of the direction
	 * @return           B^2 calculated from the spatial plaquette
	 */
	public double getBsquaredFromLinks(int index, int direction) {
		
		double norm = as*as;
		int j=0, k=0;
		switch (direction) {
		case 0:
			j = 1;
			k = 2;
			break;

		case 1:
			j = 0;
			k = 2;
			break;

		case 2:
			j = 0;
			k = 1;
			break;
		}
		double res = getPlaquette(index, j, k, 1, 1).getLinearizedAlgebraElement().square()/norm;

		return res;
	}

	public double getGaussConstraintSquared(int index) {
		
		YMField gauss = cells[index].getEmptyField(numCol);
		YMField temp = cells[index].getEmptyField(numCol);
		double norm = 2.0/(at);

		for (int i = 0; i < numDim; i++) {
			int id2 = shift(index, i, -1);
			temp.set(getU(id2, i).adj().mult(getUnext(id2, i)).proj());
			gauss.addequate(getE(index, i).sub(temp.mult(norm)));
		}
		return gauss.square() / Math.pow( as * as * gaugeCoupling, 2.0);
	}

}
