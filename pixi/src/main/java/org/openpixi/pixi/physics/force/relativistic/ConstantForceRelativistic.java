package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.force.ConstantForce;

import java.lang.Math;

public class ConstantForceRelativistic extends ConstantForce {
	
	/** New empty force */
	public ConstantForceRelativistic()
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
		
		double ux = p.vx * Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		double uy = p.vy * Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
		
		return -drag * ux + p.mass * gx + p.charge * ex +
				p.charge * uy * bz;
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D p) {
		
		double ux = p.vx * Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		double uy = p.vy * Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
		
		return - drag * uy + p.mass * gy + p.charge * ey -
				p.charge * ux * bz;
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
		double ux = p.vx * Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		
		return - drag * ux;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		double uy = p.vy * Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
		
		return - drag * uy;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D p) {
		double uy = p.vy * Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
		
		return p.charge * uy * bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		double ux = p.vx * Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		
		return - p.charge * ux * bz;
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
