package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

public class SimpleSolver extends FieldSolver {

	private double timeStep;
	private Solve solve = new Solve();


	/**A simple LeapFrog algorithm
	 * @param grid before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	@Override
	public void step(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		cellIterator.execute(grid, solve);
	}


	private class Solve implements CellAction {

		public void execute(Cell cell) {
			throw new UnsupportedOperationException();
		}

		public void execute(Grid grid, int x, int y) {
			/**curl of the E field using center difference*/
			double cz = (grid.getEyo(x+1, y) - grid.getEyo(x-1, y)) / ( 2 * grid.getCellWidth()) -
					(grid.getExo(x, y+1) - grid.getExo(x, y-1)) / ( 2 * grid.getCellHeight());

			/**Maxwell equations*/
			grid.addBz(x, y, -timeStep * cz);

			/**curl of the B field using center difference*/
			double cx = (grid.getBzo(x, y+1) - grid.getBzo(x, y-1)) / ( 2 * grid.getCellHeight());
			double cy = -(grid.getBzo(x+1, y) - grid.getBzo(x-1, y)) / ( 2 * grid.getCellWidth());

			/**Maxwell EQ*/
			grid.addEx(x, y, timeStep * (cx - grid.getJx(x, y)));
			grid.addEy(x, y, timeStep * (cy - grid.getJy(x, y)));
		}
	}

}
