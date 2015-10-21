package org.openpixi.pixi.physics.fields.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;
import java.util.Random;

public class NewLCCurrentProton implements ICurrentGenerator {

	private int direction;
	private int orientation;
	private double location;
	private double[] locationTransverse;
	private double longitudinalWidth;

	private ArrayList<GaussianCharge> charges;
	private int[] transversalNumCells;
	private AlgebraElement[] transversalChargeDensity;
	private int totalTransversalCells;

	private int numberOfColors;
	private int numberOfComponents;
	private double as;
	private double at;
	private double g;
	private Random rand;

	private int[] numCells;

	public NewLCCurrentProton(int direction, int orientation, double location, double longitudinalWidth, double[] transversalLocation){
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.locationTransverse = transversalLocation;
		this.longitudinalWidth = longitudinalWidth;
		rand = new Random();

		this.charges = new ArrayList<GaussianCharge>();
	}

	public void addCharge(double[] location, double width) {
		// This method should be called from the YAML object to add the charges for the current generator.
		this.charges.add(new GaussianCharge(location, width));
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		// 0) Define some variables.
		numberOfColors = s.getNumberOfColors();
		numberOfComponents = s.grid.getElementFactory().numberOfComponents;
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();

		// 1) Initialize transversal charge density grid using the charges array.
		numCells = s.grid.getNumCells();
		transversalNumCells = GridFunctions.reduceGridPos(numCells, direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		double[] transversalWidths = new double[totalTransversalCells];
		transversalChargeDensity = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
		}

		// Iterate over (point) charges, create a Gaussian charge distribution around them and add them to the transversal charge density.
		for (int i = 0; i < charges.size(); i++) {
			GaussianCharge c = charges.get(i);
			/*AlgebraElement chargeAmplitude = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
			for (int j = 0; j < numberOfComponents; j++) {
				chargeAmplitude.set(j, c.colorDirection[j] * c.magnitude / Math.pow(as, s.getNumberOfDimensions() - 1));
			}*/
			for (int k = 0; k < totalTransversalCells; k++) {
				double distanceConstituent = getDistance(c.location, GridFunctions.getCellPos(k, transversalNumCells), as);
				double distanceSpherical = getDistance(this.locationTransverse, GridFunctions.getCellPos(k, transversalNumCells), as);
				transversalWidths[k] += shapeFunction(distanceConstituent, 0.0, 0, c.width)/Math.PI/transversalNumCells.length;
				//initializeGlauber(c, k, chargeAmplitude);
				//initializeCGC(c, k, chargeAmplitude);
			}
		}

		for (int k = 0; k < totalTransversalCells; k++) {
			AlgebraElement chargeAmplitude = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
			for (int j = 0; j < numberOfComponents; j++) {
				chargeAmplitude.set(j, rand.nextGaussian()*transversalWidths[k] / Math.pow(as, s.getNumberOfDimensions() - 1));
			}
			transversalChargeDensity[k].addAssign(chargeAmplitude);
		}

		// 2) Interpolate grid charge and current density.
		applyCurrent(s);

		// 3) Initialize the NewLightConePoissonSolver with the transversal charge density and solve for the fields U and E.
		NewLCPoissonSolver poissonSolver = new NewLCPoissonSolver(direction, orientation, location, longitudinalWidth,
				transversalChargeDensity, transversalNumCells);
		poissonSolver.initialize(s);
		poissonSolver.solve(s);

		// You're done: charge density, current density and the fields are set up correctly.
	}

