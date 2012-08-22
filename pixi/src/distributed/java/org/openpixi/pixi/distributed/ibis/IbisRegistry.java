package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.*;
import org.openpixi.pixi.distributed.util.CountLock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles connection to the ibis registry.
 */
public class IbisRegistry {

	private static final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.ELECTIONS_STRICT, 
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);

	/** Lock to wait for all the workers to join. */
	private CountLock numOfJoinedWorkersLock;

	private final List<IbisIdentifier> workers =
			Collections.synchronizedList(new ArrayList<IbisIdentifier>());

	private IbisIdentifier master;

	private Ibis ibis;

	public Ibis getIbis() {
		return ibis;
	}

	public IbisIdentifier getMaster() {
		return master;
	}

	public List<IbisIdentifier> getWorkers() {
		return workers;
	}

	
	/**
	 * Creates the instance of ibis and immediately calls the election.
	 * Waits for all the nodes to connect.
	 */
	public IbisRegistry(int numOfWorkers, String iplServer, String iplPool) {
		System.setProperty("ibis.server.address", iplServer);
		System.setProperty("ibis.pool.name", iplPool);

		numOfJoinedWorkersLock = new CountLock(numOfWorkers);
		try {
			ibis = IbisFactory.createIbis(ibisCapabilities, new RegistryEvent(), PixiPorts.ALL_PORTS);
			master = ibis.registry().elect("Master");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		ibis.registry().enableEvents();

		numOfJoinedWorkersLock.waitForCount();
	}


	/*
	 * We have the IbisIdentifier to identify a worker.
	 * However, in the application we do not want to be dependent on Ibis and thus
	 * we identify the workers by integers from 0 to NUMBER_OF_NODES - 1.
	 * The integer id of a worker is simply its order in workers list
	 * (the id is given by the order in which the workers connected).
	 * Relies heavily on the fact that the list of workers is the same on each pc!
	 */


	public IbisIdentifier convertWorkerIDToIbisID(int workerID) {
		return workers.get(workerID);
	}


	public int convertIbisIDToWorkerID(IbisIdentifier ibisID) {
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).name().equals(ibisID.name())) {
				return i;
			}
		}
		throw new RuntimeException("Converting ibis ID to worker ID failed!");
	}


	public boolean isMaster() {
		return ibis.identifier().equals(master);
	}


	public void close() throws IOException {
		ibis.end();
	}


	/**
	 * Handles registry events.
	 */
	private class RegistryEvent implements RegistryEventHandler {

		public void died(IbisIdentifier ii) {
		}

		public void electionResult(String arg0, IbisIdentifier arg1) {
		}

		public void gotSignal(String arg0, IbisIdentifier arg1) {
		}

		public synchronized void joined(IbisIdentifier ii) {
			workers.add(ii);
			numOfJoinedWorkersLock.increase();
		}

		public void left(IbisIdentifier ii) {
		}

		public void poolClosed() {
		}

		public void poolTerminated(IbisIdentifier arg0) {
		}
	}
}
