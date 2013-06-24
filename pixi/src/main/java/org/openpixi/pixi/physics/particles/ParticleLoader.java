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
import java.util.ArrayList;

/**
 * Iterates through the particle factories creating the appropriate amount of particles of
 * a specify type. Then modifies the positions and velocities of these particle groups according
 * to the specified position and velocity distributions. 
 */
public class ParticleLoader {
	
	public List<Particle> load(List<ParticleFactory> particleFactories, double simulationWidth,
			double simulationHeight, int numCellsX, int numCellsY) throws IllegalArgumentException {
		
		 List<Particle> particles = new ArrayList<Particle>();
		 /** Starting index of a block of similar particles in the particle list */
		 int index = 0;
		 
		 for(ParticleFactory f : particleFactories) {
			 for(int i = 0; i < f.getNumberOfInstances(); i++) {
				 particles.add(f.createParticle());				 
			 }
			 
			 switch(f.getPositionDistribution()) {
				case RANDOM: {
			 		RandomPositionDistribution.apply(particles, index, index + f.getNumberOfInstances(), 
			 				0, simulationHeight, 0, simulationWidth, f.getSeedForRandom1());
			 		break;
			 	}
			 	case CONSTANT_SPACING: {
			 		ConstantSpacingDistribution.apply(particles, index, index + f.getNumberOfInstances(),
			 				0, simulationWidth, 0, simulationHeight);
			 		break;
			 	}
				default: {
					throw new IllegalArgumentException("Can't yet handle " + f.getPositionDistribution());
				}
			 }
			 
			 switch(f.getVelocityDistribution()) {
			 	case NONE: {
			 		// THIS IS FOR IMMOBILE PARTICLES!
			 		// If the particle has a velocity variable this SHOULD NOT BE USED!
			 		break;
			 	}
			 	case CONSTANT: {
			 		ConstantVelocityDistribution.apply(particles, index, index + f.getNumberOfInstances(), 
			 				f.getVelocityParameter1(), f.getVelocityParameter2());
			 		break;
			 	}
			 	case RANDOM: {
			 		RandomVelocityDistribution.apply(particles, index, index + f.getNumberOfInstances(), 
			 				f.getVelocityParameter1(), f.getVelocityParameter2(), f.getVelocityParameter3(), 
			 				f.getSeedForRandom2());
			 	}
			 	case MAXWELLIAN: {
			 		MaxwellianDistribution.apply(particles, index, index + f.getNumberOfInstances(),
			 				f.getVelocityParameter1(), f.getVelocityParameter2(), f.getSeedForRandom2());
			 		break;
			 	}
			 	case MAXWELLIAN_WITH_CUTOFF: {
			 		MaxwellianDistribution.applyWithCutoff(particles, index, 
			 				index + f.getNumberOfInstances(), f.getVelocityParameter1(), 
			 				f.getVelocityParameter2(), f.getVelocityParameter3(), f.getSeedForRandom2());
			 		break;
			 	}
				default: {
					throw new IllegalArgumentException("Can't yet handle " + f.getVelocityDistribution());
				}
			 }
			 
			 index += f.getNumberOfInstances();
			 f = null;
		 }
		 
		 return particles;
	}

}
