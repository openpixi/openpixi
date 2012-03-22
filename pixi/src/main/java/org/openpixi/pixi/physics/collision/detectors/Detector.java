package org.openpixi.pixi.physics.collision.detectors;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.collision.util.Pair;

public class Detector {
	
	public ArrayList<Pair<Particle2D, Particle2D>> overlappedPairs = new ArrayList<Pair<Particle2D, Particle2D>>();
	
	public Detector() {
		
	}
	
	public void add(ArrayList<Particle2D> parlist) {
		
	}
	
	public void addEveryStep(ArrayList<Particle2D> parlist) {
		
	}
	
	public void run() {
		
	}
	
	public ArrayList<Pair<Particle2D, Particle2D>> getOverlappedPairs() {
		return overlappedPairs;
	}
	public void reset() {
		
	}
	
	public void resetEveryStep() {
		
	}

}
