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

import org.openpixi.pixi.physics.boundary.HardWallBoundary;
import org.openpixi.pixi.physics.boundary.PeriodicBoundary;
import org.openpixi.pixi.physics.force.*;

public class InitialConditions {
	
	/**
	 * Creates all particles, initializes boundary and force
	 * @param s Simulation
	 * @param count number of particles
	 * @param radius radius of each particle
	 */
	public static void initRandomParticles(Simulation s, int count, double radius) {

		ConstantForce force = new ConstantForce();
		force.gy = -1; // -ConstantsSI.g;
		s.f.clear();
		s.f.add(force);
		
		InitialConditions.createRandomParticles(s, count, radius);
		InitialConditions.setHardWallBoundary(s);
	}

	public static void initGravity(Simulation s, int count) {
		
		ConstantForce force = new ConstantForce();
		force.gy = -1; // -ConstantsSI.g;
		s.f.clear();
		s.f.add(force);
		
		InitialConditions.createRandomParticles(s, count, 15);
		InitialConditions.setHardWallBoundary(s);
	}

	public static void initElectric(Simulation s, int count) {
		
		ConstantForce force = new ConstantForce();
		force.ey = -1; // -ConstantsSI.g;
		s.f.clear();
		s.f.add(force);
		
		InitialConditions.createRandomParticles(s, count, 15);
		InitialConditions.setHardWallBoundary(s);
	}

	public static void initMagnetic(Simulation s, int count) {
		
		ConstantForce force = new ConstantForce();
		force.bz = .1; // -ConstantsSI.g;
		s.f.clear();
		s.f.add(force);
		
		InitialConditions.createRandomParticles(s, count, 15);
		InitialConditions.setPeriodicBoundary(s);
	}

	public static void initSpring(Simulation s, int count) {

		s.f.clear();
		s.f.add(new SpringForce());
		
		s.particles.clear();
		
		for (int k = 0; k < count; k++) {
			Particle2D par = new Particle2D();
			par.x = s.width * Math.random();
			par.y = s.height * Math.random();
			par.radius = 15;
			par.vx = 10 * Math.random();
			par.vy = 0;
			par.mass = 1;
			par.charge = .1;
			s.particles.add(par);
		}
	
		InitialConditions.setPeriodicBoundary(s);
	}

	public static void createRandomParticles(Simulation s, int count, double radius) {
		s.particles.clear();
		//s.collision.det.reset();
		for (int k = 0; k < count; k++) {
			Particle2D par = new Particle2D();
			par.x = s.width * Math.random();
			par.y = s.height * Math.random();
			par.radius = radius;
			par.vx = 10 * Math.random();
			par.vy = 10 * Math.random();
			par.mass = 1;
			if (Math.random() > 0.5) {
				par.charge = .1;
			} else {
				par.charge = -.1;
			}
			s.particles.add(par);
		}
		//s.collision.det.add(s.particles);
	}

	public static void setHardWallBoundary(Simulation s) {
		s.boundary = new HardWallBoundary();
	}

	public static void setPeriodicBoundary(Simulation s) {
		s.boundary = new PeriodicBoundary();
	}

}
