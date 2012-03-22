package org.openpixi.pixi.physics.collision.detectors;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class SweepAndPrune extends Detector{
	
	//private final int MAX_PARTICLES = 10;
	//private OverlapCounter count = new OverlapCounter();
	//private SweepParticle [] axisX = new SweepParticle[MAX_PARTICLES];
	//SweepParticle [] axisY = new SweepParticle[MAX_PARTICLES];

	private ArrayList<BoundingBox> boxlist = new ArrayList<BoundingBox>();
	
	private ArrayList<SweepParticle> axisX = new ArrayList<SweepParticle>();
	private ArrayList<SweepParticle> axisY = new ArrayList<SweepParticle>();
	
	private ArrayList<Pair<BoundingBox, BoundingBox>> overlaps = new ArrayList<Pair<BoundingBox, BoundingBox>>();
	
	//private ArrayList<Pair<Particle2D, Particle2D>> overlappedPairs = new ArrayList<Pair<Particle2D, Particle2D>>();
	
	private Map<Pair<BoundingBox, BoundingBox>, OverlapCounter> overlapCounter = 
			new HashMap<Pair<BoundingBox, BoundingBox>, OverlapCounter>();
	
	//constructor
	public SweepAndPrune() {
		
		super();
	}
	
	//method to add a box to the axes
	public void add(ArrayList<Particle2D> parlist) {
		
		for(int i = 0; i < parlist.size(); i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			BoundingBox box = new BoundingBox(par);
			//boxlist.add(box);
		
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
	
	//method to remove sweep particles from list, it is used when a bounding box is removed
	private void removeSweepParticle(List<SweepParticle> list, BoundingBox box) {
		ListIterator<SweepParticle> iterator = list.listIterator();
		while(iterator.hasNext()) {
			SweepParticle spar = iterator.next();
			if(spar.bb == box) {
				iterator.remove();
				//System.out.println("Sweep particle removed");
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
			//System.out.println("Particle removed");
			
			//one needs to clean the counters too
			Iterator<Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter>> iterator = overlapCounter.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Pair<BoundingBox, BoundingBox>, OverlapCounter> entry = iterator.next();
				OverlapCounter counter = entry.getValue();
				Pair<BoundingBox, BoundingBox> pairbox = entry.getKey();
				if(pairbox.getFirst() == box || pairbox.getSecond() == box) {
					if(counter.overlappedBoolean) {
						overlaps.remove(pairbox);
					}
					
					iterator.remove();
				}
			}
		}
	}
	
	public void reset() {
		boxlist.clear();// = new ArrayList<BoundingBox>();
		
		axisX.clear();// = new ArrayList<SweepParticle>();
		axisY.clear();// = new ArrayList<SweepParticle>();
		
		overlaps.clear();// = new ArrayList<Pair<BoundingBox, BoundingBox>>();
		
		overlappedPairs.clear();// = new ArrayList<Pair<Particle2D, Particle2D>>();
		
		overlapCounter.clear();// = new HashMap<Pair<BoundingBox, BoundingBox>, OverlapCounter>();
		
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
					
					//Pair<BoundingBox, BoundingBox> pairbox = new Pair<BoundingBox, BoundingBox>(sweepPar.bb, swapPar.bb);
					
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
					//Pair<BoundingBox, BoundingBox> pairbox = new Pair<BoundingBox, BoundingBox>(sweepPar.bb, swapPar.bb);
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
					//counter.overlappedBoolean = false;
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
		
		//System.out.println(boxlist.size());
		//System.out.println(boxlist.get(1).particle.x);
		//System.out.println(overlaps.size());
	}
	
	public ArrayList<Pair<Particle2D, Particle2D>> getOverlappedPairs() {
		
		overlappedPairs.clear();
		
		for(int i = 0; i < overlaps.size(); i++) {
			BoundingBox box1 = (BoundingBox) overlaps.get(i).getFirst();
			BoundingBox box2 = (BoundingBox) overlaps.get(i).getSecond();
			Pair<Particle2D, Particle2D> pairpar = new Pair<Particle2D, Particle2D>(box1.particle, box2.particle);
			overlappedPairs.add(pairpar);
		}
		return overlappedPairs;
		
	}

}
