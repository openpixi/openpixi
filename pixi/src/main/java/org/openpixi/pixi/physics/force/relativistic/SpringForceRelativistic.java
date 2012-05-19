package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.RelativisticVelocity;
import org.openpixi.pixi.physics.force.Force;

public class SpringForceRelativistic implements Force {
	
	RelativisticVelocity relvelocity;
	
	/** New empty force */
	public SpringForceRelativistic(double c) {
		
		super();
		
		relvelocity = new RelativisticVelocity(c);
	}

	public double getForceX(Particle par)
	{
		return 0;
	}

	public double getForceY(Particle par)
	{
		return - 0.01 * (par.getY() - 200);
	}

	public double getPositionComponentofForceX(Particle par)
	{
		return 0;
	}

	public double getPositionComponentofForceY(Particle par)
	{
		return - 0.01 * (par.getY() - 200);
	}

	public double getTangentVelocityComponentOfForceX(Particle par)
	{
		return 0;
	}

	public double getTangentVelocityComponentOfForceY(Particle par)
	{
		return 0;
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vy = p.getVy() / gamma;
		return p.getCharge() * vy * getBz(p);
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		return - p.getCharge() * vx * getBz(p);
	}

	public double getBz(Particle p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}

}
