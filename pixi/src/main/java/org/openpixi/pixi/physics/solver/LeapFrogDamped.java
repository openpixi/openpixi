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

public class LeapFrogDamped extends Solver{
	
	public LeapFrogDamped()
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
		
		//help coefficients for the dragging
		double help1_coef = 1 - f.drag * dt / (2 * p.mass);
		double help2_coef = 1 + f.drag * dt / (2 * p.mass);
		
		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.x += p.vx * dt;
		p.y += p.vy * dt;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.ax = (f.getPositionComponentofForceX(p) + f.getNormalVelocityComponentofForceX(p)) / p.mass;
		p.ay = (f.getPositionComponentofForceY(p) + f.getNormalVelocityComponentofForceY(p)) / p.mass;


		// v(t+3*dt/2) = v(t+dt/2) + a(t+dt)*dt
		p.vx = (p.vx * help1_coef + p.ax * dt) / help2_coef;
		p.vy = (p.vy * help1_coef + p.ay * dt) / help2_coef;
		
	}
	
	public void prepare(Particle2D p, Force f, double dt)
	{
		double help1_coef = 1 - f.drag * dt / (2 * p.mass);
		double help2_coef = 1 + f.drag * dt / (2 * p.mass);
		
		//a(t) = F(v(t), x(t)) / m
		p.ax = (f.getPositionComponentofForceX(p) + f.getNormalVelocityComponentofForceX(p)) / p.mass;
		p.ay = (f.getPositionComponentofForceY(p) + f.getNormalVelocityComponentofForceY(p)) / p.mass;

		
		//v(t + dt / 2) = v(t) + a(t)*dt / 2
		p.vx = (p.vx * help1_coef + p.ax * dt * 0.5) / help2_coef;
		p.vy = (p.vy * help1_coef + p.ay * dt * 0.5) / help2_coef;
	}
	
	public void complete(Particle2D p, Force f, double dt)
	{
		double help1_coef = 1 - f.drag * dt / (2 * p.mass);
		double help2_coef = 1 + f.drag * dt / (2 * p.mass);
		
		//v(t) = v(t + dt / 2) - a(t)*dt / 2
		p.vx = (p.vx * help2_coef - p.ax * dt * 0.5) / help1_coef;
		p.vy = (p.vy * help2_coef - p.ay * dt * 0.5) / help1_coef;
	}

}
