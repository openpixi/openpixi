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
import org.openpixi.pixi.physics.particles.Particle;

/**
 * This class is based on the simple Euler-Richardson algorithm (it
 * represents a neat way of finding the numerical solutions of a differential
 * equation, based on the Euler algorithm).
 * 
 * <p>See also:
 * <a href="http://www.physics.udel.edu/~bnikolic/teaching/phys660/numerical_ode/node4.html">
 * http://www.physics.udel.edu/~bnikolic/teaching/phys660/numerical_ode/node4.html</a>
 * </p>
 */
public class EulerRichardson implements Solver{
	
	public EulerRichardson()
	{
		super();
	}
	
	/**
	 * Euler - Richardson algorithm.
	 * @param p before the update: x(t), v(t), a(t);
	 *                 after the update: x(t+dt), v(t+dt), a(t+dt/2)
	 */
	public void step(Particle p, Force f, double step)
	{
		//saving the starting value of the position & velocity
		double xstart = p.getX();
		double ystart = p.getY();
		double vxstart = p.getVx();
		double vystart = p.getVy();
		
		//a(t) = F(v(t), x(t)) / m
		p.setAx(f.getForceX(p) / p.getMass());
		p.setAy(f.getForceY(p) / p.getMass());
		
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		//v(t + dt / 2) = v(t) + a(t) * dt / 2
		p.setVx(p.getVx() + p.getAx() * step / 2);
		p.setVy(p.getVy() + p.getAy() * step / 2);
		
		//x(t + dt / 2) = x(t) + v(t) * dt / 2
		p.setX(p.getX() + p.getVx() * step / 2);
		p.setY(p.getY() + p.getVy() * step / 2); 
		
		//a(t + dt / 2) = F(v(t + dt / 2), x(t + dt / 2)) / m
		p.setAx(f.getForceX(p) / p.getMass());
		p.setAy(f.getForceY(p) / p.getMass());
		
		//x(t + dt) = x(t) + v(t + dt / 2) * dt
		p.setX(xstart + p.getVx() * step);
		p.setY(ystart + p.getVy() * step);
		
		//v(t + dt) = v(t) + a(t + dt / 2) * dt
		p.setVx(vxstart + p.getAx() * step);
		p.setVy(vystart + p.getAy() * step);
		
	}

	public void prepare(Particle p, Force f, double step) {
	}

	public void complete(Particle p, Force f, double step){
	}
}
