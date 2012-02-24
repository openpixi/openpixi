package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;


public class Euler extends Solver {
	public static void algorithm(Particle2D particle, Force f, double step)
	{
		//a(t) = F(v(t), x(t)) / m
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceY(particle.vx, particle.vy, particle) / particle.mass;
		
		// v(t+dt) = v(t) + a(t)*dt
		particle.vx += particle.ax * step;
		particle.vy += particle.ay * step;
		
		// x(t+dt) = x(t) + v(t+dt)*dt
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;
		
	}

}
