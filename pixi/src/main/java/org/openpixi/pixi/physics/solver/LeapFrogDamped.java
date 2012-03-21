/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.force.Force;

/**This class represents the LeapFrog algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 */
public class LeapFrogDamped extends Solver{
	
	public LeapFrogDamped()
	{
		super();
	}

	/**
	 * LeapFrog algorithm. The damping is implemented with an error O(dt^2), the same error of accuracy that the algorithm has.
	 * Warning: the velocity is stored half a time step ahead of the position.
	 * @param p before the update: x(t), v(t+dt/2), a(t);
	 *                 after the update: x(t+dt), v(t+3*dt/2), a(t+dt)
	 */
	public void step(Particle2D p, Force f, double dt) {
		
		//help coefficients for the dragging
		double help1_coef = 1 - f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		double help2_coef = 1 + f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		
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
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t+dt/2)
	 */
	public void prepare(Particle2D p, Force f, double dt)
	{
		double help1_coef = 1 - f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		double help2_coef = 1 + f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		
		//a(t) = F(v(t), x(t)) / m
		p.ax = (f.getPositionComponentofForceX(p) + f.getNormalVelocityComponentofForceX(p)) / p.mass;
		p.ay = (f.getPositionComponentofForceY(p) + f.getNormalVelocityComponentofForceY(p)) / p.mass;

		
		//v(t + dt / 2) = v(t) + a(t)*dt / 2
		p.vx = (p.vx * help1_coef + p.ax * dt * 0.5) / help2_coef;
		p.vy = (p.vy * help1_coef + p.ay * dt * 0.5) / help2_coef;
	}
	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t+dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle2D p, Force f, double dt)
	{
		double help1_coef = 1 - f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		double help2_coef = 1 + f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		
		//v(t) = v(t + dt / 2) - a(t)*dt / 2
		p.vx = (p.vx * help2_coef - p.ax * dt * 0.5) / help1_coef;
		p.vy = (p.vy * help2_coef - p.ay * dt * 0.5) / help1_coef;
	}

}
