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
package org.openpixi.pixi.physics.solver.relativistic;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.solver.Solver;

/**This class represents the LeapFrog algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 */
public class LeapFrogRelativistic implements Solver{
	
	RelativisticVelocity relvelocity;
	
	public LeapFrogRelativistic(double c)
	{
		relvelocity = new RelativisticVelocity(c);
	}

	/**
	 * LeapFrog algorithm. The damping is implemented with an linear error O(dt).
	 * Warning: the velocity is stored half a time step ahead of the position.
	 * @param p before the update: x(t), u(t+dt/2), a(t);
	 *                 after the update: x(t+dt), u(t+3*dt/2), a(t+dt)
	 *                  u(t) is the relativistic momentum
	 */
	public void step(Particle p, Force f, double dt) {
		
		double gamma = relvelocity.calculateGamma(p);
		
		// x(t+dt) = x(t) + c(t+dt/2) * dt / gamma
		p.setX(p.getX() + p.getVx() * dt / gamma);
		p.setY(p.getY() + p.getVy() * dt / gamma);

		// a(t+dt) = F(u(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.setAx(f.getForceX(p) / p.getMass());
		p.setAy(f.getForceY(p) / p.getMass());

		// u(t+3*dt/2) = u(t+dt/2) + a(t+dt)*dt
		p.setVx(p.getVx() + p.getAx() * dt);
		p.setVy(p.getVy() + p.getAy() * dt);
		
	}
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t+dt/2)
	 */
	public void prepare(Particle p, Force f, double dt)
	{
		//a(t) = F(v(t), x(t)) / m
		p.setAx(f.getForceX(p) / p.getMass());
		p.setAy(f.getForceY(p) / p.getMass());
		
		//v(t + dt / 2) = v(t) + a(t)*dt / 2
		p.setVx(p.getVx() + p.getAx() * dt);
		p.setVy(p.getVy() + p.getAy() * dt);
	}
	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t+dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle p, Force f, double dt)
	{
		//v(t) = v(t + dt / 2) - a(t)*dt / 2
		p.setVx(p.getVx() - p.getAx() * dt);
		p.setVy(p.getVy() - p.getAy() * dt);
	}

}
