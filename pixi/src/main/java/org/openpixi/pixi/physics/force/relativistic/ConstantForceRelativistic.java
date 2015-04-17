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
		ex = 0;
		ey = 0;
		ez = 0;
		bx = 0;
		by = 0;
		bz = 0;
	}
	
	//getting the force in the x - direction
	@Override
	public double getForceX(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		double vz = p.getVz() / gamma;
		
		return  p.getCharge() * ex +
				p.getCharge() * ( vy * bz - vz * by );
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		double vz = p.getVz() / gamma;
		
		return  p.getCharge() * ey +
				p.getCharge() * ( vz * bx - vx * bz );
	}
	
	//getting the force in the z - direction
	@Override
	public double getForceZ(Particle p) {
		double gamma = relvelocity.calculateGamma(p);

		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		double vz = p.getVz() / gamma;

		return p.getCharge() * ez +
				p.getCharge() * (vx * by - vy * bx);
	}
}
