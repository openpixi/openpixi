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
import org.openpixi.pixi.diagnostics.LocalDiagnostics;
import org.openpixi.pixi.diagnostics.KineticEnergy;
import org.openpixi.pixi.profile.ProfileInfo;
import org.openpixi.pixi.ui.util.*;
import java.io.IOException;


public class MainBatch {

	public static final int num_particles = 100;
	public static final double particle_radius = 0.1;
	/**Total number of timesteps*/
	public static final int steps = 100;
	private static String runid = "test";

	private static Simulation s;
	private static LocalDiagnostics localDiagnostics;
	private static EmptyParticleDataOutput pdo;

	public static void main(String[] args) {
		Debug.checkAssertsEnabled();

		Settings stt = new Settings();
		s = new Simulation(stt);
		
		stt.getParticleDiagnostics().add(new KineticEnergy());
		localDiagnostics = new LocalDiagnostics(s.grid, s.particles, stt);
		
		if (args.length == 0) {
			pdo = new EmptyParticleDataOutput();
		} else {
			try {
				pdo = new ParticleDataOutput(args[0], runid);
			} catch (IOException e) {
				System.err.print("Something went wrong when creating output files for diagnostics! \n" +
						"Please specify an output directory with write access rights!\n" +
						"Aborting...");
						return;
			}
		}
		
		for (int i = 0; i < steps; i++) {
			s.step();
			localDiagnostics.perform(i);
			localDiagnostics.output(pdo, null);
		}
		
		pdo.closeStreams();
		
		ProfileInfo.printProfileInfo();
	}

}
