package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;


public class Euler extends Solver {
	public static void algorithm(Particle2D p, Force f, double step)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
		
		// v(t+dt) = v(t) + a(t)*dt
		p.vx += p.ax * step;
		p.vy += p.ay * step;
		
		// x(t+dt) = x(t) + v(t+dt)*dt
		p.x += p.vx * step;
		p.y += p.vy * step;
		
	}

}
