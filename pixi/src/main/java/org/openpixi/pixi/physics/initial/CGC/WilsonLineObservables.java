package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * This class computes the tadpole and dipole expectation values of the Wilson line.
 * Data is provided by the Poisson solver.
 * This class is not implemented as a Diagnostic because it can only run once, during initialization and because it is
 * specific to CGC initial conditions as implemented in the initial.CGC package.
 */
public class WilsonLineObservables {

	private ICGCPoissonSolver solver;
	private IInitialChargeDensity density;
	private Simulation s;

	public WilsonLineObservables(Simulation s, ICGCPoissonSolver solver, IInitialChargeDensity density) {
		this.s = s;
		this.solver = solver;
		this.density = density;
	}

	/**
	 * Computes the tadpole expectation value and writes the results to a file.
	 * @param filename name of the output file
	 */
	public void computeTadpole(String filename) {
		// Compute tadpole expectation value
		GroupElement[] V = solver.getV();

		int[] numCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), density.getDirection());
		int totalCells = GridFunctions.getTotalNumberOfCells(numCells);

		double Vtrace = 0.0;

		for (int i = 0; i < totalCells; i++) {
			Vtrace += V[i].getRealTrace();
		}

		String output = density.getInfo() + ", V: " + Vtrace / totalCells + "\n";

		// Write to file
		try {
			File file = FileFunctions.getFile("output/" + filename);
			FileWriter pw = new FileWriter(file, true);
			pw.write(output);
			pw.close();
		} catch (IOException ex) {
			System.out.println("WilsonLineObservables: Cannot write to file " +  filename);
		}
	}

	/**
	 * Computes the dipole correlation function using binning and writes the results to a file.
	 * Not implemented yet.
	 *
	 * @param filename  name of the output file
	 */
	public void computeDipole(String filename) {
		// Compute dipole expectation value
		GroupElement[] V = solver.getV();

		int[] numCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), density.getDirection());
		int totalCells = GridFunctions.getTotalNumberOfCells(numCells);
	}
}
