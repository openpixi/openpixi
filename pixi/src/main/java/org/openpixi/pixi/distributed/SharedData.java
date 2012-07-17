package org.openpixi.pixi.distributed;

/**
 * Handles the data which needs to be shared between two nodes.
 * Specifically, sends and receives ghost and border particles and cells.
 */
public class SharedData {

	private int neighborID;

	public SharedData(int neighborID) {
		this.neighborID = neighborID;
	}
}
