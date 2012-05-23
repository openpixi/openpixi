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

	double ACCURACY_LIMIT = 1.e-16;

	public PoissonSolverTest(String testName){
		super(testName);
	}
	
	void assertAlmostEquals(String text, double x, double y, double limit) {
		if ((Math.abs(x - y) / Math.abs(x + y) > limit)
				|| (Double.isNaN(x) != Double.isNaN(y))
				|| (Double.isInfinite(x) != Double.isInfinite(y))) {
			assertTrue(text + " expected:<" + x + "> but was:<" + y + ">", false);
		}
	}

	public void testPointcharge() {

		Simulation s = InitialConditions.initEmptySimulation();
		s.width = 75;
		s.height = 10;
		
		Grid g = GridFactory.createYeeGrid(s, 95, 10, s.width, s.height);
		g.resetCurrentAndCharge();
		
		
		//writes to g.rho
		ChargeDistribution charged = new PointChargeDistribution(g);
		
		long start = System.currentTimeMillis();
		PoissonSolverPeriodic poisolver = new PoissonSolverPeriodic();
		poisolver.solve(g);
		
		long elapsed = System.currentTimeMillis()-start;
		System.out.println("\nCalculation time: "+elapsed);
		
		int indexX = (int)(g.getNumCellsX()/2);
		int indexY = (int) (g.getNumCellsY()/2);
//		assertAlmostEquals("", (g.Ey[indexX][indexY+1]/16), g.Ey[indexX][indexY+4] , ACCURACY_LIMIT);
////		charged.test();		
		output(g);
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
			g.setRho(indexX, indexY, 50);
		}
		
		public void test() {
			
			assertAlmostEquals("", g.getEx(indexX, indexY)/(g.getCellWidth()*g.getCellWidth()), g.getEx(indexX+1, indexY) , ACCURACY_LIMIT);

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
	
	public void output(Grid g) {
		
		double aspectratio = g.simulation.height/g.simulation.width;
		//deletes the old files
		File file1 = new File("\\efeld.dat");
		file1.delete();
		File file2 = new File("\\potential.dat");
		file2.delete();
		
		//creates new file "efield.dat" in working directory and writes
		//field data to it
		WriteFile fieldFile = new WriteFile("efeld", "");
		for (int i = 0; i < g.getNumCellsX(); i++) {
			for(int j = 0; j < g.getNumCellsY(); j++) {
				fieldFile.writeLine(i*g.getCellWidth() + "\t" + j*g.getCellHeight() +
						"\t" + g.getEx(i, j) + "\t" + g.getEy(i, j));
			}
		}
		fieldFile.closeFstream();
		
		WriteFile potentialFile = new WriteFile("potential", "");
		for (int i = 0; i < g.getNumCellsX(); i++) {
			for(int j = 0; j < g.getNumCellsY(); j++) {
				potentialFile.writeLine(i*g.getCellWidth() + "\t" + j*g.getCellHeight()  + "\t" + g.getPhi(i, j));
			}
		}
		potentialFile.closeFstream();
		
		//YOU NEED GNUPLOT FOR THIS http://www.gnuplot.info/
		//NEEDS TO BE IN YOUR EXECUTION PATH (i.e. PATH variable on windows)
		//plots the above output as vector field
		try {
		Runtime gnuplotrt = Runtime.getRuntime();
		Process gnuplotpr = gnuplotrt.exec("gnuplot -e \"set term png; set size ratio " + aspectratio + "; set output 'D:\\efield.png'; plot 'D:\\efeld.dat' using 1:2:3:4 with vectors head filled lt 2\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//plots potential
		try {
		Runtime gnuplotPotentialRt = Runtime.getRuntime();
		Process gnuplotPotentialPr = gnuplotPotentialRt.exec("gnuplot -e \"set term png; set size ratio " + aspectratio + "; set output 'D:\\potential.png'; plot 'D:\\potential.dat' using 1:2:3 with circles linetype palette \"");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
