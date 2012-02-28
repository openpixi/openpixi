package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;


public class Euler extends Solver {

	public Euler()
	{
		super();
	}
	public void step(Particle2D p, Force f, double step)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		// x(t+dt) = x(t) + v(t)*dt
		p.x += p.vx * step;
		p.y += p.vy * step;

		// v(t+dt) = v(t) + a(t)*dt
		p.vx += p.ax * step;
		p.vy += p.ay * step;

		//a(t + dt) = F(v(t + dt), x(t + dt)) / m
		//p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		//p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
	}

	public void prepare(Particle2D p, Force f, double step)
	{
		
	}

	public void complete(Particle2D p, Force f, double step)
	{
		
	}

}