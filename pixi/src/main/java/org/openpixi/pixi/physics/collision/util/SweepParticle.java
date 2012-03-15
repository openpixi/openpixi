package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.*;

public class SweepParticle {
	
	public Particle2D particle;
	public final int axis;
	public boolean begin;
	public double value;
	
	public SweepParticle(Particle2D particle, int axis, boolean begin) {
		
		this.particle = particle;
		this.axis = axis;
		this.begin = begin;
	}

}
