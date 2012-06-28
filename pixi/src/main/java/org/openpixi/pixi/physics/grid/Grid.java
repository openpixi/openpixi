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
	 *    For example, the hardwall boundaries allow the particle to be outside
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
	static final int EXTRA_CELLS_BEFORE_GRID = 1;
	static final int EXTRA_CELLS_AFTER_GRID = 2;

	/**contains the simulation instance that this grid belongs to*/
	public Simulation simulation;
	/**interpolation algorithm for current, charge density and force calculation*/
	private Interpolator interp;
	/**solver algorithm for the maxwell equations*/
	private FieldSolver fsolver;
	/**solver for the electrostatic poisson equation*/
	private PoissonSolver poisolver;

	/**electric current in x-Direction*/
	private double [][] jx;
	/**electric current in y-Direction*/
	private double [][] jy;

	/**sum of electric charges in a cell*/
	private double [][] rho;
	/**electrostatic potential*/
	private double [][] phi;

	/**electric field in x direction at time t+dt*/
	private double [][] Ex;
	/**electric field in y direction at time t+dt*/
	private double [][] Ey;
	/**magnetic field in z direction at time t+dt*/
	private double [][] Bz;

	/**electric field in x direction at time t*/
	private double [][] Exo;
	/**electric field in y direction at time t*/
	private double [][] Eyo;
	/**magnetic field in z direction at time t*/
	private double [][] Bzo;

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
		return jx[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setJx(int x, int y, double value) {
		this.jx[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public void addJx(int x, int y, double value) {
		this.jx[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] += value;
	}

	public double getJy(int x, int y) {
		return jy[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setJy(int x, int y, double value) {
		this.jy[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public void addJy(int x, int y, double value) {
		this.jy[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] += value;
	}

	public double getRho(int x, int y) {
		return this.rho[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setRho(int x, int y, double value) {
		this.rho[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}
	
	public void addRho(int x, int y, double value) {
		this.rho[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] += value;
	}

	public double getPhi(int x, int y) {
		return phi[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setPhi(int x, int y, double value) {
		this.phi[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public double getEx(int x, int y) {
		return Ex[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setEx(int x, int y, double value) {
		this.Ex[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public void addEx(int x, int y, double value) {
		this.Ex[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] += value;
	}

	public double getEy(int x, int y) {
		return Ey[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setEy(int x, int y, double value) {
		this.Ey[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public void addEy(int x, int y, double value) {
		this.Ey[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] += value;
	}

	public double getBz(int x, int y) {
		return Bz[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setBz(int x, int y, double value) {
		this.Bz[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public void addBz(int x, int y, double value) {
		this.Bz[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] += value;
	}

	public double getExo(int x, int y) {
		return this.Exo[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setExo(int x, int y, double value) {
		this.Exo[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public double getEyo(int x, int y) {
		return this.Eyo[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setEyo(int x, int y, double value) {
		this.Eyo[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
	}

	public double getBzo(int x, int y) {
		return this.Bzo[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID];
	}

	public void setBzo(int x, int y, double value) {
		this.Bzo[x+EXTRA_CELLS_BEFORE_GRID][y+EXTRA_CELLS_BEFORE_GRID] = value;
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

	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
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
		jx = new double[xcells][ycells];
		jy = new double[xcells][ycells];
		rho = new double[xcells][ycells];
		phi = new double[xcells][ycells];
		Ex = new double[xcells][ycells];
		Ey = new double[xcells][ycells];
		Bz = new double[xcells][ycells];
		Exo = new double[xcells][ycells];
		Eyo = new double[xcells][ycells];
		Bzo = new double[xcells][ycells];
		
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
		for(int i = 0; i < getNumCellsX() + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID; i++) {
			for(int k = 0; k < getNumCellsY() + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
				rho[i][k] = 0.0;
			}
		}
	}

	public void storeFields() {
		for (int i = 0; i < getNumCellsX() + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID; i++) {
			for (int j = 0; j < getNumCellsY() + EXTRA_CELLS_BEFORE_GRID + EXTRA_CELLS_AFTER_GRID; j++) {
				Exo[i][j] = Ex[i][j];
				Eyo[i][j] = Ey[i][j];
				Bzo[i][j] = Bz[i][j];
			}
		}
	}
}
