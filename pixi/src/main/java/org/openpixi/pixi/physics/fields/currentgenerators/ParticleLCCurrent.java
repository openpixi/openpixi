package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.particles.CGCParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * This current generator uses particles on fixed trajectories to correctly interpolate the charge and current density
 * on the grid according to CGC initial conditions.
 */
public class ParticleLCCurrent implements ICurrentGenerator {

	/**
	 * Direction of movement of the charge density. Values range from 0 to numberOfDimensions-1.
	 */
	protected int direction;

	/**
	 * Orientation of movement. Values are -1 or 1.
	 */
	protected int orientation;

	/**
	 * Longitudinal location of the initial charge density in the simulation box.
	 */
	protected double location;

	/**
	 * Longitudinal width of the charge density.
	 */
	protected double longitudinalWidth;

	/**
	 * Array containing the size of the transversal grid.
	 */
	protected int[] transversalNumCells;

	/**
	 * Transversal charge density.
	 */
	protected AlgebraElement[] transversalChargeDensity;

	/**
	 * Total number of cells in the transversal grid.
	 */
	protected int totalTransversalCells;

	/**
	 * Lattice spacing of the grid.
	 */
	protected double as;

	/**
	 * Time step used in the simulation.
	 */
	protected double at;

	/**
	 * Coupling constant used in the simulation.
	 */
	protected double g;

	/**
	 * Poisson solver for solving the CGC intitial conditions.
	 */
	protected NewLCPoissonSolver poissonSolver;

	/**
	 * Standard constructor for the ParticleLCCurrent class.
	 *
	 * @param direction Direction of the transversal charge density movement.
	 * @param orientation Orientation fo the transversal charge density movement.
	 * @param location Longitudinal starting location.
	 * @param longitudinalWidth Longitudinal width of the Gaussian shape for the charge density.
	 */
	public ParticleLCCurrent(int direction, int orientation, double location, double longitudinalWidth){
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
	}

	/**
	 * Sets the intitial transversal charge density.
	 *
	 * @param transversalChargeDensity
	 */
	public void setTransversalChargeDensity(AlgebraElement[] transversalChargeDensity) {
		this.transversalChargeDensity = transversalChargeDensity;
	}

	/**
	 * Initializes the fields, charges and currents on the grid.
	 *
	 * @param s
	 * @param totalInstances
	 */
	public void initializeCurrent(Simulation s, int totalInstances) {
		// 0) Define some variables.
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();

		// 1) Initialize transversal charge density grid using the charges array.
		transversalNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);

		// If transversal charge density is not defined at this point, initialize empty charge density.
		if(transversalChargeDensity == null) {
			transversalChargeDensity = new AlgebraElement[totalTransversalCells];
			for (int i = 0; i < totalTransversalCells; i++) {
				transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
			}
		}

		// 2) Initialize the NewLCPoissonSolver with the transversal charge density and solve for the fields U and E.
		poissonSolver = new NewLCPoissonSolver(direction, orientation, location, longitudinalWidth,
				transversalChargeDensity, transversalNumCells);
		poissonSolver.initialize(s);
		poissonSolver.solve(s);


		// 3) Interpolate grid charge and current density.
		initializeParticles(s, 1);

		// You're done: charge density, current density and the fields are set up correctly.
	}


	/**
	 * Interpolates and evolves the charges and currents to the grid.
	 * @param s
	 */
	public void applyCurrent(Simulation s) {
	}

	/**
	 * Initializes the particles according to the field initial conditions. The charge density is computed from the Gauss law
	 * violations of the initial fields. The particles are then sampled from this charge density.
	 *
	 * @param s
	 * @param particlesPerLink
	 */
	protected void initializeParticles(Simulation s, int particlesPerLink) {

		// Traverse through charge density and add particles by sampling the charge distribution
		double t0 = 0.0;	// Particles should be initialized at t = 0 and t = dt.
		double FIX_ROUND_ERRORS = 10E-12 * as;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int j = 0; j < particlesPerLink; j++) {
				double x = (1.0 * j) / (particlesPerLink);
				int[] gridPos = s.grid.getCellPos(i);
				double dz = x * as;

				// Particle position
				double[] particlePosition0 = new double[gridPos.length];
				double[] particlePosition1 = new double[gridPos.length];
				for (int k = 0; k < gridPos.length; k++) {
					particlePosition0[k] = gridPos[k] * as + FIX_ROUND_ERRORS;
					particlePosition1[k] = gridPos[k] * as + FIX_ROUND_ERRORS;
					if(k == direction) {
						particlePosition0[k] += t0 * orientation + dz;
						particlePosition1[k] += (t0 + at) * orientation + dz;
					}
				}


				AlgebraElement charge = interpolateChargeFromGrid(s, particlePosition0).mult(1.0 / particlesPerLink);

				// Particle velocity
				double[] particleVelocity = new double[gridPos.length];
				for (int k = 0; k < gridPos.length; k++) {
					if(k == direction) {
						particleVelocity[k] = 1.0 * orientation;
					} else {
						particleVelocity[k] = 0.0;
					}
				}

				CGCParticle p = new CGCParticle(s.getNumberOfDimensions(), s.getNumberOfColors(), direction);
				p.pos0 = particlePosition0; // position at t = 0
				p.pos1 = particlePosition1; // position at t = dt (optional)
				p.vel = particleVelocity;   // particle velocity at t = -dt/2.
				p.Q0 = charge;              // charge at t = 0
				p.Q1 = charge.copy();       // charge at t = dt, assume that there is no parallel transport initially (also optional).

				s.particles.add(p);
			}
		}
	}

	protected AlgebraElement interpolateChargeFromGrid(Simulation s, double[] particlePosition) {
		int[] flooredGridPos = GridFunctions.flooredGridPoint(particlePosition, as);
		int index = s.grid.getCellIndex(flooredGridPos);
		int shiftedIndex = s.grid.shift(index, direction, 1);
		AlgebraElement charge1 = poissonSolver.getGaussConstraint(index);
		AlgebraElement charge2 = poissonSolver.getGaussConstraint(shiftedIndex);

		double x = particlePosition[direction] / as - flooredGridPos[direction];

		charge1 = charge1.mult(1.0 - x);
		charge2 = charge2.mult(x);

		// Parallel transport is not needed because we assume that initial conditions are purely transversal fields.

		charge1.addAssign(charge2);

		return charge1;
	}
}
