package org.openpixi.pixi.physics.initial.YM;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.initial.IInitialCondition;

public class SU2GaussianPulse implements IInitialCondition {

	private int numberOfDimensions;
	private int numberOfComponents;
	private double[] direction;
	private double[] position;
	private double[] amplitudeSpatialDirection;
	private double[] amplitudeColorDirection;
	private double amplitudeMagnitude;
	private double[] sigma;

	private Simulation s;
	private Grid grid;
	private double timeStep;

	public SU2GaussianPulse(double[] direction,
							double[] position,
							double[] amplitudeSpatialDirection,
							double[] amplitudeColorDirection,
							double amplitudeMagnitude,
							double[] sigma) {
		this.numberOfDimensions = direction.length;
		this.numberOfComponents = amplitudeColorDirection.length;

		this.direction = direction;
		this.position = position;
		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeSpatialDirection = this.normalizeVector(amplitudeSpatialDirection);
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);
		this.amplitudeMagnitude = amplitudeMagnitude;
		this.sigma = sigma;
	}

	public void applyInitialCondition(Simulation s) {
		this.s = s;
		this.grid = s.grid;
		this.timeStep = s.getTimeStep();
		double c = s.getSpeedOfLight();

		double as = grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		ElementFactory factory = grid.getElementFactory();
		int colors = grid.getNumberOfColors();

		/*
			Setup the field amplitude for the gaussian pulse.
		 */
		AlgebraElement[] amplitudeYMField = new AlgebraElement[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			amplitudeYMField[i] = factory.algebraZero(colors);
			for (int j = 0; j < this.numberOfComponents; j++) {
				amplitudeYMField[i].set(j,this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[j]);
			}
		}

		int numberOfCells = grid.getTotalNumberOfCells();

		/*
			Cycle through each cell and apply the gaussian pulse configuration to the links and electric fields.
		 */
		for (int ci = 0; ci < numberOfCells; ci++) {
			int[] cellPosition = grid.getCellPos(ci);
			double[] currentPosition = getPosition(cellPosition);

			// Multiplicative factor for the gaussian pulse at t = 0 (for electric fields)
			double tmp = 0.0;
			for (int i = 0; i < numberOfDimensions; i++) {
				tmp += c * this.direction[i] * (currentPosition[i] - this.position[i]) / Math.pow(this.sigma[i], 2);
			}
			for (int i = 0; i < numberOfDimensions; i++) {
				tmp *= gaussian(currentPosition[i], this.position[i], this.sigma[i]);
			}
			double electricFieldFactor = -g * as * tmp;

			// Multiplicative factor for the gaussian pulse at t = -dt/2 (for links)
			tmp = 1.0;
			for (int i = 0; i < numberOfDimensions; i++) {
				tmp *= gaussian(currentPosition[i], this.position[i] - c * timeStep / 2.0 * this.direction[i], this.sigma[i]);
			}
			double gaugeFieldFactor = g * as * tmp;


			Cell currentCell = grid.getCell(ci);

			for (int i = 0; i < this.numberOfDimensions; i++) {
				//Setup the gauge links
				GroupElement U = currentCell.getU(i).mult(amplitudeYMField[i].mult(gaugeFieldFactor).getLink());
				currentCell.setU(i, U);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(electricFieldFactor));
			}
		}
	}

	private double gaussian(double x, double x0, double sx) {
		return Math.exp(-0.5 * Math.pow((x - x0) / sx, 2));
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

	private double[] getPosition(int[] cellPosition) {
		double[] position = new double[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			position[i] = cellPosition[i] * grid.getLatticeSpacing();
		}
		return position;
	}
}
