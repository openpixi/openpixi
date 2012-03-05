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

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.solver.*;

public class PeriodicBoundary extends Boundary {

	public PeriodicBoundary() {
		super();
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle2D particle, Force f, Solver s, double step) {

		//if the particle hits the walls
		if(particle.x < xmin)
		{
			particle.x += xmax - xmin;
		} else if(particle.x > xmax)
		{
			particle.x -= xmax - xmin;
		}
		if(particle.y < ymin)
		{
			particle.y += ymax - ymin;
		} else if(particle.y > ymax)
		{
			particle.y -= ymax - ymin;
		}
	}
}
