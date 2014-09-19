package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

public class SimpleSolver extends FieldSolver {

	private double timeStep;
	private Solve solve = new Solve();
        
        private double mue0 = 0.05;
        private double eps0 = 10.0;


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
			/**curl of the E field using forward difference since the B field is located in the center
                         of the grid and the E-field is located at the edges*/
			double cz = (grid.getEy(x+1, y) - grid.getEy(x, y)) / (grid.getCellWidth()) -
					(grid.getEx(x, y+1) - grid.getEx(x, y)) / (grid.getCellHeight());

			/**Maxwell equations*/
			grid.addBz(x, y, -timeStep * cz);

			/**curl of the B field using center difference*/
			double cx = (grid.getBz(x, y) - grid.getBz(x, y-1)) / (grid.getCellHeight());
			double cy = -(grid.getBz(x, y) - grid.getBz(x-1, y)) / (grid.getCellWidth());

			/**Maxwell EQ*/
			grid.addEx(x, y, timeStep * (cx/(mue0*eps0) - grid.getJx(x, y)/eps0));
			grid.addEy(x, y, timeStep * (cy/(mue0*eps0) - grid.getJy(x, y)/eps0));
		}
	}

}
