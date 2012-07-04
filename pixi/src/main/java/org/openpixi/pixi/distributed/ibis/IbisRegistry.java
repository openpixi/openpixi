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
 * Wraps up work of identifying master and slaves.
 */
public class IbisRegistry {

	/**
	 * Handles registry events.
	 */
	private class RegistryEvent implements RegistryEventHandler {

		public void died(IbisIdentifier ii) {
			all.remove(ii);
			if (ii.equals(master)) {
				masterLeftHandler.handle(null);
			} else {
				slaves.remove(ii);
			}

			if (isMaster()) {
				logger.info("Node {} left the pool", ii.name());
			}
		}

		public void electionResult(String arg0, IbisIdentifier arg1) {
		}

		public void gotSignal(String arg0, IbisIdentifier arg1) {
		}

		public void joined(IbisIdentifier ii) {
			synchronized (numOfNodesLock) {
				all.add(ii);
				if (!ii.equals(master)) {
					slaves.add(ii);
				}
				numOfNodesLock.notify();
			}

			if (isMaster()) {
				logger.info("Node {} joined the pool", ii.name());
			}
		}

		public void left(IbisIdentifier ii) {
			died(ii);
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

	private Object numOfNodesLock = new Object();

	private final List<IbisIdentifier> all =
			Collections.synchronizedList(new ArrayList<IbisIdentifier>());
	private final List<IbisIdentifier> slaves =
			Collections.synchronizedList(new ArrayList<IbisIdentifier>());

	private IbisIdentifier master;

	private Ibis ibis;

	/** This handler is called when master leaves or dies. */
	private SimpleHandler masterLeftHandler;


	public Ibis getIbis() {
		return ibis;
	}

	public void setMasterLeftHandler(SimpleHandler masterLeftHandler) {
		this.masterLeftHandler = masterLeftHandler;
	}

	public IbisIdentifier getMaster() {
		return master;
	}

	public List<IbisIdentifier> getSlaves() {
		return slaves;
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
			while (all.size() < numOfNodes) {
				try {
					numOfNodesLock.wait();
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
	}
}
