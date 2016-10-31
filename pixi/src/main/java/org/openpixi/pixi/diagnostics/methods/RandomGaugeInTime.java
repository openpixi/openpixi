package org.openpixi.pixi.diagnostics.methods;

import java.io.IOException;
import java.util.ArrayList;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.gauge.RandomGauge;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Applies the Coulomb Gauge to the current simulation at specified times.
 */
public class RandomGaugeInTime implements Diagnostics {

	private double timeInterval;
	private int stepInterval;
	private double timeOffset;
	private int stepOffset;
	/**
	 * Random vector in color space.
	 */
	private double[] randomVector;

	public RandomGaugeInTime(double timeInterval, double timeOffset, double[] randomVector) {
		this.timeInterval = timeInterval;
		this.timeOffset = timeOffset;
		this.randomVector = randomVector;
	}

	@Override
	public void initialize(Simulation s) {
		this.stepInterval = (int) Math.max(Math.round((timeInterval / s.getTimeStep())), 1);
		this.stepOffset = (int) (timeOffset / s.getTimeStep());
	}

	@Override
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps)
			throws IOException {
		if ((steps - stepOffset) % stepInterval == 0) {
			RandomGauge randomGauge = new RandomGauge(grid);
			randomGauge.setRandomVector(randomVector);
			randomGauge.applyGaugeTransformation(grid);
		}
	}
}
