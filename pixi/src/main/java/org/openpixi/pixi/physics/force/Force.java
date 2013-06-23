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
//First I would like to start with very simple class Force.java, so we could see the graphic result.
package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.particles.Particle;

public interface Force {

	/**
	 * Total force in the x-direction.
	 *
	 * This should always equal getPositionComponentofForceX(p) +
	 * getNormalVelocityComponentofForceX(p) +
	 * getTangentVelocityComponentOfForceX(p).
	 */
	public double getForceX(Particle p);

	/**
	 * Total force in the y-direction.
	 *
	 * This should always equal getPositionComponentofForceY(p) +
	 * getNormalVelocityComponentofForceY(p) +
	 * getTangentVelocityComponentOfForceY(p).
	 */
	public double getForceY(Particle p);

	/**
	 * Position dependent component of the force in x-direction.
	 */
	public double getPositionComponentofForceX(Particle p);

	/**
	 * Position dependent component of the force in y-direction.
	 */
	public double getPositionComponentofForceY(Particle p);

	/**
	 * Velocity dependent component of the force in the propagating direction of
	 * the particle (x-component).
	 *
	 * This should always equal -getLinearDragCoefficient(p) * p.vx.
	 */
	public double getTangentVelocityComponentOfForceX(Particle p);

	/**
	 * Velocity dependent component of the force in the propagating direction of
	 * the particle (y-component).
	 *
	 * This should always equal -getLinearDragCoefficient(p) * p.vy.
	 */
	public double getTangentVelocityComponentOfForceY(Particle p);

	/**
	 * Velocity dependent component of the force orthogonal to the propagating
	 * direction of the particle (x-component).
	 *
	 * This should always equal p.charge * p.vy * getBz(p).
	 */
	public double getNormalVelocityComponentofForceX(Particle p);

	/**
	 * 
	 * Velocity dependent component of the force orthogonal to the propagating
	 * direction of the particle (y-component).
	 *
	 * This should always equal -p.charge * p.vx * getBz(p)
	 */
	public double getNormalVelocityComponentofForceY(Particle p);

	/**
	 * Magnetic field the particle is exposed to.
	 */
	public double getBz(Particle p);

	/**
	 * Drag coefficient for a drag term that is linear in the velocity of the
	 * particle.
	 */
	public double getLinearDragCoefficient(Particle p);

}
