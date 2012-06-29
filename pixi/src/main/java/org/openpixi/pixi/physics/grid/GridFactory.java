package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;

/**
 * Creates specific types of grid.
 * TODO replace this class and initial conditions with Settings class which can be created
 * from file or command line
 */
public class GridFactory {

	public static Grid createSimpleGrid(Simulation s,
			int numCellsX, int numCellsY, double simWidth, double simHeight) {

		s.setInterpolator(new CloudInCell());
		Grid grid =  new Grid(
				numCellsX, numCellsY, simWidth, simHeight,
				GridBoundaryType.Hardwall,
				new SimpleSolver(),
				new PoissonSolverFFTPeriodic());
		s.setGrid(grid);
		return grid;
	}

	public static Grid createYeeGrid(Simulation s,
			int numCellsX, int numCellsY, double simWidth, double simHeight) {

		s.setInterpolator(new ChargeConservingAreaWeighting());
		Grid grid = new Grid(
				numCellsX, numCellsY, simWidth, simHeight,
				GridBoundaryType.Hardwall,
				new YeeSolver(),
				new PoissonSolverFFTPeriodic());
		s.setGrid(grid);
		return grid;
	}
}
