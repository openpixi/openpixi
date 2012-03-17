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
package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.Algorithms.*;

import org.openpixi.pixi.physics.collision.detectors.*;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.*;
//import org.openpixi.pixi.collision.detectors.*;

import java.util.ArrayList;


public class Collision {
	
	private Detector det;
	private CollisionAlgorithm alg;
	//private ArrayList<Pair<Particle2D, Particle2D>> pairs;
	
	public Collision(Detector det, CollisionAlgorithm alg) {
		this.det = det;
		this.alg = alg;
		
	}
	
	public void check(ArrayList<Particle2D> parlist, Force f, Solver s, double step)
	{
		det.reset();
		this.det.add(parlist);
		this.det.run();
		//System.out.println("Particles added");
		this.alg.collide(det.getOverlappedPairs(), f, s, step);
	}

}
