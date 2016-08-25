package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;

public class FastTYMSolver extends FieldSolver
{

	private double timeStep;
	private UpdateLinks linkUpdater = new UpdateLinks();
	private CombinedUpdate combinedUpdate = new CombinedUpdate();

	@Override
	public FieldSolver clone() {
		FastTYMSolver clone = new FastTYMSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.linkUpdater = linkUpdater;
		clone.combinedUpdate = combinedUpdate;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {
		combinedUpdate.at = timeStep;

		cellIterator.execute(grid, combinedUpdate);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		linkUpdater.at = timeStep;
		cellIterator.execute(grid, linkUpdater);
	}

	private class CombinedUpdate implements CellAction {

		private double at;

		/**
		 * Combined update of fields and links using the sum of staples.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				GroupElement V;
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					GroupElement temp = grid.getU(index, i).mult(grid.getStapleSum(index, i));
					grid.addE(index, i, temp.proj().mult(at)); // area factors already included in getStapleSum()
					grid.addE(index, i, grid.getJ(index, i).mult(-at));
					V = grid.getE(index, i).mult(-at).getLink();
					V.multAssign(grid.getU(index, i));
					grid.setUnext(index, i, V);
				}
			}
		}
	}

	private class UpdateLinks implements CellAction {
		private double at;

		/**
		 * Updates the links matrices in a given cell.
		 * @param grid	Reference to the grid
		 * @param index	Cell index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				GroupElement V;
				for (int k = 0; k < grid.getNumberOfDimensions(); k++) {
					V = grid.getE(index, k).mult(-at).getLink();
					V.multAssign(grid.getU(index, k));
					grid.setUnext(index, k, V);

				}
			}
		}
	}
}
