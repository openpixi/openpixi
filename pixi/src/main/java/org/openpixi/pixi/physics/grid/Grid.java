package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.grid.*;

public class Grid {

	/**contains the simulation instance that this grid belongs to*/
	public Simulation simulation;
	/**interpolation algorithm for current, charge density and force calculation*/
	protected Interpolator interp;
	/**solver algorithm for the maxwell equations*/
	protected FieldSolver fsolver;

	/**electric current in x-Direction*/
	protected double [][] jx;
	/**electric current in y-Direction*/
	protected double [][] jy;

	/**sum of electric charges in a cell*/
	protected double [][] rho;
	/**electrostatic potential*/
	protected double [][] phi;

	/**electric field in x direction at time t+dt*/
	protected double [][] Ex;
	/**electric field in y direction at time t+dt*/
	protected double [][] Ey;
	/**magnetic field in z direction at time t+dt*/
	protected double [][] Bz;

	/**electric field in x direction at time t*/
	protected double [][] Exo;
	/**electric field in y direction at time t*/
	protected double [][] Eyo;
	/**magnetic field in z direction at time t*/
	protected double [][] Bzo;

	/**number of cells in x direction*/
	protected int numCellsX;
	/**number of cells in x direction*/
	protected int numCellsY;
	/**width of each cell*/
	protected double cellWidth;
	/**height of each cell*/
	protected double cellHeight;

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
		return jx[x+1][y+1];
	}

	public void setJx(int x, int y, double value) {
		this.jx[x+1][y+1] = value;
	}

	public void addJx(int x, int y, double value) {
		this.jx[x+1][y+1] += value;
	}

	public double getJy(int x, int y) {
		return jy[x+1][y+1];
	}

	public void setJy(int x, int y, double value) {
		this.jy[x+1][y+1] = value;
	}

	public void addJy(int x, int y, double value) {
		this.jy[x+1][y+1] += value;
	}

	public double getRho(int x, int y) {
		return this.rho[x+1][y+1];
	}

	public void setRho(int x, int y, double value) {
		this.rho[x+1][y+1] = value;
	}

	public double getPhi(int x, int y) {
		return phi[x+1][y+1];
	}

	public void setPhi(int x, int y, double value) {
		this.phi[x+1][y+1] = value;
	}

	public double getEx(int x, int y) {
		return Ex[x+1][y+1];
	}

	public void setEx(int x, int y, double value) {
		this.Ex[x+1][y+1] = value;
	}

	public void addEx(int x, int y, double value) {
		this.Ex[x+1][y+1] += value;
	}

	public double getEy(int x, int y) {
		return Ey[x+1][y+1];
	}

	public void setEy(int x, int y, double value) {
		this.Ey[x+1][y+1] = value;
	}

	public void addEy(int x, int y, double value) {
		this.Ey[x+1][y+1] += value;
	}

	public double getBz(int x, int y) {
		return Bz[x+1][y+1];
	}

	public void setBz(int x, int y, double value) {
		this.Bz[x+1][y+1] = value;
	}

	public void addBz(int x, int y, double value) {
		this.Bz[x+1][y+1] += value;
	}

	public double getExo(int x, int y) {
		return this.Exo[x+1][y+1];
	}

	public void setExo(int x, int y, double value) {
		this.Exo[x+1][y+1] = value;
	}

	public double getEyo(int x, int y) {
		return this.Eyo[x+1][y+1];
	}

	public void setEyo(int x, int y, double value) {
		this.Eyo[x+1][y+1] = value;
	}

	public double getBzo(int x, int y) {
		return this.Bzo[x+1][y+1];
	}

	public void setBzo(int x, int y, double value) {
		this.Bzo[x+1][y+1] = value;
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
			Interpolator interp) {

		this.simulation = s;
		this.fsolver = fsolver;
		this.interp = interp;

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

		int xcells = numCellsX + 2;
		int ycells = numCellsY + 2;
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
		for(int i = 0; i < getNumCellsX(); i++) {
			for(int k = 0; k < getNumCellsY(); k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
				rho[i][k] = 0.0;
			}
		}
	}

	public void storeFields() {
		for (int i = 0; i < getNumCellsX(); i++) {
			for (int j = 0; j < getNumCellsY(); j++) {
				Exo[i][j] = Ex[i][j];
				Eyo[i][j] = Ey[i][j];
				Bzo[i][j] = Bz[i][j];
			}
		}
	}

	public double getJxSum() {
		return getFieldSum(jx);
	}

	public double getJySum() {
		return getFieldSum(jy);
	}

	private double getFieldSum(double[][] field) {
		double sum = 0;
		for (double[] row: field) {
			for (double value: row) {
				sum += value;
			}
		}
		return sum;
	}
}
