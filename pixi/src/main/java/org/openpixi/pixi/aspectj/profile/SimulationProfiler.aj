package org.openpixi.pixi.aspectj.profile;

import org.aspectj.lang.annotation.AdviceName;

/**
 * Measures the duration of the simulation and the duration of its four main steps
 * (push particles, interpolate to grid, solve fields, interpolate to particle)
 */
public aspect SimulationProfiler {

	private long simulationTime;
	private long pushParticlesTime;
	private long interpolateToGridTime;
	private long solveFieldsTime;
	private long interpolateToParticleTime;


	public long getSimulationTime() {
		return simulationTime;
	}

	public long getPushParticlesTime() {
		return pushParticlesTime;
	}

	public long getInterpolateToGridTime() {
		return interpolateToGridTime;
	}

	public long getSolveFieldsTime() {
		return solveFieldsTime;
	}

	public long getInterpolateToParticleTime() {
		return interpolateToParticleTime;
	}


	@AdviceName("recordSimulationStepTime")
	Object around(): execution(* *..Simulation.step()) {
		long start = System.nanoTime();
		Object o = proceed();
		simulationTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("recordParticlePushTime")
	Object around(): execution(* *..ParticleMover.push(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		pushParticlesTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("recordInterpolateToGridTime")
	Object around(): execution(* *..InterpolationIterator.interpolateToGrid(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		interpolateToGridTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("recordInterpolateToParticleTime")
	Object around(): execution(* *..InterpolationIterator.interpolateToParticle(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		interpolateToParticleTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("recordSolveFieldsTime")
	Object around(): execution(* *..Grid.updateGrid(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		solveFieldsTime += System.nanoTime() - start;
		return o;
	}


	@AdviceName("printTimes")
	after(): execution(* *..ProfileRunner.main(..)) {
		Print.totalTime("Simulation time", simulationTime);
		Print.partTime("Push particles time", pushParticlesTime, simulationTime);
		Print.partTime("Interpolate to grid time", interpolateToGridTime, simulationTime);
		Print.partTime("Solve fields time", solveFieldsTime, simulationTime);
		Print.partTime("Interpolate to particle time", interpolateToParticleTime, simulationTime);
	}
}
