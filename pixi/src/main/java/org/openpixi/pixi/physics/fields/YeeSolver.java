package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.grid.Grid;

public class YeeSolver {

	double cx, cy, cz;
	
	public YeeSolver() {
		
	}
	
	/**A simple LeapFrog algorithm
	 * @param p before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	public void step(Grid g) {
		
		for (int i = 1; i < g.numCellsX; i++) {
			for (int j = 1; j < g.numCellsY; j++) {
				
				/**curl of the E field using center difference*/
				cz = (g.Ey[i+1][j] - g.Ey[i-1][j]) / ( 2 * g.cellWidth) - 
						(g.Ex[i][j+1] - g.Ex[i][j-1]) / ( 2 * g.cellHeight);
				
				/**Maxwell equations*/
				g.Bz[i][j] += -g.s.tstep * cz;

				/**curl of the B field using center difference*/
				cx = (g.Bz[i][j+1] - g.Bz[i][j-1]) / ( 2 * g.cellHeight);
				cy = -(g.Bz[i+1][j] - g.Bz[i-1][j]) / ( 2 * g.cellWidth);
				
				/**Maxwell EQ*/
				g.Ex[i][j] += g.s.tstep * (cx - g.jx[i][j]);
				g.Ey[i][j] += g.s.tstep * (cy - g.jy[i][j]);
			}
		}
	}
	
}
