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

import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;

/**This class represents the LeapFrog algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 */
public class LeapFrogHalfStep implements Solver{
	
	public LeapFrogHalfStep()
	{
		super();
	}
	public void step(Particle p, Force f, double dt) {
		
		/**
		 * LeapFrog algorithm.
		 * The velocity is stored at the same times as the position.
		 * @param p before the update: x(t), v(t), a(t);
		 *                 after the update: x(t+dt), v(t+dt), a(t+dt)
		 */
		// v(t+dt/2) = v(t) + a(t)*dt/2
		p.setVx(p.getVx() + p.getAx() * dt / 2.0);
		p.setVy(p.getVy() + p.getAy() * dt / 2.0);

		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.setX(p.getX() + p.getVx() * dt);
		p.setY(p.getY() + p.getVy() * dt);

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.setAx(f.getForceX(p) / p.getMass());
		p.setAy(f.getForceY(p) / p.getMass());

		// v(t+dt) = v(t+dt/2) + a(t+dt)*dt/2
		p.setVx(p.getVx() + p.getAx() * dt / 2.0);
		p.setVy(p.getVy() + p.getAy() * dt / 2.0);
		
		
	}

	public void prepare(Particle p, Force f, double step) {
	}

	public void complete(Particle p, Force f, double step){
	}
}
