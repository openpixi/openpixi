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

/**This class represents the simple Semi Implicit Euler algorithm.
 * For more information: 
 * http://en.wikipedia.org/wiki/Semi-implicit_Euler_method
 */
public class SemiImplicitEuler extends Solver {
	
	public SemiImplicitEuler()
	{
		super();
	}
	
	/**
	 * Semi Implicit Euler algorithm.
	 * @param p before the update: x(t), v(t), a(t);
	 *                 after the update: x(t+dt), v(t+dt), a(t);
	 */
	public void step(Particle2D p, Force f, double step)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;
		
		// v(t+dt) = v(t) + a(t)*dt
		p.vx += p.ax * step;
		p.vy += p.ay * step;
		
		// x(t+dt) = x(t) + v(t+dt)*dt
		p.x += p.vx * step;
		p.y += p.vy * step;
		
	}
	
}
