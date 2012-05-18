package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.*;

public class BoundingBox {
	
	public Particle particle;
	public double xMin;
	public double xMax;
	public double yMin;
	public double yMax;
	
	public BoundingBox(Particle particle) {
		
		this.particle = particle;
		update();
	}

	public void update() {
		this.xMin = particle.x - particle.radius;
		this.xMax = particle.x + particle.radius;
		this.yMin = particle.y - particle.radius;
		this.yMax = particle.y + particle.radius;
	}
	
	
}
