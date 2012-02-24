package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.Force;
import org.openpixi.pixi.physics.Particle2D;

public class LeapFrogHalfStep {
	public static void algorithm(Particle2D p, Force f, double dt) {
		
		/**
		 * LeapFrog algorithm.
		 * The velocity is stored at the same times as the position.
		 * @param p before the update: x(t), v(t), a(t);
		 *                 after the update: x(t+dt), v(t+dt), a(t+dt)
		 */
		// v(t+dt/2) = v(t) + a(t)*dt/2
		p.vx += p.ax * dt / 2.0;
		p.vy += p.ay * dt / 2.0;

		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.x += p.vx * dt;
		p.y += p.vy * dt;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		// v(t+dt) = v(t+dt/2) + a(t+dt)*dt/2
		p.vx += p.ax * dt / 2.0;
		p.vy += p.ay * dt / 2.0;
	}

}
