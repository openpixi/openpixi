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
package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;


public class SimpleGrid extends Grid {

	public SimpleGrid(Simulation s) {

		super(s);

		numCellsX = 10 + 3;
		numCellsY = 10 + 3;
		cellWidth = s.width/numCellsX;
		cellHeight = s.height/numCellsY;

		fsolver = new SimpleSolver();
		interp = new CloudInCell(this);
		SimpleGridForce force = new SimpleGridForce();
		s.f.add(force);

		createGrid();
	}

	//a method to change the dimensions of the cells, i.e. the width and the height
	@Override
	public void changeDimension(double width, double height, int xbox, int ybox)
	{
		setNumCellsX(xbox + 3);
		setNumCellsY(ybox + 3);

		createGrid();

		setGrid(width, height);
	}


	@Override
	public void setGrid(double width, double height)
	{
		cellWidth = width / (numCellsX - 3);
		cellHeight = height / (numCellsY - 3);

		for (Particle p: simulation.particles){
			//assuming rectangular particle shape i.e. area weighting
			p.setChargedensity(p.getCharge() / (cellWidth * cellHeight));
		}

		//include updateGrid() and the first calculation of Fields here
	}


	@Override
	public void updateGrid(ArrayList<Particle> particles, double tstep)
	{
		getInterp().interpolateToGrid(particles);
		storeFields();
		getFsolver().step(this, tstep);
		getInterp().interpolateToParticle(particles);
	}
}
