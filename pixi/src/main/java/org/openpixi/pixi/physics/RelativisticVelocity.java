package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.particles.Particle;

public class RelativisticVelocity {
	
	private double c;
	
	public RelativisticVelocity(double c) {
		this.c = c;
	}
	
	/**gamma = sqrt[1 + (u / c)^2]. This expression is obtained by solving gamma = 1/sqrt(1-(v/c)^2) with v = u/gamma for gamma.*/
	public double calculateGamma(double ux, double uy) {		
		return Math.sqrt(1 + (ux * ux + uy * uy) / (c * c));
	}
	
	public double calculateGamma(Particle p) {
		return calculateGamma(p.getVx(), p.getVy());
	}

}