	public void applyCurrent(Simulation s) {
		int maxDirection = numCells[direction];
		double t = s.totalSimulationTime;

		AlgebraElement[] lastCurrents = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			lastCurrents[i] =s.grid.getElementFactory().algebraZero();
		}
		for (int i = 0; i < maxDirection; i++) {
			double z = i * as - location;


			double s0 = g * as * shapeFunction(z, t - at, orientation, longitudinalWidth);  // shape at t-dt times g*as
			double s1 = g * as * shapeFunction(z, t, orientation, longitudinalWidth);  // shape at t times g*as
			double s2 = g * as * shapeFunction(z, t - at/2, orientation, longitudinalWidth);  // shape at t-dt/2 times g*as
			double ds = (s1 - s0)/at; // time derivative of the shape function

			for (int j = 0; j < totalTransversalCells; j++) {
				int[] transversalGridPos = GridFunctions.getCellPos(j, transversalNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transversalGridPos, direction, i);
				int cellIndex = s.grid.getCellIndex(gridPos);

				// a) Interpolate transversal charge density to grid charge density with a Gauss profile (at t).
				s.grid.addRho(cellIndex, transversalChargeDensity[j].mult(s1));

				// b) Compute gird current density in a charge conserving manner at (t-dt/2).

				// Method 1: Sampling the analytical result on the grid (not charge conserving)
				//s.grid.addJ(cellIndex, direction, transversalChargeDensity[j].mult(s2*orientation));

				// Method 2: Setting the current according to the continuity equation (charge conserving)
				if(Math.abs(ds * as) > 0.00000001) {
					// In the case of CGC initial conditions the continuity equation reduces to
					//     j^a_{x+i, i} = j^a_{x,i} - \dot{\rho^a} as,
					// where i is the direction in which the sheet is moving.
					// For a single thin sheet of charges this is correct since there are no longitudinal gauge fields
					// A_{x,i}. However during the collision of two sheets I don't want to assume that this can not
					// happen. Therefore I generalize the spatial derivative to a covariant spatial derivative by
					// parallel transporting the current for the neighbouring lattice site. The equation then reads
					//     j^a_{x+i} = U_{x, i}^t j^a_x U_{x,i} - \dot{\rho^a} as.
					GroupElement U = s.grid.getLink(cellIndex, direction, 1, 0);
					lastCurrents[j].addAssign(transversalChargeDensity[j].mult(-ds * as).act(U));
					s.grid.addJ(cellIndex, direction,
							lastCurrents[j]
					);
				}
			}
		}
	}

	private double shapeFunction(double z, double t, int o, double width) {
		Gaussian gauss = new Gaussian(0.0, width);
		return gauss.value(z - o * t);
	}

	private double getDistance(double[] center, int[] position, double spacing) {
		double distance = 0.0;
		for (int j = 0; j < position.length; j++) {
			distance += Math.pow(center[j] - spacing*position[j], 2);
		}
		return Math.sqrt(distance);
	}

	private void initializeGlauber(GaussianCharge c, int k, AlgebraElement charge) {
		double distance = getDistance(c.location, GridFunctions.getCellPos(k, transversalNumCells), as);
		transversalChargeDensity[k].addAssign(charge.mult(shapeFunction(distance, 0.0, 0, c.width)/Math.PI/transversalNumCells.length));
	}

	private void initializeCGC(GaussianCharge c, int k, AlgebraElement charge) {
		double distance = getDistance(c.location, GridFunctions.getCellPos(k, transversalNumCells), as);
		double randomWidth = shapeFunction(distance, 0.0, 0, c.width)/Math.PI/transversalNumCells.length;
		transversalChargeDensity[k].addAssign(charge.mult(rand.nextGaussian()*randomWidth));
	}

	class PointCharge {
		public double[] location;
		public double[] colorDirection;
		double magnitude;

		public PointCharge() {}
		public PointCharge(double[] location, double[] colorDirection, double magnitude) {
			this.location = location;
			this.colorDirection = normalize(colorDirection);
			this.magnitude = magnitude;
		}

		private double[] normalize(double[] v) {
			double norm = 0.0;
			for (int i = 0; i < v.length; i++) {
				norm += v[i] * v[i];
			}
			norm = Math.sqrt(norm);
			double[] result = new double[v.length];
			for (int i = 0; i < v.length; i++) {
				result[i] = v[i] / norm;
			}
			return result;
		}
	}

	class GaussianCharge extends PointCharge {
		public double[] location;
		double width;

		public GaussianCharge(double[] location, double width) {
			this.location = location;
			this.width = width;
		}
	}
}
