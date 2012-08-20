package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerToWorker;
import org.openpixi.pixi.physics.GeneralBoundaryType;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.util.IntBox;
import org.openpixi.pixi.physics.util.Point;

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

	/** Maps region to neighbor. */
	private NeighborMap neighborMap;

	/** Maps neighbor to SharedData. */
	private Map<Integer, SharedData> sharedData = new HashMap<Integer, SharedData>();

	private IbisRegistry registry;

	private Thread particleExchangeThread;


	public SharedDataManager(
			int thisWorkerID,
			IntBox[] partitions,
	        IntBox globalSimArea,
	        GeneralBoundaryType boundaryType,
	        IbisRegistry registry) {

		this.registry = registry;
		this.neighborMap = new NeighborMap(thisWorkerID, partitions, globalSimArea, boundaryType);
	}


	//----------------------------------------------------------------------------------------------
	// Methods required for initialization of distributed simulation
	//----------------------------------------------------------------------------------------------


	public SharedData getBoundarySharedData(int boundaryRegion) {
		int neighbor = neighborMap.getBoundaryNeighbor(boundaryRegion);
		if (neighbor != NeighborMap.NO_NEIGHBOR) {
			return getSharedData(neighbor);
		}
		else {
			return null;
		}
	}


	public Point getBoundaryDirections(int boundaryRegion) {
		return neighborMap.getBoundaryNeighborsDirections(boundaryRegion);
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


	public List<Point> getBorderDirections(int borderRegion) {
		List<Point> retval = new ArrayList<Point>();
		Point[] directions = neighborMap.getBorderNeighborsDirections(borderRegion);
		if (directions != null) {
			for (Point direction: directions) {
				if (direction != null) {
					retval.add(direction);
				}
			}
		}
		return retval;
	}


	/**
	 * If the SharedData for the given neighbor does not exist it is created on the fly.
	 */
	private SharedData getSharedData(int neighbor) {
		if (!sharedData.containsKey(neighbor)) {
			sharedData.put(
					neighbor,
					new SharedData(new WorkerToWorker(registry, neighbor)));
		}
		return sharedData.get(neighbor);
	}


	public void setParticleBoundaries(ParticleBoundaries particleBoundaries) {
		for (SharedData sd: sharedData.values()) {
			sd.setParticleBoundaries(particleBoundaries);
		}
	}


	public void setGrid(Grid grid) {
		for (SharedData sd: sharedData.values()) {
			sd.setGrid(grid);
		}
	}


	public void initializeCommunication() {
		for (SharedData sd: sharedData.values()) {
			sd.initializeCommunication();
		}
	}


	//----------------------------------------------------------------------------------------------
	// Methods required during distributed simulation
	//----------------------------------------------------------------------------------------------


	/**
	 * The exchange of particles can last some time as we have to wait for the arriving particles
	 * before we send the border particles.
	 * As we do not want to stall the calling thread,
	 * we start the exchange of particles in a new thread.
	 */
	public void startExchangeOfParticles() {
		particleExchangeThread = new Thread(new Runnable() {
			public void run() {
				for (SharedData sd: sharedData.values()) {
					sd.sendLeavingParticles();
				}

				// Before we send the border particles we have to wait
				// for all the arriving particles as they are as well border particles.
				waitForArrivingParticles();
				for (SharedData sd: sharedData.values()) {
					sd.sendBorderParticles();
				}
			}
		});
		particleExchangeThread.start();
	}


	public List<Particle> getArrivingParticles() {
		List<Particle> arrivingParticles = new ArrayList<Particle>();
		for (SharedData sd: sharedData.values()) {
			arrivingParticles.addAll(sd.getArrivingParticles());
		}
		return arrivingParticles;
	}


	public List<Particle> getGhostParticles() {
		List<Particle> ghostParticles = new ArrayList<Particle>();
		for (SharedData sd: sharedData.values()) {
			ghostParticles.addAll(sd.getGhostParticles());
		}
		return ghostParticles;
	}


	public List<Particle> getLeavingParticles() {
		List<Particle> leavingParticles = new ArrayList<Particle>();
		for (SharedData sd: sharedData.values()) {
			leavingParticles.addAll(sd.getLeavingParticles());
		}
		return leavingParticles;
	}


	public void exchangeCells() {
		for (SharedData sd: sharedData.values()) {
			sd.sendBorderCells();
		}
	}


	public void waitForGhostCells() {
		for (SharedData sd: sharedData.values()) {
			sd.waitForGhostCells();
		}
	}


	private void waitForArrivingParticles() {
		for (SharedData sd: sharedData.values()) {
			sd.waitForArrivingParticles();
		}
	}


	public void cleanUpCellCommunication() {
		for (SharedData sd: sharedData.values()) {
			sd.cleanUpCellCommunication();
		}
	}


	public void cleanUpParticleCommunication() {
		// Wait for the exchange of particles to finish
		// (particularly wait for finishing the sending of border particles).
		// Otherwise, we can end up concurrently modifying the border particles
		// (this thread cleans up the list of border particles).
		try {
			particleExchangeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		for (SharedData sd: sharedData.values()) {
			sd.cleanUpParticleCommunication();
		}
	}


	public void close() {
		for (SharedData sd: sharedData.values()) {
			sd.closeSendPorts();
		}
		for (SharedData sd: sharedData.values()) {
			sd.closeReceivePorts();
		}
	}
}
