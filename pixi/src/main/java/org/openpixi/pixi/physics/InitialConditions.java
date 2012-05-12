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

import java.util.ArrayList;

import org.openpixi.pixi.physics.boundary.*;
import org.openpixi.pixi.physics.collision.*;
import org.openpixi.pixi.physics.fields.*;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.grid.*;
import org.openpixi.pixi.physics.solver.*;

public class InitialConditions {
	
	/**Random angle*/
	private static double phi;

	public static Simulation initEverything() {
		
		Simulation s = new Simulation();
		
		//basic simulation parameters
		s.tstep = 1;
		s.c = 1;
		s.width = 2 * s.c;
		s.height = 2 * s.c;
		
		//external forces
		s.f.clear();
		ConstantForce cf = new ConstantForce();
		s.f.add(cf);
		
		//creates 10 particles in simulation area
		s.particles = createRandomParticles(s.width, s.height, s.c, 10, 1);
		
		s.psolver = new Boris();
		//always do prepareAllParticles when psolver algorithm is changed!
		s.prepareAllParticles();
		
		//also adds a SimpleGridForce to the forces list
		s.grid = new YeeGrid(s);		
		s.boundary = new PeriodicBoundary(s);
		
		return s;
	}
	
	public static Simulation initRandomParticles(int count, int radius) {
		
		Simulation s = new Simulation();
		
		//basic simulation parameters
		s.tstep = 1;
		s.c = 3;
		s.width = 700;
		s.height = 500;
		
		//external forces
		s.f.clear();
		ConstantForce cf = new ConstantForce();
		s.f.add(cf);
		
		//creates 10 particles in simulation area
		s.particles = createRandomParticles(s.width, s.height, s.c, count, radius);
		
		s.psolver = new EulerRichardson();
		//always do prepareAllParticles when psolver algorithm is changed!
		s.prepareAllParticles();
		
		s.boundary = new HardWallBoundary(s);
				
		return s;
	}
	
	public static Simulation initGravity(int count, int radius) {

		Simulation s = new Simulation();
		
		//basic simulation parameters
		s.tstep = 1;
		s.c = 3;
		s.width = 700;
		s.height = 500;
		
		//external forces
		s.f.clear();
		ConstantForce cf = new ConstantForce();
		cf.gy = -1;
		s.f.add(cf);
		
		//creates 10 particles in simulation area
		s.particles = createRandomParticles(s.width, s.height, s.c, count, radius);
		
		s.psolver = new EulerRichardson();
		//always do prepareAllParticles when psolver algorithm is changed!
		s.prepareAllParticles();
		
		s.boundary = new HardWallBoundary(s);
				
		return s;
	}

	public static Simulation initElectric(int count, int radius) {

		Simulation s = new Simulation();
		
		//basic simulation parameters
		s.tstep = 1;
		s.c = 3;
		s.width = 700;
		s.height = 500;
		
		//external forces
		s.f.clear();
		ConstantForce cf = new ConstantForce();
		cf.ey = -1;
		s.f.add(cf);
		
		//creates 10 particles in simulation area
		s.particles = createRandomParticles(s.width, s.height, s.c, count, radius);
		
		s.psolver = new EulerRichardson();
		//always do prepareAllParticles when psolver algorithm is changed!
		s.prepareAllParticles();
		
		s.boundary = new HardWallBoundary(s);
				
		return s;
	}

	public static Simulation initMagnetic(int count, int radius) {

		Simulation s = new Simulation();
		
		//basic simulation parameters
		s.tstep = 1;
		s.c = 3;
		s.width = 700;
		s.height = 500;
		
		//external forces
		s.f.clear();
		ConstantForce cf = new ConstantForce();
		cf.bz = -1;
		s.f.add(cf);
		
		//creates 10 particles in simulation area
		s.particles = createRandomParticles(s.width, s.height, s.c, count, radius);
		
		s.psolver = new EulerRichardson();
		//always do prepareAllParticles when psolver algorithm is changed!
		s.prepareAllParticles();
		
		s.boundary = new HardWallBoundary(s);
				
		return s;
	}

	public static Simulation initSpring(int count, int radius) {
		
		Simulation s = new Simulation();
		
		//basic simulation parameters
		s.tstep = 1;
		s.c = 3;
		s.width = 700;
		s.height = 500;
		
		//external forces
		s.f.clear();
		SpringForce sf = new SpringForce();
		s.f.add(sf);
		ConstantForce cf = new ConstantForce();
		s.f.add(cf);
		
		//creates one particle
		s.particles.clear();
		
		for (int k = 0; k < count; k++) {
			Particle2D par = new Particle2D();
			par.x = s.width * Math.random();
			par.y = s.height * Math.random();
			par.radius = 15;
			par.vx = 10 * Math.random();
			par.vy = 0;
			par.mass = 1;
			par.charge = .001;
			s.particles.add(par);
		}
		
		s.psolver = new EulerRichardson();
		//always do prepareAllParticles when psolver algorithm is changed!
		s.prepareAllParticles();
		
		s.boundary = new PeriodicBoundary(s);
				
		return s;
	}

	public static Simulation initEmptySimulation() {
		
		Simulation s = new Simulation();		
		return s;
	}
	
	/**Creates particles on random positions with random speeds
	 * @param width		maximal x-coodrinate
	 * @param height	maximal y-coordinate
	 * @param maxspeed	maximal particle speed
	 * @param count		number of particles to be created
	 * @param radius	particle radius
	 * @return ArrayList of Particle2D
	 */
	public static ArrayList<Particle2D> createRandomParticles(double width, double height, double maxspeed, int count, double radius) {
		
		ArrayList<Particle2D> particlelist = new ArrayList<Particle2D>(count);
		
		for (int k = 0; k < count; k++) {
			Particle2D p = new Particle2D();
			p.x = width * Math.random();
			p.y = height * Math.random();
			p.radius = radius;
			phi = 2 * Math.PI * Math.random();
			p.vx = maxspeed * Math.cos(phi);
			p.vy = maxspeed * Math.sin(phi);
			p.mass = 1;
			if (Math.random() > 0.5) {
				p.charge = .1;
			} else {
				p.charge = -.1;
			}
			particlelist.add(p);
		}
		
		return particlelist;
	}

}
