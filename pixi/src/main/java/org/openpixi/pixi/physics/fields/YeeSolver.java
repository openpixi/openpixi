package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.grid.Grid;

/**
 * TODO remake so that we can use the super class cell iterator and take advantage of threads
 */
public class YeeSolver extends FieldSolver {

	public YeeSolver() {

	}

	/**A simple LeapFrog algorithm
	 * @param g before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	@Override
	public void step(Grid g, double timeStep) {

		double cx, cy, cz;

		//update boundary for y=const
		for (int i = 0; i < g.getNumCellsX(); i++) {

			int ip = i + 1;
			int im = i - 1;

			if (ip >= g.getNumCellsX()){
				ip = 0;
			}
			if (im < 0) {
				im = g.getNumCellsX()-1;
			}

			//lower border
			/**curl of the E field using center difference*/
			cz = (g.getEyo(ip, 0) - g.getEyo(im, 0)) / ( 2 * g.getCellWidth()) -
					(g.getExo(i, 1) - g.getExo(i, g.getNumCellsY()-1)) / ( 2 * g.getCellHeight());

			/**Maxwell EQ*/
			g.addBz(i, 0, -timeStep * cz);

			/**curl of the B field using center difference*/
			cx = (g.getBzo(i, 1) - g.getBzo(i, g.getNumCellsY()-1)) / ( 2 * g.getCellHeight());
			cy = -(g.getBzo(ip, 0) - g.getBzo(im, 0)) / ( 2 * g.getCellWidth());

			/**Maxwell EQ*/
			g.addEx(i, 0, timeStep * (cx - g.getJx(i,0)));
			g.addEy(i, 0, timeStep * (cy - g.getJy(i,0)));

			//upper border
			/**curl of the E field using center difference*/
			cz = (g.getEyo(ip, g.getNumCellsY()-1) - g.getEyo(im, g.getNumCellsY()-1)) / ( 2 * g.getCellWidth()) -
					(g.getExo(i, 0) - g.getExo(i, g.getNumCellsY()-2)) / ( 2 * g.getCellHeight());

			/**Maxwell EQ*/
			g.addBz(i, g.getNumCellsY()-1, -timeStep * cz);

			/**curl of the B field using center difference*/
			cx = (g.getBzo(i, 0) - g.getBzo(i, g.getNumCellsY()-2)) / ( 2 * g.getCellHeight());
			cy = -(g.getBzo(ip, g.getNumCellsY()-1) - g.getBzo(im, g.getNumCellsY()-1)) / ( 2 * g.getCellWidth());

			/**Maxwell EQ*/
			g.addEx(i, g.getNumCellsY()-1, timeStep * (cx - g.getJx(i, g.getNumCellsY()-1)));
			g.addEy(i, g.getNumCellsY()-1, timeStep * (cy - g.getJy(i, g.getNumCellsY()-1)));

		}

		//update boundary for x=const
		for (int j = 1; j < g.getNumCellsY() - 1; j++) {

			//left border
			/**curl of the E field using center difference*/
			cz = (g.getEyo(1, j) - g.getEyo(g.getNumCellsX()-1, j)) / ( 2 * g.getCellWidth()) -
					(g.getExo(0, j+1) - g.getExo(0, j-1)) / ( 2 * g.getCellHeight());

			/**Maxwell EQ*/
			g.addBz(0, j, -timeStep * cz);

			/**curl of the B field using center difference*/
			cx = (g.getBzo(0, j+1) - g.getBzo(0, j-1)) / ( 2 * g.getCellHeight());
			cy = -(g.getBzo(1, j) - g.getBzo(g.getNumCellsX()-1, j)) / ( 2 * g.getCellWidth());

			/**Maxwell EQ*/
			g.addEx(0,j, timeStep * (cx - g.getJx(0, j)));
			g.addEy(0,j, timeStep * (cy - g.getJy(0, j)));

			//right border
			/**curl of the E field using center difference*/
			cz = (g.getEyo(0, j) - g.getEyo(g.getNumCellsX()-2, j)) / ( 2 * g.getCellWidth()) -
					(g.getExo(g.getNumCellsX()-1, j+1) - g.getExo(g.getNumCellsX()-1, j-1)) / ( 2 * g.getCellHeight());

			/**Maxwell EQ*/
			g.addBz(g.getNumCellsX()-1, j, -timeStep * cz);

			/**curl of the B field using center difference*/
			cx = (g.getBzo(g.getNumCellsX()-1, j+1) - g.getBzo(g.getNumCellsX()-1, j-1)) / ( 2 * g.getCellHeight());
			cy = -(g.getBzo(0, j) - g.getBzo(g.getNumCellsX()-2, j)) / ( 2 * g.getCellWidth());

			/**Maxwell EQ*/
			g.addEx(g.getNumCellsX()-1, j, timeStep * (cx - g.getJx(g.getNumCellsX()-1, j)));
			g.addEy(g.getNumCellsX()-1, j, timeStep * (cy - g.getJy(g.getNumCellsX()-1, j)));

		}


		for (int i = 1; i < g.getNumCellsX() - 1; i++) {
			for (int j = 1; j < g.getNumCellsY() - 1; j++) {

				/**curl of the E field using center difference*/
				cz = (g.getEyo(i+1, j) - g.getEyo(i-1, j)) / ( 2 * g.getCellWidth()) -
						(g.getExo(i, j+1) - g.getExo(i, j-1)) / ( 2 * g.getCellHeight());

				/**Maxwell EQ*/
				g.addBz(i, j, -timeStep * cz);

				/**curl of the B field using center difference*/
				cx = (g.getBzo(i, j+1) - g.getBzo(i, j-1)) / ( 2 * g.getCellHeight());
				cy = -(g.getBzo(i+1, j) - g.getBzo(i-1, j)) / ( 2 * g.getCellWidth());

				/**Maxwell EQ*/
				g.addEx(i, j, timeStep * (cx - g.getJx(i, j)));
				g.addEy(i, j, timeStep * (cy - g.getJy(i, j)));
			}
		}
	}

}
