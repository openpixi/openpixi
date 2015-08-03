package org.openpixi.pixi.physics.fields.fieldgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.SU2Matrix;

public class SUNFocusedGaussianPulse implements IFieldGenerator {

	private int numberOfDimensions;
	private int numberOfComponents;
	private double[] direction;
	private double[] position;
	private double amplitudePolarisationAngle;
	private double[] amplitudeColorDirection;
	private double amplitudeMagnitude;
	private double sigma;
	private double angle;
	private double distance;

	private Simulation s;
	private Grid grid;
	private double timeStep;

	private double[] referenceDirection;
	private double[] rotationAxis;
	private double rotationAngle;

	private final double ALMOST_ZERO = 10e-12;

	public SUNFocusedGaussianPulse(double[] direction,
								   double[] position,
								   double amplitudePolarisationAngle,
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
		this.amplitudePolarisationAngle = amplitudePolarisationAngle;
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);
		this.amplitudeMagnitude = amplitudeMagnitude;
		this.sigma = sigma;
		this.angle = angle;
		this.distance = distance;

		this.referenceDirection = new double[]{1.0, 0.0, 0.0};
		this.rotationAxis = normalizeVector(cross(referenceDirection, this.direction));
		if(dot(referenceDirection, this.direction) > 0 ) {
			this.rotationAngle = Math.asin(norm(cross(referenceDirection, this.direction)));
		} else {
			this.rotationAngle = Math.asin(norm(cross(referenceDirection, this.direction))) + Math.PI;
		}

		// if direction points in the same direction as reference direction then the rotation has to be done differently.
		if(norm(cross(referenceDirection, this.direction)) < ALMOST_ZERO) {
			this.rotationAxis = new double[]{0.0, 1.0, 0.0};
			this.rotationAngle = Math.acos(dot(referenceDirection, this.direction)); // should either be +pi, -pi or 0
		}
	}

	public void applyFieldConfiguration(Simulation s) {
		this.s = s;
		this.grid = s.grid;
		this.timeStep = s.getTimeStep();
		double c = s.getSpeedOfLight();

		double as = grid.getLatticeSpacing();
		double g = s.getCouplingConstant();


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

			double[] rotatedPos = this.rotateVector(pos, rotationAxis, rotationAngle);
			double[] spherical = this.convertToSpherical(rotatedPos[0], rotatedPos[1], rotatedPos[2]);


			// Setup the field amplitude for the focused gaussian pulse.

			double[] amplitudeSpatialDirection = getVectorFieldDirection(spherical, amplitudePolarisationAngle);
			amplitudeSpatialDirection = rotateVector(amplitudeSpatialDirection, rotationAxis, -rotationAngle);
			SU2Field[] amplitudeYMField = new SU2Field[this.numberOfDimensions];
			for (int i = 0; i < this.numberOfDimensions; i++) {
				amplitudeYMField[i] = new SU2Field(
						this.amplitudeMagnitude * amplitudeSpatialDirection[i] * this.amplitudeColorDirection[0],
						this.amplitudeMagnitude * amplitudeSpatialDirection[i] * this.amplitudeColorDirection[1],
						this.amplitudeMagnitude * amplitudeSpatialDirection[i] * this.amplitudeColorDirection[2]);
			}

			// Multiplicative factor for the focused gaussian pulse at t = 0 (for electric fields)
			double electricFieldFactor = g * as * c * (spherical[0] - this.distance) / (sigma * sigma)
					* pulseFunction(spherical[0], spherical[1], spherical[2], 0);

			// Multiplicative factor for the focused gaussian pulse at t = -dt/2 (for links)
			double gaugeFieldFactor = g * as * pulseFunction(spherical[0], spherical[1], spherical[2], - this.timeStep / 2.0);


			Cell currentCell = grid.getCell(ci);

			for (int i = 0; i < this.numberOfDimensions; i++) {
				//Setup the gauge links
				SU2Matrix U = (SU2Matrix) currentCell.getU(i).mult(amplitudeYMField[i].mult(gaugeFieldFactor).getLinkExact());
				currentCell.setU(i, U);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(electricFieldFactor));
			}
		}

		// Apply Coulomb gauge
		CoulombGauge coulombGauge = new CoulombGauge(s.grid);
		coulombGauge.applyGaugeTransformation(grid);
	}

	private double[] convertToSpherical(double x, double y, double z) {
		double r = Math.sqrt(x * x + y * y + z * z);
		double p;
		if(Math.abs(x) > ALMOST_ZERO || Math.abs(y) > ALMOST_ZERO) {
			p = Math.atan2(y, x);
		} else {
			p = 0.0;
		}
		double t;
		if(r > 10e-10) {
			t = Math.acos(z / r);
		} else {
			t = 0.0;
		}
		return new double[]{r, p, t};
	}

	private double[] getVectorFieldDirection(double[] spherical, double a) {
		double[] vector = new double[3];
		double t = spherical[2];
		double p = spherical[1];

		vector[0] = Math.cos(a)*Math.cos(t) * Math.cos(p)	+ Math.sin(a) * (- Math.sin(p));
		vector[1] = Math.cos(a)*Math.cos(t) * Math.sin(p)	+ Math.sin(a) * Math.cos(p);
		vector[2] = Math.cos(a)*Math.sin(t);

		return normalizeVector(vector);
	}

	private double pulseFunction(double r, double ph, double th, double t) {
		// Shape for the radial part
		double gauss = gaussian(r, this.distance - this.s.getSpeedOfLight() * t, this.sigma);

		// Shape for the angular parts
		// This is very clumsy but solves a problem connected to periodicity with angular coordinates.
		double phshape = 0.0;
		double thshape;

		for (int i = -1; i <= 1; i++) {
			phshape += angularShapeFunction(ph + 2.0 * Math.PI * i, 0, angle, 2.0, 4.0);
		}


		thshape = angularShapeFunction(th, Math.PI/2.0, angle, 2.0, 4.0);

		return gauss * phshape * thshape;
	}

	private double angularShapeFunction(double a, double a0, double da, double p, double q) {
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
		double[] output = new double[vector.length];
		double n = norm(vector);
		for (int i = 0; i < vector.length; i++) {
			output[i] = vector[i] / n;
		}
		return output;
	}

	private double[] rotateVector(double[] v, double[] k, double a) {
		if(Math.abs(a) > ALMOST_ZERO) {
			double[] c = cross(k, v);
			double d = dot(k, v);
			double ca = Math.cos(a);
			double sa = Math.sin(a);

			double[] result = new double[3];
			for (int i = 0; i < 3; i++) {
				result[i] = v[i] * ca + c[i] * sa + k[i] * d * (1.0 - ca);
			}
			return result;
		}
		return v.clone();
	}

	private double dot(double[] v1, double[] v2) {
		return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
	}

	private double norm(double[] v) {
		return Math.sqrt(dot(v,v));
	}

	private double[] cross(double[] v1, double[] v2) {
		return new double[]{
				v1[1] * v2[2] - v1[2] * v2[1],
				v1[2] * v2[0] - v1[0] * v2[2],
				v1[0] * v2[1] - v1[1] * v2[0]
		};
	}

	private double[] getPosition(int[] cellPosition) {
		double[] position = new double[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			position[i] = cellPosition[i] * grid.getLatticeSpacing();
		}
		return position;
	}
}
