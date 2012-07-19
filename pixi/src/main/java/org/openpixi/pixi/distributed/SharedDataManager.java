package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds shared data for each boundary and border region.
 * Provides access to the shared data and operations upon the shared data.
 *
 * The shared data for a specific neighbor is created according to demand on the fly.
 */
public class SharedDataManager {

	/** Maps neighbor to SharedData. */
	private Map<Integer, SharedData> sharedData = new HashMap<Integer, SharedData>();

	/** Maps region to neighbor. */
	private NeighborMap neighborMap;


	public SharedDataManager(int thisWorkerID, IntBox[] partitions,
	                         IntBox globalSimArea, GeneralBoundaryType boundaryType) {
		this.neighborMap = new NeighborMap(thisWorkerID, partitions, globalSimArea, boundaryType);
	}


	public void registerBoundaryCell(int boundaryRegion, Cell cell) {
		int neighbor = neighborMap.getBoundaryNeighbor(boundaryRegion);
		if (neighbor != NeighborMap.NO_NEIGHBOR) {
			getSharedData(neighbor).registerBoundaryCell(cell);
		}
	}


	public void registerBorderCell(int borderRegion, Cell cell) {
		int[] neighbors = neighborMap.getBorderNeighbors(borderRegion);
		for (int neighbor: neighbors) {
			if (neighbor != NeighborMap.NO_NEIGHBOR) {
				getSharedData(neighbor).registerBorderCell(cell);
			}
		}
	}


	/**
	 * If the SharedData for the given neighbor does not exist it is created on the fly.
	 */
	private SharedData getSharedData(int neighbor) {
		if (!sharedData.containsKey(neighbor)) {
			sharedData.put(neighbor, new SharedData(neighbor));
		}
		return sharedData.get(neighbor);
	}
}
