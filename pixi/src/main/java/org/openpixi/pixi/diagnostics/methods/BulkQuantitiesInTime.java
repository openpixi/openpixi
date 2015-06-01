package org.openpixi.pixi.diagnostics.methods;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.IOException;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;

public class BulkQuantitiesInTime implements Diagnostics {

	private String path;
	private double interval;

	/** Performes the desired diagnostics*/
	public void calculate(Grid grid, ArrayList<IParticle> particles, double time) throws IOException {
		if(time % interval == 0) {
			
			File file = getOutputFile(path);
			FileWriter pw = new FileWriter(file, true);
			FieldMeasurements fieldMeasurements = new FieldMeasurements();
			
			if(time == 0) {
				pw.write("#time \t E^2 \t B^2");
				pw.write("\n");
			} else {}
			
			pw.write(time + "\t");
			
			pw.write(fieldMeasurements.calculateEsquared(grid) + "\t");
			pw.write(fieldMeasurements.calculateBsquared(grid) + "\t");
				
			pw.write("\n");
			
			pw.close();
			
		} else {}
	}
	
	/** Sets the file path*/
	public void setPath(String path) {
		this.path = path;
	}
	
	/** Sets the measurement interval*/
	public void setInterval(double interval) {
		this.interval = interval;
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
