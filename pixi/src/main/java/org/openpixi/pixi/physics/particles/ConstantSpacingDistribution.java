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

package org.openpixi.pixi.physics.particles;

import java.util.List;

/**
 * Tries to place the particles on the simulation area s.t. the densities in x and y direction
 * have the same ratio as the width and height of the simulation area.
 * 
 * MOST OF THE TIME IT WILL FAIL!
 * 
 * Best used with width/height ratio of 1 and a particle number that is a power of 2.
 */
public class ConstantSpacingDistribution {

	public static List<Particle> apply(List<Particle> particles, int startIndex, int endIndex,
			double startX, double endX, double startY, double endY) throws IllegalArgumentException {
		
		int numOfParticles = endIndex - startIndex;
		double width  = endX - startX;
		double height = endY - startY;
		
		int numOfParticlesX = (int) Math.sqrt((width / height) * numOfParticles);
		if (numOfParticlesX == 0) {
			throw new IllegalArgumentException("The width of the provided area is too small!");
		}
		int numOfParticlesY = (int) numOfParticles / numOfParticlesX;
		
		// 2 added in the numerator because we do not want to place
		// the particles on the boundaries
		double deltaX = width / (numOfParticlesX + 2);
		double deltaY = height / (numOfParticlesY + 2);

		if (numOfParticlesX * numOfParticlesY != numOfParticles) {
			throw new IllegalArgumentException("We do not know how to distribute " + 
		numOfParticles + " on an area with this width and height! Try other parameters.");
		}
		
		for (int i = startIndex; i < endIndex; i++) {
			
			for (int n = 0; n < numOfParticlesX; n++) {
				for (int m = 0; m < numOfParticlesY; m++) {
					particles.get(i).setX( startX + (n+1) * deltaX );
					particles.get(i).setY( startY + (m+1) * deltaY );
				}
			}

		}
		
		return particles;
	}
	
}
