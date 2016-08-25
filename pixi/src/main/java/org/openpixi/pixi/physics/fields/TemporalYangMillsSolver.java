package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.math.GroupElement;

public class TemporalYangMillsSolver extends FieldSolver
{

	private double timeStep;
	private UpdateFields fieldUpdater = new UpdateFields();
	private UpdateLinks linkUpdater = new UpdateLinks();

	@Override
	public FieldSolver clone() {
		TemporalYangMillsSolver clone = new TemporalYangMillsSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.fieldUpdater = fieldUpdater;
		clone.linkUpdater = linkUpdater;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {
		this.timeStep = timeStep;

		// fieldUpdater.plaquetteFactor includes are of a plaquette which depends on two indices.
		fieldUpdater.plaquetteFactor = new double[grid.getNumberOfDimensions()];
		fieldUpdater.unitFactor = new double[grid.getNumberOfDimensions()];
		for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
			fieldUpdater.plaquetteFactor[j] = timeStep / Math.pow(grid.getLatticeSpacing(j), 2);
			fieldUpdater.unitFactor[j] = - grid.getTemporalSpacing() * grid.getLatticeUnitFactor(j);
		}

		linkUpdater.at = timeStep;
		cellIterator.execute(grid, fieldUpdater);
		cellIterator.execute(grid, linkUpdater);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		linkUpdater.at = timeStep;
		cellIterator.execute(grid, linkUpdater);
	}


	private class UpdateFields implements CellAction
	{
		private double[] plaquetteFactor;
		private double[] unitFactor;

		/**
		 * Updates the electric fields at a given coordinate
		 *
		 * @param index  Lattice index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					GroupElement temp = grid.getElementFactory().groupZero();
					for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
						if (j != i) {
							temp.addAssign(grid.getPlaquette(index, i, j, 1, 1, 0).mult(plaquetteFactor[j]));
							temp.addAssign(grid.getPlaquette(index, i, j, 1, -1, 0).mult(plaquetteFactor[j]));
						}
					}
					grid.addE(index, i, temp.proj());
					grid.addE(index, i, grid.getJ(index, i).mult(unitFactor[i]));
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
