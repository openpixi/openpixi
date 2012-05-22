package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import junit.framework.TestCase;
import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.GridFactory;
import org.openpixi.pixi.ui.util.WriteFile;

public class PoissonSolverTest extends TestCase {

	public PoissonSolverTest(String testName){
		super(testName);
	}

	public void testPointcharge() {

		Simulation s = InitialConditions.initEmptySimulation();
		s.width = 100;
		s.height = 100;

		Grid g = GridFactory.createSimpleGrid(s, 10, 10, 1, 1);
		g.setRho(5,5, 1);

		PoissonSolver.solve2D(g);

		WriteFile file = new WriteFile();
		for (int x = 0; x < g.getNumCellsX(); x++) {
			for (int y = 0; y < g.getNumCellsY(); y++) {
				file.writeFile("potential", "C:\\out", g.getPhi(x, y) +"");
			}
		}
	}

	public void testFFT() {

		DoubleFFT_2D fft = new DoubleFFT_2D(10,10);
		double[][] field = new double[10][20];

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 20; j++) {
				field[i][j] = 0;
			}
		}

		fft.complexForward(field);

	}
}
