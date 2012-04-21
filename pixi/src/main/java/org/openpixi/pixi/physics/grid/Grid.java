package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.*;

public class Grid {
	
	public Simulation s;
	public Interpolator interp;
	
	/**Electric current in x-Direction*/
	public double [][] jx;
	/**Electric current in y-Direction*/
	public double [][] jy;

	/**Electric charge sum of a cell*/
	public double [][] rho;
	
	/**Electric field in x direction*/
	public double [][] Ex;
	/**Electric field in y direction*/
	public double [][] Ey;
	/**Magnetic field in z direction*/
	public double [][] Bz;
	
	/** old electric field in x direction*/
	public double [][] Exo;
	/**old electric field in y direction*/
	public double [][] Eyo;
	/**old magnetic field in z direction*/
	public double [][] Bzo;
	
	/**Number of cells in x direction*/
	public int numCellsX;
	/**Number of cells in x direction*/
	public int numCellsY;
	
	public double cellWidth;
	public double cellHeight;
	
	public Grid(Simulation s) {
		
		this.s = s;
		
	}
	
	public void changeDimension(double width, double height, int xbox, int ybox) {
		
	}
	
	public void setGrid(double width, double height) {
		
	}
	
	public void updateGrid(ArrayList<Particle2D> particles) {
		
	}

}
