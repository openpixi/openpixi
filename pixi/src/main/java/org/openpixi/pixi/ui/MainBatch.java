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

import java.io.FileNotFoundException;
import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.diagnostics.DiagnosticsScheduler;
import org.openpixi.pixi.profile.ProfileInfo;
import org.openpixi.pixi.ui.util.*;
import java.io.IOException;
import org.openpixi.pixi.physics.InitialConditions;
import static org.openpixi.pixi.ui.MainBatch.iterations;

public class MainBatch {

	/**
	 * Total number of iterations
	 */
	public static int iterations;
	/**
	 * Used to mark output files
	 */
	private static String runid;
	private static Simulation simulation;
	private static DiagnosticsScheduler diagnostics;
	private static EmptyDataOutput dataOutput;

	/**
	 * Can be run with two input parameters. The first specifies the XML
	 * settings file. The second specifies the directory where diagnostic output
	 * should be saved. If one wants to use the default values its best to
	 * provide an empty settings file. (dont forget to add the <settings>
	 * </settings> root element, the empty file should still comply with the XML
	 * specification!)
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		Debug.checkAssertsEnabled();

		// Creates a settings class with the default parameters
		Settings settings = new Settings();

		// Checks if the user has specified at least one parameter.
		// If so creates a parser and uses the parameter as the
		// path to the settings file.
		if (args.length != 0) {
			Parser parser = new Parser(settings);
			parser.parse(args[0]);
		}

		if (settings.getSimulationType() == 0) {
			// Creates the actual physics simulation that can be run iteratively.
			simulation = new Simulation(settings);
			//simulation = InitialConditions.initTwoStream(0.01,1,50);
			simulation = InitialConditions.initPair(0.1,1);
			//simulation = InitialConditions.initOneTest(0.01,1);
			
			// Reads out the settings that are needed for this UI.
			// This must be placed after the parsing process.
			iterations = simulation.getIterations();
			runid = settings.getRunid();


			for (int i = 0; i <= iterations;) {
				// advance the simulation by one step
				simulation.step(i);
				i++;

			}
			
			//simulation.close();

		} else {}
	}
}
