package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.grid.*;

public class Grid {
	
	/**contains the simulation instance that this grid belongs to*/
	public Simulation simulation;
	/**interpolation algorithm for current, charge density and force calculation*/
	public Interpolator interp;
	/**solver algorithm for the maxwell equations*/
	public FieldSolver fsolver;
	
	/**electric current in x-Direction*/
	public double [][] jx;
	/**electric current in y-Direction*/
	public double [][] jy;

	/**sum of electric charges in a cell*/
	public double [][] rho;
	/**electrostatic potential*/
	public double [][] phi;
	
	/**electric field in x direction at time t+dt*/
	public double [][] Ex;
	/**electric field in y direction at time t+dt*/
	public double [][] Ey;
	/**magnetic field in z direction at time t+dt*/
	public double [][] Bz;
	
	/**electric field in x direction at time t*/
	public double [][] Exo;
	/**electric field in y direction at time t*/
	public double [][] Eyo;
	/**magnetic field in z direction at time t*/
	public double [][] Bzo;
	
	/**number of cells in x direction*/
	public int numCellsX;
	/**number of cells in x direction*/
	public int numCellsY;
	/**width of each cell*/
	public double cellWidth;
	/**height of each cell*/
	public double cellHeight;
	
	public Grid(Simulation s) {
		
		this.simulation = s;
		
	}
	
	public void changeDimension(double width, double height, int xbox, int ybox) {
		
	}
	
	public void setGrid(double width, double height) {
		
	}
	
	public void updateGrid(ArrayList<Particle2D> particles) {
		
	}
	
	public void reset() {
		for(int i = 0; i < numCellsX; i++) {
			for(int k = 0; k < numCellsY; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
				rho[i][k] = 0.0;
			}
		}
	}
	
	public void initFields() {
		for (int i = 0; i < numCellsX; i++) {
			for (int j = 0; j < numCellsY; j++) {
				Ex[i][j] = 0.0;
				Ey[i][j] = 0.0;
				Bz[i][j] = 0.0;
			}
		}
	}
	
	public void save() {
		for (int i = 0; i < numCellsX; i++) {
			for (int j = 0; j < numCellsY; j++) {
				Exo[i][j] = Ex[i][j];
				Eyo[i][j] = Ey[i][j];
				Bzo[i][j] = Bz[i][j];
			}
		}
		
	}

}
