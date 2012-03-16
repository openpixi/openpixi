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


public class ParticleMover {

	public static void particlePush(Simulation s) {
		for (Particle2D p : s.particles) {
			s.psolver.step(p, s.f, s.tstep);
			s.boundary.check(p, s.f, s.psolver, s.tstep);
		}
		
	}

	public static void prepareAllParticles(Simulation s) {
		for (Particle2D p : s.particles) {
			s.psolver.prepare(p, s.f, s.tstep);
		}
	}

	public static void completeAllParticles(Simulation s) {
		for (Particle2D p : s.particles) {
			s.psolver.complete(p, s.f, s.tstep);
		}
	}
}
