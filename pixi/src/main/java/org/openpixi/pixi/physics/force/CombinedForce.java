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
	
	public double getForceZ(Particle p) {
		double sum = 0;
		for (Force f : forces) {
			sum += f.getForceZ(p);
		}
		return sum;
	}

	public void remove(Force force) {
		forces.remove(force);
	}
}
