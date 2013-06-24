package org.openpixi.pixi.physics.collision.detectors;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.*;
import org.openpixi.pixi.physics.particles.Particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class SweepAndPrune extends Detector{
	
	private ArrayList<Pair<Particle, Particle>> overlappedPairs = new ArrayList<Pair<Particle, Particle>>();

	private ArrayList<BoundingBox> boxlist = new ArrayList<BoundingBox>();
	
	private ArrayList<SweepParticle> axisX = new ArrayList<SweepParticle>();
	private ArrayList<SweepParticle> axisY = new ArrayList<SweepParticle>();
	
	private ArrayList<Pair<BoundingBox, BoundingBox>> overlaps = new ArrayList<Pair<BoundingBox, BoundingBox>>();
	
	//private ArrayList<Pair<Particle2D, Particle2D>> overlappedPairs = new ArrayList<Pair<Particle2D, Particle2D>>();
	
	private Map<Pair<BoundingBox, BoundingBox>, OverlapCounter> overlapCounter = 
			new HashMap<Pair<BoundingBox, BoundingBox>, OverlapCounter>();
	
	//constructor
	public SweepAndPrune(ArrayList<Particle> parlist) {
		
		boxlist.clear();
		axisX.clear();
		axisY.clear();
		overlaps.clear();
		overlappedPairs.clear();
		overlapCounter.clear();
		
		for(int i = 0; i < parlist.size(); i++) {
			Particle par = (Particle) parlist.get(i);
			BoundingBox box = new BoundingBox(par);
		
			if(!boxlist.contains(box)) {
				boxlist.add(box);
				
				axisX.add(new SweepParticle(box, 0, true));
				axisX.add(new SweepParticle(box, 0, false));
				axisY.add(new SweepParticle(box, 1, true));
				axisY.add(new SweepParticle(box, 1, false));
				//System.out.println("Particles added");
			}
		}
	}
	
	
	//adding a method for sorting the lists
	private void sortList(ArrayList<SweepParticle> list) {
		
		for(int i = 1; i < list.size(); i++) {
			
			SweepParticle sweepPar = list.get(i);
			double sweepParValue = sweepPar.updateGetValue();
			
			int j = i - 1;
			
			while(j >= 0 && (list.get(j).updateGetValue() > sweepParValue)) {
				
				SweepParticle swapPar = list.get(j);
				
				//creating a pair of the possibly overlapping particles
				Pair<BoundingBox, BoundingBox> pairbox = new Pair<BoundingBox, BoundingBox>(sweepPar.bb, swapPar.bb);
				
				if(sweepPar.begin && !swapPar.begin) {
					
					//setting them into a list
					if(overlapCounter.containsKey(pairbox)) {
						overlapCounter.get(pairbox).overlaps++;
					}
					else {
						OverlapCounter newOverlapCounter = new OverlapCounter();
						newOverlapCounter.overlaps = 1;
						overlapCounter.put(pairbox, newOverlapCounter);
					}
				}
				if(!sweepPar.begin && swapPar.begin) {
					if(overlapCounter.containsKey(pairbox)) {
						overlapCounter.get(pairbox).overlaps--;
					}
				}
				
				list.set(j + 1, swapPar);
				j = j - 1;
			}
			
			list.set(j + 1, sweepPar);
			//System.out.println("Particles swapped");
		}
	}
	
	public void run() {
		
		//sorting the axes lists
		sortList(axisX);
		sortList(axisY);
		//System.out.println("Axes sorted");
		
		//one needs to look at the counters (similar like with the remove method)
		Iterator<Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter>> iterator = overlapCounter.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter> entry = iterator.next();
			OverlapCounter counter = entry.getValue();
			Pair<BoundingBox, BoundingBox> pairbox = entry.getKey();
			
			//System.out.println(counter.overlaps);
			if(counter.overlappedBoolean) {
				if(counter.overlaps < 2) {
					overlaps.remove(pairbox);
					counter.overlappedBoolean = false;

				}
				else if (counter.overlaps > 1) {
				}
			}
			else {
				if(counter.overlaps > 1) {
					overlaps.add(pairbox);
					counter.overlappedBoolean = true;
				}
			}
			if(counter.overlaps < 1) {
				iterator.remove();
			}
		}
	}
	
	public ArrayList<Pair<Particle, Particle>> getOverlappedPairs() {
		
		overlappedPairs.clear();
		
		for(int i = 0; i < overlaps.size(); i++) {
			BoundingBox box1 = (BoundingBox) overlaps.get(i).getFirst();
			BoundingBox box2 = (BoundingBox) overlaps.get(i).getSecond();
			Pair<Particle, Particle> pairpar = new Pair<Particle, Particle>(box1.particle, box2.particle);
			overlappedPairs.add(pairpar);
		}
		return overlappedPairs;
		
	}

}
