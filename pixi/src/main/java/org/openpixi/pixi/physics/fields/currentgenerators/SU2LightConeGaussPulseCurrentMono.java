package org.openpixi.pixi.physics.fields.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.SU2AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.LightConePoissonSolver;
import org.openpixi.pixi.physics.fields.TempGaugeLightConeGaussPoissonSolver;
import org.openpixi.pixi.physics.grid.Grid;

public class SU2LightConeGaussPulseCurrentMono implements ICurrentGenerator {

	private int direction;
	private double[] location;
	private double[] amplitudeColorDirection;
	private double magnitude;
	private double width;
	private Grid grid;
	private int orientation;
	private LightConePoissonSolver poisson;

	public SU2LightConeGaussPulseCurrentMono(int direction, double[] location, double width, double[] amplitudeColorDirection, double magnitude, int orientation) {

		this.direction = direction;
		this.location = location;

		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

		this.magnitude = magnitude;
		this.width = width;
		this.orientation = orientation;
		this.poisson = new TempGaugeLightConeGaussPoissonSolver(location, direction, Integer.signum(orientation), width);
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		applyCurrent(s);
		poisson.solve(s.grid);
	}

	public void applyCurrent(Simulation s) {
		this.grid = s.grid;
		double as = grid.getLatticeSpacing();
		double at = s.getTimeStep();
		int time = s.totalSimulationSteps;
		int numberOfCells = grid.getNumCells(direction);
		double g = s.getCouplingConstant();
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
		SU2AlgebraElement fieldAmplitude = new SU2AlgebraElement(
				this.magnitude * speed * this.amplitudeColorDirection[0],
				this.magnitude * speed * this.amplitudeColorDirection[1],
				this.magnitude * speed * this.amplitudeColorDirection[2]);

		/*
			Setup the field amplitude for the charge.
		 */
		SU2AlgebraElement chargeAmplitude = new SU2AlgebraElement(
				this.magnitude * this.amplitudeColorDirection[0],
				this.magnitude * this.amplitudeColorDirection[1],
				this.magnitude * this.amplitudeColorDirection[2]);

		fieldAmplitude.multAssign(1 * g * as);	// This factor comes from the dimensionality of the current density
		chargeAmplitude.multAssign(1 * g * as);	// The factor g*as comes from our definition of electric fields!!

		/*
			Find the nearest grid point and apply the current configuration to the cell current.
		 */
		double position = location[direction] + speed * time * at;
		double posCharge = position - speed*at/2;

		for (int i = 0; i < numberOfCells; i++) {
			pos[direction] = i;
			int cellIndex = grid.getCellIndex(pos);

			grid.addJ(cellIndex, direction, fieldAmplitude.mult(shape(position, i*as)));
			grid.addRho(cellIndex, chargeAmplitude.mult(shape(posCharge, i * as)));
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
		//Gaussian gauss = new Gaussian(1.0/(width*Math.sqrt(2*Math.PI)), mean, width);
		//double value = Math.exp(-Math.pow(x - mean, 2)/(2*width*width))/(width*Math.sqrt(2*Math.PI));
		return gauss.value(x);
	}
}