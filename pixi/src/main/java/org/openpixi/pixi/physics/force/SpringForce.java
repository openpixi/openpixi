package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;

public class SpringForce implements Force {

	public double getForceX(Particle2D par)
	{
		return 0;
	}

	public double getForceY(Particle2D par)
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

	public double getNormalVelocityComponentofForceX(Particle2D p) {
		return 0;
	}

	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return 0;
	}

	public double getBz(Particle2D p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle2D p) {
		return 0;
	}
	
}
