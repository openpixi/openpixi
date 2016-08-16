package org.openpixi.pixi.diagnostics.methods;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Locale;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.measurements.FieldMeasurements;

public class TimeMeasurement implements Diagnostics {

	private String path;
	private double timeInterval;
	private int stepInterval;
	private Simulation simulation;
	long stept0;
	long t0;
	Runtime runtime;

	private static final long GIGABYTE = 1024L * 1024L * 1024L;

	public TimeMeasurement(String path, double timeInterval)
	{
		this.path = path;
		this.timeInterval = timeInterval;
	}

	/**
	 * Initializes the TimeMeasurement object.
	 * It sets the step interval and creates/deletes the output file.
	 *
	 * @param s    Instance of the simulation object
	 */
	public void initialize(Simulation simulation)
	{
		this.simulation = simulation;
		this.stepInterval = (int) (timeInterval / this.simulation.getTimeStep());

		runtime = Runtime.getRuntime();
		stept0 = System.nanoTime();
		t0 = stept0;
		long jvmUpTime = ManagementFactory.getRuntimeMXBean().getUptime();

		// Create/delete file.
		FileFunctions.clearFile("output/" + path);

		// Write first line.
		File file = FileFunctions.getFile("output/" + path);
		try {
			FileWriter pw = new FileWriter(file, true);
			pw.write("Initialization time: " + jvmUpTime + "ms\n");
			pw.close();
		} catch (IOException ex) {
			System.out.println("TimeMeasurement Error: Could not write to file '" + path + "'.");
		}
	}

	/**
	 * Calculates timing information and writes them to the output file.
	 *
	 * @param grid		Reference to the Grid instance.
	 * @param particles	Reference to the list of particles.
	 * @param steps		Total simulation steps so far.
	 * @throws IOException
	 */
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if((steps % stepInterval == 0) || (steps == simulation.getIterations())) {

			long stept1 = System.nanoTime();

			// Some diagnostic stuff
			int stepdt = (int) ((stept1 - stept0) / 1000 / 1000);
			double currentTime = simulation.totalSimulationTime;
			double totalTime = simulation.getIterations() * simulation.getTimeStep();
			double memory = ((int) (100 * runtime.totalMemory() / GIGABYTE)) / 100.0;
			int mempercent = (int) (100 * runtime.totalMemory() / runtime.maxMemory());

			File file = FileFunctions.getFile("output/" + path);
			FileWriter pw = new FileWriter(file, true);

			pw.write("step " + currentTime + "/" + totalTime + " (" + stepdt + "ms)\n");
			pw.write("memory: " + memory + "gb (" + mempercent + "%)\n");

			if (steps == simulation.getIterations()) {
				// End of simulation

				// dt in seconds
				long t1 = System.nanoTime();
				int dt = (int) ((t1 - t0) / 1000 / 1000 / 1000);
				int avg = (int) ((t1 - t0) / 1000 / 1000) / simulation.getIterations();
				pw.write("Simulation time: " + dt + " s (average " + avg + "ms)\n");
			}

			pw.close();

			stept0 = stept1;
		}
	}
}