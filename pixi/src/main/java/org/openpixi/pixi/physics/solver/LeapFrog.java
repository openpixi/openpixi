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
public class LeapFrog extends Solver{
	
	public LeapFrog()
	{
		super();
	}

	/**
	 * LeapFrog algorithm. The damping is implemented with an linear error O(dt).
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
		p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;

		// v(t+3*dt/2) = v(t+dt/2) + a(t+dt)*dt
		p.vx += p.ax * dt;
		p.vy += p.ay * dt;
		
	}
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t+dt/2)
	 */
	public void prepare(Particle2D p, Force f, double dt)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;
		
		//v(t + dt / 2) = v(t) + a(t)*dt / 2
		p.vx += p.ax * dt / 2;
		p.vy += p.ay * dt / 2;
	}
	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t+dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle2D p, Force f, double dt)
	{
		//v(t) = v(t + dt / 2) - a(t)*dt / 2
		p.vx -= p.ax * dt / 2;
		p.vy -= p.ay * dt / 2;
	}

}
