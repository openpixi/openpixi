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

/**The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */
public class BorisRelativistic extends SolverRelativistic{
	
	public BorisRelativistic()
	{
		super();
	}
	
	/**
	 * Boris algorithm for implementing the electric and magnetic field.
	 * The damping is implemented with an linear error O(dt).
	 * Warning: the velocity is stored half a time step before of the position.
	 * @param p before the update: x(t), v(t-dt/2);
	 *                 after the update: x(t+dt), v(t+dt/2)
	 */
	public void step(Particle2D p, Force f, double step) {

		// remember for complete()
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;
		
		double ux = p.vx * Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		double uy = p.vy * Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
		
		double vxminus = ux + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = uy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.getBz(p) * step / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = (vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass) 
				+ f.getTangentVelocityComponentOfForceX(p) * step / p.mass) / Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		p.vy = (vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass) 
				+ f.getTangentVelocityComponentOfForceY(p) * step / p.mass) / Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
		
		p.x += p.vx * step;
		p.y += p.vy * step;
	}	
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t-dt/2)
	 */
	public void prepare(Particle2D p, Force f, double dt)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;

		//v(t - dt / 2) = v(t) - a(t)*dt / 2
		p.vx -= (p.ax * dt / 2) / Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		p.vy -= (p.ay * dt / 2) / Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
	}

	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle2D p, Force f, double dt)
	{
		//v(t) = v(t - dt / 2) + a(t)*dt / 2
		p.vx += (p.ax * dt / 2) / Math.sqrt(1 / (1 - (p.vx / ConstantsSI.c) * (p.vx / ConstantsSI.c)));
		p.vy += (p.ay * dt / 2) / Math.sqrt(1 / (1 - (p.vy / ConstantsSI.c) * (p.vy / ConstantsSI.c)));
	}
}
