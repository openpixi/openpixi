package org.openpixi.pixi.physics;

public class SpringForce extends Force{
	
	public SpringForce()
	{
		super();
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
}
