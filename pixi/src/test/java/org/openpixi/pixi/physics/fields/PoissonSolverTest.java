package org.openpixi.pixi.physics.fields;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.grid.*;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.ui.util.WriteFile;

import edu.emory.mathcs.jtransforms.fft.*;

public class PoissonSolverTest extends TestCase {
	
	public PoissonSolverTest(String testName){
		super(testName);
	}
	
	public void testPointcharge() {
		
		Simulation s = InitialConditions.initBasicSimulation();
		
		Grid g = new Grid(s);
		g.numCellsX = 10;
		g.numCellsY = 10;
		g.cellWidth = 1;
		g.cellHeight = 1;
		g.rho = new double[g.numCellsX][g.numCellsY];
		g.phi = new double[g.numCellsX][g.numCellsY];
		
		for (int i = 0; i < g.numCellsX; i++) {
			for (int j = 0; j < g.numCellsY; j++) {
				g.rho[i][j] = 0;
				g.phi[i][j] = 0;
			}
		}
		
		g.rho[5][5] = 1;
		
		PoissonSolver.solve2D(g);
		
		WriteFile file = new WriteFile();
		for (double[] x : g.phi) {
			for(double y : x) {
				file.writeFile("potential", "C:\\out", y +"");
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
