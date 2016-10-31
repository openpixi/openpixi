package org.openpixi.pixi.diagnostics.methods;

import java.io.IOException;
import java.util.ArrayList;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Applies the Coulomb Gauge to the current simulation at specified times.
 */
public class CoulombGaugeInTime implements Diagnostics {

	private double timeInterval;
	private int stepInterval;
	private double timeOffset;
	private int stepOffset;

	public CoulombGaugeInTime(double timeInterval, double timeOffset) {
		this.timeInterval = timeInterval;
		this.timeOffset = timeOffset;
	}

	@Override
	public void initialize(Simulation s) {
		this.stepInterval = (int) Math.max((timeInterval / s.getTimeStep()), 1);
		this.stepOffset = (int) (timeOffset / s.getTimeStep());
	}

	@Override
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps)
			throws IOException {
		if ((steps - stepOffset) % stepInterval == 0) {
			CoulombGauge coulombGauge = new CoulombGauge(grid);
			coulombGauge.applyGaugeTransformation(grid);
		}
	}
}
