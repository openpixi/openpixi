package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.fields.YeeSolver;

/**
 * Creates specific types of grid.
 */
public class GridFactory {

	public static Grid createSimpleGrid(
			Simulation s,
			int numCellsX, int numCellsY, double simWidth, double simHeight) {

		return new Grid(s,
				numCellsX, numCellsY, simWidth, simHeight,
				new SimpleSolver(),
				new CloudInCell());
	}

	public static Grid createYeeGrid(
			Simulation s,
			int numCellsX, int numCellsY, double simWidth, double simHeight) {

		return new Grid(s,
				numCellsX, numCellsY, simWidth, simHeight,
				new YeeSolver(),
				new ChargeConservingAreaWeighting());
	}
}
