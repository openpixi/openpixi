package org.openpixi.pixi.physics.collision.detectors;

import java.util.ArrayList;
import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.particles.Particle;

public class AllParticles extends Detector{
	
	private ArrayList<Pair<Particle, Particle>> overlappedPairs = new ArrayList<Pair<Particle, Particle>>();
	
	private ArrayList<Particle> particlelist = new ArrayList<Particle>();
	
	public AllParticles(ArrayList<Particle> parlist) {
		particlelist.clear();
		
		for(int i = 0; i < parlist.size(); i++) {
			Particle par = (Particle) parlist.get(i);
			this.particlelist.add(par);
		}
		
	}
	
	public void run() {
		for(int i = 0; i < (particlelist.size() - 1); i++)
		{
			Particle p1 = (Particle) particlelist.get(i);
			
			for(int k = (i + 1); k < particlelist.size(); k++)
			{
				Particle p2 = (Particle) particlelist.get(k);
				//double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
				
				if(Math.abs(p1.getX() - p2.getX()) <= (p1.getRadius() + p2.getRadius())) {
					
					if(Math.abs(p1.getY() - p2.getY()) <= (p1.getRadius() + p2.getRadius())) {
						
						Pair<Particle, Particle> pair = new Pair<Particle, Particle>(p1, p2);
						overlappedPairs.add(pair);
					}					
				}
			}
			
		}
	}
	
	public ArrayList<Pair<Particle, Particle>> getOverlappedPairs() {
		
		return overlappedPairs;
	}

}
