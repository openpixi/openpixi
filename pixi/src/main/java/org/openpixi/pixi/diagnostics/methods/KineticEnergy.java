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

import org.openpixi.pixi.diagnostics.DataOutput;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.Particle;

import java.util.ArrayList;

/**
 * Calculates the the (classic) kinetic energy of all particles that are in the provided
 * particle list.
 * This is a local method! (it could be performed on individual particles)
 */
public class KineticEnergy implements Diagnostics {
	
	/** Storage */
	private double totalKineticEnergy;
	
	/** Period of calculation */
	private int calculationPeriod;
	/** The next iteration when this diagnostic should be performed */
	private int nextIteration = 0;
	/** Determines whether there is new data. I.e. whether calculate was called but the
	 * new data not yet extracted with getData.
	 */
	private boolean newData = false;
	
	public KineticEnergy(int calculationPeriod) {
		this.calculationPeriod = calculationPeriod;
	}
	
	public void calculate(Grid grid, ArrayList<Particle> particles) {
		totalKineticEnergy = 0;
		
		for(Particle p : particles) {
			totalKineticEnergy += p.getMass()*(p.getVx() * p.getVx() + p.getVy()*p.getVy());
		}
		
		totalKineticEnergy = totalKineticEnergy/2;
		
		// Bookkeeping
		nextIteration += calculationPeriod;
		newData = true;
	}
	
	public int getNextIteration() {
		return nextIteration;
	}
	
	public boolean checkIfNewData() {
		return newData;
	}
	
	public void getData(DataOutput out) {
		out.kineticEnergy(totalKineticEnergy);
		newData = false;
	}
}
