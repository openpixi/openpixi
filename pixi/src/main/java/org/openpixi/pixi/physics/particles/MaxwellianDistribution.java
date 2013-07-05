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


public class MaxwellianDistribution {		
	/** Generates thermal particles that have a maxwellian distribution in 
	 *  momentum space.
	 *  TODO make this compatible with relativistic momenta
	 */
	public static List<Particle> apply (List<Particle> particles, int startIndex, int endIndex,
			double thermalVelocityX, double thermalVelocityY, long seed) {
		
		Random rand = new Random(seed);
		
		// Temporary variables used later
		double rnd1;
		double rnd2;
		double rnd3;
		
		//0.5 is the mass of the electron that is used later
		//Factor of 2 comes from the denominator in the exponent of
		//the Maxwellian distribution.
		//NOTE: There are no further factors because we are inverting
		//the cumulative distribution hence the factors cancel
		
		/**Velocity normalization in x direction, temperature dependent */
		double vnormX = Math.sqrt(2) * thermalVelocityX;
		/**Velocity normalization in x direction, temperature dependent */
		double vnormY = Math.sqrt(2) * thermalVelocityY;
		
		//Generates thermal electrons that are randomly distributed
		//across the simulation area
		for (int i = startIndex; i < endIndex; i++) {			
			do {
				rnd1 = rand.nextDouble();
				rnd2 = rand.nextDouble();
				rnd3 = (rnd1*rnd1 + rnd2*rnd2);
			} while (rnd3 > 1);
			
			rnd3 = Math.sqrt( - Math.log(rnd3) / rnd3 );
			particles.get(i).setVx( vnormX * rnd1 * rnd3 );
			particles.get(i).setVy( vnormY * rnd2 * rnd3 );
		}			
			
		return particles;
	}
	
	public static List<Particle> applyWithCutoff (List<Particle> particles, int startIndex, int endIndex,
			double thermalVelocityX, double thermalVelocityY, double cutoffVelocity,  long seed) {
		
		Random rand = new Random(seed);
		
		// Temporary variables used later
		double rnd1;
		double rnd2;
		double rnd3;
		
		//0.5 is the mass of the electron that is used later
		//Factor of 2 comes from the denominator in the exponent of
		//the Maxwellian distribution.
		//NOTE: There are no further factors because we are inverting
		//the cumulative distribution hence the factors cancel
		double vnormX = Math.sqrt(2) * thermalVelocityX;
		double vnormY = Math.sqrt(2) * thermalVelocityY;
		
		/**Cutoff velocity SQUARED*/
		cutoffVelocity *= cutoffVelocity;
		
		//Generates thermal electrons that are randomly distributed
		//across the simulation area
		for (int i = startIndex; i < endIndex; i++) {
			
			do {
				rnd1 = rand.nextDouble();
				rnd2 = rand.nextDouble();
				rnd3 = (rnd1*rnd1 + rnd2*rnd2);
			} while (rnd3 > 1);
			
			// !!! CHECK THIS FORMULA AGAIN !!!
			rnd3 = Math.sqrt(
					(cutoffVelocity - Math.log(rnd3 + (1-rnd3) * Math.exp(cutoffVelocity)))
					/ rnd3);
			particles.get(i).setVx( vnormX * rnd1 * rnd3 );
			particles.get(i).setVy( vnormY * rnd2 * rnd3 );	
		}
		
		return particles;
	}
}
