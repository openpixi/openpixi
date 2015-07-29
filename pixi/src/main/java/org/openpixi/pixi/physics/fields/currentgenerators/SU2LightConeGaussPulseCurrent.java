package org.openpixi.pixi.physics.fields.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.LightConePoissonSolver;
import org.openpixi.pixi.physics.fields.TempGaugeLightConeGaussPoissonSolver;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;

public class SU2LightConeGaussPulseCurrent implements ICurrentGenerator {

	private int direction;
	private double[] location;
	private double[] amplitudeColorDirection;
	private double magnitude;
	private double width;
	private Grid grid;
	private int orientation;
	private LightConePoissonSolver poisson;

	public SU2LightConeGaussPulseCurrent(int direction, double[] location, double width, double[] amplitudeColorDirection, double magnitude, int orientation) {

		this.direction = direction;
		this.location = location;

		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

		this.magnitude = magnitude;
		this.width = width;
		this.orientation = orientation;
		this.poisson = new TempGaugeLightConeGaussPoissonSolver(location, direction, orientation, width);
	}

	public void applyCurrent(Simulation s) {
		this.grid = s.grid;
		double as = grid.getLatticeSpacing();
		double at = s.getTimeStep();
		int time = s.totalSimulationSteps;
		int numberOfCells = grid.getNumCells(direction);
		double g = s.getCouplingConstant();
		double normFactor = as/(Math.pow(as, grid.getNumberOfDimensions())*at);
		double chargeNorm = 1.0/(Math.pow(as, grid.getNumberOfDimensions()));
		double speed = s.getSpeedOfLight()*Integer.signum(orientation);
		int[] pos = new int[location.length];
		for (int i = 0; i < location.length; i++) {
			pos[i] = (int) Math.rint(location[i]/as);
			if( (s.totalSimulationSteps == 0) && (Math.abs((location[i]/as) % pos[i]) > 0.0001) ) {
				System.out.println("SU2LightConeGaussPulseCurrent: location is at a non-integer grid position!.");
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

		fieldAmplitude.multequate(chargeNorm*g*as);	// This factor comes from the dimensionality of the current density
		chargeAmplitude.multequate(chargeNorm*g*as);	// The factor g*as comes from our definition of electric fields!!

		/*
			Find the nearest grid point and apply the current configuration to the cell current.
		 */
		int position;
		if(orientation < 0) {
			position = (int) Math.ceil(Math.rint(location[direction]/as) + speed * time * at / as);
		} else {
			position = (int) Math.floor(Math.rint(location[direction]/as) + speed * time * at / as);
		}

		for (int i = 0; i < numberOfCells; i++) {
			pos[direction] = i;
			int cellIndex = grid.getCellIndex(pos);
			int chargeIndex = cellIndex;
			if(orientation < 0) {
				chargeIndex = grid.shift(chargeIndex, direction, 1);
			}

			grid.addJ(cellIndex, direction, fieldAmplitude.mult(shape(position*as, i*as)));
			grid.setRho(chargeIndex, chargeAmplitude.mult(shape(position*as, i*as)));
		}

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

	private double shape(double mean, double x) {
		Gaussian gauss = new Gaussian(mean, width);
		double value = gauss.value(x);
		return value;
	}
}
