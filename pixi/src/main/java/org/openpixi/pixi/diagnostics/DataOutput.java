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

/**
 * Since every diagnostics class has its own kind of data, every class needs
 * its own interface. This interface specifies what kind of data the diagnostics
 * class wants to provide.
 * Therefore if one adds a diagnostics class one should add a method here that is
 * called from the getData method of the diagnostics class.
 * This is split from the GridDataOutput s.t. the DataOutput objects do not get
 * too crowded.
 */
public interface DataOutput {
	
	public void kineticEnergy(double var);
	
	public void potential(double[][] var);

}
