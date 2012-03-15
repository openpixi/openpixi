package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.*;

public class BoundingBox {
	
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
	
	public BoundingBox(Particle2D particle) {
		
		this.xMin = particle.x - 3 * particle.radius;
		this.xMax = particle.x + 3 * particle.radius;
		this.yMin = particle.y - 3 * particle.radius;
		this.yMax = particle.y + 3 * particle.radius;
	}
	
	
}
