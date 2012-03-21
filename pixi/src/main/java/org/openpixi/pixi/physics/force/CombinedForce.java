package org.openpixi.pixi.physics.force;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;

/**
 * Combines various forces into a single force.
 */
public class CombinedForce extends Force {

	ArrayList<Force> forces = new ArrayList<Force>();

	public CombinedForce() {
		super();
	}

	/**
	 * Adds another force.
	 * @param f
	 */
	public void add(Force force) {
		forces.add(force);
	}

	/**
	 * Clears all forces.
	 */
	public void clear() {
		forces.clear();
	}

	@Override
	public double getForceX(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getForceX(p);
		}
		return sum;
	}

	@Override
	public double getForceY(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getForceY(p);
		}
		return sum;
	}

	@Override
	public double getPositionComponentofForceX(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getPositionComponentofForceX(p);
		}
		return sum;
	}

	@Override
	public double getPositionComponentofForceY(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getPositionComponentofForceY(p);
		}
		return sum;
	}

	@Override
	public double getTangentVelocityComponentOfForceX(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getTangentVelocityComponentOfForceX(p);
		}
		return sum;
	}

	@Override
	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getTangentVelocityComponentOfForceY(p);
		}
		return sum;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getNormalVelocityComponentofForceX(p);
		}
		return sum;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getNormalVelocityComponentofForceY(p);
		}
		return sum;
	}

	@Override
	public double getBz(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getBz(p);
		}
		return sum;
	}

	@Override
	public double getLinearDragCoefficient(Particle2D p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getLinearDragCoefficient(p);
		}
		return sum;
	}
}
