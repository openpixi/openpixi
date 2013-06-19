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

package org.openpixi.pixi.ui.util;

import org.openpixi.pixi.diagnostics.DataOutput;

/**
 * This is an example DataOutput object as needed to extract data
 * from the diagnostics package. Here we decided not to use the data.
 * This class is used when the user does not want to have any diagnostic
 * output.
 */
public class EmptyDataOutput implements DataOutput {
	
	public void kineticEnergy(double var) {
		//DO NOTHING
	}
	
	public void potential(double[][] phi) {
		//DO NOTHING
	}
	
	public void setIteration(int iteration) {
		//DO NOTHING
	}
	
	public void closeStreams() {
		//DO NOTHING
	}
	
}
