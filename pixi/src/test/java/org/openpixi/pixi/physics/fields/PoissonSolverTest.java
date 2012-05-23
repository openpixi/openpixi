package org.openpixi.pixi.physics.fields;

import junit.framework.TestCase;
import java.io.File;
import org.openpixi.pixi.physics.fields.PoissonSolverPeriodic;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.GridFactory;
import org.openpixi.pixi.ui.util.WriteFile;

public class PoissonSolverTest extends TestCase {

	private double ACCURACY_LIMIT = 1.e-5;
	private Simulation s;
	private Grid g;
	private PoissonSolverPeriodic poisolver;

	public PoissonSolverTest(String testName){
		super(testName);
		
		this.s = InitialConditions.initEmptySimulation();
		s.width = 10;
		s.height = 10;
		
		this.g = GridFactory.createYeeGrid(s, 10, 10, s.width, s.height);
		g.resetCurrentAndCharge();
		
		this.poisolver = new PoissonSolverPeriodic();
	}

	private void PointchargeTest() {	
		
		//writes to g.rho
		ChargeDistribution charged = new PointChargeDistribution(g);
		
		long start = System.currentTimeMillis();
		poisolver.solve(g);
		
		long elapsed = System.currentTimeMillis()-start;
		System.out.println("\nCalculation time: "+elapsed);
//		output(g);
		
		int indexX = (int)(g.getNumCellsX()/2);
		int indexY = (int) (g.getNumCellsY()/2);
//		assertAlmostEquals("", (g.Ey[indexX][indexY+1]/16), g.Ey[indexX][indexY+4] , ACCURACY_LIMIT);
		charged.test();		
	}
	
	private double[][] pointCharge(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		int indexX = (int)(numCellsX/2);
		int indexY = (int) (numCellsY/2);
		rho[indexX][indexY] = 50;
		return rho;
	}
	
	public interface ChargeDistribution {			
		/**Tests PoisonSolver and the analytic results*/
		void test();
	}
	
	public class PointChargeDistribution implements ChargeDistribution {
		
		Grid g;
		int indexX;
		int indexY;
		
		PointChargeDistribution(Grid g) {
			this.g = g;
			int indexX = (int)(g.getNumCellsX()/2);
			int indexY = (int) (g.getNumCellsY()/2);
			g.setRho(indexX, indexY, 1);
		}
		
		public void test() {
			
			assertAlmostEquals("", g.getPhi(indexX, indexY)/(g.getCellWidth()), g.getPhi(indexX+1, indexY) , ACCURACY_LIMIT);
		}
		
	}
	
	private double[][] randomChargeDistribution(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		double charge = 1;
		if (Math.random() < 0.5) {
			charge = -charge;
		}
		for(int i = 0; i < numCellsX; i++) {
			for(int j = 0; j < numCellsY; j++) {
				rho[i][j] = charge * Math.random();
			}
		}
		
		return rho;
	}
	
	private double[][] pointChargeShifted(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		int indexX = (int)(numCellsX/2);
		int indexY = (int) (numCellsY/2);
		rho[indexX][indexY] = 1;
		rho[indexX+1][indexY] = 1;
		rho[indexX][indexY+1] = 1;
		rho[indexX+1][indexY+1] = 1;
		return rho;
	}
	
	private double[][] dipole(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		int indexX = (int)(numCellsX/2);
		int indexY = (int) (numCellsY/2);
		rho[indexX-2][indexY] = 10;
		rho[indexX+2][indexY] = -10;
		return rho;
	}
	
	private double[][] lineChargeOnEdge(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		double charge = 1;
		for(int i = 0; i < numCellsX; i++) {
				rho[i][0] = charge;
				rho[i][numCellsY-1] = charge;				
		}
		
		for(int i = 0; i < numCellsY; i++) {
			rho[0][i] = charge;
			rho[numCellsX-1][i] = charge;				
		}
		return rho;
	}
	
	private double[][] lineChargeOnSide(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		double charge = 1;		
		for(int i = 0; i < numCellsY; i++) {
				rho[0][i] = charge;			
		}
		return rho;
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
	
	void assertAlmostEquals(String text, double x, double y, double limit) {
		if ((Math.abs(x - y) / Math.abs(x + y) > limit)
				|| (Double.isNaN(x) != Double.isNaN(y))
				|| (Double.isInfinite(x) != Double.isInfinite(y))) {
			assertTrue(text + " expected:<" + x + "> but was:<" + y + ">", false);
		}
	}
}
