package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;

public class ConstantForce implements Force {

	/** Constant gravity in x-direction */
	public double gx;
	/** Constant gravity in y-direction */
	public double gy;
	/** Drag coefficient */
	public double drag;
	/** Electric field in x - direction */
	public double ex;
	/** Electric field in y - direction */
	public double ey;
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
		drag = 0;
		ex = 0;
		ey = 0;
		bz = 0;
	}

	public double getForceX(Particle2D p) {
		return -drag * p.vx + p.mass * gx + p.charge * ex +
				p.charge * p.vy * bz;
	}

	public double getForceY(Particle2D p) {
		return - drag * p.vy + p.mass * gy + p.charge * ey -
				p.charge * p.vx * bz;
	}

	public double getPositionComponentofForceX(Particle2D p) {
		return p.mass * gx + p.charge * ex;
	}

	public double getPositionComponentofForceY(Particle2D p) {
		return p.mass * gy + p.charge * ey;
	}

	public double getTangentVelocityComponentOfForceX(Particle2D p) {
		
		return - drag * p.vx;
	}

	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		
		return - drag * p.vy;
	}

	public double getNormalVelocityComponentofForceX(Particle2D p) {
		return p.charge * p.vy * bz;
	}

	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return - p.charge * p.vx * bz;
	}

	public double getBz(Particle2D p) {
		return bz;
	}

	public double getLinearDragCoefficient(Particle2D p) {
		return drag;
	}
	
}
