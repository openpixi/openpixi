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
//First I would like to start with very simple class Force.java, so we could see the graphic result.
package org.openpixi.pixi.physics.force;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;

public class Force {

	/** Constant gravity in x-direction */
	public double gx;

	/** Constant gravity in y-direction */
	public double gy;

	/** Drag coefficient */
	public double drag;

	/** Electric field in x - direction */
	public double ex;

	/** Electric field in y - direction */
	public double ey;

	/** Magnetic field in z - direction */
	public double bz;
	
	Simulation s;
	
	/** New empty force */
	public Force(Simulation s)
	{
		this.s = s;
		reset();
	}

	public void reset()
	{
		gx = 0;
		gy = 0;
		drag = 0;
		ex = 0;
		ey = 0;
		bz = 0;
	}
	
	//getting the force in the x - direction
	public double getForceX(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return -drag * par.vx + par.mass * gx + par.charge * (ex + s.currentGrid.Ex[xCellPosition][yCellPosition]) +
				par.charge * par.vy * (bz + s.currentGrid.Bz[xCellPosition][yCellPosition]);
	}
	
	//getting the force in the y - direction
	public double getForceY(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return - drag * par.vy + par.mass * gy + par.charge * (ey  + s.currentGrid.Ey[xCellPosition][yCellPosition]) -
				par.charge * par.vx * (bz + s.currentGrid.Bz[xCellPosition][yCellPosition]);
	}
	
	public double getPositionComponentofForceX(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.mass * gx + par.charge * (ex + s.currentGrid.Ex[xCellPosition][yCellPosition]);
	}
	
	public double getPositionComponentofForceY(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.mass * gy + par.charge * (ey  + s.currentGrid.Ey[xCellPosition][yCellPosition]);
	}
	
	public double getTangentVelocityComponentOfForceX(Particle2D par) {
		
		return - drag * par.vx;
	}
	
	public double getTangentVelocityComponentOfForceY(Particle2D par) {
		
		return - drag * par.vy;
	}
	
	public double getNormalVelocityComponentofForceX(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return par.charge * par.vy * (bz + s.currentGrid.Bz[xCellPosition][yCellPosition]);
	}
	
	public double getNormalVelocityComponentofForceY(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return - par.charge * par.vx * (bz + s.currentGrid.Bz[xCellPosition][yCellPosition]);
	}
	
	public double getB(Particle2D par) {
		
		int xCellPosition = s.currentGrid.checkCellX(par);
		int yCellPosition = s.currentGrid.checkCellY(par);
		return bz + s.currentGrid.Bz[xCellPosition][yCellPosition];
	}
	
}
