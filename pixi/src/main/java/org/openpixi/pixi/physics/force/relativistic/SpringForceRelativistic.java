package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.force.Force;

public class SpringForceRelativistic implements Force {

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
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		double uy = p.vy * gamma;
		return p.charge * uy * getBz(p);
	}

	public double getNormalVelocityComponentofForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		double ux = p.vx * gamma;
		return - p.charge * ux * getBz(p);
	}

	public double getBz(Particle2D p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle2D p) {
		return 0;
	}

}
