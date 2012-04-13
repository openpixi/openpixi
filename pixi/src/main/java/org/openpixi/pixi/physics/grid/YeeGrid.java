package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.*;

public class YeeGrid extends Grid {
	
	public YeeGrid(Simulation s) {
		
		super(s);

		numCellsX = 10;
		numCellsY = 10;
		cellWidth = s.width/numCellsX;
		cellHeight = s.height/numCellsY;

		interp = new ChargeConservingAreaWeighting(this);

		jx = new double[numCellsX+2][numCellsY+2];
		jy = new double[numCellsX+2][numCellsY+2];
		rho = new double[numCellsX+2][numCellsY+2];
		Ex = new double[numCellsX+2][numCellsY+2];
		Ey = new double[numCellsX+2][numCellsY+2];
		Bz = new double[numCellsX+2][numCellsY+2];
		
	}
	
	public void updateGrid(ArrayList<Particle2D> particles) {
		
		interp.interpolateToGrid(particles);
		s.fsolver.step(this);
	}

}
