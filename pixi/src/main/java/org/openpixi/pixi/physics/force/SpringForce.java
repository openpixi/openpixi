package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.particles.Particle;

public class SpringForce implements Force {

	public double getForceX(Particle par)
	{
		return 0;
	}

	public double getForceY(Particle par)
	{
		return - 0.01 * (par.getY() - 50);
	}

	public double getPositionComponentofForceX(Particle par)
	{
		return 0;
	}

	public double getPositionComponentofForceY(Particle par)
	{
		return - 0.01 * (par.getY() - 50);
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
		return 0;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		return 0;
	}

	public double getBz(Particle p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}
	
}
