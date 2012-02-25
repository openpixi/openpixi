package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;


public class SemiImplicitEuler extends Solver {
	
	public SemiImplicitEuler()
	{
		super();
	}
	public void step(Particle2D p, Force f, double step)
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
	
	public void prepare(Particle2D p, Force f, double step)
	{
		
	}
	
	public void complete(Particle2D p, Force f, double step)
	{
		// v(t) = v(t + dt) - a(t)*dt
		p.vx -= p.ax * step;
		p.vy -= p.ay * step;
	}

}
