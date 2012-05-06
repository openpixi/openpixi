package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.grid.*;

public class SimpleSolver extends FieldSolver {
	
	double cx, cy, cz;
	
	public SimpleSolver() {
		
	}
	
	/**A simple LeapFrog algorithm
	 * @param p before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	public void step(Grid g) {
		
		for (int i = 1; i < g.numCellsX - 1; i++) {
			for (int j = 1; j < g.numCellsY - 1; j++) {
				
				/**curl of the E field using center difference*/
				cz = (g.Eyo[i+1][j] - g.Eyo[i-1][j]) / ( 2 * g.cellWidth) - 
						(g.Exo[i][j+1] - g.Exo[i][j-1]) / ( 2 * g.cellHeight);
				
				/**Maxwell equations*/
				g.Bz[i][j] += -g.simulation.tstep * cz;

				/**curl of the B field using center difference*/
				cx = (g.Bzo[i][j+1] - g.Bzo[i][j-1]) / ( 2 * g.cellHeight);
				cy = -(g.Bzo[i+1][j] - g.Bzo[i-1][j]) / ( 2 * g.cellWidth);
				
				/**Maxwell EQ*/
				g.Ex[i][j] += g.simulation.tstep * (cx - g.jx[i][j]);
				g.Ey[i][j] += g.simulation.tstep * (cy - g.jy[i][j]);
			}
		}
	}

}
