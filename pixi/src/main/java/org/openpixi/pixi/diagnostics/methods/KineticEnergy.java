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

package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.ParticleDataOutput;
import org.openpixi.pixi.physics.Particle;
import java.util.ArrayList;

/**
 * Calculates the the (classic) kinetic energy of all particles that are in the provided
 * particle list.
 * This is a local method! (it could be performed on individual particles)
 */
public class KineticEnergy implements ParticleMethod {
	
	private double totalKineticEnergy;
	
	public void calculate(ArrayList<Particle> particles) {
		totalKineticEnergy = 0;
		
		for(Particle p : particles) {
			totalKineticEnergy += p.getMass()*(p.getVx() * p.getVx() + p.getVy()*p.getVy());
		}
		
		totalKineticEnergy = totalKineticEnergy/2;
	}
	
	public void getData(ParticleDataOutput out) {
		out.kineticEnergy(totalKineticEnergy);
	}
}
