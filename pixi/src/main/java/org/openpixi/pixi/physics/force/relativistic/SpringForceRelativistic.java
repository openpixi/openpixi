package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;

public class SpringForceRelativistic implements Force {

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
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		double uy = p.getVy() * gamma;
		return p.getCharge() * uy * getBz(p);
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		double ux = p.getVx() * gamma;
		return - p.getCharge() * ux * getBz(p);
	}

	public double getBz(Particle p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}

}
