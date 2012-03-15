package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class SweepAndPrune {
	
	//private final int MAX_PARTICLES = 10;
	//private OverlapCounter count = new OverlapCounter();
	private ArrayList<BoundingBox> boxlist = new ArrayList<BoundingBox>();
	private ArrayList<SweepParticle> axisX = new ArrayList<SweepParticle>();
	//private SweepParticle [] axisX = new SweepParticle[MAX_PARTICLES];
	private ArrayList<SweepParticle> axisY = new ArrayList<SweepParticle>();
	//SweepParticle [] axisY = new SweepParticle[MAX_PARTICLES];
	private ArrayList<Pair<BoundingBox, BoundingBox>> overlaps = new ArrayList<Pair<BoundingBox, BoundingBox>>();
	private ArrayList<Pair<Particle2D, Particle2D>> overlappedPairs = new ArrayList<Pair<Particle2D, Particle2D>>();
	private Map<Pair<BoundingBox, BoundingBox>, OverlapCounter> overlapCounter = 
			new LinkedHashMap<Pair<BoundingBox, BoundingBox>, OverlapCounter>();
	
	//constructor
	public SweepAndPrune(ArrayList<Particle2D> parlist) {
		
		for(int i = 0; i < parlist.size(); i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			BoundingBox box = new BoundingBox(par);
			boxlist.set(i, box);
			add(box);
		}
	}
	
	//method to add a box to the axes
	public void add(BoundingBox box) {
		if(!boxlist.contains(box)) {
			axisX.add(new SweepParticle(box, 0, true));
			axisX.add(new SweepParticle(box, 0, false));
			axisY.add(new SweepParticle(box, 1, true));
			axisY.add(new SweepParticle(box, 1, false));
		}
	}
	
	//method to remove sweep particles from list, it is used when a bounding box is removed
	private void removeSweepParticle(ArrayList<SweepParticle> list, BoundingBox box) {
		ListIterator<SweepParticle> iterator = list.listIterator();
		while(iterator.hasNext()) {
			SweepParticle spar = iterator.next();
			if(spar.bb == box) {
				iterator.remove();
			}
		}
	}
	
	//method to remove a bounding box from list
	public void remove(BoundingBox box) {
		if(boxlist.contains(box)) {
			boxlist.remove(box);
			
			//removing the sweep particles from the lists of the axes
			removeSweepParticle(axisX, box);
			removeSweepParticle(axisY, box);
			
			//one needs to clean the counters too
			Iterator<Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter>> iterator = overlapCounter.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter> entry = iterator.next();
				OverlapCounter counter = entry.getValue();
				Pair<BoundingBox, BoundingBox> pairbox = entry.getKey();
				if(pairbox.getFirst() == box || pairbox.getSecond() == box) {
					if(counter.overlapping) {
						overlaps.remove(pairbox);
					}
					
					iterator.remove();
				}
			}
		}
	}
	
	//adding a method for sorting the lists
	private void sortList(ArrayList<SweepParticle> list) {
		
		for(int i = 0; i < list.size(); i++) {
			
			SweepParticle sweepPar = list.get(i);
			double sweepParValue = sweepPar.updateGetValue();
			
			int j = i - 1;
			
			while(i >= 0 && (list.get(i).updateGetValue() > sweepParValue)) {
				
				SweepParticle swapPar = list.get(i);
				
				if(sweepPar.begin && !swapPar.begin) {
					
					//creating a pair of the possibly overlapping particles
					Pair<BoundingBox, BoundingBox> pairbox = new Pair<BoundingBox, BoundingBox>(sweepPar.bb, swapPar.bb);
					
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
					Pair<BoundingBox, BoundingBox> pairbox = new Pair<BoundingBox, BoundingBox>(sweepPar.bb, swapPar.bb);
					
					if(overlapCounter.containsKey(pairbox)) {
						overlapCounter.get(pairbox).overlaps--;
					}
				}
				
				list.set(j + 1, swapPar);
				j--;
			}
				list.set(j+1, sweepPar);	
			
		}
	}
	
	public void run() {
		
		//sorting the axes lists
		sortList(axisX);
		sortList(axisY);
		
		int countOverlaps = 0;
		//one needs to look at the counters (similar like with the remove method)
		Iterator<Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter>> iterator = overlapCounter.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter> entry = iterator.next();
			OverlapCounter counter = entry.getValue();
			Pair<BoundingBox, BoundingBox> pairbox = entry.getKey();
			
			if(counter.overlaps == 3) {
				countOverlaps++;
			}
			
			if(counter.overlapping) {
				if(counter.overlaps < 3) {
					overlaps.remove(pairbox);
					counter.overlapping = false;
				}
			}
			else {
				if(counter.overlaps > 2) {
					overlaps.add(pairbox);
					counter.overlapping = true;
				}
			}
		}
	}
	
	public ArrayList<Pair<Particle2D, Particle2D>> getOverlappedPairs() {
		
		for(int i = 0; i < overlaps.size(); i++) {
			BoundingBox box1 = (BoundingBox) overlaps.get(i).getFirst();
			BoundingBox box2 = (BoundingBox) overlaps.get(i).getSecond();
			Pair<Particle2D, Particle2D> pairpar = new Pair<Particle2D, Particle2D>(box1.particle, box2.particle);
			overlappedPairs.set(i, pairpar);
		}
		return overlappedPairs;
		
	}

}
