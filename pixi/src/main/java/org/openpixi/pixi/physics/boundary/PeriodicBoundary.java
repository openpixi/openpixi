package org.openpixi.pixi.physics.boundary;

import org.openpixi.pixi.physics.Particle2D;

public class PeriodicBoundary extends Boundary {

	public PeriodicBoundary() {
		super();
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle2D particle) {

		//if the particle hits the walls
		if(particle.x < xmin)
		{
			particle.x += xmax - xmin;
		} else if(particle.x > xmax)
		{
			particle.x -= xmax - xmin;
		}
		if(particle.y < ymin)
		{
			particle.y = ymax - ymin;
		} else if(particle.y > ymax)
		{
			particle.y -= ymax - ymin;
		}
	}
}
