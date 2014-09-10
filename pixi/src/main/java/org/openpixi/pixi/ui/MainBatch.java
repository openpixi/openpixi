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
//import org.openpixi.pixi.physics.ParallelSimulationCL;
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

		// Reads out the settings that are needed for this UI.
		// This must be placed after the parsing process.
		iterations = settings.getIterations();
		runid = settings.getRunid();

		if (settings.getSimulationType() == 0) {
			// Creates the actual physics simulation that can be run iteratively.
			simulation = new Simulation(settings);
			// Creates the diagnostics wrapper class that knows about all
			// enabled diagnostic methods.
			diagnostics = new DiagnosticsScheduler(simulation.grid, simulation.particles, settings);

			// Checks if the user has specified at least two parameters
			// If this is not the case, the diagnostics output is disabled
			// the program will not output anything.
			if (args.length < 2) {
				dataOutput = new EmptyDataOutput();
			} else {
				// The program expects a directory (to which the output files will be saved) as its
				// second parameter. This checks if the directory is specified correctly. Tries to
				// fix it if not.
				if (args[1].substring(args[1].length() - 1) != System.getProperty("file.separator")) {
					args[1] = args[1] + System.getProperty("file.separator");
				}

				// Tries to create the output objects. If something is wrong with the specified
				// directory the program will give an error and terminate. 
				try {
					dataOutput = new DataOutput(args[1], runid, simulation.grid);
				} catch (IOException e) {
					System.err.print("Something went wrong when creating output files for diagnostics! \n"
							+ "Please specify an output directory with write access rights!\n"
							+ "The directory that you specified was " + args[1] + "\n"
							+ "Aborting...");
					return;
				}
			}

			//Performs diagnostics on the initial state of the simulation
			dataOutput.setIteration(0);
			diagnostics.performDiagnostics(0);
			diagnostics.output(dataOutput);

			for (int i = 0; i < iterations;) {
				// advance the simulation by one step
				simulation.step();

				i++;

				dataOutput.setIteration(i);
				diagnostics.performDiagnostics(i);
				diagnostics.output(dataOutput);
			}

			dataOutput.closeStreams();

			if (settings.getWriteToFile() == 1) {
				simulation.writeToFile();
			}

			ProfileInfo.printProfileInfo();

		} else {
			//ParallelSimulationCL simulationCL = new ParallelSimulationCL(settings);
			//simulationCL.runParallelSimulation();

			//if (settings.getWriteToFile() == 1) {
			//	simulationCL.writeToFile();
			//}
		}
	}
}
