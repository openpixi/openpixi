package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;

public class ImplicitTYMSolver extends FieldSolver
{

	private double timeStep;
	private UpdateLinks linkUpdater = new UpdateLinks();
	private ImplicitBegin implicitBegin = new ImplicitBegin();
	private ImplicitStep implicitStep = new ImplicitStep();
	private ImplicitEnd implicitEnd = new ImplicitEnd();

	@Override
	public FieldSolver clone() {
		ImplicitTYMSolver clone = new ImplicitTYMSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.linkUpdater = linkUpdater;
		clone.implicitBegin = implicitBegin;
		clone.implicitEnd = implicitEnd;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {
		Grid implicitGrid = new Grid(grid); // Copy grid.

		implicitBegin.at = timeStep;
		implicitBegin.unitFactor = new double[grid.getNumberOfDimensions()];
		for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
			implicitBegin.unitFactor[i] =  - grid.getLatticeUnitFactor(i) * grid.getTemporalSpacing();
		}
		implicitBegin.implicitGrid = implicitGrid;
		cellIterator.execute(grid, implicitBegin);

		implicitStep.implicitGrid = implicitGrid;
		implicitStep.at = implicitBegin.at;
		implicitStep.unitFactor = implicitBegin.unitFactor;
		for (int i = 0; i < 10; i++) {
			cellIterator.execute(grid, implicitStep);
		}

		implicitEnd.implicitGrid = implicitGrid;
		cellIterator.execute(grid, implicitEnd);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		linkUpdater.at = timeStep;
		cellIterator.execute(grid, linkUpdater);
	}

	private class ImplicitBegin implements CellAction {

		private double at;
		private double[] unitFactor;
		private Grid implicitGrid;

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
					implicitGrid.addE(index, i, temp.proj().mult(at)); // area factors already included in getStapleSum()
					implicitGrid.addE(index, i, grid.getJ(index, i).mult(unitFactor[i]));
					V = implicitGrid.getE(index, i).mult(-at).getLink();
					V.multAssign(grid.getU(index, i));
					implicitGrid.setUnext(index, i, V);
				}
			}
		}
	}

	private class ImplicitStep implements CellAction {

		private double at;
		private double[] unitFactor;
		private int beamdirection = 0;

		private Grid implicitGrid;

		/**
		 * Combined update of fields and links using the sum of staples.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				GroupElement V;
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					// Start from previous E
					implicitGrid.setE(index, i, grid.getE(index,i));

					// add current non-transverse contributions:
					GroupElement temp = grid.getU(index, i).mult(grid.getTransverseStapleSum(index, i, beamdirection, false));
					implicitGrid.addE(index, i, temp.proj().mult(at)); // area factors already included in getStapleSum()

		//			// add current transverse contributions:
		//			temp = grid.getU(index, i).mult(grid.getTransverseStapleSum(index, i, beamdirection, true));
		//			implicitGrid.addE(index, i, temp.proj().mult(at * 0.5)); // area factors already included in getStapleSum()

					// add 1/2 of future transverse contributions:
					temp = implicitGrid.getU(index, i).mult(implicitGrid.getTransverseStapleSum(index, i, beamdirection, true));
					implicitGrid.addE(index, i, temp.proj().mult(at * 0.5)); // area factors already included in getStapleSum()

					// add 1/2 of past transverse contributions:
					// (Note that grid.Unext contains the old U previous to grid.E)
					temp = grid.getUnext(index, i).mult(grid.getTransverseStapleSumNext(index, i, beamdirection, true));
					implicitGrid.addE(index, i, temp.proj().mult(at * 0.5)); // area factors already included in getStapleSum()

					// add current:
					implicitGrid.addE(index, i, grid.getJ(index, i).mult(unitFactor[i]));
					V = implicitGrid.getE(index, i).mult(-at).getLink();
					V.multAssign(implicitGrid.getU(index, i));
					implicitGrid.setUnext(index, i, V);
				}
			}
		}
	}

	private class ImplicitEnd implements CellAction {

		private Grid implicitGrid;

		/**
		 * Combined update of fields and links using the sum of staples.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					grid.setE(index, i, implicitGrid.getE(index, i));
					grid.setUnext(index, i, implicitGrid.getUnext(index, i));
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
