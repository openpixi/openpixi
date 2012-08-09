package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.grid.ChargeConservingAreaWeighting;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.ui.util.WriteFile;

import java.io.File;

public class PoissonSolverCalculations {
	
	private Simulation s;
	private Grid g;
	private PoissonSolver poisolver;
	
	public PoissonSolverCalculations() {

		Settings stt = new Settings();
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		stt.setGridCellsX(100);
		stt.setGridCellsY(100);
		stt.setGridSolver(new YeeSolver());
		stt.setInterpolator(new ChargeConservingAreaWeighting());

		this.s = new Simulation(stt);
		this.g = s.grid;
		g.resetCurrent();
		
		this.poisolver = new PoissonSolverFFTPeriodic();
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
	
	private double[][] randomChargeDistribution(int numCellsX, int numCellsY) {
		double[][] rho = new double[numCellsX][numCellsY];
		double charge = 10;
		if (Math.random() < 0.5) {
			charge = -charge;
		}
		for(int i = 0; i < numCellsX; i++) {
			for(int j = 0; j < numCellsY; j++) {
				g.setRho(i, j, charge * Math.random());
			}
		}
		
		return rho;
	}
	
	public static void output(Grid g) {
		
		double aspectratio = g.getCellHeight() * g.getNumCellsY() / g.getCellWidth() * g.getNumCellsX();
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

	public static void main(String[] args) {
		
		PoissonSolverCalculations pc = new PoissonSolverCalculations();
		//writes to g.rho
		pc.randomChargeDistribution(pc.g.getNumCellsX(), pc.g.getNumCellsY());
				
		long start = System.currentTimeMillis();
		pc.poisolver.solve(pc.g);				
		long elapsed = System.currentTimeMillis()-start;
		System.out.println("\nCalculation time: "+elapsed);
		
		PoissonSolverCalculations.output(pc.g);
		
		interpolatorAndPoissonsolver();
	}
	
	public static void interpolatorAndPoissonsolver() {

		Settings stt = new Settings();
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);
		stt.setSpeedOfLight(Math.sqrt(stt.getSimulationWidth() * stt.getSimulationWidth() +
				stt.getSimulationHeight() * stt.getSimulationHeight())/5);

		stt.setNumOfParticles(1);
		stt.setParticleRadius(1);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

		stt.setGridCellsX(100);
		stt.setGridCellsY(100);
		stt.setGridSolver(new YeeSolver());
		stt.setInterpolator(new ChargeConservingAreaWeighting());

		Simulation s = new Simulation(stt);
		PoissonSolverCalculations.output(s.grid);
	}

}
