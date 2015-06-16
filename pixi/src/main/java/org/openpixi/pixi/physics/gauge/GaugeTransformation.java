package org.openpixi.pixi.physics.gauge;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.SU2Matrix;
import org.openpixi.pixi.physics.grid.YMField;

public class GaugeTransformation {

	private GaugeTransformationAction gaugeTransformationAction = new GaugeTransformationAction();

	/** Gauge transformation */
	private LinkMatrix[] g;

	/** Get gauge transformation array */
	public LinkMatrix[] getG() {
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
		int numberOfCells = grid.getNumberOfCells();

		int colors = grid.getNumberOfColors();
		if (colors == 2) {
			g = new SU2Matrix[numberOfCells];
			for (int i = 0; i < g.length; i++) {
				g[i] = new SU2Matrix(1, 0, 0, 0);
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
		public void execute(Grid grid, int[] coor) {
			int cellIndex = grid.getCellIndex(coor);
			for (int dir = 0; dir < grid.getNumberOfDimensions(); dir++) {
				/*
				 * U_i(x) -> g(x) U_i(x) g^\dagger(x+i)
				 */
				LinkMatrix U = grid.getU(coor, dir);
				int shiftedCellIndex = grid.getCellIndex(grid.shift(coor, dir, 1));
				LinkMatrix gdaggershifted = g[shiftedCellIndex].adj();
				U = g[cellIndex].mult(U).mult(gdaggershifted);
				grid.setU(coor, dir, U);

				/*
				 * E_i(x) -> g(x) E_i(x) g^\dagger(x)
				 */
				YMField E = grid.getE(coor, dir);
				LinkMatrix gdagger = g[cellIndex].adj();
				E = (g[cellIndex].mult(E.getLink()).mult(gdagger)).getLinearizedAlgebraElement();
				// TODO: rather work with exact mapping?
				//E = (g[cellIndex].mult(E.getLinkExact()).mult(gdagger)).getAlgebraElement();
				grid.setE(coor, dir, E);
			}
		}
	}

}