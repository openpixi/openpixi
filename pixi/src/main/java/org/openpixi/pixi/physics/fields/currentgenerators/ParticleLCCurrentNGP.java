package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * This current generator uses particles on fixed trajectories to correctly interpolate the charge and current density
 * on the grid according to CGC initial conditions.
 */
public class ParticleLCCurrentNGP implements ICurrentGenerator {

	/**
	 * Direction of movement of the charge density. Values range from 0 to numberOfDimensions-1.
	 */
	private int direction;

	/**
	 * Orientation of movement. Values are -1 or 1.
	 */
	private int orientation;

	/**
	 * Longitudinal location of the initial charge density in the simulation box.
	 */
	private double location;

	/**
	 * Longitudinal width of the charge density.
	 */
	private double longitudinalWidth;

	/**
	 * Array containing the size of the transversal grid.
	 */
	private int[] transversalNumCells;

	/**
	 * Transversal charge density.
	 */
	private AlgebraElement[] transversalChargeDensity;

	/**
	 * Total number of cells in the transversal grid.
	 */
	private int totalTransversalCells;

	/**
	 * Lattice spacing of the grid.
	 */
	private double as;

	/**
	 * Time step used in the simulation.
	 */
	private double at;

	/**
	 * Coupling constant used in the simulation.
	 */
	private double g;

	/**
	 * List of particles which sample the charge distributions. The charges of these particles are evolved and interpolated consistently according to the field.
	 */
	private ArrayList<Particle> particles;

	/**
	 * Poisson solver for solving the CGC intitial conditions.
	 */
	NewLCPoissonSolver poissonSolver;

	/**
	 * Standard constructor for the ParticleLCCurrent class.
	 *
	 * @param direction Direction of the transversal charge density movement.
	 * @param orientation Orientation fo the transversal charge density movement.
	 * @param location Longitudinal starting location.
	 * @param longitudinalWidth Longitudinal width of the Gaussian shape for the charge density.
	 */
	public ParticleLCCurrentNGP(int direction, int orientation, double location, double longitudinalWidth){
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
		applyCurrent(s);

		// You're done: charge density, current density and the fields are set up correctly.
	}


	/**
	 * Interpolates and evolves the charges and currents to the grid.
	 * @param s
	 */
	public void applyCurrent(Simulation s) {
		evolveCharges(s);
		removeParticles(s);
		interpolateChargesAndCurrents(s);
	}

	/**
	 * Initializes the particles according to the field initial conditions. The charge density is computed from the Gauss law
	 * violations of the initial fields. The particles are then sampled from this charge density.
	 *
	 * @param s
	 * @param particlesPerLink
	 */
	private void initializeParticles(Simulation s, int particlesPerLink) {
		particles = new ArrayList<Particle>();

		// Traverse through charge density and add particles by sampling the charge distribution
		double t0 = - 2*at;
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

				// Create particle instance and add to particle array.
				Particle p = new Particle();
				p.pos0 = particlePosition0;
				p.pos1 = particlePosition1;
				p.vel = particleVelocity;
				p.Q0 = charge;
				p.Q1 = charge.copy();

				particles.add(p);
			}
		}
	}

	private AlgebraElement interpolateChargeFromGrid(Simulation s, double[] pos) {
		int[] ngp = GridFunctions.nearestGridPoint(pos, s.grid.getLatticeSpacing());
		return poissonSolver.getGaussConstraint(s.grid.getCellIndex(ngp));
	}

	/**
	 * Evolves the particle positions and charges according to the Wong equations.
	 *
	 * @param s
	 */
	private void evolveCharges(Simulation s) {
		for(Particle p : particles) {
			// swap variables for charge and position
			p.swap();
			// move particle position according to velocity
			p.move(at);

			// Evolve particle charges
			int cellIndexOld = s.grid.getCellIndex(GridFunctions.nearestGridPoint(p.pos0, as));
			int cellIndexNew = s.grid.getCellIndex(GridFunctions.nearestGridPoint(p.pos1, as));

			if(cellIndexOld != cellIndexNew) {
				// two cell move
				GroupElement U = s.grid.getLink(cellIndexOld, direction, orientation, 1);
				p.evolve(U);
			}
		}
	}

	/**
	 * Checks if particles have left the simulation box and removes them if necessary.
	 *
	 * @param s
	 */
	private void removeParticles(Simulation s) {
		// Remove particles which have left the simulation box.
		ArrayList<Particle> removeList = new ArrayList<Particle>();
		for(Particle p : particles) {
			for (int i = 0; i < s.getNumberOfDimensions(); i++) {
				if(p.pos1[i] > s.getSimulationBoxSize(i) || p.pos1[i] < 0) {
					removeList.add(p);
				}
			}
		}
		particles.removeAll(removeList);
	}

	/**
	 * Interpolates the particle charges to the grid and computes charge conserving currents.
	 *
	 * @param s
	 */
	private void interpolateChargesAndCurrents(Simulation s) {
		double c = as / at;
		// Interpolate particle charges to charge density on the grid
		for(Particle p : particles) {
			int cellIndexOld = s.grid.getCellIndex(GridFunctions.nearestGridPoint(p.pos0, as));
			int cellIndexNew = s.grid.getCellIndex(GridFunctions.nearestGridPoint(p.pos1, as));

			// 1) Charge interpolation
			s.grid.addRho(cellIndexNew, p.Q1);

			// 2) Charge conserving current calculation
			if(cellIndexOld != cellIndexNew) {
				// two cell move
				if(cellIndexOld < cellIndexNew) {
					AlgebraElement J = p.Q0.mult(c * orientation);
					s.grid.addJ(cellIndexOld, direction, J);
				} else {
					AlgebraElement J = p.Q1.mult(c * orientation);
					s.grid.addJ(cellIndexNew, direction, J);
				}
			}
		}
	}

	/**
	 * Particle class used by the current generator to evolve and interpolate charges on the grid.
	 */
	class Particle {
		public double[] pos0;
		public double[] pos1;
		public double[] vel;

		public AlgebraElement Q0;
		public AlgebraElement Q1;

		public void swap() {
			AlgebraElement tQ = Q0;
			Q0 = Q1;
			Q1 = tQ;

			double[] tPos = pos0;
			pos0 = pos1;
			pos1 = tPos;
		}

		public void move(double dt) {
			for (int i = 0; i < pos0.length; i++) {
				pos1[i] = pos0[i] + vel[i] * dt;
			}
		}

		public void evolve(GroupElement U) {
			Q1 = Q0.act(U.adj());
		}
	}
}
