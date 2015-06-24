package org.openpixi.pixi.physics.fields.fieldgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.SU2Matrix;

public class SU2FocusedGaussianPulse implements IFieldGenerator {

	private int numberOfDimensions;
	private int numberOfComponents;
	private double[] direction;
	private double[] position;
	private double[] amplitudeSpatialDirection;
	private double[] amplitudeColorDirection;
	private double amplitudeMagnitude;
	private double sigma;
	private double angle;
	private double distance;

	private Simulation s;
	private Grid grid;
	private double timeStep;

	private double ph0;
	private double th0;

	public SU2FocusedGaussianPulse(double[] direction,
								   double[] position,
								   double[] amplitudeSpatialDirection,
								   double[] amplitudeColorDirection,
								   double amplitudeMagnitude,
								   double sigma,
								   double angle,
								   double distance) {
		this.numberOfDimensions = direction.length;
		this.numberOfComponents = amplitudeColorDirection.length;

		this.direction = this.normalizeVector(direction);
		this.position = position;
		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeSpatialDirection = this.normalizeVector(amplitudeSpatialDirection);
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);
		this.amplitudeMagnitude = amplitudeMagnitude;
		this.sigma = sigma;
		this.angle = angle;
		this.distance = distance;

		/*
			Precompute some angles.
		 */
		double[] directionSpherical = this.convertToSpherical(this.direction[0], this.direction[1], this.direction[2]);

		this.ph0 = directionSpherical[1];
		this.th0 = directionSpherical[2];
	}

	public void applyFieldConfiguration(Simulation s) {
		this.s = s;
		this.grid = s.grid;
		this.timeStep = s.getTimeStep();
		double c = s.getSpeedOfLight();

		double as = grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		/*
			Setup the field amplitude for the focused gaussian pulse.
		 */
		SU2Field[] amplitudeYMField = new SU2Field[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			amplitudeYMField[i] = new SU2Field(
					this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[0],
					this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[1],
					this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[2]);
		}

		int numberOfCells = grid.getTotalNumberOfCells();

		/*
			Cycle through each cell and apply the focused gaussian pulse configuration to the links and electric fields.
		 */
		for (int ci = 0; ci < numberOfCells; ci++) {
			int[] cellPosition = grid.getCellPos(ci);
			double[] currentPosition = getPosition(cellPosition);

			// Set origin to focal point of the pulse.
			double[] pos = new double[numberOfDimensions];
			for (int i = 0; i < numberOfDimensions; i++) {
				pos[i] = currentPosition[i] - this.position[i];
			}

			double[] spherical = this.convertToSpherical(pos[0], pos[1], pos[2]);

			// Multiplicative factor for the focused gaussian pulse at t = 0 (for electric fields)
			double electricFieldFactor = g * as * c * (spherical[0] - this.distance) / (sigma * sigma)
					* pulseFunction(spherical[0], spherical[1], spherical[2], 0);

			// Multiplicative factor for the focused gaussian pulse at t = dt/2 (for links)
			double gaugeFieldFactor = g * as * pulseFunction(spherical[0], spherical[1], spherical[2], this.timeStep / 2.0);


			Cell currentCell = grid.getCell(ci);

			for (int i = 0; i < this.numberOfDimensions; i++) {
				//Setup the gauge links
				SU2Matrix U = (SU2Matrix) currentCell.getU(i).mult(amplitudeYMField[i].mult(gaugeFieldFactor).getLinkExact());
				currentCell.setU(i, U);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(electricFieldFactor));
			}
		}
	}

	private double[] convertToSpherical(double x, double y, double z) {
		double r = Math.sqrt(x * x + y * y + z * z);
		double p = Math.atan2(y, x);
		double t = Math.acos(z / r);
		return new double[]{r, p, t};
	}

	private double pulseFunction(double r, double ph, double th, double t) {
		// Shape for the radial part
		double gauss = gaussian(r, this.distance - this.s.getSpeedOfLight() * t, this.sigma);

		// Shape for the angular parts
		// This is very clumsy but solves a problem connected to periodicity with angular coordinates.
		double phshape = 0.0;
		double thshape = 1.0;

		for (int i = -1; i <= 1; i++) {
			phshape += anglularShapeFunction(ph + 2.0 * Math.PI * i, ph0, angle, 2.0, 4.0);
		}


		thshape = anglularShapeFunction(th, th0, angle, 2.0, 4.0);

		return gauss * phshape * thshape;
	}

	private double anglularShapeFunction(double a, double a0, double da, double p, double q) {
		if (a0 - da < a && a < a0 + da) {
			double A = Math.pow(Math.abs(da), p);
			double B = Math.pow(Math.abs(a0 - a), p);

			return Math.pow(Math.sin((A - B) / A), q);
		}
		return 0.0;
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
