package org.openpixi.pixi.physics.boundary;

import org.openpixi.pixi.physics.Particle2D;

public class HardWallBoundary {
	public double xmin;
	public double xmax;
	public double ymin;
	public double ymax;

	/**
	 * Constructor
	 */
	public HardWallBoundary() {
	}
	
	public void setBoundaries(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle2D particle) {

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
