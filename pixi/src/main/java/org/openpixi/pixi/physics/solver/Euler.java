package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;


public class Euler {
	public static void algorithm(Particle2D particle, Force f, double step)
	{
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		
		particle.vx += particle.ax * step;
		particle.vy += particle.ay * step;
		
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;
		
	}

}
