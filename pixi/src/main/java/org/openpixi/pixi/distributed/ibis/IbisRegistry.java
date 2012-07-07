package org.openpixi.pixi.distributed.ibis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ibis.ipl.*;
import org.openpixi.pixi.distributed.SimpleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles connection to the ibis registry.
 */
public class IbisRegistry {

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

		public void joined(IbisIdentifier ii) {
			synchronized (numOfNodesLock) {
				workers.add(ii);
				numOfNodesLock.notify();
			}

			if (isMaster()) {
				logger.info("Node {} joined the pool", ii.name());
			}
		}

		public void left(IbisIdentifier ii) {
		}

		public void poolClosed() {
		}

		public void poolTerminated(IbisIdentifier arg0) {
		}
	}

	private static final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.ELECTIONS_STRICT, 
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);

	private static Logger logger = LoggerFactory.getLogger(IbisRegistry.class);

	/** Lock to wait for all the nodes to join.*/
	private Object numOfNodesLock = new Object();

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
	public IbisRegistry(int numOfNodes) throws Exception {
		ibis = IbisFactory.createIbis(ibisCapabilities, new RegistryEvent(), PixiPorts.ALL_PORTS);
		master = ibis.registry().elect("Master");
		ibis.registry().enableEvents();

		if (isMaster()) {
			logger.info(" Master is {} ", master.name());
		}

		waitForJoin(numOfNodes);
	}


	/*
	 * We have the IbisIdentifier to identify a node.
	 * However, in the application we do not want to be dependent on Ibis and thus
	 * we identify the nodes by integers from 0 to NUMBER_OF_NODES - 1.
	 * The integer id of a node is simply its order in workers list
	 * (the id is given by the order in which the nodes connected).
	 * Relies heavily on the fact that the list of workers is the same on each pc!
	 */


	public IbisIdentifier convertNodeIDToIbisID(int nodeID) {
		return workers.get(nodeID);
	}


	public int convertIbisIDToNodeID(IbisIdentifier ibisID) {
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i) == ibisID) {
				return i;
			}
		}
		throw new RuntimeException("Converting ibis ID to node ID failed!");
	}


	public boolean isMaster() {
		return ibis.identifier().equals(master);
	}


	public void close() throws IOException {
		ibis.end();
	}


	/**
	 * Waits for the specified number of nodes to join the pool.
	 */
	private void waitForJoin(int numOfNodes) {
		synchronized (numOfNodesLock) {
			while (workers.size() < numOfNodes) {
				try {
					numOfNodesLock.wait();
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		for (IbisIdentifier worker: workers) {
			sb.append(worker.name() + "\t");
		}

		logger.debug("Workers: {}", sb.toString().trim());
	}
}
