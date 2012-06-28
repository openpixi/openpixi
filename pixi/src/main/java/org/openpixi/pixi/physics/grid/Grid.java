package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.fields.PoissonSolver;

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

	/**contains the simulation instance that this grid belongs to*/
	public Simulation simulation;
	/**interpolation algorithm for current, charge density and force calculation*/
	private Interpolator interp;
	/**solver algorithm for the maxwell equations*/
	private FieldSolver fsolver;
	/**solver for the electrostatic poisson equation*/
	private PoissonSolver poisolver;

	private Cell[][] cells;

	/**number of cells in x direction*/
	private int numCellsX;
	/**number of cells in x direction*/
	private int numCellsY;
	/**width of each cell*/
	private double cellWidth;
	/**height of each cell*/
	private double cellHeight;

	public Interpolator getInterp() {
		return interp;
	}

	public void setInterp(Interpolator interp) {
		this.interp = interp;
	}

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

	public void setNumCellsX(int numCellsX) {
		this.numCellsX = numCellsX;
	}

	public int getNumCellsY() {
		return numCellsY;
	}

	public void setNumCellsY(int numCellsY) {
		this.numCellsY = numCellsY;
	}

	public double getCellWidth() {
		return cellWidth;
	}

	public double getCellHeight() {
		return cellHeight;
	}	

	Grid(Simulation s,
			int numCellsX, int numCellsY,
			double simWidth, double simHeight,
			FieldSolver fsolver,
			Interpolator interp,
			PoissonSolver poisolver) {

		this.simulation = s;
		this.fsolver = fsolver;
		this.interp = interp;
		this.poisolver = poisolver;

		SimpleGridForce force = new SimpleGridForce();
		s.f.add(force);

		set(numCellsX, numCellsY, simWidth, simHeight);
	}

	public void set(int numCellsX, int numCellsY,
			double simWidth, double simHeight) {

		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;
		this.cellWidth = simWidth/numCellsX;
		this.cellHeight = simHeight/numCellsY;

		int xcells = EXTRA_CELLS_BEFORE_GRID + numCellsX + EXTRA_CELLS_AFTER_GRID;
		int ycells = EXTRA_CELLS_BEFORE_GRID + numCellsY + EXTRA_CELLS_AFTER_GRID;
		cells = new Cell[xcells][ycells];
		for (int x = 0; x < xcells; x++) {
			for (int y = 0; y < ycells; y++) {
				cells[x][y] = new Cell();
			}
		}
		
		interp.interpolateChargedensity(simulation.particles, this);
		poisolver.solve(this);

		for (Particle p: simulation.particles){
			//assuming rectangular particle shape i.e. area weighting
			p.setChargedensity(p.getCharge() / (cellWidth * cellHeight));
		}
	}

	public void updateGrid(ArrayList<Particle> particles, double tstep) {
		getInterp().interpolateToGrid(particles, this);
		storeFields();
		getFsolver().step(this, tstep);
		getInterp().interpolateToParticle(particles, this);
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
