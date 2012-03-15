package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.collision.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SweepAndPrune {
	
	private final int MAX_PARTICLES = 10;
	//private ArrayList<BoundingBox> axisX = new ArrayList<BoundingBox>(MAX_PARTICLES);
	private BoundingBox [] axisX = new BoundingBox[MAX_PARTICLES];
	//private ArrayList<BoundingBox> axisY = new ArrayList<BoundingBox>(MAX_PARTICLES);
	BoundingBox [] axisY = new BoundingBox[MAX_PARTICLES];
	private Set<Pair<BoundingBox, BoundingBox>> overlaps = new LinkedHashSet<Pair<BoundingBox, BoundingBox>>();
	private Map<Pair<BoundingBox, BoundingBox>, Integer> overlapCounter = new LinkedHashMap<Pair<BoundingBox, BoundingBox>, Integer>();
	
	public SweepAndPrune() {
		
	}
	
	public void add(BoundingBox box) {
		
	}

}
