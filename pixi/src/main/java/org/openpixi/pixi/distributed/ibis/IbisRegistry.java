package org.openpixi.pixi.distributed.ibis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ibis.ipl.*;
import org.openpixi.pixi.distributed.SimpleHandler;

/**
 * Wraps up work with ibis registry.
 * Handles the master election.
 * Collects information about other ibises connected to the pool.
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
		}

		public void electionResult(String arg0, IbisIdentifier arg1) {
		}

		public void gotSignal(String arg0, IbisIdentifier arg1) {
		}

		public void joined(IbisIdentifier ii) {
			synchronized (this) {
				all.add(ii);
				if (!ii.equals(master)) {
					slaves.add(ii);
				}
				notify();
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

	/** For distributing the problem. */
	public static final PortType DISTRIBUTE_PORT = new PortType(
			PortType.COMMUNICATION_RELIABLE,
			PortType.SERIALIZATION_OBJECT,
			PortType.RECEIVE_AUTO_UPCALLS,
			PortType.CONNECTION_ONE_TO_MANY);

	/** For collecting the results. */
    public static final PortType COLLECT_PORT = new PortType(
    		PortType.COMMUNICATION_RELIABLE,
    		PortType.SERIALIZATION_OBJECT, 
    		PortType.RECEIVE_AUTO_UPCALLS,
    		PortType.CONNECTION_MANY_TO_ONE);

    /** For the exchange of ghost cells and particles during the simulation. */
    public static final PortType EXCHANGE_PORT = new PortType(
    		PortType.COMMUNICATION_RELIABLE,
            PortType.SERIALIZATION_OBJECT, 
            PortType.RECEIVE_AUTO_UPCALLS,
            PortType.CONNECTION_ONE_TO_ONE);

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
	 */
	public IbisRegistry() throws Exception {
		ibis = IbisFactory.createIbis(
				ibisCapabilities, new RegistryEvent(),
				EXCHANGE_PORT, COLLECT_PORT, DISTRIBUTE_PORT);
		master = ibis.registry().elect("Master");
		ibis.registry().enableEvents();
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
	public void waitForJoin(int numOfNodes) {
		synchronized (this) {
			while (all.size() < numOfNodes) {
				try {
					wait();
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
	}
}