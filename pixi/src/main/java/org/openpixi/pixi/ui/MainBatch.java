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
import org.openpixi.pixi.profile.ProfileInfo;

public class MainBatch {

	public static final int num_particles = 100;
	public static final double particle_radius = 0.1;
	/**Total number of timesteps*/
	public static final int steps = 10;

	public static Simulation s;

	public static void main(String[] args) {
		Debug.checkAssertsEnabled();

		s = new Simulation(new Settings());

		System.out.println("-------- INITIAL CONDITIONS--------");

		for (int i=0; i < 10; i++) {
			System.out.println(s.particles.get(i).getX());
		}

		System.out.println("\n-------- SIMULATION RESULTS --------");

		for (int i = 0; i < steps; i++) {
			s.step();
		}

		for (int i=0; i < 10; i++) {
			System.out.println(s.particles.get(i).getX());
		}

		System.out.println("\nCurrent: ");

		for (int i = 0; i < s.grid.getNumCellsX(); i++) {
				System.out.println(s.grid.getJx(i,2));
		}

		System.out.println(s.grid.getCellWidth());
		System.out.println(s.grid.getCellHeight());

		ProfileInfo.printProfileInfo();
	}

}
