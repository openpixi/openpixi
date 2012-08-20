package org.openpixi.pixi.aspectj.debug;

import org.openpixi.pixi.distributed.Worker;

/**
 * Abstract aspect which defines pointcuts for distributed simulation.
 */
public abstract aspect DistributedSimulationDebug {
	protected pointcut underWorkerStep(Worker w): cflow(call(* *..step(..)) && target(w));
}
