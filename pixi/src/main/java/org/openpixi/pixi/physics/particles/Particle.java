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
package org.openpixi.pixi.physics.particles;

import java.awt.Color;
import java.io.Serializable;

public abstract class Particle implements Serializable {

	//----------------------------------------------------------------------------------------------
	// GETTERS
	//----------------------------------------------------------------------------------------------
	
	public double getX() {return 0;}
	
	public double getY() {return 0;}
	
	public double getZ() {return 0;}
	
	public double getPrevX() {return 0;}
	
	public double getPrevY() {return 0;}
	
	public double getPrevZ() {return 0;}
	
	public double getRadius() {return 0;}

	public Color getColor() {return Color.black;}
	
	public double getVx() {return 0;}
	
	public double getVy() {return 0;}
	
	public double getVz() {return 0;}
	
	public double getAx() {return 0;}
	
	public double getAy() {return 0;}
	
	public double getAz() {return 0;}
	
	public double getMass() {return 0;}
	
	public double getCharge() {return 0;}
	
	public double getEx() {return 0;}
	
	public double getEy() {return 0;}
	
	public double getEz() {return 0;}
	
	public double getBx() {return 0;}
	
	public double getBy() {return 0;}
	
	public double getBz() {return 0;}
	
	public double getPrevBx() {return 0;}
	
	public double getPrevBy() {return 0;}
	
	public double getPrevBz() {return 0;}
	
	public double getPrevPositionComponentForceX() {return 0;}
	
	public double getPrevPositionComponentForceY() {return 0;}
	
	public double getPrevPositionComponentForceZ() {return 0;}
	
	public double getPrevTangentVelocityComponentOfForceX() {return 0;}
	
	public double getPrevTangentVelocityComponentOfForceY() {return 0;}
	
	public double getPrevTangentVelocityComponentOfForceZ() {return 0;}
	
	public double getPrevNormalVelocityComponentOfForceX() {return 0;}
	
	public double getPrevNormalVelocityComponentOfForceY() {return 0;}
	
	public double getPrevNormalVelocityComponentOfForceZ() {return 0;}
	
	public double getPrevLinearDragCoefficient() {return 0;}

	//----------------------------------------------------------------------------------------------
	// SETTERS
	//----------------------------------------------------------------------------------------------
	
	public void setX(double x) {}

	public void addX(double x) {}
	
	public void setPrevX(double prevX) {}

	public void addPrevX(double x) {}

	public void setY(double y) {}

	public void addY(double y) {}
	
	public void setPrevY(double prevY) {}

	public void addPrevY(double y) {}

	public void setZ(double z) {}

	public void addZ(double z) {}
	
	public void setPrevZ(double prevZ) {}

	public void addPrevZ(double z) {}

	public void setRadius(double radius) {}

	public void setColor(Color color) {}

	public void setVx(double vx) {}

	public void setVy(double vy) {}
	
	public void setVz(double vz) {}
	
	public void setAx(double ax) {}

	public void setAy(double ay) {}
	
	public void setAz(double az) {}

	public void setMass(double mass) {}

	public void setCharge(double charge) {}

	public void setEx(double Ex) {}

	public void setEy(double Ey) {}
	
	public void setEz(double Ez) {}
	
	public void setBx(double Bx) {}
	
	public void setBy(double By) {}

	public void setBz(double Bz) {}
	
	public void setPrevBx(double prevBx) {}
	
	public void setPrevBy(double prevBy) {}
	
	public void setPrevBz(double prevBz) {}

	public void setPrevPositionComponentForceX(double argument) {}

	public void setPrevPositionComponentForceY(double argument) {}
	
	public void setPrevPositionComponentForceZ(double argument) {}

	public void setPrevTangentVelocityComponentOfForceX(double argument) {}

	public void setPrevTangentVelocityComponentOfForceY(double argument) {}
	
	public void setPrevTangentVelocityComponentOfForceZ(double argument) {}

	public void setPrevNormalVelocityComponentOfForceX(double argument) {}

	public void setPrevNormalVelocityComponentOfForceY(double argument) {}
	
	public void setPrevNormalVelocityComponentOfForceZ(double argument) {}

	public void setPrevLinearDragCoefficient(double argument) {}

	//----------------------------------------------------------------------------------------------
	// UTILITY METHODS
	//----------------------------------------------------------------------------------------------

	public void storePosition() {}
	
	public void applyPeriodicBoundary(double argument1, double argument2, double argument3) {}
	
	public Particle copy() {
		Particle p = new ParticleFull();
		return p;
	}

	@Override
	public String toString() {return null;}
}
