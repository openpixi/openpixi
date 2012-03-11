package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.boundary.*;

public class Simulation {
	
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
	public static Particle2D[] particles = new Particle2D[num_particles];
	static Force  f= new Force();
	static Boundary boundary = new HardWallBoundary();
		
	private static void CreateParticles(int num_particles, double particle_radius) {
		for (int i = 0; i < num_particles; i++) {
			particles[i] = new Particle2D();
			particles[i].x = width * Math.random();
			particles[i].y = height * Math.random();
			particles[i].radius = particle_radius;
			particles[i].vx = 10 * Math.random();
			particles[i].vy = 10 * Math.random();
			particles[i].mass = 1;
			if (Math.random() > 0.5) {
				particles[i].charge = 1;
			} else {
				particles[i].charge = -1;
			}
			ParticleMover.solver.prepare(particles[i], f, tstep);
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
			System.out.println(particles[i].x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");			
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < steps; i++) {
			ParticleMover.particlePush(num_particles);
			InterpolatorParticlesGrid.interpolateParticlesGrid(num_particles, particles);
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(particles[i].x);	
		}
		
		System.out.println("\nCurrent: ");
		
		for (int i = 0; i < Simulation.num_cells_x; i++) {
				System.out.println(InterpolatorParticlesGrid.jx[i][0]);
		}
		
		System.out.println("\nCalculation time: "+elapsed);
	}

}
