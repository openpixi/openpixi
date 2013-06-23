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
import java.util.Random;


public class RandomVelocityDistribution {

	public static List<Particle> apply(List<Particle> particles, int startIndex, int endIndex,
			double minVelocityX, double minVelocityY, double maxVelocity,
			long seed) {
		
		Random rand = new Random(seed);
		
		// Temporary variables used later
		double rnd1;
		double rnd2;
		double rnd3;
		
		for(int i = startIndex; i < endIndex; i++) {
			
			if ( minVelocityX >= maxVelocity && minVelocityY >= maxVelocity) {
				rnd1 = maxVelocity;
				rnd2 = maxVelocity;
			} else do {
				rnd1 = rand.nextDouble() * (maxVelocity - minVelocityX) + minVelocityX;
				rnd2 = rand.nextDouble() * (maxVelocity - minVelocityY) + minVelocityY;
				rnd3 = (rnd1*rnd1 + rnd2*rnd2);
			} while (rnd3 > maxVelocity);
			
			rnd3 = 2 * Math.PI * rand.nextDouble();
			
			particles.get(i).setVx(rnd1 * Math.cos(rnd3));
			particles.get(i).setVy(rnd2 * Math.sin(rnd3));				 
		 }
		
		return particles;
	}
	
}
