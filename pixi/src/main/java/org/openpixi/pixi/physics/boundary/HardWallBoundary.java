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
package org.openpixi.pixi.physics.boundary;

import org.openpixi.pixi.physics.Particle2D;

public class HardWallBoundary extends Boundary {

	public HardWallBoundary() {
		super();
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle2D particle) {

		//if the particle hits the walls
		if(particle.x - particle.radius < xmin)
		{
			particle.vx = Math.abs(particle.vx);
		} else if(particle.x + particle.radius > xmax)
		{
			particle.vx = - Math.abs(particle.vx);
		}
		if(particle.y - particle.radius < ymin)
		{
			particle.vy = Math.abs(particle.vy);
		} else if(particle.y + particle.radius > ymax)
		{
			particle.vy = - Math.abs(particle.vy);
		}
	}
}
