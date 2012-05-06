package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.*;

public class Grid {
	
	public Simulation simulation;
	public Interpolator interp;
	
	/**electric current in x-Direction*/
	public double [][] jx;
	/**electric current in y-Direction*/
	public double [][] jy;

	/**sum of electric charges in a cell*/
	public double [][] rho;
	
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

}
