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

import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.ParticleMover;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.ConstantForce;
import org.openpixi.pixi.physics.force.SimpleGridForce;

public class MainBatch {

	public static final int num_particles = 1000;
	public static final double particle_radius = 0.1;
	/**Total number of timesteps*/
	public static final int steps = 1000;
	
	public static Simulation s1;

	public static void main(String[] args) {
		
		s1 = new Simulation();
				
		InitialConditions.createRandomParticles(s1, num_particles, particle_radius);
		ConstantForce force = new ConstantForce();
		force.bz = 0.001;
		force.ex = 0.1;
		s1.f.add(force);
		s1.f.add(new SimpleGridForce(s1));
		ParticleMover.prepareAllParticles(s1);
		
		System.out.println("-------- INITIAL CONDITIONS--------");		
		
		for (int i=0; i < 10; i++) {
			System.out.println(s1.particles.get(i).x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");		
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < steps; i++) {
			s1.step();
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(s1.particles.get(i).x);	
		}
		
		System.out.println("\nCurrent: ");
		
		for (int i = 0; i < s1.currentGrid.numCellsX; i++) {
				System.out.println(s1.currentGrid.jx[i][0]);
		}
		
		System.out.println("\nCalculation time: "+elapsed);
	}

}
