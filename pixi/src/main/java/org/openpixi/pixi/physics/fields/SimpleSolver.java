package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.CurrentGrid;
import org.openpixi.pixi.physics.Simulation;

public class SimpleSolver extends FieldSolver {
	
	double cx, cy, cz;
	
	public SimpleSolver() {
		
	}
	
	/**A simple LeapFrog algorithm
	 * @param p before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	public void step(CurrentGrid g) {
		
		for (int i = 1; i < g.numCellsX-1; i++) {
			for (int j = 1; j < g.numCellsY-1; j++) {
				
				/**curl of the B field using center difference*/
				cx = (g.Bz[i][j+1]-2*g.Bz[i][j]+g.Bz[i][j-1])/(2*g.cellHeight);
				cy = -(g.Bz[i+1][j]-2*g.Bz[i][j]+g.Bz[i-1][j])/(2*g.cellWidth);
				cz = (g.By[i+1][j]-2*g.By[i][j]+g.By[i-1][j])/(2*g.cellWidth)-(g.Bx[i][j+1]-2*g.Bx[i][j]+g.Bx[i][j-1])/(2*g.cellHeight);
				
				/**Maxwell EQ*/
				g.Ex[i][j] += Simulation.tstep*(cx-g.jx[i][j]);
				g.Ey[i][j] += Simulation.tstep*(cy-g.jy[i][j]);
				g.Ez[i][j] += Simulation.tstep*(cz);
				
				/**curl of the E field using center difference*/
				cx = (g.Ez[i][j+1]-2*g.Ez[i][j]+g.Ez[i][j-1])/(2*g.cellHeight);
				cy = -(g.Ez[i+1][j]-2*g.Ez[i][j]+g.Ez[i-1][j])/(2*g.cellWidth);
				cz = (g.Ey[i+1][j]-2*g.Ey[i][j]+g.Ey[i-1][j])/(2*g.cellWidth)-(g.Ex[i][j+1]-2*g.Ex[i][j]+g.Ex[i][j-1])/(2*g.cellHeight);
				
				/**Maxwell EQ*/
				g.Bx[i][j] += -Simulation.tstep*cx;
				g.By[i][j] += -Simulation.tstep*cy;
				g.Bz[i][j] += -Simulation.tstep*cz;
			}
		}
		
	}

}
