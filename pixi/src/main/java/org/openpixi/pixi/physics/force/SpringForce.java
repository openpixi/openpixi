package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;

public class SpringForce extends Force{
	
	public SpringForce()
	{
		super();
	}
	
	//getting the force in the x - direction
	@Override
	public double getForceX(Particle2D par)
	{
		return 0;
	}
	
	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D par)
	{
		return - 0.01 * (par.y - 200);
	}

	@Override
	public double getPositionComponentofForceX(Particle2D par)
	{
		return 0;
	}

	@Override
	public double getPositionComponentofForceY(Particle2D par)
	{
		return - 0.01 * (par.y - 200);
	}

	@Override
	public double getTangentVelocityComponentOfForceX(Particle2D par)
	{
		return 0;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle2D par)
	{
		return 0;
	}
	
}
