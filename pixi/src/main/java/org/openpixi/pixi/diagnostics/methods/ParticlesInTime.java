package org.openpixi.pixi.diagnostics.methods;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.IOException;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
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
		this.stepInterval = (int) Math.max((timeInterval / s.getTimeStep()), 1);

		// Create/delete file.
		FileFunctions.clearFile(path);

		// Write first line.
		String[] directionNames = new String[] {"x", "y", "z"};
		File file = FileFunctions.getFile(path);
		try {
			FileWriter pw = new FileWriter(file, true);
			pw.write("#time\t");
			for(int i = 0; i < s.getNumberOfDimensions(); i++) {
				if(i < 3) {
					pw.write(directionNames[i] + "\t");
				} else {
					pw.write("d" + i + "\t");
				}
			}

			for(int i = 0; i < s.getNumberOfDimensions(); i++) {
				if(i < 3) {
					pw.write("p" + directionNames[i] + "\t");
				} else {
					pw.write("pd" + i + "\t");
				}
			}
			pw.write("\n");
			pw.close();
		} catch (IOException ex) {
			System.out.println("ParticlesInTime Error: Could not write to file '" + path + "'.");
		}
	}

	/**
	 * Writes the positions and velocites
	 *
	 * @param grid		Reference to the Grid instance.
	 * @param particles	Reference to the list of particles.
	 * @param steps		Total simulation steps so far.
	 * @throws IOException
	 */
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps % stepInterval == 0) {

			File file = FileFunctions.getFile(path);
			FileWriter pw = new FileWriter(file, true);
			pw.write(steps * s.getTimeStep() + "\t");

			for (int i = 0; i < particles.size(); i++) {
				IParticle p = particles.get(i);
				/*
					This writes the particles position and velocity via the getters Particle.getPosition() and
					Particle.getVelocity(). Depending on the particle solver this can lead to quantities evaluated at
					different times, e.g. in the leap frog algorithm positions are evaluated at even time steps and
					velocities at odd time steps in between the even ones.
				 */
				for (int j = 0; j < s.getNumberOfDimensions(); j++) {
					pw.write(p.getPosition(j) + "\t");
				}
				for (int j = 0; j < s.getNumberOfDimensions(); j++) {
					pw.write(p.getVelocity(j) + "\t");
				}
			}
			pw.write("\n");
			pw.close();
		}
	}
}