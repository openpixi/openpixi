package org.openpixi.pixi.physics.collision.detectors;

import java.util.ArrayList;
import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.BoundingBox;

public class AllListParticles extends Detector{
	
	private ArrayList<Particle2D> particlelist = new ArrayList<Particle2D>();
	
	public AllListParticles() {
	
		super();
	}
	
	public void add(ArrayList<Particle2D> parlist) {
		
		for(int i = 0; i < parlist.size(); i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			this.particlelist.add(par);
		}
	}

}
