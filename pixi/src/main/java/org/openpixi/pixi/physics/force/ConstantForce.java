package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.particles.Particle;

public class ConstantForce implements Force {

	/** Electric field in x - direction */
	public double ex;
	/** Electric field in y - direction */
	public double ey;
	/** Electric field in z - direction */
	public double ez;
	/** Magnetic field in x - direction */
	public double bx;
	/** Magnetic field in y - direction */
	public double by;
	/** Magnetic field in z - direction */
	public double bz;

	/** New empty force */
	public ConstantForce()
	{
		reset();
	}

	public void reset()
	{
		ex = 0;
		ey = 0;
		ez = 0;
		bx = 0;
		by = 0;
		bz = 0;
	}

	public double getForceX(Particle p) {
		return  p.getCharge() * ex +
				p.getCharge() * ( p.getVy() * bz - p.getVz() * by );
	}

	public double getForceY(Particle p) {
		return  p.getCharge() * ey +
				p.getCharge() * ( p.getVz() * bx - p.getVx() * bz );
	}
	
	public double getForceZ(Particle p) {
		return  p.getCharge() * ez +
				p.getCharge() * ( p.getVx() * by - p.getVy() * bx );
	}
	
}
