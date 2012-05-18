package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.Particle;
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
	public double getForceX(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		double uy = p.getVy() * gamma;
		
		return -drag * ux + p.getMass() * gx + p.getCharge() * ex +
				p.getCharge() * uy * bz;
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		double uy = p.getVy() * gamma;
		
		return - drag * uy + p.getMass() * gy + p.getCharge() * ey -
				p.getCharge() * ux * bz;
	}

	@Override
	public double getPositionComponentofForceX(Particle p) {
		return p.getMass() * gx + p.getCharge() * ex;
	}

	@Override
	public double getPositionComponentofForceY(Particle p) {
		return p.getMass() * gy + p.getCharge() * ey;
	}

	@Override
	public double getTangentVelocityComponentOfForceX(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		
		return - drag * ux;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.getVy() * gamma;
		
		return - drag * uy;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.getVy() * gamma;
		
		return p.getCharge() * uy * bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		
		return - p.getCharge() * ux * bz;
	}

	@Override
	public double getBz(Particle p) {
		return bz;
	}

	@Override
	public double getLinearDragCoefficient(Particle p) {
		return drag;
	}
	
}
