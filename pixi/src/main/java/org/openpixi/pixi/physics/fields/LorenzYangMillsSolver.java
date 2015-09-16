package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;

public class LorenzYangMillsSolver extends FieldSolver
{

	private double timeStep;
	private SolveEOM fieldUpdater = new SolveEOM();
	private SolveGaugeCondition gaugeSolver = new SolveGaugeCondition();
	private ComputeElectricFields electricFieldSolver = new ComputeElectricFields();

	@Override
	public FieldSolver clone() {
		LorenzYangMillsSolver clone = new LorenzYangMillsSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.fieldUpdater = fieldUpdater;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {

		/*
			Important note: When the field solver is called, storeFields() has been called once. For the case of the
			LorenzYangMillsSolver this means that past links are saved in Unext and present links are saved in U. This
			creates some slight inconsistency with the notes written for this solver.
		 */
		this.timeStep = timeStep;
		fieldUpdater.at = timeStep;
		fieldUpdater.as = grid.getLatticeSpacing();
		fieldUpdater.g = grid.getGaugeCoupling();
		fieldUpdater.ts = Math.pow(timeStep / grid.getLatticeSpacing(), 2);

		gaugeSolver.ts = fieldUpdater.ts;

		cellIterator.execute(grid, fieldUpdater);
		cellIterator.execute(grid, gaugeSolver);
		cellIterator.execute(grid, electricFieldSolver);

	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		cellIterator.execute(grid, electricFieldSolver);
	}


	private class SolveEOM implements CellAction
	{
		private double as;
		private double at;
		private double g;
		private double ts;

		public void execute(Grid grid, int index) {
			for(int i = 0; i < grid.getNumberOfDimensions(); i++)
			{
				AlgebraElement result = grid.getElementFactory().algebraZero();

				int shiftedIndex = grid.shift(index, i, 1);

				// Contribution from past temporal plaquette -P^a( U_{x, i -0})
				GroupElement tempPlaq = grid.getU(index, i).mult(grid.getU0next(shiftedIndex).adj()).mult(grid.getUnext(index, i).adj()).mult(grid.getU0next(index));
				result.addAssign(tempPlaq.proj().mult(-1.0));

				// Contribution from present spatial plaquettes
				GroupElement spatialPlaquettes = grid.getElementFactory().groupZero();
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					if(j != i) {
						spatialPlaquettes.addAssign(grid.getPlaquette(index, i, j, 1, 1, 0));
						spatialPlaquettes.addAssign(grid.getPlaquette(index, i, j, 1, -1, 0));
					}
				}
				result.addAssign(spatialPlaquettes.proj().mult(ts));

				// Contribution from current
				result.addAssign(grid.getJ(index, i).mult(- at * at));

				// Exponentiate to obtain next temporal plaquette U_{x,i0}.
				GroupElement newTempPlaquette = result.getLink().adj();

				// Compute next spatial link from temporal plaquette and other links. Set it correctly on the grid (overwrite).
				grid.setUnext(index, i, grid.getU0(index).adj().mult(newTempPlaquette).mult(grid.getU(index, i)).mult(grid.getU0(shiftedIndex)));
			}
		}
	}

	private class SolveGaugeCondition implements CellAction
	{
		private double ts;

		public void execute(Grid grid, int index) {
			AlgebraElement result = grid.getElementFactory().algebraZero();
			result.addAssign( grid.getU0next(index).proj() );
			GroupElement spatialLinks = grid.getElementFactory().groupZero();
			for(int i = 0; i < grid.getNumberOfDimensions(); i++) {
				/*
				// second order central finite difference
				int index1 = index;
				int index2 = grid.shift(index, i, -1);
				spatialLinks.addAssign(grid.getU(index1, i));
				spatialLinks.addAssign(grid.getU(index2, i).mult(-1.0));
				 */

				// fourth order central finite difference
				int index1 = index;
				int index2 = grid.shift(index, i, -1);
				int index3 = grid.shift(index, i, 1);
				int index4 = grid.shift(index2, i, -1);
				spatialLinks.addAssign(grid.getU(index1, i).mult(-9.0/8.0));
				spatialLinks.addAssign(grid.getU(index2, i).mult(+9.0/8.0));
				spatialLinks.addAssign(grid.getU(index3, i).mult(+1.0/24.0));
				spatialLinks.addAssign(grid.getU(index4, i).mult(-1.0/24.0));
			}
			result.addAssign(spatialLinks.proj().mult(-ts));
			// Exponentiate to find the next temporal gauge link
			grid.setU0next(index, result.getLink());
		}
	}

	private class ComputeElectricFields implements CellAction
	{
		public void execute(Grid grid, int index) {
			for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
				grid.setE(index, i, grid.getEFromLinks(index, i));
			}
		}
	}

}
