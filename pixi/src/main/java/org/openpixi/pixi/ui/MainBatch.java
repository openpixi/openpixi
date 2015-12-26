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

public class MainBatch {

	/**
	 * Total number of iterations
	 */
	public static int iterations;
	private static Simulation simulation;

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
		//Debug.checkAssertsEnabled();
		// Checks if the user has specified at least one parameter.
		// If so creates a parser and uses the parameter as the
		// path to the settings file.
		if (args.length != 0) {
			File file = new File(args[0]);

			if(file.exists()) {
				if(file.isFile()) {
					try {
						System.out.println("Running " + file.getPath());
						String string = FileIO.readFile(file);
						runSimulationFromString(string);

					} catch (IOException e) {
						System.out.println("Error opening " + args[0]);
					}
				} else if(file.isDirectory()){
					System.out.println("Loading configuration files from " + file.getPath());
					FilenameFilter filter = new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".yaml");
						}};
					File[] listOfFiles = file.listFiles(filter);
					for(File f : listOfFiles) {
						try {
							System.out.println("Running " + f.getPath());
							String string = FileIO.readFile(f);
							runSimulationFromString(string);
						} catch (IOException e) {
							System.out.println("Error opening " + f.getPath());
						}
					}
				}
			}
		}
		System.out.println("Done!");
		System.exit(0);
	}

	public static void runSimulationFromString(String configurationString) {
		initializeSimulationFromString(configurationString);
		try {
			simulation.run();
		} catch (IOException e) {
			System.out.println("MainBatch: something went wrong.");
		}
	}

	public static void initializeSimulationFromString(String configurationString) {
		// Creates a settings class with the default parameters
		Settings settings = new Settings();
		YamlParser yamlParser = new YamlParser(settings);
		yamlParser.parseString(configurationString);

		// Initialize the simulation
		simulation = new Simulation(settings);
	}

	public static void step() {
		try {
			simulation.step();
		} catch (IOException e) {
			System.out.println("MainBatch: something went wrong.");
		}
	}
}
