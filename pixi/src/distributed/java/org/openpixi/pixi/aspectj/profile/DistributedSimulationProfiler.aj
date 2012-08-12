package org.openpixi.pixi.aspectj.profile;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.distributed.ibis.IbisRegistry;

/**
 * Measures additional times present in distributed simulation.
 */
public aspect DistributedSimulationProfiler {

	private long arrivingParticlesWaitingTime;
	private long ghostParticlesWaitingTime;
	private long ghostCellsWaitingTime;
	private boolean isMaster;

	/** Detects distributed simulation. */
	private boolean isDistributedSimulation;


	@AdviceName("isMaster")
	after(IbisRegistry registry): execution(*..new(..)) && target(registry) {
		isMaster = registry.isMaster();
	}


	@AdviceName("recordArrivingParticlesWaitingTime")
	Object around(): execution(* *..SharedDataManager.getArrivingParticles(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		arrivingParticlesWaitingTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("recordGhostParticlesWaitingTime")
	Object around(): execution(* *..SharedDataManager.getGhostParticles(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		ghostParticlesWaitingTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("recordGhostCellsWaitingTime")
	Object around(): execution(* *..SharedDataManager.waitForGhostCells(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		ghostCellsWaitingTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("detectDistributedRun")
	after(): execution(*..Node.new(..)) {
		isDistributedSimulation = true;
	}


	@AdviceName("printTimes")
	after(): execution(* *..ProfileRunner.main(..)) {
		if (!isDistributedSimulation) {
			return;
		}

		SimulationProfiler sp = SimulationProfiler.aspectOf();
		Print.partTime("Arriving particles waiting time",
				arrivingParticlesWaitingTime, sp.getInterpolateToGridTime());
		Print.partTime("Ghost particles waiting time",
				ghostParticlesWaitingTime, sp.getInterpolateToGridTime());
		Print.partTime("Ghost cells waiting time",
				ghostCellsWaitingTime, sp.getInterpolateToParticleTime());
		if (isMaster) {
			System.out.println("MASTER");
		}
	}
}
