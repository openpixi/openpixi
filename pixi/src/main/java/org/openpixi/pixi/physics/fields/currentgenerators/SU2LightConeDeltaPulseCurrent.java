package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.fields.LightConePoissonSolver;

public class SU2LightConeDeltaPulseCurrent implements ICurrentGenerator {

	private int direction;
	private double[] location;
	private double[] amplitudeColorDirection;
	private double magnitude;
	private Grid grid;
	private int orientation;
	private LightConePoissonSolver poisson;

	public SU2LightConeDeltaPulseCurrent(int direction, double[] location, double[] amplitudeColorDirection, double magnitude, int orientation) {

		this.direction = direction;
		this.location = location;

		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

		this.magnitude = magnitude;
		this.orientation = orientation;
		this.poisson = new LightConePoissonSolver(location, direction, orientation);
	}

	public void applyCurrent(Simulation s) {
		this.grid = s.grid;
		double as = grid.getLatticeSpacing();
		double at = s.getTimeStep();
		int time = s.totalSimulationSteps;
		double g = s.getCouplingConstant();
		double normFactor = as/(Math.pow(as, grid.getNumberOfDimensions())*at);
		double chargeNorm = 1.0/(Math.pow(as, grid.getNumberOfDimensions()));
		double speed = s.getSpeedOfLight()*Integer.signum(orientation);
		int[] pos = new int[location.length];
		for (int i = 0; i < location.length; i++) {
			pos[i] = (int) Math.rint(location[i]/as);
			if( (s.totalSimulationSteps == 0) && (Math.abs((location[i]/as) % pos[i]) > 0.0001) ) {
				System.out.println("SU2LightConeDeltaPulseCurrent: location is at a non-integer grid position!.");
			}
		}

		/*
			Setup the field amplitude for the current.
		 */
		SU2Field fieldAmplitude = new SU2Field(
				this.magnitude * speed * this.amplitudeColorDirection[0],
				this.magnitude * speed * this.amplitudeColorDirection[1],
				this.magnitude * speed * this.amplitudeColorDirection[2]);

		/*
			Setup the field amplitude for the charge.
		 */
		SU2Field chargeAmplitude = new SU2Field(
				this.magnitude * this.amplitudeColorDirection[0],
				this.magnitude * this.amplitudeColorDirection[1],
				this.magnitude * this.amplitudeColorDirection[2]);

		fieldAmplitude.multequate(chargeNorm);	// This factor comes from the dimensionality of the current density
		chargeAmplitude.multequate(chargeNorm);

		/*
			Find the nearest grid point and apply the current configuration to the cell current.
		 */
		int position;
		if(orientation < 0) {
			position = (int) Math.ceil(Math.rint(location[direction]/as) + speed * time * at / as);
		} else {
			position = (int) Math.floor(Math.rint(location[direction]/as) + speed * time * at / as);
		}
		//int position = (int) Math.rint(initialPosition + speed*time*at/as);
		//int position = (int) Math.floor(initialPosition + speed * time * at / as);
		pos[direction] = position;
		int cellIndex = grid.getCellIndex(pos);

		int chargeIndex = cellIndex;
		if(orientation < 0) {
			chargeIndex = grid.shift(chargeIndex, direction, 1);
		}

		grid.addJ(cellIndex, direction, fieldAmplitude.mult(g * as));	// The factor g*as comes from our definition of electric fields!!
		grid.setRho(chargeIndex, chargeAmplitude.mult(g * as));
		if(time == 0) {
			poisson.solve(grid);
		}
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
