package org.openpixi.pixi.aspectj.profile;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.distributed.profile.DistributedProfileInfo;

/**
 * Measures additional times present in distributed simulation.
 */
public aspect DistributedSimulationProfiler {

	@AdviceName("recordArrivingParticlesWaitingTime")
	Object around(): execution(* *..SharedDataManager.getArrivingParticles(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		DistributedProfileInfo.addArrivingParticlesWaitingTime(System.nanoTime() - start);
		return o;
	}


	@AdviceName("recordGhostParticlesWaitingTime")
	Object around(): execution(* *..SharedDataManager.getGhostParticles(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		DistributedProfileInfo.addGhostParticlesWaitingTime(System.nanoTime() - start);
		return o;
	}


	@AdviceName("recordGhostCellsWaitingTime")
	Object around(): execution(* *..SharedDataManager.waitForGhostCells(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		DistributedProfileInfo.addGhostCellsWaitingTime(System.nanoTime() - start);
		return o;
	}
}
