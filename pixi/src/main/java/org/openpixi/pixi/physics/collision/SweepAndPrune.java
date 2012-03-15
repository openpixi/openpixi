package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.collision.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class SweepAndPrune {
	
	private final int MAX_PARTICLES = 10;
	private ArrayList<BoundingBox> boxlist = new ArrayList<BoundingBox>();
	private ArrayList<SweepParticle> axisX = new ArrayList<SweepParticle>();
	//private SweepParticle [] axisX = new SweepParticle[MAX_PARTICLES];
	private ArrayList<SweepParticle> axisY = new ArrayList<SweepParticle>();
	//SweepParticle [] axisY = new SweepParticle[MAX_PARTICLES];
	private ArrayList<Pair<BoundingBox, BoundingBox>> overlaps = new ArrayList<Pair<BoundingBox, BoundingBox>>();
	private Map<Pair<BoundingBox, BoundingBox>, Integer> overlapCounter = new LinkedHashMap<Pair<BoundingBox, BoundingBox>, Integer>();
	
	public SweepAndPrune() {
		
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
		}
	}

}
