package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.collision.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SweepAndPrune {
	
	private final int MAX_PARTICLES = 10;
	//private ArrayList<SweepParticle> axisX = new ArrayList<SweepParticle>(MAX_PARTICLES);
	private SweepParticle [] axisX = new SweepParticle[MAX_PARTICLES];
	//private ArrayList<SweepParticle> axisY = new ArrayList<SweepParticle>(MAX_PARTICLES);
	SweepParticle [] axisY = new SweepParticle[MAX_PARTICLES];
	private Set<Pair<BoundingBox, BoundingBox>> overlaps = new LinkedHashSet<Pair<BoundingBox, BoundingBox>>();
	private Map<Pair<BoundingBox, BoundingBox>, Integer> overlapCounter = new LinkedHashMap<Pair<BoundingBox, BoundingBox>, Integer>();
	
	public SweepAndPrune() {
		
	}
	
	int i = 0;
	public void add(BoundingBox box) {
		axisX[i * 2] = new SweepParticle(box, 0, true);
		axisX[i * 2 + 1] = new SweepParticle(box, 0, false);
		axisY[i * 2] = new SweepParticle(box, 1, true);
		axisY[i * 2 + 1] = new SweepParticle(box, 1, false);
		i++;
	}

}
