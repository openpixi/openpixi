package org.openpixi.pixi.aspectj.profile;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.profile.ProfileInfo;

/**
 * Measures the duration of the simulation and the duration of its four main steps
 * (push particles, interpolate to grid, solve fields, interpolate to particle)
 */
public aspect SimulationProfiler {


	@AdviceName("recordProfilingOn")
	after(): call(*..Simulation.new(..)) {
		ProfileInfo.setProfilingOn();
	}


	@AdviceName("recordSimulationStepTime")
	Object around(): execution(* *..Simulation.step()) {
		long start = System.nanoTime();
		Object o = proceed();
		ProfileInfo.addSimulationTime(System.nanoTime() - start);
		return o;
	}


	@AdviceName("recordParticlePushTime")
	Object around(): execution(* *..ParticleMover.push(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		ProfileInfo.addPushParticlesTime(System.nanoTime() - start);
		return o;
	}


	@AdviceName("recordInterpolateToGridTime")
	Object around(): execution(* *..Interpolation.interpolateToGrid(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		ProfileInfo.addInterpolateToGridTime(System.nanoTime() - start);
		return o;
	}


	@AdviceName("recordInterpolateToParticleTime")
	Object around(): execution(* *..Interpolation.interpolateToParticle(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		ProfileInfo.addInterpolateToParticleTime(System.nanoTime() - start);
		return o;
	}


	@AdviceName("recordSolveFieldsTime")
	Object around(): execution(* *..Grid.updateGrid(..)) {
		long start = System.nanoTime();
		Object o = proceed();
		ProfileInfo.addSolveFieldsTime(System.nanoTime() - start);
		return o;
	}
}
