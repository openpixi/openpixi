package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;

public class YeeGrid extends Grid {
	
	public YeeGrid(Simulation s) {
		
		super(s);

		numCellsX = 10;
		numCellsY = 10;
		cellWidth = s.width/numCellsX;
		cellHeight = s.height/numCellsY;
		
		fsolver = new YeeSolver();
		interp = new ChargeConservingAreaWeighting(this);
		SimpleGridForce force = new SimpleGridForce(s);
		s.f.add(force);

		jx = new double[numCellsX][numCellsY];
		jy = new double[numCellsX][numCellsY];
		rho = new double[numCellsX][numCellsY];
		Ex = new double[numCellsX][numCellsY];
		Ey = new double[numCellsX][numCellsY];
		Bz = new double[numCellsX][numCellsY];
		Exo = new double[numCellsX][numCellsY];
		Eyo = new double[numCellsX][numCellsY];
		Bzo = new double[numCellsX][numCellsY];
		initFields();
		
	}
	
	//a method to change the dimensions of the cells, i.e. the width and the height
	public void changeDimension(double width, double height, int xbox, int ybox)
	{
		numCellsX = xbox;
		numCellsY = ybox;
		
		jx = new double[numCellsX][numCellsY];
		jy = new double[numCellsX][numCellsY];
		rho = new double[numCellsX][numCellsY];
		Ex = new double[numCellsX][numCellsY];
		Ey = new double[numCellsX][numCellsY];
		Bz = new double[numCellsX][numCellsY];
		Exo = new double[numCellsX][numCellsY];
		Eyo = new double[numCellsX][numCellsY];
		Bzo = new double[numCellsX][numCellsY];
		initFields();
		
		setGrid(width, height);
	}
	
	public void setGrid(double width, double height)
	{
		cellWidth = width / numCellsX;
		cellHeight = height / numCellsY;
		
		for (Particle2D p: simulation.particles){
			//assuming rectangular particle shape i.e. area weighting
			p.data.chargedensity = p.charge / (cellWidth * cellHeight);
		}
		
		//include updateGrid() and the first calculation of Fields here
	}
	
	public void updateGrid(ArrayList<Particle2D> particles) {
		
		reset();
		interp.interpolateToGrid(particles);
		save();
		fsolver.step(this);
		interp.interpolateToParticle(particles);
	}

}
