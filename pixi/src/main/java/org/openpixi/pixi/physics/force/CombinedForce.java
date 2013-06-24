package org.openpixi.pixi.physics.force;


import org.openpixi.pixi.physics.particles.Particle;

import java.util.ArrayList;

/**
 * Combines various forces into a single force.
 */
public class CombinedForce implements Force {

	public ArrayList<Force> forces = new ArrayList<Force>();

	/**
	 * Adds another force.
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

	public double getForceX(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getForceX(p);
		}
		return sum;
	}

	public double getForceY(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getForceY(p);
		}
		return sum;
	}

	public double getPositionComponentofForceX(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getPositionComponentofForceX(p);
		}
		return sum;
	}

	public double getPositionComponentofForceY(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getPositionComponentofForceY(p);
		}
		return sum;
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getTangentVelocityComponentOfForceX(p);
		}
		return sum;
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getTangentVelocityComponentOfForceY(p);
		}
		return sum;
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getNormalVelocityComponentofForceX(p);
		}
		return sum;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getNormalVelocityComponentofForceY(p);
		}
		return sum;
	}

	public double getBz(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getBz(p);
		}
		return sum;
	}

	public double getLinearDragCoefficient(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getLinearDragCoefficient(p);
		}
		return sum;
	}

	public void remove(Force force) {
		forces.remove(force);
	}
}
