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

package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellIterator;
import org.openpixi.pixi.physics.grid.*;

/**
 * TODO extract the iteration over the cells to a separate higher level class
 * TODO make this class an interface
 */
public class FieldSolver {

	protected CellIterator cellIterator;

	public FieldSolver() {

	}

	public void initializeIterator(CellIterator cellIterator, int numCellsX, int numCellsY) {
		this.cellIterator = cellIterator;
		this.cellIterator.setNormalMode(numCellsX, numCellsY);
	}

	public void step(Grid g, double timeStep) {

	}

}