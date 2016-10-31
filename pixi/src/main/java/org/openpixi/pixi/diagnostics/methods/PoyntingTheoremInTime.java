package org.openpixi.pixi.diagnostics.methods;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Locale;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;

public class PoyntingTheoremInTime implements Diagnostics {

	private String path;
	private double timeInterval;
	private int stepInterval;
	private boolean supressOutput;
	private Simulation s;
	private PoyntingTheoremBuffer poyntingTheorem;

	public PoyntingTheoremInTime(String path, double timeInterval)
	{
		this(path, timeInterval, false);
	}

	public PoyntingTheoremInTime(String path, double timeInterval, boolean supressOutput)
	{
		this.path = path;
		this.timeInterval = timeInterval;
		this.supressOutput = supressOutput;
	}

	/**
	 * Initializes the BulkQuantitiesInTime object.
	 * It sets the step interval and creates/deletes the output file.
	 *
	 * @param s    Instance of the simulation object
	 */
	public void initialize(Simulation s)
	{
		this.s = s;
		this.stepInterval = (int) Math.max((timeInterval / s.getTimeStep()), 1);
		poyntingTheorem = PoyntingTheoremBuffer.getOrAppendInstance(s);
		//poyntingTheorem.initialize(s);

		if(!supressOutput) {
			// Create/delete file.
			FileFunctions.clearFile(path);

			// Write first line.
			File file = FileFunctions.getFile(path);
			try {
				FileWriter pw = new FileWriter(file, true);
				pw.write("#time");
				pw.write(" \t Energy density");
				pw.write(" \t dE/dt");
				pw.write(" \t div S");
				pw.write(" \t B rot E - E rot B");
				pw.write(" \t J*E");
				pw.write(" \t dE/dt + div S + J*E");
				pw.write(" \t Time-integrated div S");
				pw.write(" \t Time-integrated B rot E - E rot B");
				pw.write(" \t Time-integrated J*E");
				pw.write(" \t E + time-integrated(div S + J*E)");
				pw.write(" \t E + time-integrated(B rot E - E rot B + J*E)");
				pw.write("\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("PoyntingTheoremInTime Error: Could not write to file '" + path + "'.");
			}
		}
	}

	/**
	 * Computes the average of the diagonal components of the stress-energy tensor over the lattice and writes them to the output file.
	 *
	 * @param grid		Reference to the Grid instance.
	 * @param particles	Reference to the list of particles.
	 * @param steps		Total simulation steps so far.
	 * @throws IOException
	 */
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {

		// Calculate quantities at each time step
		double energyDensity = poyntingTheorem.getTotalEnergyDensity();
		double energyDensityDerivative = poyntingTheorem.getTotalEnergyDensityDerivative();
		double divS = poyntingTheorem.getTotalDivS();
		double brotEminusErotB = poyntingTheorem.getTotalBrotEminusErotB();
		double jS = poyntingTheorem.getTotalJE();
		double poyntingTheoremSum = energyDensityDerivative + divS + jS;
		double integratedDivS = poyntingTheorem.getIntegratedTotalDivS();
		double integratedBrotEminusErotB = poyntingTheorem.getIntegratedTotalBrotEminusErotB();
		double integratedJS = poyntingTheorem.getIntegratedTotalJE();
		double integratedPoyntingTheorem1 = poyntingTheorem.getTotalEnergyDensity()
				+ integratedDivS + integratedJS;
		double integratedPoyntingTheorem2 = poyntingTheorem.getTotalEnergyDensity()
				+ integratedBrotEminusErotB + integratedJS;

		// Output only at desired interval
		if(steps % stepInterval == 0) {

			if(!supressOutput) {
				File file = FileFunctions.getFile(path);
				FileWriter pw = new FileWriter(file, true);
				DecimalFormat formatter = new DecimalFormat("0.################E0");

				pw.write(formatter.format(steps * s.getTimeStep()));
				pw.write("\t" + formatter.format(energyDensity));
				pw.write("\t" + formatter.format(energyDensityDerivative));
				pw.write("\t" + formatter.format(divS));
				pw.write("\t" + formatter.format(brotEminusErotB));
				pw.write("\t" + formatter.format(jS));
				pw.write("\t" + formatter.format(poyntingTheoremSum));
				pw.write("\t" + formatter.format(integratedDivS));
				pw.write("\t" + formatter.format(integratedBrotEminusErotB));
				pw.write("\t" + formatter.format(integratedJS));
				pw.write("\t" + formatter.format(integratedPoyntingTheorem1));
				pw.write("\t" + formatter.format(integratedPoyntingTheorem2));
				pw.write("\n");

				pw.close();
			}
		}
	}
}