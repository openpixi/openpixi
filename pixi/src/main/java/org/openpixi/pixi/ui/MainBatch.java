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

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.methods.KineticEnergy;
import org.openpixi.pixi.diagnostics.methods.Potential;
import org.openpixi.pixi.profile.ProfileInfo;
import org.openpixi.pixi.ui.util.*;
import java.io.IOException;


public class MainBatch {

	/**Total number of iterations*/
	public static int iterations;
	public static int particleDiagnosticsIntervall;
	public static int gridDiagnosticsIntervall;
	private static String runid;

	private static Simulation s;
	private static Diagnostics diagnostics;
	private static EmptyParticleDataOutput pdo;
	private static EmptyGridDataOutput gdo;

	public static void main(String[] args) {
		Debug.checkAssertsEnabled();

		Settings settings = new Settings();
		if (args.length != 0){
			Parser parser = new Parser(settings);
			parser.parse(args[0]);
		}
		
		iterations = settings.getIterations();
		particleDiagnosticsIntervall = settings.getParticleDiagnosticsIntervall();
		gridDiagnosticsIntervall = settings.getGridDiagnosticsIntervall();
		runid = settings.getRunid();
		
		s = new Simulation(settings);
		
		settings.getGridDiagnostics().add(new Potential(s.grid));
		diagnostics = new Diagnostics(s.grid, s.particles, settings);
		
		if (args.length < 2) {
			pdo = new EmptyParticleDataOutput();
			gdo = new EmptyGridDataOutput();
		} else {
			if (args[1].substring(args[1].length() -1) != System.getProperty("file.separator")) {
				args[1] = args[1] + System.getProperty("file.separator");
			}
			try {
				pdo = new ParticleDataOutput(args[1], runid);
				gdo = new GridDataOutput(args[1], runid, s.grid);
			} catch (IOException e) {
				System.err.print("Something went wrong when creating output files for diagnostics! \n" +
						"Please specify an output directory with write access rights!\n" + 
						"The directory that you specified was " + args[1] + "\n" +
						"Aborting...");
						return;
			}
		}
		
		pdo.startIteration(0);
		diagnostics.particles();
		diagnostics.outputParticles(pdo);
		gdo.startIteration(0);
		diagnostics.grid();
		diagnostics.outputGrid(gdo);
		
		for (int i = 0; i < iterations; i++) {
			s.step();
			if ( i == particleDiagnosticsIntervall) {
				pdo.startIteration(i);
				diagnostics.particles();
				diagnostics.outputParticles(pdo);
				particleDiagnosticsIntervall += particleDiagnosticsIntervall;
			}
			if ( i == gridDiagnosticsIntervall) {
				gdo.startIteration(i);
				diagnostics.grid();
				diagnostics.outputGrid(gdo);
				gridDiagnosticsIntervall += gridDiagnosticsIntervall;
			}
		}
		
		pdo.closeStreams();
		gdo.closeStreams();
		
		ProfileInfo.printProfileInfo();
	}

}
