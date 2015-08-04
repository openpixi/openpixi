package org.openpixi.pixi.physics.gauge;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;

public class GaugeTransformation {

	private GaugeTransformationAction gaugeTransformationAction = new GaugeTransformationAction();

	/** Gauge transformation */
	private GroupElement[] g;

	/** Get gauge transformation array */
	public GroupElement[] getG() {
		return g;
	}

	/**
	 * Constructor. Obtain size of required grid from other grid.
	 * @param grid Grid that should be duplicated in size.
	 */
	public GaugeTransformation(Grid grid) {
		resetGaugeTransformation(grid);
	}

	/**
	 * Reset gauge transformation
	 * @param grid
	 */
	private void resetGaugeTransformation(Grid grid) {
		int numberOfCells = grid.getTotalNumberOfCells();

		int colors = grid.getNumberOfColors();
		if (colors == 2) {
			g = new SU2GroupElement[numberOfCells];
			for (int i = 0; i < g.length; i++) {
				g[i] = new SU2GroupElement(1, 0, 0, 0);
			}
		} else {
			System.out.println("Gauge transformation for SU(" + colors + ") not defined.\n");
		}
	}

	/**
	 * Apply the current gauge transformation to the grid
	 *
	 * @param grid
	 */
	public void applyGaugeTransformation(Grid grid) {
		grid.getCellIterator().execute(grid, gaugeTransformationAction);
	}

	private class GaugeTransformationAction implements CellAction {
		public void execute(Grid grid, int index) {
			for (int dir = 0; dir < grid.getNumberOfDimensions(); dir++) {
				/*
				 * U_i(x) -> g(x) U_i(x) g^\dagger(x+i)
				 */
				GroupElement U = grid.getU(index, dir);
				int shiftedCellIndex = grid.shift(index, dir, 1);
				GroupElement gdaggershifted = g[shiftedCellIndex].adj();
				U = g[index].mult(U).mult(gdaggershifted);
				grid.setU(index, dir, U);

				/*
				 * Unext_i(x) -> g(x) Unext_i(x) g^\dagger(x+i)
				 */
				GroupElement Unext = grid.getUnext(index, dir);
				Unext = g[index].mult(Unext).mult(gdaggershifted);
				grid.setUnext(index, dir, Unext);

				/*
				 * E_i(x) -> g(x) E_i(x) g^\dagger(x)
				 */
				AlgebraElement E = grid.getE(index, dir);
				GroupElement gdagger = g[index].adj();
				E = (g[index].mult(E.getLinearizedLink()).mult(gdagger)).proj();
				// TODO: rather work with exact mapping?
				//E = (g[cellIndex].mult(E.getLink()).mult(gdagger)).getAlgebraElement();
				grid.setE(index, dir, E);
			}
		}
	}

}