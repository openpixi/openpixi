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
import org.openpixi.pixi.physics.solver.Solver;

/**This class represents the simple Semi Implicit Euler algorithm.
 * For more information: 
 * http://en.wikipedia.org/wiki/Semi-implicit_Euler_method
 */
public class SemiImplicitEulerRelativistic implements Solver {
	
	RelativisticVelocity relvelocity;

	private SemiImplicitEulerRelativistic() {}
	
	public SemiImplicitEulerRelativistic(double c)
	{
		relvelocity = new RelativisticVelocity(c);
	}
	
	/**
	 * Semi Implicit Euler algorithm.
	 * @param p before the update: x(t), u(t), a(t);
	 *                 after the update: x(t+dt), u(t+dt), a(t);
	 *                  u(t) is the relativistic momentum
	 */
	public void step(Particle p, Force f, double step)
	{
		//a(t) = F(u(t), x(t)) / m
		p.setAx(f.getForceX(p) / p.getMass());
		p.setAy(f.getForceY(p) / p.getMass());
		
		// u(t+dt) = u(t) + a(t)*dt
		p.setVx(p.getVx() + p.getAx() * step);
		p.setVy(p.getVy() + p.getAy() * step);
		
		double gamma = relvelocity.calculateGamma(p);
		
		// x(t+dt) = x(t) + u(t+dt) * dt / gamma
		p.setX(p.getX() + p.getVx() * step / gamma);
		p.setY(p.getY() + p.getVy() * step / gamma);
		
	}

	public void prepare(Particle p, Force f, double step) {
	}

	public void complete(Particle p, Force f, double step){
	}
}
