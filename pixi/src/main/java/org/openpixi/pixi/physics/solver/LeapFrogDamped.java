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

/**This class represents the LeapFrog algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 */
public class LeapFrogDamped implements Solver{
	
	public LeapFrogDamped()
	{
		super();
	}

	/**
	 * LeapFrog algorithm. The damping is implemented with an error O(dt^2), the same error of accuracy that the algorithm has.
	 * Warning: the velocity is stored half a time step ahead of the position.
	 * @param p before the update: x(t), v(t-dt/2), a(t);
	 *                 after the update: x(t+dt), v(t+dt/2), a(t+dt)
	 */
	public void step(Particle p, Force f, double dt) {
		
		double getPositionComponentofForceX = f.getPositionComponentofForceX(p);
		double getPositionComponentofForceY = f.getPositionComponentofForceY(p);
		double getNormalVelocityComponentofForceX = f.getNormalVelocityComponentofForceX(p);
		double getNormalVelocityComponentofForceY = f.getNormalVelocityComponentofForceY(p);
		double getLinearDragCoefficient = f.getLinearDragCoefficient(p);
		double getMass = p.getMass();
	
		// remember for complete()
		p.setPrevPositionComponentForceX(getPositionComponentofForceX);
		p.setPrevPositionComponentForceY(getPositionComponentofForceY);
		p.setPrevNormalVelocityComponentOfForceX(getNormalVelocityComponentofForceX);
		p.setPrevNormalVelocityComponentOfForceY(getNormalVelocityComponentofForceY);	
		p.setPrevLinearDragCoefficient(getLinearDragCoefficient);
		
		//help coefficients for the dragging
		double help1_coef = 1 - getLinearDragCoefficient * dt / (2 * getMass);
		double help2_coef = 1 + getLinearDragCoefficient * dt / (2 * getMass);
		
		// v(t+dt/2) = v(t-dt/2) + a(t)*dt
		p.setVx((p.getVx() * help1_coef + p.getAx() * dt) / help2_coef);
		p.setVy((p.getVy() * help1_coef + p.getAy() * dt) / help2_coef);
		
		
		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.setX(p.getX() + p.getVx() * dt);
		p.setY(p.getY() + p.getVy() * dt);

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.setAx((getPositionComponentofForceX + getNormalVelocityComponentofForceX) / getMass);
		p.setAy((getPositionComponentofForceY + getNormalVelocityComponentofForceY) / getMass);
		
	}
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t-dt/2)
	 */
	public void prepare(Particle p, Force f, double dt)
	{
		double getPositionComponentofForceX = f.getPositionComponentofForceX(p);
		double getPositionComponentofForceY = f.getPositionComponentofForceY(p);
		double getNormalVelocityComponentofForceX = f.getNormalVelocityComponentofForceX(p);
		double getNormalVelocityComponentofForceY = f.getNormalVelocityComponentofForceY(p);
		double getLinearDragCoefficient = f.getLinearDragCoefficient(p);
		double getMass = p.getMass();
		
		// remember for complete()
		p.setPrevPositionComponentForceX(getPositionComponentofForceX);
		p.setPrevPositionComponentForceY(getPositionComponentofForceY);
		p.setPrevNormalVelocityComponentOfForceX(getNormalVelocityComponentofForceX);
		p.setPrevNormalVelocityComponentOfForceY(getNormalVelocityComponentofForceY);
		p.setPrevLinearDragCoefficient(getLinearDragCoefficient);
		
		dt = - dt * 0.5;
		
		//help coefficients for the dragging
		double help1_coef = 1 - getLinearDragCoefficient * dt / (2 * getMass);
		double help2_coef = 1 + getLinearDragCoefficient * dt / (2 * getMass);
		
		//a(t) = F(v(t), x(t)) / m
		p.setAx((f.getPositionComponentofForceX(p) + f.getNormalVelocityComponentofForceX(p)) / getMass);
		p.setAy((f.getPositionComponentofForceY(p) + f.getNormalVelocityComponentofForceY(p)) / getMass);
		
		//v(t - dt / 2) = v(t) - a(t)*dt / 2
		p.setVx((p.getVx() * help1_coef + p.getAx() * dt) / help2_coef);
		p.setVy((p.getVy() * help1_coef + p.getAy() * dt) / help2_coef);
	}
	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle p, Force f, double dt)
	{
		double getPrevLinearDragCoefficient = p.getPrevLinearDragCoefficient();
		double getMass = p.getMass();
		
		dt = dt * 0.5;

		double help1_coef = 1 - getPrevLinearDragCoefficient * dt / (2 * getMass);
		double help2_coef = 1 + getPrevLinearDragCoefficient * dt / (2 * getMass);
		
		p.setAx((p.getPrevPositionComponentForceX() + p.getPrevNormalVelocityComponentOfForceX()) / getMass);
		p.setAy((p.getPrevPositionComponentForceY() + p.getPrevNormalVelocityComponentOfForceY()) / getMass);
		
		//v(t) = v(t - dt /2) + a(t)*dt / 2
		p.setVx((p.getVx() * help1_coef + p.getAx() * dt) / help2_coef);
		p.setVy((p.getVy() * help1_coef + p.getAy() * dt) / help2_coef);
	}

}
