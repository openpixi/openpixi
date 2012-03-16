package org.openpixi.pixi.physics.collision.Algorithms;

import java.util.ArrayList;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

public abstract class CollisionAlgorithm {
	
	public CollisionAlgorithm() {
		
	}
	
	public void doCollision(ArrayList<Pair<Particle2D, Particle2D>> pairs, Force f, Solver s, double step) {
		
	}

}
