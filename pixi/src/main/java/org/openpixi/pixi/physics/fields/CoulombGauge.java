package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.SU2Matrix;
import org.openpixi.pixi.physics.grid.YMField;

public class CoulombGauge {

	GaugeTransform gaugeTransform = new GaugeTransform();

	Grid gaugedGrid;

	/** Gauge transformation */
	private LinkMatrix[] g;

	public CoulombGauge (Grid grid) {
		gaugedGrid = new Grid(grid);
	}

	public void fixGauge(Grid grid) {

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

		/*
			Cycle through each cell and apply the gauge transformation
		 */
		gaugedGrid.getCellIterator().execute(gaugedGrid, gaugeTransform);

	}

	public Grid getGaugedGrid() {
		return gaugedGrid;
	}

	private class GaugeTransform implements CellAction {
		public void execute(Grid grid, int[] coor) {
			int cellIndex = grid.getCellIndex(coor);
			for (int d = 0; d < grid.getNumberOfDimensions(); d++) {
				// TODO: replace dummy transformation
				LinkMatrix U = grid.getU(coor, d);
				U = U.mult(g[cellIndex]);
				grid.setU(coor, d, U);
				YMField E = grid.getE(coor, d);
				E = (E.getLink().mult(g[cellIndex])).getLinearizedAlgebraElement();
				grid.setE(coor, d, E);
			}
		}
	}
}
