package org.openpixi.pixi.physics.gauge;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.SU2Matrix;
import org.openpixi.pixi.physics.grid.YMField;

public class GaugeTransformation {

	GaugeTransform gaugeTransform = new GaugeTransform();

	public Grid gaugedGrid;

	/** Gauge transformation */
	public LinkMatrix[] g;


	/**
	 * Constructor. Obtain size of required grid from other grid.
	 * @param grid Grid that should be duplicated in size.
	 */
	public GaugeTransformation(Grid grid) {
		gaugedGrid = new Grid(grid);
	}

	/**
	 * Copy grid and reset gauge transformation
	 */
	public void copyGrid(Grid grid) {


		// Copy grid
		int numberOfCells = grid.getNumberOfCells();

		int colors = grid.getNumberOfColors();
		if (colors != 2) {System.out.println("Coulomb gauge for SU(" + colors + ") not defined.\n");
			return;
		}

		g = new SU2Matrix[numberOfCells];

		// Reset the gauge transformation
		for (int i = 0; i < g.length; i++) {
			g[i] = new SU2Matrix(1, 0, 0, 0);
		}

		/*
			Copy the U-field.
		 */
		for (int ci = 0; ci < numberOfCells; ci++) {
			int[] cellPosition = grid.getCellPos(ci);
			for (int d = 0; d < grid.getNumberOfDimensions(); d++) {
				LinkMatrix U = grid.getU(cellPosition, d);
				gaugedGrid.setU(cellPosition, d, U);
				YMField E = grid.getE(cellPosition, d);
				gaugedGrid.setE(cellPosition, d, E);
			}
		}
	}

	public void applyGaugeTransformation() {
		gaugedGrid.getCellIterator().execute(gaugedGrid, gaugeTransform);
	}

	private class GaugeTransform implements CellAction {
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