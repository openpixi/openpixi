package org.openpixi.pixi.physics.fields;

import java.io.*;
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
		
		Simulation s = InitialConditions.initEmptySimulation();
		s.width = 100;
		s.height = 100;
		
		Grid g = new Grid(s);
		g.numCellsX = 10;
		g.numCellsY = 10;
		g.cellWidth = s.width / g.numCellsX;
		g.cellHeight = s.height / g.numCellsY;
		g.rho = new double[g.numCellsX][g.numCellsY];
		g.Ex = new double[g.numCellsX][g.numCellsY];
		g.Ey = new double[g.numCellsX][g.numCellsY];
		
		for (int i = 0; i < g.numCellsX; i++) {
			for (int j = 0; j < g.numCellsY; j++) {
				g.rho[i][j] = 0;
				g.Ex[i][j] = 0;
				g.Ey[i][j] = 0;
			}
		}
		
//		g.rho = pointChargeShifted(g.numCellsX, g.numCellsY);
//		g.rho = pointCharge(g.numCellsX, g.numCellsY);
		g.rho = dipole(g.numCellsX, g.numCellsY);
//		g.rho = randomChargeDistribution(g.numCellsX, g.numCellsY);
//		g.rho = lineChargeOnEdge(g.numCellsX, g.numCellsY);
		
		long start = System.currentTimeMillis();
		
		PoissonSolver.solve2D(g);
		
		long elapsed = System.currentTimeMillis()-start;
		System.out.println("\nCalculation time: "+elapsed);
		
		//deletes the old file
		File file = new File("\\efeld.dat");
		file.delete();
		
		//creates new file "efield.dat" in working directory and writes
		//field data to it
		WriteFile fieldFile = new WriteFile("efeld", "");
		for (int i = 0; i < g.numCellsX; i++) {
			for(int j = 0; j < g.numCellsY; j++) {
				fieldFile.writeLine(i*g.cellWidth + "\t" + j*g.cellHeight +
						"\t" + g.Ex[i][j] + "\t" + g.Ey[i][j]);
			}
		}
		fieldFile.closeFstream();
		
		//YOU NEED GNUPLOT FOR THIS http://www.gnuplot.info/
		//NEEDS TO BE IN YOUR EXECUTION PATH (i.e. PATH variable on windows)
		//plots the above output as vector field
		try {
		Runtime gnuplotrt = Runtime.getRuntime();
		Process gnuplotpr = gnuplotrt.exec("gnuplot -e \"set term png; set output 'D:\\efield.png'; plot 'D:\\efeld.dat' using 1:2:3:4 with vectors head filled lt 2\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	private double[][] randomChargeDistribution(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		for(int i = 0; i < numCellsX; i++) {
			for(int j = 0; j < numCellsY; j++) {
				rho[i][j] = 10 * Math.random();
			}
		}
		
		return rho;
	}
	
	private double[][] pointCharge(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		int indexX = (int)(numCellsX/2);
		int indexY = (int) (numCellsY/2);
		rho[indexX][indexY] = 5;
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
