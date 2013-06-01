/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openpixi.pixi.physics.collision.detectors;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.collision.util.KdTree;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.collision.util.ParticleBoundingBoxPoint;

/**
 *
 * @author Clemens
 */
public class KdTreeCollision extends Detector{
	private ArrayList<Pair<Particle, Particle>> overlappedPairs = new ArrayList<Pair<Particle, Particle>>();
	
	private ArrayList<Particle> particlelist = new ArrayList<Particle>();

	public KdTreeCollision(ArrayList<Particle> parlist) {
			particlelist.clear();

			for(Particle par:parlist) {
				this.particlelist.add(par);
			}

	}
	
	public void run()
	{
		ArrayList<Particle> collisions = new ArrayList<Particle>();
		ArrayList<ParticleBoundingBoxPoint> pbbps = new ArrayList<ParticleBoundingBoxPoint>();
		
		for(Particle par:particlelist)
		{
			pbbps.add(new ParticleBoundingBoxPoint(par,par.getX()-par.getRadius(),par.getY()-par.getRadius()));
			pbbps.add(new ParticleBoundingBoxPoint(par,par.getX()-par.getRadius(),par.getY()+par.getRadius()));
			pbbps.add(new ParticleBoundingBoxPoint(par,par.getX()+par.getRadius(),par.getY()-par.getRadius()));
			pbbps.add(new ParticleBoundingBoxPoint(par,par.getX()+par.getRadius(),par.getY()+par.getRadius()));
		}
		
		KdTree kd = new KdTree(pbbps,0);
		
		for(Particle par:particlelist)
		{
			double x = par.getX();
			double y = par.getY();
			double r = par.getRadius();
			collisions = kd.Search(x-r, x+r, y-r, y+r, 0);
			for(Particle par2:collisions)
			{
				/*if(overlappedPairs.contains(new Pair(par,par2))||overlappedPairs.contains(new Pair(par2,par)))
				{
					//is already in list
				}
				else
				{
				* 
				*/
					overlappedPairs.add(new Pair(par,par2));
				//}
			}
		}
		
	}
	
	public ArrayList<Pair<Particle, Particle>> getOverlappedPairs() {
		return overlappedPairs;
	}
}
