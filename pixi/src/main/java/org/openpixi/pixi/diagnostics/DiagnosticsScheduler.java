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

import org.openpixi.pixi.diagnostics.methods.Diagnostics;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.Particle;
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
public class DiagnosticsScheduler {
	
	private Grid grid;
	private ArrayList<Particle> particles;
	
	/** List of all diagnostic classes that should be applied */
	private List<Diagnostics> diagnostics;
	
	
	public DiagnosticsScheduler(Grid grid, ArrayList<Particle> particles, Settings stt) {
		
		this.grid = grid;
		this.particles = particles;
		diagnostics = stt.getDiagnostics();
		
	}
	
	/** SHOULD BE CALLED IN EVERY ITERATION! Checks the intervalls of each diagnostic
	 * method itself. */
	public void performDiagnostics(int iteration) {
		for(Diagnostics m : diagnostics) {
			if (iteration == m.getNextIteration()) {
				m.calculate(grid, particles);
			}
		}
	}
	
	public void output(DataOutput out) {
		for(Diagnostics m : diagnostics) {
			if (m.checkIfNewData()) {
				m.getData(out);
			}
		}
	}
}
