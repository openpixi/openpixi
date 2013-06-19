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
 * This class should not have any SETTERS. It should be created with the constructor
 * and used by the particle loader where it gets deleted.
 */
public class ParticleFactory {

	// Parameters for the creation process
	private final boolean immobile;
	private final int numberOfInstances;
	private final PositionDistribution positionDistribution;
	private final VelocityDistribution velocityDistribution;
	private final double driftVelocityX;
	private final double driftVelocityY;
	private final double thermalVelocityX;
	private final double thermalVelocityY;
	private final double maxVelocity;
	
	// Parameters to be given to the Particles that this
	// factory will create.
	/** mass of the particle */
	private double mass;
	/** electric charge of the particle */
	private double charge;
	/** radius of particle */
	private double radius;
	
	
	public ParticleFactory(int numberOfInstances, double mass, double charge, double radius,
			PositionDistribution pdist, VelocityDistribution vdist,
			double driftVelocityX, double driftVelocityY, 
			double thermalVelocityX, double thermalVelocityY,
			double cutoffVelocity, boolean immobile) {
		
		this.numberOfInstances = numberOfInstances;
		this.mass = mass;
		this.charge = charge;
		this.radius = radius;
		this.driftVelocityX = driftVelocityX;
		this.driftVelocityY = driftVelocityY;
		this.thermalVelocityX = thermalVelocityX;
		this.thermalVelocityY = thermalVelocityY;
		this.maxVelocity = cutoffVelocity;
		this.immobile = immobile;
		
		this.velocityDistribution = vdist;
		this.positionDistribution = pdist;
		
	}
	
	public int getNumberOfInstances() {
		return numberOfInstances;
	}
	
	public double getDriftVelocityX() {
		return driftVelocityX;
	}
	
	public double getDriftVelocityY() {
		return driftVelocityY;
	}
	
	public double getThermalVelocityX() {
		return thermalVelocityX;
	}
	
	public double getThermalVelocityY() {
		return thermalVelocityY;
	}
	
	public double getMaxVelocity() {
		return maxVelocity;
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
