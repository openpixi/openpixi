package org.openpixi.pixi.physics.force;


import org.openpixi.pixi.physics.particles.IParticle;

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

    public double getForce(int i, IParticle p)
    {
        double sum = 0.0;
        for(Force f : forces)
        {
            sum += f.getForce(i, p);
        }
        return sum;
    }

	public double getForceX(IParticle p) {
		return getForce(0, p);
	}

	public double getForceY(IParticle p) {
        return getForce(1, p);
	}
	
	public double getForceZ(IParticle p) {
        return getForce(2, p);
	}

	public void remove(Force force) {
		forces.remove(force);
	}
}
