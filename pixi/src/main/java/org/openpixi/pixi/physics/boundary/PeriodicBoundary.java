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
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.*;

public class PeriodicBoundary extends Boundary {

	public PeriodicBoundary(Simulation s) {
		super(s);
	}

	/**
	 * Reflect a particle off the boundaries.
	 */
	public void check(Particle particle, Force f, Solver s, double step) {

		//if the particle hits the walls
		if(particle.getX() < xmin) {
			
			particle.setPrevX(particle.getPrevX() + xmax - xmin);
			particle.setX(particle.getX() + xmax - xmin);
			
		} else if(particle.getX() > xmax) {
			
			particle.setPrevX(particle.getPrevX() - xmax - xmin);
			particle.setX(particle.getX() - xmax - xmin);
			
		}
		if(particle.getY() < ymin) {

			particle.setPrevY(particle.getPrevY() + ymax - ymin);
			particle.setY(particle.getY() + ymax - ymin);
			
		} else if(particle.getY() > ymax) {
			
			particle.setPrevY(particle.getPrevY() - ymax - ymin);
			particle.setY(particle.getY() - ymax - ymin);
		}
		
		//safety code for particles that cover a distance
		//greater than 2*xmax during one timestep
		if (particle.getX() < xmin || particle.getX() > xmax) {
			particle.setX(xmax / 2.0);
			particle.setVx(0);
		}
		
		if (particle.getY() < ymin || particle.getY() > ymax) {
			particle.setY(ymax / 2.0);
			particle.setVy(0);
		}
	}
}
