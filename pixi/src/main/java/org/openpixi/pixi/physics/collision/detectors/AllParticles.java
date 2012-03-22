package org.openpixi.pixi.physics.collision.detectors;

import java.util.ArrayList;
import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.BoundingBox;
import org.openpixi.pixi.physics.collision.util.Pair;

public class AllParticles extends Detector{
	
	//private ArrayList<Pair<Particle2D, Particle2D>> overlappedPairs = new ArrayList<Pair<Particle2D, Particle2D>>();
	
	private ArrayList<Particle2D> particlelist = new ArrayList<Particle2D>();
	
	public AllParticles() {
	
		super();
	}
	
	public void addEveryStep(ArrayList<Particle2D> parlist) {
		
		for(int i = 0; i < parlist.size(); i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			this.particlelist.add(par);
		}
	}
	
	public void run() {
		for(int i = 0; i < (particlelist.size() - 1); i++)
		{
			Particle2D p1 = (Particle2D) particlelist.get(i);
			//double x1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y);
			for(int k = (i + 1); k < particlelist.size(); k++)
			{
				Particle2D p2 = (Particle2D) particlelist.get(k);
				double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
				if(distance <= (p1.radius + p2.radius)) {
					
					Pair<Particle2D, Particle2D> pair = new Pair<Particle2D, Particle2D>(p1, p2);
					overlappedPairs.add(pair);
					
				}
			}
			
		}
	}
	
	public void resetEveryStep() {
		
		particlelist.clear();
	}
	
	public ArrayList<Pair<Particle2D, Particle2D>> getOverlappedPairs() {
		
		return overlappedPairs;
	}

}
