package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TadpoleInitialAveraged implements Diagnostics {

	private String path;
	private double timeInterval, regulator;
	private int stepInterval, direction, orientation;
	private boolean supressOutput;
	private Simulation s;
	private FieldMeasurements fieldMeasurements;

	public TadpoleInitialAveraged(String path, double timeInterval)
	{
		this(path, timeInterval, false);
	}

	public TadpoleInitialAveraged(String path, double timeInterval, boolean supressOutput)
	{
		this.path = path;
		this.timeInterval = timeInterval;
		this.supressOutput = supressOutput;
	}

	/**
	 * Initializes the TadpoleInitialAveraged object.
	 * It creates/deletes the output file.
	 *
	 * @param s    Instance of the simulation object
	 */
	public void initialize(Simulation s)
	{
		this.s = s;
		this.fieldMeasurements = new FieldMeasurements();

		if(!supressOutput) {
			// Create/delete file.
			//FileFunctions.clearFile("output/" + path);

			// Write first line.
			File file = FileFunctions.getFile("output/" + path);
			try {
				FileWriter pw = new FileWriter(file, true);
				pw.write("#mass \t tr[V]");
				pw.write("\n");
				pw.close();
			} catch (IOException ex) {
				System.out.println("TadpoleInitialAveraged Error: Could not write to file '" + path + "'.");
			}
		}
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public void setRegulator(double regulator) {
		this.regulator = regulator;
	}

	/**
	 * Computes the trace of the Wilson line along the longitudinal direction, averages it over the transversal plane and writes the result to the output file.
	 *
	 * @param grid		Reference to the Grid instance.
	 * @param particles	Reference to the list of particles.
	 * @param steps		Total simulation steps so far.
	 * @throws IOException
	 */
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {

		int longitudinalNumCells = grid.getNumCells(direction);
		int[] transverseNumCells = GridFunctions.reduceGridPos(grid.getNumCells(), direction);
		int totalTransverseCells = GridFunctions.getTotalNumberOfCells(transverseNumCells);
		GroupElement tadLink = grid.getElementFactory().groupIdentity();
		double tadTrace = 0;

		for (int i = 0; i < totalTransverseCells; i++) {
			// Current position
			int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
			for (int k = 0; k < longitudinalNumCells; k++) {
				int z = (orientation < 0) ? k : (longitudinalNumCells - k - 1);
				int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
				int index = grid.getCellIndex(gridPos);
				tadLink.multAssign(grid.getUnext(index,0));
			}
			tadTrace += tadLink.getRealTrace()/totalTransverseCells/2;				///The factor of /2 turns out after analyzing the results!! Matter of convention!!
			tadLink.set(grid.getElementFactory().groupIdentity());
		}

		if(!supressOutput) {
			File file = FileFunctions.getFile("output/" + path);
			FileWriter pw = new FileWriter(file, true);
			DecimalFormat formatter = new DecimalFormat("0.################E0");

			pw.write(formatter.format(regulator)+ "\t");
			pw.write(formatter.format(tadTrace));
			pw.write("\n");

			pw.close();
		}

	}
}