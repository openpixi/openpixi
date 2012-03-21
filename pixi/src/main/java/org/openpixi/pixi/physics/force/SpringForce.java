package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;

public class SpringForce extends Force{
	
	public SpringForce(Simulation s)
	{
		super(s);
	}
	
	//getting the force in the x - direction
	public double getForceX(double vx, double vy, Particle2D par)
	{
		return 0;
	}
	
	//getting the force in the y - direction
	public double getForceY(double vx, double vy, Particle2D par)
	{
		return - 0.01 * (par.y - 200);
	}
	
	public double getPositionComponentofForceX(Particle2D par)
	{
		return 0;
	}
	
	public double getPositionComponentofForceY(Particle2D par)
	{
		return - 0.01 * (par.y - 200);
	}
	
	public double getTangentVelocityComponentOfForceX(Particle2D par)
	{
		return 0;
	}
	
	public double getTangentVelocityComponentOfForceY(Particle2D par)
	{
		return 0;
	}
	
}
