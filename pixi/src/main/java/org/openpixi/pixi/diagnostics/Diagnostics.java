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

package org.openpixi.pixi.diagnostics;

import org.openpixi.pixi.diagnostics.methods.GridMethod;
import org.openpixi.pixi.diagnostics.methods.ParticleMethod;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for all diagnostics that should be applied to the simulation during
 * execution.
 * It can perform diagnostics on the particles and on the grid separately. Same goes for
 * the extraction of the calculated data. The output methods are meant to be used by some
 * kind of UI that provides an DataOutput object. The getData() methods of the diagnostic
 * classes are called which then write to the DataOutput object in their specific way.
 * The UI can then decide how it processes the data further.
 * TODO Split this in local and global diagnostics. The local ones can be performed on
 * a part of the whole simulation (like the part running on one computing node) whereas
 * the global methods need data from the whole simulation.
 * TODO parallelize the diagnostics methods.
 */
public class Diagnostics {
	
	private Grid grid;
	private ArrayList<Particle> particles;
	
	/** List of all diagnostic classes that should be applied to the particles*/
	private List<ParticleMethod> particleDiagnostics;
	/** List of all diagnostic classes that should be applied to the grid*/
	private List<GridMethod> gridDiagnostics;
	
	
	public Diagnostics(Grid grid, ArrayList<Particle> particles, Settings stt) {
		
		this.grid = grid;
		this.particles = particles;
		particleDiagnostics = stt.getParticleDiagnostics();
		gridDiagnostics = stt.getGridDiagnostics();		
	}
	
	/** Performs the specified diagnostics on the particle list*/
	public void particles() {
		for(ParticleMethod m : particleDiagnostics){
			m.calculate(particles);
		}
	}
	/** Performs the specified diagnostics on the grid*/
	public void grid() {
		for(GridMethod m : gridDiagnostics) {
			m.calculate(grid);
		}
	}
	
	public void outputParticles(ParticleDataOutput pout) {
		for(ParticleMethod m : particleDiagnostics){
			m.getData(pout);
		}
	}
	
	public void outputGrid(GridDataOutput gout) {
		for(GridMethod m : gridDiagnostics) {
			m.getData(gout);
		}
	}
}
