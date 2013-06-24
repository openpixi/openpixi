package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.particles.Particle;

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
		this.xMin = particle.getX() - particle.getRadius();
		this.xMax = particle.getX() + particle.getRadius();
		this.yMin = particle.getY() - particle.getRadius();
		this.yMax = particle.getY() + particle.getRadius();
	}
	
	
}
