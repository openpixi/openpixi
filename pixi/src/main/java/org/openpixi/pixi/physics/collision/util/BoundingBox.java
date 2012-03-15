package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.*;

public class BoundingBox {
	
	public Particle2D particle;
	public double xMin;
	public double xMax;
	public double yMin;
	public double yMax;
	
	public BoundingBox(Particle2D particle) {
		
		this.particle = particle;
		this.xMin = particle.x - 3 * particle.radius;
		this.xMax = particle.x + 3 * particle.radius;
		this.yMin = particle.y - 3 * particle.radius;
		this.yMax = particle.y + 3 * particle.radius;
	}
	
	
}
