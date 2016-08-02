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

		String output = density.getInfo() + ", V: " + FileFunctions.format(Vtrace / totalCells) + "\n";

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

		// Bin the results to get the correlator as a function of distance.
		int numBins = (int) Math.sqrt(totalCells) / 2;
		double[] trVVbinned = new double[numBins];
		int[] counter = new int[numBins];
		double maximumDistance = Math.min(numCells[0], numCells[1]) * 0.5;
		double ds = maximumDistance / ((double) numBins);

		for (int i = 0; i < numBins; i++) {
			trVVbinned[i] = 0.0;
			counter[i] = 0;
		}

		for (int i = 0; i < totalCells; i++) {
			GroupElement V1 = V[i];
			int[] x = GridFunctions.getCellPos(i, numCells);
			for (int j = 0; j < totalCells; j++) {
				int[] y = GridFunctions.getCellPos(j, numCells);
				double[] dxy = new double[2];
				double dist = 0.0;
				for (int k = 0; k < 2; k++) {
					dxy[k] = Math.abs(x[k] - y[k]);
					// Wrap distance around periodic boundary.
					if(dxy[k] > numCells[k] / 2) {
						dxy[k] -= numCells[k];
					}
					dist += dxy[k] * dxy[k];
				}
				dist = Math.sqrt(dist);
				int bin = (int) (dist / ds);
				if(bin < numBins) {
					GroupElement V2 = V[j].adj();
					trVVbinned[bin] += V1.mult(V2).getRealTrace();
					counter[bin]++;
				}

			}
		}

		for (int i = 0; i < numBins; i++) {
			trVVbinned[i] /= (double) counter[i];
		}

		// Write data to file.
		try {
			File file = FileFunctions.getFile("output/" + filename);
			FileWriter pw = new FileWriter(file, true);
			pw.write(density.getInfo()+"\n");
			pw.write("d\ttr(V V^t)\n");
			for (int bin = 0; bin < numBins; bin++) {
				pw.write(FileFunctions.format(bin * ds * s.grid.getLatticeSpacing()) + "\t");
				pw.write(FileFunctions.format(trVVbinned[bin]) + "\n");
			}
			pw.close();
		} catch (IOException ex) {
			System.out.println("WilsonLineObservables: Cannot write to file " +  filename);
		}
	}

}
