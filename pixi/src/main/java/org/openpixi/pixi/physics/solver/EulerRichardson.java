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
public class EulerRichardson {
	public static void algorithm(Particle2D particle, Force f, double step)
	{
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		double vxmiddle = particle.vx + particle.ax * step / 2;
		double vymiddle = particle.vy + particle.ay * step / 2;
		
		//double xmiddle = x + vx * step / 2;    actually, this two equations are not needed, but I've written them
		//double ymiddle = y + vy * step / 2;    so the algorithm is complete
		
		double axmiddle = f.getForceX(vxmiddle, vymiddle, particle) / particle.mass;
		double aymiddle = f.getForceY(vxmiddle, vymiddle, particle) / particle.mass;
		
		particle.vx += axmiddle * step;
		particle.vy += aymiddle * step;
		
		particle.x += vxmiddle * step;
		particle.y += vymiddle * step;
		
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ax = f.getForceY(particle.vx, particle.vy, particle) / particle.mass;
	}

}
