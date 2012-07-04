package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.PortType;

/**
 * Defines ibis ports used in pixi.
 */
public class PixiPorts {

	/** For communication from master to all slaves. */
	public static final PortType SCATTER_PORT = new PortType(
			PortType.COMMUNICATION_RELIABLE,
			PortType.SERIALIZATION_OBJECT,
			PortType.RECEIVE_AUTO_UPCALLS,
			PortType.RECEIVE_EXPLICIT,
			PortType.CONNECTION_ONE_TO_MANY);

	public static final String SCATTER_PORT_ID = "scatter";

	/** For communication from slaves to master. */
	public static final PortType GATHER_PORT = new PortType(
			PortType.COMMUNICATION_RELIABLE,
			PortType.SERIALIZATION_OBJECT,
			PortType.RECEIVE_AUTO_UPCALLS,
			PortType.CONNECTION_MANY_TO_ONE);

	public static final String GATHER_PORT_ID = "gather";

	/** For the exchange of ghost cells and particles during the simulation. */
	public static final PortType EXCHANGE_PORT = new PortType(
			PortType.COMMUNICATION_RELIABLE,
			PortType.SERIALIZATION_OBJECT,
			PortType.RECEIVE_AUTO_UPCALLS,
			PortType.CONNECTION_ONE_TO_ONE);

	public static final String EXCHANGE_PORT_ID = "exchange";

	public static final PortType[] ALL_PORTS = {SCATTER_PORT, GATHER_PORT, EXCHANGE_PORT};
}
