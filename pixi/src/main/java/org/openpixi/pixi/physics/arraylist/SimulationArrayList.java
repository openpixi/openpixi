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

package org.openpixi.pixi.physics.arraylist;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Force;
import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.boundary.*;

public class SimulationArrayList {
	
	private static final int num_particles = 1000;
	private static final double particle_radius = 0.1;
	/**Total number of timesteps*/
	private static final int steps = 1000;
	/**Timestep*/
	static final double tstep = 1;
	/**Width of simulated area*/
	public static final int width = 100;
	/**Height of simulated area*/
	public static  final int  height = 100;
	
	static int num_cells_x = 10;
	static int num_cells_y = 10;
	static final double cell_width = width/num_cells_x;
	static final double cell_height = height/num_cells_y;
	
	/**Contains all Particle2D objects*/
	public static ArrayList<Particle2D> particles = new ArrayList<Particle2D>(num_particles);
	static Force  f= new Force();
	static Boundary boundary = new HardWallBoundary();
		
	private static void CreateParticles(int num_particles, double particle_radius) {
		particles.clear();
		particles.ensureCapacity(num_particles);
		for (int i = 0; i < num_particles; i++) {
			Particle2D p = new Particle2D();
			p.x = width * Math.random();
			p.y = height * Math.random();
			p.radius = particle_radius;
			p.vx = 10 * Math.random();
			p.vy = 10 * Math.random();
			p.mass = 1;
			if (Math.random() > 0.5) {
				p.charge = 1;
			} else {
				p.charge = -1;
			}
			ParticleMoverArrayList.solver.prepare(p, f, tstep);
			particles.add(p);
		}
	}
	
	public static void main(String[] args) {
				
		boundary.xmin = 0;
		boundary.xmax = (double) width;
		boundary.ymin = 0;
		boundary.ymax = (double) height;
				
		CreateParticles(num_particles, particle_radius);
		f.bz = 0.001;
		f.ex = 0.1;
		
		System.out.println("-------- INITIAL CONDITIONS--------");		
		
		for (int i=0; i < 10; i++) {
			System.out.println(particles.get(i).x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");			
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < steps; i++) {
			ParticleMoverArrayList.particlePush(num_particles);
			InterpolatorParticlesGridArrayList.interpolateParticlesGrid(num_particles, particles);
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(particles.get(i).x);	
		}
		
		System.out.println("\nCurrent: ");
		
		for (int i = 0; i < SimulationArrayList.num_cells_x; i++) {
				System.out.println(InterpolatorParticlesGridArrayList.jx[i][0]);
		}
		
		System.out.println("\nCalculation time: "+elapsed);
	}

}
