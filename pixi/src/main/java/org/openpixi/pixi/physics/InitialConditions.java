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

package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.force.ConstantForce;

public class InitialConditions {
	public static Simulation initEmptySimulation() {
		Settings sst = new Settings();

		sst.setNumberOfDimensions(3);
		sst.setNumberOfColors(2);
		sst.setTimeStep(0.1);
		sst.setTMax(1000);
		sst.setSpeedOfLight(1);
		sst.setRelativistic(true);
		sst.setGridStep(1);
		sst.setGridCellsX(1);
		sst.setGridCellsY(1);
		sst.setGridCellsZ(1);
		sst.setNumOfParticles(0);

		sst.addForce(new ConstantForce());

		return new Simulation(sst);
	}
}
