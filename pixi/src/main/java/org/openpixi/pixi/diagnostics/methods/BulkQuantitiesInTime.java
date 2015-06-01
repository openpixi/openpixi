package org.openpixi.pixi.diagnostics.methods;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.IOException;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;

public class BulkQuantitiesInTime implements Diagnostics {

	private String path;
	private double timeInterval;
	private int stepInterval;
	private Simulation s;
	private FieldMeasurements fieldMeasurements;

	public BulkQuantitiesInTime(String path, double timeInterval)
	{
		this.path = path;
		this.timeInterval = timeInterval;
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
		this.stepInterval = (int) (timeInterval / this.s.getTimeStep());
		this.fieldMeasurements = new FieldMeasurements();

		clear();
	}

	/** Performes the desired diagnostics*/
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {
			
			File file = getOutputFile(path);
			FileWriter pw = new FileWriter(file, true);
			
			if(steps == 0) {
				pw.write("#time \t E^2 \t B^2");
				pw.write("\n");
			} else {}
			
			pw.write(steps * s.getTimeStep() + "\t");
			
			pw.write(fieldMeasurements.calculateEsquared(grid) + "\t");
			pw.write(fieldMeasurements.calculateBsquared(grid) + "\t");
				
			pw.write("\n");
			
			pw.close();
			
		} else {}
	}
	
	/** Checks if the files are already existent and deletes them*/
	public void clear() {
		File particlesfile = getOutputFile(path);
		boolean fileExists1 = particlesfile.exists();
		if(fileExists1 == true) {
			particlesfile.delete();
		}
	}
	
	/** Creates a file with a given name in the output folder*/
	private File getOutputFile(String filename) {
		// Default output path is
		// 'output/' + filename
		File fullpath = new File("output");
		if(!fullpath.exists()) fullpath.mkdir();

		return new File(fullpath, filename);
	}
}
