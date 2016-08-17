/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openpixi.pixi.ui;

import java.io.File;
import java.io.FileNotFoundException;
import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.util.*;
import org.openpixi.pixi.ui.util.yaml.YamlParser;

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;

public class MainBatch {

	/**
	 * Total number of iterations
	 */
	public static int iterations;
	private static Simulation simulation;

	private static final long GIGABYTE = 1024L * 1024L * 1024L;


	/**
	 * This class takes an input parameter which specifies the YAML file.
	 *
	 * Launch using:
	 * <pre>
	 * mvn exec:java -Dexec.mainClass=org.openpixi.pixi.ui.MainBatch -Dexec.args="One_particle_Test.yaml"
	 * </pre>
	 * or
	 * <pre>
	 * java -cp target/pixi-0.6-SNAPSHOT.jar org.openpixi.pixi.ui.MainBatch "One_particle_Test.yaml"
	 * </pre>
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {

		// Set US locale for numeric output ("1.23"[US] instead of "1,23"[DE])
		Locale.setDefault(Locale.US);

		//Debug.checkAssertsEnabled();
		// Checks if the user has specified at least one parameter.
		// If so creates a parser and uses the parameter as the
		// path to the settings file.
		if (args.length != 0) {
			File file = new File(args[0]);

			if(file.exists()) {
				if(file.isFile()) {
					try {
						System.out.println("MainBatch: Running " + file.getPath());
						String string = FileIO.readFile(file);
						runSimulationFromString(string);

					} catch (IOException e) {
						System.out.println("MainBatch: Error opening " + args[0]);
					}
				} else if(file.isDirectory()){
					System.out.println("MainBatch: Loading configuration files from " + file.getPath());
					FilenameFilter filter = new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".yaml");
						}};
					File[] listOfFiles = file.listFiles(filter);
					for(File f : listOfFiles) {
						try {
							System.out.println("MainBatch: Running " + f.getPath());
							String string = FileIO.readFile(f);
							runSimulationFromString(string);
						} catch (IOException e) {
							System.out.println("MainBatch: Error opening " + f.getPath());
						}
					}
				}
			}
		}
		System.out.println("MainBatch: Done!");
		System.exit(0);
	}

	public static void runSimulationFromString(String configurationString) {
		initializeSimulationFromString(configurationString);

		// Simulation run and time measurement
		long t0 = System.nanoTime();
		Runtime runtime = Runtime.getRuntime();
		try {
			while(simulation.continues()) {
				long stept0 = System.nanoTime();
				simulation.step();

				// Some diagnostic stuff
				int stepdt = (int) ((System.nanoTime() - stept0) / 1000 / 1000);
				double currentTime = simulation.totalSimulationTime;
				double totalTime = simulation.getIterations() * simulation.getTimeStep();
				double memory = ((int) (100 * runtime.totalMemory() / GIGABYTE)) / 100.0;
				int mempercent = (int) (100 * runtime.totalMemory() / runtime.maxMemory());

				System.out.println("MainBatch: step " + currentTime + "/" + totalTime + " (" + stepdt + "ms)");
				System.out.println("MainBatch: memory: " + memory + "gb (" + mempercent + "%)");
			}
			simulation.run();
		} catch (IOException e) {
			System.out.println("MainBatch: something went wrong.");
		}

		// dt in seconds
		long t1 = System.nanoTime();
		int dt = (int) ((t1 - t0) / 1000 / 1000 / 1000);
		int avg = (int) ((t1 - t0) / 1000 / 1000) / simulation.getIterations();
		System.out.println("MainBatch: Simulation time: " + dt + " s (average " + avg + "ms)");
	}

	public static void initializeSimulationFromString(String configurationString) {
		// Creates a settings class with the default parameters
		Settings settings = new Settings();
		YamlParser yamlParser = new YamlParser(settings);
		yamlParser.parseString(configurationString);

		// Initialization time measurement
		long t0 = System.nanoTime();

		// Initialize the simulation
		simulation = new Simulation(settings);

		// dt in milliseconds
		long t1 = System.nanoTime();
		int dt = (int) ((t1 - t0) / 1000 / 1000);
		System.out.println("MainBatch: Initialization time: " + dt + " ms.");
	}

	public static void step() {
		try {
			simulation.step();
		} catch (IOException e) {
			System.out.println("MainBatch: something went wrong.");
		}
	}
}
