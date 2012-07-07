package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all the results coming from workers.
 */
public class ResultsHolder {

	public int[] nodeIDs;
	public List<List<Particle>> particlePartitions = new ArrayList<List<Particle>>();
	public Cell[][][] gridPartitions;

	public ResultsHolder(int numOfNodes) {
		nodeIDs = new int[numOfNodes];
		gridPartitions = new Cell[numOfNodes][][];
		for (int i = 0; i < numOfNodes; i++) {
			particlePartitions.add(new ArrayList<Particle>());
		}
	}
}
