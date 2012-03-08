package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.boundary.*;

public class Simulation {
	
	private static final int NUM_PARTICLES = 1000;
	private static final double PARTICLE_RADIUS = 0.1;
	/**Total number of timesteps*/
	private static final int STEPS = 1000;
	/**Timestep*/
	private static final double TSTEP = 1;
	/**Width of simulated area*/
	private static final int WIDTH = 100;
	/**Height of simulated area*/
	private static  final int  HEIGHT = 100;
	
	/**Contains current solver algorithm*/
	private static Solver solver = new Boris();
	/**Contains all Particle2D objects*/
	private static Particle2D[] pararray = new Particle2D[NUM_PARTICLES];
	private static Force  f= new Force();
	private static Boundary boundary = new HardWallBoundary();
	
	private static void CreateParticles(int NUM_PARTICLES, double PARTICLE_RADIUS) {
		for (int i = 0; i < NUM_PARTICLES; i++) {
			pararray[i] = new Particle2D();
			pararray[i].x = WIDTH * Math.random();
			pararray[i].y = HEIGHT * Math.random();
			pararray[i].radius = PARTICLE_RADIUS;
			pararray[i].vx = 10 * Math.random();
			pararray[i].vy = 10 * Math.random();
			pararray[i].mass = 1;
			if (Math.random() > 0.5) {
				pararray[i].charge = 1;
			} else {
				pararray[i].charge = -1;
			}
			solver.prepare(pararray[i], f, TSTEP);
		}
	}
	
		private static void ParticleMover(int NUM_PARTICLES) {
		
		for (int i = 0; i < NUM_PARTICLES; i++) {
			solver.step(pararray[i], f, TSTEP);
			boundary.check(pararray[i], f, solver, TSTEP);
		}
		
	}
		/*
		private static void InterpolateToGrid(int NUM_PARTICLES) {
			
		for (int i = 0; i < NUM_PARTICLES; i++) {
		}
		
	}*/
		
	public static void main(String[] args) {
				
		boundary.xmin = 0;
		boundary.xmax = (double) WIDTH;
		boundary.ymin = 0;
		boundary.ymax = (double) HEIGHT;
				
		CreateParticles(NUM_PARTICLES, PARTICLE_RADIUS);
		f.bz = 0.001;
		f.ex = 0.1;
		
		System.out.println("-------- INITIAL CONDITIONS--------");		
		
		for (int i=0; i < 10; i++) {
			System.out.println(pararray[i].x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");			
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < STEPS; i++) {
			ParticleMover(NUM_PARTICLES);
			//InterpolateToGrid(NUM_PARTICLES);
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(pararray[i].x);	
		}
		
		System.out.println("\n Calculation time: "+elapsed);
	}

}
