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
public class EulerRichardson extends Solver{
	
	public EulerRichardson()
	{
		super();
	}
	
	public void step(Particle2D p, Force f, double step)
	{
		//saving the starting value of the position
		double xstart = p.x;
		double ystart = p.y;
		
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
		
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		//v(t + dt / 2) = v(t) + a(t) * dt / 2
		double vxmiddle = p.vx + p.ax * step / 2;
		double vymiddle = p.vy + p.ay * step / 2;
		
		//x(t + dt / 2) = x(t) + v(t) * dt / 2
		p.x += p.vx * step / 2;
		p.y += p.vy * step / 2; 
		
		//a(t + dt / 2) = F(v(t + dt / 2), x(t + dt / 2)) / m
		double axmiddle = f.getForceX(vxmiddle, vymiddle, p) / p.mass;
		double aymiddle = f.getForceY(vxmiddle, vymiddle, p) / p.mass;
		
		//v(t + dt) = v(t) + a(t + dt / 2) * dt
		p.vx += axmiddle * step;
		p.vy += aymiddle * step;
		
		//x(t + dt) = x(t) + v(t + dt / 2) * dt
		p.x = xstart + vxmiddle * step;
		p.y = ystart + vymiddle * step;
		
		//a(t) = F(v(t + dt), x(t + dt)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
	}
	
	public void prepare(Particle2D p, Force f, double step)
	{
		
	}
	
	public void complete(Particle2D p, Force f, double step)
	{
		double vxmiddle = p.vx + p.ax * step / 2;
		double vymiddle = p.vy + p.ay * step / 2;
		
		double axmiddle = f.getForceX(vxmiddle, vymiddle, p) / p.mass;
		double aymiddle = f.getForceY(vxmiddle, vymiddle, p) / p.mass;
		
		//v(t + dt) = v(t) + a(t + dt / 2) * dt
		p.vx -= axmiddle * step;
		p.vy -= aymiddle * step;
		//p.vx -= p.ax * step;
		//p.vy -= p.ay * step;
	}

}
