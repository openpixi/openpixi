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

import org.openpixi.pixi.physics.Particle2D;

public class Force {

	/** New empty force */
	public Force() {
	}

	/** Total force in the x-direction */
	public double getForceX(Particle2D p) {
		return getPositionComponentofForceX(p) + getNormalVelocityComponentofForceX(p) + getTangentVelocityComponentOfForceX(p);
	}

	/** Total force in the y-direction */
	public double getForceY(Particle2D p) {
		return getPositionComponentofForceY(p) + getNormalVelocityComponentofForceY(p) + getTangentVelocityComponentOfForceY(p);
	}

	public double getPositionComponentofForceX(Particle2D p) {
		return 0;
	}

	public double getPositionComponentofForceY(Particle2D p) {
		return 0;
	}

	public double getTangentVelocityComponentOfForceX(Particle2D p) {
		return -getLinearDragCoefficient(p) * p.vx;
	}

	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		return -getLinearDragCoefficient(p) * p.vy;
	}

	public double getNormalVelocityComponentofForceX(Particle2D p) {
		return p.charge * p.vy * getBz(p);
	}

	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return - p.charge * p.vx * getBz(p);
	}

	public double getBz(Particle2D p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle2D p) {
		return 0;
	}

}
