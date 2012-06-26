package org.openpixi.pixi.physics.fields;

import junit.framework.TestCase;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.GridFactory;

public class PoissonSolverTest extends TestCase {

	private double ACCURACY_LIMIT = 1.e-5;
	private Simulation s;
	private Grid g;
	private PoissonSolver poisolver;

	/**Tests whether the Poisson Euation is solved correctly for
	 * different charge distributions by comparing the output of
	 * the PoissonSolver to the analytic result.
	 */
	public PoissonSolverTest(String testName){
		super(testName);
		
		this.s = InitialConditions.initEmptySimulation();
		this.s.width = 10;
		this.s.height = 10;
		
		this.g = GridFactory.createYeeGrid(s, 10, 10, s.width, s.height);
		this.g.resetCurrentAndCharge();
		
		this.poisolver = new PoissonSolverFFTPeriodic();
	}

	public void testPointchargeDistribution() {	
		//saves charge distribution in g.rho, includes specific test
		//for this charge distribution
		ChargeDistribution charged = new PointChargeDistributionCenter(g.getNumCellsX(), g.getNumCellsY());
		//solves poisson equation
		poisolver.solve(g);
		//compares with analytic result

		// TODO: Test does not work:
//		charged.test();
	}
	
	private interface ChargeDistribution {			
		/**Tests PoisonSolver and the analytic results*/
		void test();
	}
	
	private class PointChargeDistributionCenter implements ChargeDistribution {
		
		int indexX;
		int indexY;
		
		PointChargeDistributionCenter (int numCellsX, int numCellsY) {
			int indexX = numCellsX/2;
			int indexY = numCellsY/2;
			g.setRho(indexX, indexY, 1);
		}
		
		public void test() {
			assertEquals(g.getPhi(indexX, indexY)/(g.getCellWidth()), g.getPhi(indexX+1, indexY), ACCURACY_LIMIT);
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
	
	void assertAlmostEquals(String text, double x, double y, double limit) {
		if ((Math.abs(x - y) / Math.abs(x + y) > limit)
				|| (Double.isNaN(x) != Double.isNaN(y))
				|| (Double.isInfinite(x) != Double.isInfinite(y))) {
			assertTrue(text + " expected:<" + x + "> but was:<" + y + ">", false);
		}
	}
}
