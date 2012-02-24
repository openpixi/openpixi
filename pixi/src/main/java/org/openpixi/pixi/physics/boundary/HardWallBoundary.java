package org.openpixi.pixi.physics.boundary;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.solver.*;

public class HardWallBoundary extends Boundary {

	public HardWallBoundary() {
		super();
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle2D particle, Solver s) {

		//if the particle hits the walls
		if(particle.x - particle.radius < xmin)
		{
			particle.vx = Math.abs(particle.vx);
		} else if(particle.x + particle.radius > xmax)
		{
			particle.vx = - Math.abs(particle.vx);
		}
		if(particle.y - particle.radius < ymin)
		{
			particle.vy = Math.abs(particle.vy);
		} else if(particle.y + particle.radius > ymax)
		{
			particle.vy = - Math.abs(particle.vy);
		}
	}
}
