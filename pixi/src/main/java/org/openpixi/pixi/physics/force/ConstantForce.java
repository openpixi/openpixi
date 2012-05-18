package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle;

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

	public double getForceX(Particle p) {
		return -drag * p.vx + p.mass * gx + p.charge * ex +
				p.charge * p.vy * bz;
	}

	public double getForceY(Particle p) {
		return - drag * p.vy + p.mass * gy + p.charge * ey -
				p.charge * p.vx * bz;
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.mass * gx + p.charge * ex;
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.mass * gy + p.charge * ey;
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		
		return - drag * p.vx;
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		
		return - drag * p.vy;
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		return p.charge * p.vy * bz;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		return - p.charge * p.vx * bz;
	}

	public double getBz(Particle p) {
		return bz;
	}

	public double getLinearDragCoefficient(Particle p) {
		return drag;
	}
	
}
