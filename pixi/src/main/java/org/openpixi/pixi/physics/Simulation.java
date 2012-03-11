package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.boundary.*;

public class Simulation {
	
	private static final int num_particles = 1000;
	private static final double particle_radius = 0.1;
	/**Total number of timesteps*/
	private static final int steps = 1000;
	/**Timestep*/
	private static final double tstep = 1;
	/**Width of simulated area*/
	private static final int width = 100;
	/**Height of simulated area*/
	private static  final int  height = 100;
	
	/**Contains current solver algorithm*/
	private static Solver solver = new Boris();
	/**Contains all Particle2D objects*/
	private static Particle2D[] pararray = new Particle2D[num_particles];
	private static Force  f= new Force();
	private static Boundary boundary = new HardWallBoundary();
	
	private static void CreateParticles(int NUM_PARTICLES, double PARTICLE_RADIUS) {
		for (int i = 0; i < NUM_PARTICLES; i++) {
			pararray[i] = new Particle2D();
			pararray[i].x = width * Math.random();
			pararray[i].y = height * Math.random();
			pararray[i].radius = PARTICLE_RADIUS;
			pararray[i].vx = 10 * Math.random();
			pararray[i].vy = 10 * Math.random();
			pararray[i].mass = 1;
			if (Math.random() > 0.5) {
				pararray[i].charge = 1;
			} else {
				pararray[i].charge = -1;
			}
			solver.prepare(pararray[i], f, tstep);
		}
	}
	
		private static void ParticleMover(int NUM_PARTICLES) {
		
		for (int i = 0; i < NUM_PARTICLES; i++) {
			solver.step(pararray[i], f, tstep);
			boundary.check(pararray[i], f, solver, tstep);
		}
		
	}
		/*
		private static void InterpolateToGrid(int NUM_PARTICLES) {
			
		for (int i = 0; i < NUM_PARTICLES; i++) {
		}
		
	}*/
		
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
			System.out.println(pararray[i].x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");			
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < steps; i++) {
			ParticleMover(num_particles);
			//InterpolateToGrid(NUM_PARTICLES);
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(pararray[i].x);	
		}
		
		System.out.println("\n Calculation time: "+elapsed);
	}

}
