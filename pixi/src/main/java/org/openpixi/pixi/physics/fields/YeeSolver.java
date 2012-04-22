package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.grid.Grid;

public class YeeSolver extends FieldSolver {

	double cx, cy, cz;
	
	public YeeSolver() {
		
	}
	
	/**A simple LeapFrog algorithm
	 * @param p before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	public void step(Grid g) {
		
		//update boundary for y=const
		for (int i = 0; i < g.numCellsX; i++) {
			
			int ip = i + 1;
			int im = i - 1;
			
			if (ip >= g.numCellsX){
				ip = 0;
			}
			if (im < 0) {
				im = g.numCellsX-1;
			}
			
			//lower border
			/**curl of the E field using center difference*/
			cz = (g.Eyo[ip][0] - g.Eyo[im][0]) / ( 2 * g.cellWidth) - 
					(g.Exo[i][1] - g.Exo[i][g.numCellsY-1]) / ( 2 * g.cellHeight);
			
			/**Maxwell EQ*/
			g.Bz[i][0] += -g.s.tstep * cz;

			/**curl of the B field using center difference*/
			cx = (g.Bzo[i][1] - g.Bzo[i][g.numCellsY-1]) / ( 2 * g.cellHeight);
			cy = -(g.Bzo[ip][0] - g.Bzo[im][0]) / ( 2 * g.cellWidth);
			
			/**Maxwell EQ*/
			g.Ex[i][0] += g.s.tstep * (cx - g.jx[i][0]);
			g.Ey[i][0] += g.s.tstep * (cy - g.jy[i][0]);
			
			//upper border
			/**curl of the E field using center difference*/
			cz = (g.Eyo[ip][g.numCellsY-1] - g.Eyo[im][g.numCellsY-1]) / ( 2 * g.cellWidth) - 
					(g.Exo[i][0] - g.Exo[i][g.numCellsY-2]) / ( 2 * g.cellHeight);
			
			/**Maxwell EQ*/
			g.Bz[i][g.numCellsY-1] += -g.s.tstep * cz;

			/**curl of the B field using center difference*/
			cx = (g.Bzo[i][0] - g.Bzo[i][g.numCellsY-2]) / ( 2 * g.cellHeight);
			cy = -(g.Bzo[ip][g.numCellsY-1] - g.Bzo[im][g.numCellsY-1]) / ( 2 * g.cellWidth);
			
			/**Maxwell EQ*/
			g.Ex[i][g.numCellsY-1] += g.s.tstep * (cx - g.jx[i][g.numCellsY-1]);
			g.Ey[i][g.numCellsY-1] += g.s.tstep * (cy - g.jy[i][g.numCellsY-1]);
			
		}
		
		//update boundary for x=const
		for (int j = 1; j < g.numCellsY - 1; j++) {
			
			//left border
			/**curl of the E field using center difference*/
			cz = (g.Eyo[1][j] - g.Eyo[g.numCellsX-1][j]) / ( 2 * g.cellWidth) - 
					(g.Exo[0][j+1] - g.Exo[0][j-1]) / ( 2 * g.cellHeight);
			
			/**Maxwell EQ*/
			g.Bz[0][j] += -g.s.tstep * cz;

			/**curl of the B field using center difference*/
			cx = (g.Bzo[0][j+1] - g.Bzo[0][j-1]) / ( 2 * g.cellHeight);
			cy = -(g.Bzo[1][j] - g.Bzo[g.numCellsX-1][j]) / ( 2 * g.cellWidth);
			
			/**Maxwell EQ*/
			g.Ex[0][j] += g.s.tstep * (cx - g.jx[0][j]);
			g.Ey[0][j] += g.s.tstep * (cy - g.jy[0][j]);
			
			//right border
			/**curl of the E field using center difference*/
			cz = (g.Eyo[0][j] - g.Eyo[g.numCellsX-2][j]) / ( 2 * g.cellWidth) - 
					(g.Exo[g.numCellsX-1][j+1] - g.Exo[g.numCellsX-1][j-1]) / ( 2 * g.cellHeight);
			
			/**Maxwell EQ*/
			g.Bz[g.numCellsX-1][j] += -g.s.tstep * cz;

			/**curl of the B field using center difference*/
			cx = (g.Bzo[g.numCellsX-1][j+1] - g.Bzo[g.numCellsX-1][j-1]) / ( 2 * g.cellHeight);
			cy = -(g.Bzo[0][j] - g.Bzo[g.numCellsX-2][j]) / ( 2 * g.cellWidth);
			
			/**Maxwell EQ*/
			g.Ex[g.numCellsX-1][j] += g.s.tstep * (cx - g.jx[g.numCellsX-1][j]);
			g.Ey[g.numCellsX-1][j] += g.s.tstep * (cy - g.jy[g.numCellsX-1][j]);
			
		}
		
		
		for (int i = 1; i < g.numCellsX - 1; i++) {
			for (int j = 1; j < g.numCellsY - 1; j++) {
						
				/**curl of the E field using center difference*/
				cz = (g.Eyo[i+1][j] - g.Eyo[i-1][j]) / ( 2 * g.cellWidth) - 
						(g.Exo[i][j+1] - g.Exo[i][j-1]) / ( 2 * g.cellHeight);
				
				/**Maxwell EQ*/
				g.Bz[i][j] += -g.s.tstep * cz;

				/**curl of the B field using center difference*/
				cx = (g.Bzo[i][j+1] - g.Bzo[i][j-1]) / ( 2 * g.cellHeight);
				cy = -(g.Bzo[i+1][j] - g.Bzo[i-1][j]) / ( 2 * g.cellWidth);
				
				/**Maxwell EQ*/
				g.Ex[i][j] += g.s.tstep * (cx - g.jx[i][j]);
				g.Ey[i][j] += g.s.tstep * (cy - g.jy[i][j]);
			}
		}
	}
	
}
