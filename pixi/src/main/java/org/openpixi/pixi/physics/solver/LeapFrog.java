/*The algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 * The first equations that are as comments now are just a different way of writing the algorithm.
 * The other equations that are used now one can find in the second link.
 * Personally I find the second way better, but it is just a question of taste.
 */

package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;

public class LeapFrog extends Solver{
	
	public LeapFrog()
	{
		super();
	}

	/**
	 * LeapFrog algorithm.
	 * Warning: the velocity is stored half a time step ahead of the position.
	 * @param p before the update: x(t), v(t+dt/2), a(t);
	 *                 after the update: x(t+dt), v(t+3*dt/2), a(t+dt)
	 */
	public void step(Particle2D p, Force f, double dt) {
		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.x += p.vx * dt;
		p.y += p.vy * dt;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		// v(t+3*dt/2) = v(t+dt/2) + a(t+dt)*dt
		p.vx += p.ax * dt;
		p.vy += p.ay * dt;
		
	}
	
	public void prepare(Particle2D p, Force f, double dt)
	{
		//a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
		
		//v(t + dt / 2) = v(t) + a(t+dt)*dt / 2
		p.vx += p.ax * dt / 2.0;
		p.vy += p.ay * dt / 2.0;
	}
	
	public void finalizing(Particle2D p, Force f, double dt)
	{
		//v(t) = v(t + dt / 2) - a(t)*dt / 2
		p.vx -= p.ax * dt / 2.0;
		p.vy -= p.ay * dt / 2.0;
	}

}
