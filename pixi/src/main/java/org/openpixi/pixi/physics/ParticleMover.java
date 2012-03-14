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
	static Solver solver = new Boris();
	
	static void particlePush(int num_particles) {
		
		for (int i = 0; i < num_particles; i++) {
			solver.step(Simulation.particles.get(i), Simulation.f, Simulation.tstep);
			Simulation.boundary.check(Simulation.particles.get(i), Simulation.f, solver, Simulation.tstep);
		}
		
	}

}
