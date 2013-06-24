package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.solver.Solver;
import org.openpixi.pixi.physics.force.Force;

/**
 * Logs the following particle movement information:
 * - position before move
 * - position after move and before boundary check
 * - distance covered + warnings when the distance is larger than the size of cell
 *   or simulation area
 * - position after boundary check
 *
 * <p>Enable logging by setting the variable 'enabled' below to 'true'.</p>
 */
public privileged aspect MovementDebug {

	/**
	 * Enable logging by setting this variable to 'true'.
	 */
	final static boolean enabled = false;

	pointcut particleChecked(Particle p):
			call(* *..ParticleBoundaries.applyOnParticleCenter(Solver, Force, Particle, ..)) 
			&& args(Solver, Force, p, ..) && withincode(* *..Push.execute(..));

	pointcut underSimulationStep(Simulation sim):
			cflow(call(* *..step()) && target(sim));

	@AdviceName("logMovement")
	Object around(Particle p, Simulation sim): if(enabled) && particleChecked(p) && underSimulationStep(sim) {

		double prevX = p.getPrevX();
		double prevY = p.getPrevY();
		double beforeBoundaryX = p.getX();
		double beforeBoundaryY = p.getY();
		double distanceX = beforeBoundaryX - prevX;
		double distanceY = beforeBoundaryY - prevY;
		Object retval = proceed(p, sim);
		double afterBoundaryX = p.getX();
		double afterBoundaryY = p.getY();

		System.out.println(String.format(
				"Particle %d moved from %s to %s (before boundary) %s (after boundary) " +
				"covering distance %.3f",
				p.id,
				positionToStr(prevX, prevY),
				positionToStr(beforeBoundaryX, beforeBoundaryY),
				positionToStr(afterBoundaryX, afterBoundaryY),
				Math.sqrt(distanceX * distanceX + distanceY * distanceY)));
		
		if (distanceX > sim.grid.getCellWidth() || distanceY > sim.grid.getCellHeight()) {
			System.out.println(
					"WARNING: Particle " + p.id +
					" covered distance larger than the dimensions of one CELL!");
		}

		if (distanceX > sim.getWidth() || distanceY > sim.getHeight()) {
			System.out.println(
					"WARNING: Particle " + p.id +
							" covered distance larger than the dimensions of SIMULATION AREA!");
		}

		return retval;
	}

	private String positionToStr(double x, double y) {
		return String.format("[%.3f,%.3f]", x, y);
	}
}
