package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;

public class ConstantForce extends Force {

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
		super();
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
	
	//getting the force in the x - direction
	@Override
	public double getForceX(Particle2D p) {
		return -drag * p.vx + p.mass * gx + p.charge * ex +
				p.charge * p.vy * bz;
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D p) {
		return - drag * p.vy + p.mass * gy + p.charge * ey -
				p.charge * p.vx * bz;
	}

	@Override
	public double getPositionComponentofForceX(Particle2D p) {
		return p.mass * gx + p.charge * ex;
	}

	@Override
	public double getPositionComponentofForceY(Particle2D p) {
		return p.mass * gy + p.charge * ey;
	}

	@Override
	public double getTangentVelocityComponentOfForceX(Particle2D p) {
		
		return - drag * p.vx;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		
		return - drag * p.vy;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D p) {
		return p.charge * p.vy * bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return - p.charge * p.vx * bz;
	}

	@Override
	public double getBz(Particle2D p) {
		return bz;
	}

	@Override
	public double getLinearDragCoefficient(Particle2D p) {
		return drag;
	}
	
}
