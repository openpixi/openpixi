package org.openpixi.pixi.physics.fields.fieldgenerators;

import org.openpixi.pixi.diagnostics.methods.GaussConstraintRestoration;
import org.openpixi.pixi.math.SU2AlgebraElement;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

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

	/**
	 * Creates the instance of the field generator.
	 *
	 * @param direction						spatial direction of the pulse
	 * @param position						point of convergence
	 * @param amplitudePolarisationAngle	angle of the polarisation vector
	 * @param amplitudeColorDirection		color amplitude of the pulse
	 * @param amplitudeMagnitude			overall magnitude of the total amplitude
	 * @param sigma							width of the radial Gauss profile
	 * @param angle							angular spread of the pulse at beginning
	 * @param distance						starting distance from the point of convergence
	 */
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

	/**
	 * Applies the pulse configuration to the grid.
	 *
	 * @param s	reference to the simulation
	 */
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
			SU2AlgebraElement[] amplitudeYMField = new SU2AlgebraElement[this.numberOfDimensions];
			for (int i = 0; i < this.numberOfDimensions; i++) {
				amplitudeYMField[i] = new SU2AlgebraElement(
						this.amplitudeMagnitude * amplitudeSpatialDirection[i] * this.amplitudeColorDirection[0],
						this.amplitudeMagnitude * amplitudeSpatialDirection[i] * this.amplitudeColorDirection[1],
						this.amplitudeMagnitude * amplitudeSpatialDirection[i] * this.amplitudeColorDirection[2]);
			}

			// Multiplicative factor for the focused gaussian pulse at t = 0 (for electric fields)
			double electricFieldFactor = g * as * c * (spherical[0] - this.distance) / (sigma * sigma)
					* pulseFunction(spherical[0], spherical[1], spherical[2], 0);

			// Multiplicative factor for the focused gaussian pulse at t = -dt/2 (for links)
			double gaugeFieldFactor = g * as * pulseFunction(spherical[0], spherical[1], spherical[2], - this.timeStep / 2.0);
			double gaugeFieldFactor2 = g * as * pulseFunction(spherical[0], spherical[1], spherical[2], + this.timeStep / 2.0);


			Cell currentCell = grid.getCell(ci);

			for (int i = 0; i < this.numberOfDimensions; i++) {
				//Setup the gauge links
				SU2GroupElement U = (SU2GroupElement) currentCell.getU(i).mult(amplitudeYMField[i].mult(gaugeFieldFactor).getLink());
				currentCell.setU(i, U);

				SU2GroupElement Unext = (SU2GroupElement) currentCell.getUnext(i).mult(amplitudeYMField[i].mult(gaugeFieldFactor2).getLink());
				currentCell.setUnext(i, Unext);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(electricFieldFactor));
			}
		}

		// Apply Coulomb gauge
		CoulombGauge coulombGauge = new CoulombGauge(s.grid);
		coulombGauge.applyGaugeTransformation(grid);

		// Restore Gauss constraint
		GaussConstraintRestoration gaussRestoration = new GaussConstraintRestoration(10.0, 0.0, 0.5, 100, 10e-4);
		gaussRestoration.iterateRestorationAlgorithm(grid);
	}

	/**
	 * Converts 3D euclidian coordinates to spherical coordinates with the origin at (0,0,0).
	 * @param x	x coordinate
	 * @param y	y coordinate
	 * @param z	z coordinate
	 * @return	an array with the spherical cooridinates {radius, phi (azimuthal angle), theta (polar angle)}.
	 */
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

	/**
	 * Returns the polarisation direction of the pulse at a certain point.
	 * @param spherical	point given in spherical coordinates
	 * @param a			polarisation angle
	 * @return			normalized polarisation vector at a given point
	 */
	private double[] getVectorFieldDirection(double[] spherical, double a) {
		double[] vector = new double[3];
		double t = spherical[2];
		double p = spherical[1];

		vector[0] = Math.cos(a)*Math.cos(t) * Math.cos(p)	+ Math.sin(a) * (- Math.sin(p));
		vector[1] = Math.cos(a)*Math.cos(t) * Math.sin(p)	+ Math.sin(a) * Math.cos(p);
		vector[2] = Math.cos(a)*Math.sin(t);

		return normalizeVector(vector);
	}

	/**
	 * A function which determines the shape of the pulse.
	 *
	 * @param r		radius
	 * @param ph	azimuthal angle
	 * @param th	polar angle
	 * @param t 	time argument: this is used to correctly set E and U which are defined at different times.
	 * @return		(scalar) profile of the pulse
	 */
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

	/**
	 * Auxiliary function determining the angular shape of the pulse.
	 *
	 * @param a		angle (can be polar or azimuthal)
	 * @param a0	angular origin
	 * @param da	angular spread of the pulse
	 * @param p		geometric parameter determining the exact shape
	 * @param q		geometric parameter determining the exact shape
	 * @return		angular profile of the pulse
	 */
	private double angularShapeFunction(double a, double a0, double da, double p, double q) {
		if (a0 - da < a && a < a0 + da) {
			double A = Math.pow(Math.abs(da), p);
			double B = Math.pow(Math.abs(a0 - a), p);

			return Math.pow(Math.sin((A - B) / A), q);
		}
		return 0.0;
	}

	/**
	 * Non-normalized Gaussian function
	 *
	 * @param x		function argument
	 * @param x0	center of the gaussian
	 * @param sx	width of the gaussian (sigma)
	 * @return		gaussian profile
	 */
	private double gaussian(double x, double x0, double sx) {
		return Math.exp(-0.5 * Math.pow((x - x0) / sx, 2));
	}

	/**
	 * Normalizes an n-dimensional double vector.
	 *
	 * @param vector	double vector
	 * @return			normalized vector
	 */
	private double[] normalizeVector(double[] vector) {
		double[] output = new double[vector.length];
		double n = norm(vector);
		for (int i = 0; i < vector.length; i++) {
			output[i] = vector[i] / n;
		}
		return output;
	}

	/**
	 * Rotates a vector v with respect to the rotation axis k and rotation angle a.
	 * This is an implementation of Rodrigues' rotation formula.
	 *
	 * @param v		vector which the rotation is applied to
	 * @param k		rotation axis
	 * @param a		rotation angle
	 * @return		rotated vector
	 */
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

	/**
	 * scalar product of two 3D vectors.
	 * @param v1	vector 1
	 * @param v2	vector 2
	 * @return		v1.v2
	 */
	private double dot(double[] v1, double[] v2) {
		return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
	}

	/**
	 * norm of a vector
	 *
	 * @param v	a vector
	 * @return	|v|
	 */
	private double norm(double[] v) {
		return Math.sqrt(dot(v,v));
	}

	/**
	 * Cross product of two vectors in 3D.
	 * @param v1	vector 1
	 * @param v2	vector 2
	 * @return	v1 x v2
	 */
	private double[] cross(double[] v1, double[] v2) {
		return new double[]{
				v1[1] * v2[2] - v1[2] * v2[1],
				v1[2] * v2[0] - v1[0] * v2[2],
				v1[0] * v2[1] - v1[1] * v2[0]
		};
	}

	/**
	 * Converts a cell position to the real position in the simulation box.
	 *
	 * @param cellPosition	cell position
	 * @return				position in the simulation box
	 */
	private double[] getPosition(int[] cellPosition) {
		double[] position = new double[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			position[i] = cellPosition[i] * grid.getLatticeSpacing();
		}
		return position;
	}
}
