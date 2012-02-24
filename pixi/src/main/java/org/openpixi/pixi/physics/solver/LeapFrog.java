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

	/**
	 * LeapFrog algorithm.
	 * Warning: the velocity is stored half a time step ahead of the position.
	 * @param particle before the update: x(t), v(t+dt/2), a(t);
	 *                 after the update: x(t+dt), v(t+3*dt/2), a(t+dt)
	 */
	public static void algorithm(Particle2D particle, Force f, double step) {
		// x(t+dt) = x(t) + v(t+dt/2)*dt
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceY(particle.vx, particle.vy, particle) / particle.mass;

		// v(t+3*dt/2) = v(t+dt/2) + a(t+dt)*dt
		particle.vx += particle.ax * step;
		particle.vy += particle.ay * step;
		
	}

	/**
	 * LeapFrog algorithm.
	 * The velocity is stored at the same times as the position.
	 * @param particle before the update: x(t), v(t), a(t);
	 *                 after the update: x(t+dt), v(t+dt), a(t+dt)
	 */
	public static void algorithmHalfStep(Particle2D particle, Force f, double step) {
		// v(t+dt/2) = v(t) + a(t)*dt/2
		particle.vx += particle.ax * step / 2.0;
		particle.vy += particle.ay * step / 2.0;

		// x(t+dt) = x(t) + v(t+dt/2)*dt
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceY(particle.vx, particle.vy, particle) / particle.mass;

		// v(t+dt) = v(t+dt/2) + a(t+dt)*dt/2
		particle.vx += particle.ax * step / 2.0;
		particle.vy += particle.ay * step / 2.0;
	}

}
