package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;

public class YeeGrid extends Grid {
	
	/**Creates a 10x10 Grid for a given simulation(area)
	 * and adds a SimpleGridForce to the forces list.
	 * @param s Simulation
	 */
	public YeeGrid(Simulation s) {
		
		super(s);

		numCellsX = 10;
		numCellsY = 10;
		cellWidth = s.width/numCellsX;
		cellHeight = s.height/numCellsY;
		
		fsolver = new YeeSolver();
		interp = new ChargeConservingAreaWeighting(this);
		SimpleGridForce force = new SimpleGridForce();
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
	@Override
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
	
	@Override
	public void setGrid(double width, double height)
	{
		cellWidth = width / numCellsX;
		cellHeight = height / numCellsY;
		
		for (Particle p: simulation.particles){
			//assuming rectangular particle shape i.e. area weighting
			p.chargedensity = p.charge / (cellWidth * cellHeight);
		}
		
		//include updateGrid() and the first calculation of Fields here
	}
	
	@Override
	public void updateGrid(ArrayList<Particle> particles) {
		
		reset();
		interp.interpolateToGrid(particles);
		save();
		fsolver.step(this);
		interp.interpolateToParticle(particles);
	}

}
