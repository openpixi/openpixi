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
import org.openpixi.pixi.diagnostics.DiagnosticsScheduler;
import org.openpixi.pixi.ui.util.*;
import org.openpixi.pixi.ui.util.yaml.YamlParser;

import java.io.IOException;
import org.openpixi.pixi.physics.InitialConditions;

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
		Debug.checkAssertsEnabled();

		// Creates a settings class with the default parameters
		Settings settings = new Settings();

		// Checks if the user has specified at least one parameter.
		// If so creates a parser and uses the parameter as the
		// path to the settings file.
		if (args.length != 0) {
			File path = new File("input");
			File file = new File(path, args[0]);
			try {
				String string = FileIO.readFile(file);
				YamlParser parser = new YamlParser(settings);
				parser.parseString(string);
				System.out.println("Using " + args[0]);
			} catch (IOException e) {
				System.out.println("Error opening " + args[0]);
			}
		}

		// Creates the actual physics simulation that can be run iteratively.
		//simulation = new Simulation(settings);

		//simulation = InitialConditions.initTwoStream(0.01,1,50);
		simulation = InitialConditions.initEmptySimulation();
		//simulation = InitialConditions.initOneTest(0.01,1);
		//simulation = InitialConditions.initOneTest3D(0.01,1);
		//simulation = InitialConditions.initInterpolationTest3D(0.01,1);
		//simulation = InitialConditions.initWaveTest(1);

		while (simulation.continues()) {
			// advance the simulation by one step
			simulation.step();
		}

		//simulation.close();
	}
}
