package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


	/**
	 * Registers the given cell in a given region as a boundary cell.
	 * The cell is only registered if the neighbor for the given region exists.
	 * In another words, we have to have a neighbor to whom to send the cell.
	 */
	public void registerBoundaryCell(int boundaryRegion, Cell cell) {
		int neighbor = neighborMap.getBoundaryNeighbor(boundaryRegion);
		if (neighbor != NeighborMap.NO_NEIGHBOR) {
			getSharedData(neighbor).registerBoundaryCell(cell);
		}
	}


	/**
	 * Registers the given cell in a given region as a border cell.
	 * The cell is only registered if the neighbor for the given region exists.
	 */
	public void registerBorderCell(int borderRegion, Cell cell) {
		int[] neighbors = neighborMap.getBorderNeighbors(borderRegion);
		for (int neighbor: neighbors) {
			if (neighbor != NeighborMap.NO_NEIGHBOR) {
				getSharedData(neighbor).registerBorderCell(cell);
			}
		}
	}


	public SharedData getBoundarySharedData(int boundaryRegion) {
		int neighbor = neighborMap.getBoundaryNeighbor(boundaryRegion);
		if (neighbor != NeighborMap.NO_NEIGHBOR) {
			return getSharedData(neighbor);
		}
		else {
			return null;
		}
	}


	public List<SharedData> getBorderSharedData(int borderRegion) {
		List<SharedData> retval = new ArrayList<SharedData>();
		int[] neighbors = neighborMap.getBorderNeighbors(borderRegion);
		for (int neighbor: neighbors) {
			if (neighbor != NeighborMap.NO_NEIGHBOR) {
				retval.add(getSharedData(neighbor));
			}
		}
		return retval;
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
