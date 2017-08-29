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

public class BulkQuantitiesInTime implements Diagnostics {

	private String path;
	private double timeInterval;
	private int stepInterval;
	private boolean supressOutput;
	private Simulation s;
	private FieldMeasurements fieldMeasurements;

	public double eSquared;
	public double bSquared;
	public double px;
	public double py;
	public double pz;
	public double gaussViolation;
	public double totalChargeSquared;

	public BulkQuantitiesInTime(String path, double timeInterval)
	{
		this(path, timeInterval, false);
	}

	public BulkQuantitiesInTime(String path, double timeInterval, boolean supressOutput)
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
		this.stepInterval = (int) Math.max(Math.round((timeInterval / this.s.getTimeStep())), 1);
		this.fieldMeasurements = new FieldMeasurements();

		if(!supressOutput) {
			// Create/delete file.
			FileFunctions.clearFile(path);

			// Write first line.
			File file = FileFunctions.getFile(path);
			try {
				FileWriter pw = new FileWriter(file, true);
				pw.write("#time \t E^2 \t B^2 \t P_x \t P_y \t P_z \t G \t rho^2");
				pw.write("\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("BulkQuantitiesInTime Error: Could not write to file '" + path + "'.");
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
		if(steps % stepInterval == 0) {
			//TODO Make this method d-dimensional!!
			double[] esquares = new double[3];
			double[] bsquares = new double[3];
			for (int i = 0; i < 3; i++) {
				esquares[i] = fieldMeasurements.calculateEsquared(grid, i);
				bsquares[i] = fieldMeasurements.calculateBsquared(grid, i);
			}

			eSquared = esquares[0] + esquares[1] + esquares[2];
			bSquared = bsquares[0] + bsquares[1] + bsquares[2];
			px = -esquares[0] + esquares[1] + esquares[2] - bsquares[0] + bsquares[1] + bsquares[2];
			py = +esquares[0] - esquares[1] + esquares[2] + bsquares[0] - bsquares[1] + bsquares[2];
			pz = +esquares[0] + esquares[1] - esquares[2] + bsquares[0] + bsquares[1] - bsquares[2];

			gaussViolation = fieldMeasurements.calculateGaussConstraint(grid);
			totalChargeSquared = fieldMeasurements.calculateTotalChargeSquared(grid);

			if(!supressOutput) {
				File file = FileFunctions.getFile(path);
				FileWriter pw = new FileWriter(file, true);
				DecimalFormat formatter = new DecimalFormat("0.################E0");

				pw.write(formatter.format(steps * s.getTimeStep()) + "\t");
				pw.write(formatter.format(eSquared)+ "\t");
				pw.write(formatter.format(bSquared) + "\t");
				pw.write(formatter.format(px) + "\t");
				pw.write(formatter.format(py) + "\t");
				pw.write(formatter.format(pz) + "\t");
				pw.write(formatter.format(gaussViolation) + "\t");
				pw.write(formatter.format(totalChargeSquared));
				pw.write("\n");

				pw.close();
			}
			
		}
	}
}