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


/**
 * Holds parameters that determine how particles are created. There should be such a
 * Factory class for every type of particle that should appear in the simulation.
 * 
 * This class should not have any SETTERS. It should be created with the constructor
 * and used by the particle loader where it gets deleted.
 */
public class ParticleFactory {

	// Parameters for the creation process
	private final boolean immobile;
	private final int numberOfInstances;
	private final PositionDistribution positionDistribution;
	private final VelocityDistribution velocityDistribution;
	// The following parameters may have different interpretations depending on the
	// velocity distribution used.
	private final double velocityParameter1;
	private final double velocityParameter2;
	private final double velocityParameter3;
	private final long seedForRandom1;
	private final long seedForRandom2;
	
	// Parameters to be given to the Particles that this
	// factory will create.
	/** mass of the particle */
	private double mass;
	/** electric charge of the particle */
	private double charge;
	/** radius of particle */
	private double radius;
	
	/**
	 * The constructor sets all the necessary parameters. There are no further setters.
	 * 
	 * @param numberOfInstances int 	Number of particles of this type to be created.
	 * @param mass 				double 	Mass of this type of particle.
	 * @param charge 			double 	Charge of this type of particle.
	 * @param radius			double 	Radius of this type of particle.
	 * @param pdist				enum	Determines how the particles are distributed in space.
	 * @param vdist				enum	Determines how the particles are distributed in momentum space.
	 * @param velocityParameter1 double	Has different meaning depending on the velocity distribution
	 * 									you choose. Usually the lower cutoff or mean speed in
	 * 									x direction.
	 * @param velocityParameter2 double	Has different meaning depending on the velocity distribution
	 * 									you choose. Usually the lower cutoff or mean speed in 
	 * 									direction.
	 * @param velocityParameter3 double	Has different meaning depending on the velocity distribution
	 * 									you choose. Usually the maximal particle speed.
	 * @param immobile			boolean	Determines whether the lightweight immobile particle class
	 * 									should be used.
	 * @param seedForRandom1	long	Usually seed for random positions distribution.
	 * @param seedForRandom2	long	Usually seed for random velocity distribution.
	 */
	public ParticleFactory(int numberOfInstances, double mass, double charge, double radius,
			PositionDistribution pdist, VelocityDistribution vdist,
			double velocityParameter1, double velocityParameter2,
			double velocityParameter3, boolean immobile,
			long seedForRandom1, long seedForRandom2) {
		
		this.numberOfInstances = numberOfInstances;
		this.mass = mass;
		this.charge = charge;
		this.radius = radius;
		this.velocityParameter1 = velocityParameter1;
		this.velocityParameter2 = velocityParameter2;
		this.velocityParameter3 = velocityParameter3;
		this.seedForRandom1 = seedForRandom1;
		this.seedForRandom2 = seedForRandom2;
		this.immobile = immobile;
		
		this.velocityDistribution = vdist;
		this.positionDistribution = pdist;
		
	}
	
	public int getNumberOfInstances() {
		return numberOfInstances;
	}
	
	public double getVelocityParameter1() {
		return velocityParameter1;
	}
	
	public double getVelocityParameter2() {
		return velocityParameter2;
	}
	
	public double getVelocityParameter3() {
		return velocityParameter3;
	}
	
	public long getSeedForRandom1() {
		return seedForRandom1;
	}
	
	public long getSeedForRandom2() {
		return seedForRandom2;
	}
	
	public VelocityDistribution getVelocityDistribution() {
		return velocityDistribution;
	}
	
	public PositionDistribution getPositionDistribution() {
		return positionDistribution;
	}
	
	public Particle createParticle() {
		Particle p;
		
		if (immobile) {
			p = new ImmobileParticle();
			p.setCharge(charge);
			p.setRadius(radius);
		} else {
			p = new ParticleFull();
			p.setMass(mass);
			p.setCharge(charge);
			p.setRadius(radius);
		}
		
		return p;
	}
	
	public enum PositionDistribution {
		RANDOM,	CONSTANT_SPACING
	}
	
	public enum VelocityDistribution {
		NONE, CONSTANT, RANDOM,	MAXWELLIAN, MAXWELLIAN_WITH_CUTOFF
	}
}
