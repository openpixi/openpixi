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

package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.solver.*;

public class ParticleMover {

	/**Contains current solver algorithm*/
	public static Solver solver = new Boris();
	
	public static void particlePush() {
		
		for (Particle2D p : Simulation.particles) {
			solver.step(p, Simulation.f, Simulation.tstep);
			Simulation.boundary.check(p, Simulation.f, solver, Simulation.tstep);
		}
		
	}

	public static void prepareAllParticles() {
		for (Particle2D p : Simulation.particles) {
			solver.prepare(p, Simulation.f, Simulation.tstep);
		}
	}

	public static void completeAllParticles() {
		for (Particle2D p : Simulation.particles) {
			solver.complete(p, Simulation.f, Simulation.tstep);
		}
	}
}
