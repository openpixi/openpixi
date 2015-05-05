package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.parallel.cellaccess.CellIterator;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.grid.YMField;

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

	public void setFTensor(int[] coor, int dir1, int dir2, YMField field) {
		cells[index(coor)].setFieldStrength(dir1, dir2, field);
	}
	
	public LinkMatrix getU(int[] coor, int dir) {
		return cells[index(coor)].getU(dir);
	}

	public void setU(int[] coor, int dir, LinkMatrix mat) {
		cells[index(coor)].setU(dir, mat);
	}
	
	public LinkMatrix getUnext(int[] coor, int dir) {
		return cells[index(coor)].getUnext(dir);
	}

	public void setUnext(int[] coor, int dir, LinkMatrix mat) {
		cells[index(coor)].setUnext(dir, mat);
	}
	
	public void resetCharge(int[] coor) {
		cells[index(coor)].resetCharge();
	}
	
	public int getNumCells(int dir) {
		return numCells[dir];
	}

	public double getLatticeSpacing() {
		return as;
	}
	
	public Cell getCell(int[] coor) {
		return cells[index(coor)];
	}

	public Cell[] getCells() {
		return cells;
	}

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

	private void createGrid() {
		
		int length = 1;
		for(int i = 0; i < numDim; i++) {
			length *= numCells[i];
		}
		cells = new Cell[length];

		for(int i = 0; i < length; i++) {
			cells[i] = new Cell(numDim, numCol);
		}
	
	}

	public void updateGrid(double tstep) {
		getFsolver().step(this, tstep);
		storeFields();
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
	private int index(int[] coor) {
		
		int[] modCoor = periodic(coor);
		int res = modCoor[0];
		
		for (int i = 1; i < numDim; ++i) {
			res += modCoor[i]*numCells[i-1];
		}
		return res;
	}
	
	private int[] periodic(int[] coor) {
		
		int[] res = new int[numDim];
		for (int i = 0; i < numDim; ++i) {
			res[i] = (coor[i] + numCells[i]) % numCells[i];
		}
		return res;
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
	
	public YMField FieldFromForwardPlaquette(int[] coor, int j, int k) {
		
		YMField res = cells[index(coor)].getEmptyField(numCol);
		int[] coor1 = new int[numDim];
		int[] coor2 = new int[numDim];
		System.arraycopy(coor, 0, coor1, 0, coor.length);
		System.arraycopy(coor, 0, coor2, 0, coor.length);
		coor1[j]++;
		coor2[k]++;

		res.FieldFromForwardPlaquette(cells[index(coor)].getU(j), cells[index(coor1)].getU(k), cells[index(coor2)].getU(j), cells[index(coor)].getU(k));

		return res;
	}
	
	public YMField FieldFromBackwardPlaquette(int[] coor, int j, int k) {
		
		YMField res = cells[index(coor)].getEmptyField(numCol);
		int[] coor1 = new int[numDim];
		int[] coor2 = new int[numDim];
		System.arraycopy(coor, 0, coor1, 0, coor.length);
		System.arraycopy(coor, 0, coor2, 0, coor.length);
		coor1[j]++;
		coor1[k]--;
		coor2[k]--;

		res.FieldFromBackwardPlaquette(cells[index(coor)].getU(j), cells[index(coor1)].getU(k), cells[index(coor2)].getU(j), cells[index(coor2)].getU(k));

		return res;
	}

}
