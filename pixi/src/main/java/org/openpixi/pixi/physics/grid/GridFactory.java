package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;

/**
 * Creates specific types of grid.
 */
public class GridFactory {

	public static Grid createSimpleGrid(
			Simulation s,
			int numCellsX, int numCellsY, double simWidth, double simHeight) {

		return new Grid(s,
				numCellsX, numCellsY, simWidth, simHeight,
				GridBoundaryType.Hardwall,
				new SimpleSolver(),
				new CloudInCell(),
				new PoissonSolverFFTPeriodic());
	}

	public static Grid createYeeGrid(
			Simulation s,
			int numCellsX, int numCellsY, double simWidth, double simHeight) {

		return new Grid(s,
				numCellsX, numCellsY, simWidth, simHeight,
				GridBoundaryType.Hardwall,
				new YeeSolver(),
				new ChargeConservingAreaWeighting(),
				new PoissonSolverFFTPeriodic());
	}
}
