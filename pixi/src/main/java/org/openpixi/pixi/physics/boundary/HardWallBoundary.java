package org.openpixi.pixi.physics.boundary;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.solver.*;

public class HardWallBoundary extends Boundary {

	public HardWallBoundary() {
		super();
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle2D particle, Force f, Solver s, double step) {
		
		
		//if the particle hits the walls
		if(particle.x - particle.radius < xmin)
		{
			s.complete(particle, f, step);
			particle.vx = Math.abs(particle.vx);
			s.prepare(particle, f, step);
		} else if(particle.x + particle.radius > xmax)
		{
			s.complete(particle, f, step);
			particle.vx = - Math.abs(particle.vx);
			s.prepare(particle, f, step);
		}
		if(particle.y - particle.radius < ymin)
		{
			s.complete(particle, f, step);
			particle.vy = Math.abs(particle.vy);
			s.prepare(particle, f, step);
		} else if(particle.y + particle.radius > ymax)
		{
			s.complete(particle, f, step);
			particle.vy = - Math.abs(particle.vy);
			s.prepare(particle, f, step);
		}
		
	}
}
