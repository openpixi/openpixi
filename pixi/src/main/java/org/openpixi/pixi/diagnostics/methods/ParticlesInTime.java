package org.openpixi.pixi.diagnostics.methods;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.IOException;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

public class ParticlesInTime implements Diagnostics {

	private String path;
	private double timeInterval;
	private int stepInterval;
	private Simulation s;

	public ParticlesInTime(String path, double timeInterval)
	{
		this.path = path;
		this.timeInterval = timeInterval;
	}

	/**
	 * Initializes the ParticlesInTime object.
	 * It sets the step interval and creates/deletes the output file.
	 *
	 * @param s    Instance of the simulation object
	 */
	public void initialize(Simulation s)
	{
		this.s = s;
		stepInterval =  (int) (this.timeInterval / s.getTimeStep());

		clear();
	}

	/** Performes the desired diagnostics*/
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {
			
			File file = getOutputFile(path);
			FileWriter pw = new FileWriter(file, true);
			
			if(steps == 0) {
				pw.write("#time \t x \t y \t z \t px \t py \t pz");
				pw.write("\n");
			} else {}
			
			pw.write(steps * s.getTimeStep() + "\t");
			
			for (int i = 0; i < particles.size(); i++) {
				pw.write(particles.get(i).getPosition(0) + "\t");
				pw.write(particles.get(i).getPosition(1) + "\t");
				pw.write(particles.get(i).getPosition(2) + "\t");
				pw.write(particles.get(i).getVelocity(0) + "\t");
				pw.write(particles.get(i).getVelocity(1) + "\t");
				pw.write(particles.get(i).getVelocity(2) + "\t");
			}
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
