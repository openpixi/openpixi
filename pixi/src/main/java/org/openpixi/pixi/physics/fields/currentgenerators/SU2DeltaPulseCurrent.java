package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;

public class SU2DeltaPulseCurrent implements ICurrentGenerator {

	private int direction;
	private int[] location;
	private double[] amplitudeColorDirection;
	private double magnitude;
	private double speed;
	private Simulation s;
	private Grid grid;

	public SU2DeltaPulseCurrent(int direction, int[] location, double[] amplitudeColorDirection, double magnitude, double speed) {

		this.direction = direction;
		this.location = location;

		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

		this.magnitude = magnitude;
		this.speed = speed;
	}

	public void applyCurrent(Simulation s) {
		this.s = s;
		this.grid = s.grid;
		double as = grid.getLatticeSpacing();
		double at = s.getTimeStep();
		double time = s.totalSimulationTime;
		double g = s.getCouplingConstant();
		double normFactor = as/(Math.pow(as, grid.getNumberOfDimensions())*at);
		int initialPosition = location[direction];

		/*
			Setup the field amplitude for the current.
		 */
		SU2Field fieldAmplitude = new SU2Field(
				this.magnitude * this.speed * this.amplitudeColorDirection[0],
				this.magnitude * this.speed * this.amplitudeColorDirection[1],
				this.magnitude * this.speed * this.amplitudeColorDirection[2]);

		fieldAmplitude.mult(normFactor);	// This factor comes from the dimensionality of the current density

		/*
			Find the nearest grid point and apply the current configuration to the cell current.
		 */
		int position = (int) Math.rint(initialPosition + speed*time/as);
		location[direction] = position;
		int cellIndex = grid.getCellIndex(location);

		grid.addJ(cellIndex, direction, fieldAmplitude.mult(g*as));	// The factor g*as comes from our definition of electric fields!!


	}

	private double[] normalizeVector(double[] vector) {
		double norm = 0.0;
		double[] output = new double[vector.length];
		for (int i = 0; i < vector.length; i++) {
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < vector.length; i++) {
			output[i] = vector[i] / norm;
		}
		return output;
	}
}
