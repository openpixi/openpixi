package org.openpixi.physics.solver;

import org.openpixi.physics.*;

public class Euler {
	
	private Particle2D particle;
	private Force f;
	
	public Euler(Particle2D particle, Force f)
	{
		this.particle = particle;
		this.f = f;
	}
	
	public Euler()
	{
		
	}
	public Particle2D getParticle2D()
	{
		return this.particle;
	}
	public void algorithm(double step)
	{
		particle.setAccelerationX(f.getForceX(particle.vx, particle.vy, particle) / particle.getMass());
		particle.setAccelerationY(f.getForceX(particle.vx, particle.vy, particle) / particle.getMass());
		
		//if the particle hits the walls
		if(particle.x < 0)
		{
			particle.x = 0;
			particle.vx = - particle.vx;
		}
		if(particle.x > particle.rightBoundary)
		{
			particle.x = particle.rightBoundary;
			particle.vx = - particle.vx;
		}
		if(particle.y < 0)
		{
			particle.y = 0;
			particle.vy = - particle.vy;
		}
		if(particle.y > particle.bottomBoundary)
		{
			particle.y = particle.bottomBoundary;
			particle.vy = - particle.vy;
		}
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		double vxmiddle = particle.vx + particle.getAccelerationX() * step / 2;
		double vymiddle = particle.vy + particle.getAccelerationY() * step / 2;
		
		//double xmiddle = x + vx * step / 2;    actually, this two equations are not needed, but I've written them
		//double ymiddle = y + vy * step / 2;    so the algorithm is complete
		
		double axmiddle = f.getForceX(vxmiddle, vymiddle, particle) / particle.getMass();
		double aymiddle = f.getForceY(vxmiddle, vymiddle, particle) / particle.getMass();
		
		particle.vx += axmiddle * step;
		particle.vy += aymiddle * step;
		
		particle.x += vxmiddle * step;
		particle.y += vymiddle * step;
		
		particle.setAccelerationX(f.getForceX(particle.vx, particle.vy, particle) / particle.getMass());
		particle.setAccelerationY(f.getForceY(particle.vx, particle.vy, particle) / particle.getMass());
	}

}
