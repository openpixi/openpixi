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

import java.util.ArrayList;
import java.io.IOException;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Every diagnostics method should implement this.
 */
public interface Diagnostics {
	
	/** Performes the desired diagnostics*/
	public void calculate(Grid grid, ArrayList<IParticle> particles, double time) throws IOException;
	
	/** Sets the file path*/
	public void setPath(String path);
	
	/** Sets the measurement interval*/
	public void setInterval(double interval);
	
	/** Checks if the files are already existent and deletes them*/
	public void clear();

}
