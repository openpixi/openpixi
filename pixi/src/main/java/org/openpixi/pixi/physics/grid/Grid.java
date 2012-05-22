package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.FieldSolver;
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
		return jx[x][y];
	}

	public void setJx(int x, int y, double value) {
		this.jx[x][y] = value;
	}

	public void addJx(int x, int y, double value) {
		this.jx[x][y] += value;
	}

	public double getJy(int x, int y) {
		return jy[x][y];
	}

	public void setJy(int x, int y, double value) {
		this.jy[x][y] = value;
	}

	public void addJy(int x, int y, double value) {
		this.jy[x][y] += value;
	}

	public double getRho(int x, int y) {
		return this.rho[x][y];
	}

	public void setRho(int x, int y, double value) {
		this.rho[x][y] = value;
	}

	public double getPhi(int x, int y) {
		return phi[x][y];
	}

	public void setPhi(int x, int y, double value) {
		this.phi[x][y] = value;
	}

	public double getEx(int x, int y) {
		return Ex[x][y];
	}

	public void setEx(int x, int y, double value) {
		this.Ex[x][y] = value;
	}

	public void addEx(int x, int y, double value) {
		this.Ex[x][y] += value;
	}

	public double getEy(int x, int y) {
		return Ey[x][y];
	}

	public void setEy(int x, int y, double value) {
		this.Ey[x][y] = value;
	}

	public void addEy(int x, int y, double value) {
		this.Ey[x][y] += value;
	}

	public double getBz(int x, int y) {
		return Bz[x][y];
	}

	public void setBz(int x, int y, double value) {
		this.Bz[x][y] = value;
	}

	public void addBz(int x, int y, double value) {
		this.Bz[x][y] += value;
	}

	public double getExo(int x, int y) {
		return this.Exo[x][y];
	}

	public void setExo(int x, int y, double value) {
		this.Exo[x][y] = value;
	}

	public double getEyo(int x, int y) {
		return this.Eyo[x][y];
	}

	public void setEyo(int x, int y, double value) {
		this.Eyo[x][y] = value;
	}

	public double getBzo(int x, int y) {
		return this.Bzo[x][y];
	}

	public void setBzo(int x, int y, double value) {
		this.Bzo[x][y] = value;
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

	public Grid(Simulation s) {
		this.simulation = s;
	}

	public void changeDimension(double width, double height, int xbox, int ybox) {

	}

	public void setGrid(double width, double height) {

	}

	public void updateGrid(ArrayList<Particle> particles, double tstep) {

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

	public void createGrid() {
		jx = new double[numCellsX][numCellsY];
		jy = new double[numCellsX][numCellsY];
		rho = new double[numCellsX][numCellsY];
		phi = new double[numCellsX][numCellsY];
		Ex = new double[numCellsX][numCellsY];
		Ey = new double[numCellsX][numCellsY];
		Bz = new double[numCellsX][numCellsY];
		Exo = new double[numCellsX][numCellsY];
		Eyo = new double[numCellsX][numCellsY];
		Bzo = new double[numCellsX][numCellsY];
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

	private double getFieldSign(double[][] field) {
		double s = 0;
		for (int i = 0; i < field.length; i++) {
			for(int j = 0; j < field[0].length; j++) {
				if(field[i][j] != 0){
					s = Math.signum(field[i][j]);
				}
			}
		}
		return s;
	}
}
