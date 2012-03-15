package org.openpixi.pixi.physics.collision.util;

import org.openpixi.pixi.physics.*;

public class SweepParticle {
	
	public BoundingBox bb;
	public Particle2D particle;
	public final int axis;
	public boolean begin;
	public double value;
	
	public SweepParticle(BoundingBox bb, Particle2D particle, int axis, boolean begin) {
		
		this.bb = bb;
		this.particle = particle;
		this.axis = axis;
		this.begin = begin;
	}
	
	public double updateGetValue() {
		if(this.begin) {
			if(this.axis == 0) {
				return bb.xMin;
			}
			else {
				return bb.yMin;
			}
		}
		else {
			if(this.axis == 0) {
				return bb.xMax;
			}
			else {
				return bb.yMax;
			}
		}
	}

}
