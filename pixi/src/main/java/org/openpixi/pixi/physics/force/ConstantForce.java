package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.particles.Particle;

public class ConstantForce implements Force {

	/** Constant gravity in x-direction */
	public double gx;
	/** Constant gravity in y-direction */
	public double gy;
	/** Constant gravity in z-direction */
	public double gz;
	/** Drag coefficient */
	public double drag;
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
		gx = 0;
		gy = 0;
		gz = 0;
		drag = 0;
		ex = 0;
		ey = 0;
		ez = 0;
		bx = 0;
		by = 0;
		bz = 0;
	}

	public double getForceX(Particle p) {
		return -drag * p.getVx() + p.getMass() * gx + p.getCharge() * ex +
				p.getCharge() * ( p.getVy() * bz - p.getVz() * by );
	}

	public double getForceY(Particle p) {
		return - drag * p.getVy() + p.getMass() * gy + p.getCharge() * ey +
				p.getCharge() * ( p.getVz() * bx - p.getVx() * bz );
	}
	
	public double getForceZ(Particle p) {
		return - drag * p.getVz() + p.getMass() * gz + p.getCharge() * ez +
				p.getCharge() * ( p.getVx() * by - p.getVy() * bx );
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.getMass() * gx + p.getCharge() * ex;
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.getMass() * gy + p.getCharge() * ey;
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		
		return - drag * p.getVx();
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		
		return - drag * p.getVy();
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		return p.getCharge() * p.getVy() * bz;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		return - p.getCharge() * p.getVx() * bz;
	}

	public double getBz(Particle p) {
		return bz;
	}

	public double getLinearDragCoefficient(Particle p) {
		return drag;
	}
	
}
