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
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		double uy = p.vy * gamma;
		
		return -drag * ux + p.mass * gx + p.charge * ex +
				p.charge * uy * bz;
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		double uy = p.vy * gamma;
		
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
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return - drag * ux;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return - drag * uy;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return p.charge * uy * bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
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
