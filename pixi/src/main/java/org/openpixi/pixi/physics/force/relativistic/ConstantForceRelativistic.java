package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.RelativisticVelocity;
import org.openpixi.pixi.physics.force.ConstantForce;
import org.openpixi.pixi.physics.particles.Particle;

public class ConstantForceRelativistic extends ConstantForce {
	
	RelativisticVelocity relvelocity;
	
	/** New empty force */
	public ConstantForceRelativistic(double c)
	{
		super();
		reset();

		relvelocity = new RelativisticVelocity(c);
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
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		
		return -drag * vx + p.getMass() * gx + p.getCharge() * ex +
				p.getCharge() * vy * bz;
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		
		return - drag * vy + p.getMass() * gy + p.getCharge() * ey -
				p.getCharge() * vx * bz;
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
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		
		return - drag * vx;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		double vy = p.getVy() / gamma;
		
		return - drag * vy;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vy = p.getVy() / gamma;
		
		return p.getCharge() * vy * bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		
		return - p.getCharge() * vx * bz;
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
