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
public class ParticleLCCurrentCIC implements ICurrentGenerator {

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
	 * Standard constructor for the ParticleLCCurrentCIC class.
	 *
	 * @param direction Direction of the transversal charge density movement.
	 * @param orientation Orientation fo the transversal charge density movement.
	 * @param location Longitudinal starting location.
	 * @param longitudinalWidth Longitudinal width of the Gaussian shape for the charge density.
	 */
	public ParticleLCCurrentCIC(int direction, int orientation, double location, double longitudinalWidth){
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
		double prefactor = g * as;
		double FIX_ROUND_ERRORS = 10E-12 * as;
		//double FIX_ROUND_ERRORS = 0;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			//AlgebraElement charge = poissonSolver.getGaussConstraint(i);
			for (int j = 0; j < particlesPerLink; j++) {
				double x = (1.0 * j) / (particlesPerLink);
				AlgebraElement charge = interpolateChargeFromGrid(s, i, direction, x).mult(1.0 / particlesPerLink);
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
				/*
				if(charge.square() > 10E-20 * prefactor) {
					Particle p = new Particle();
					p.pos0 = particlePosition0;
					p.pos1 = particlePosition1;
					p.vel = particleVelocity;
					p.Q0 = charge;
					p.Q1 = charge.copy();

					particles.add(p);
				}
				*/
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

	private AlgebraElement interpolateChargeFromGrid(Simulation s, int index, int direction, double x) {
		int shiftedIndex = s.grid.shift(index, direction, 1);
		AlgebraElement charge1 = poissonSolver.getGaussConstraint(index);
		AlgebraElement charge2 = poissonSolver.getGaussConstraint(shiftedIndex);

		charge1 = charge1.mult(1.0 - x);
		charge2 = charge2.mult(x);


		GroupElement U = s.grid.getU(index, direction);
		GroupElement U1 = U.getAlgebraElement().mult(x).getLink().adj();
		GroupElement U2 = U.getAlgebraElement().mult(1.0 - x).getLink();

		charge1.actAssign(U1);
		charge2.actAssign(U2);


		charge1.addAssign(charge2);

		return charge1;
	}

	/**
	 * Evolves the particle positions and charges according to the Wong equations.
	 *
	 * @param s
	 */
	private void evolveCharges(Simulation s) {
		double totalCharge = 0.0;
		for(Particle p : particles) {
			// swap variables for charge and position
			p.swap();
			// move particle position according to velocity
			p.move(at);

			// Evolve particle charges
			// check if one cell or two cell move
			int longitudinalIndexOld = (int) (p.pos0[direction] / as);
			int longitudinalIndexNew = (int) (p.pos1[direction] / as);


			if(longitudinalIndexOld == longitudinalIndexNew) {
				// one cell move
				int cellIndexNew = s.grid.getCellIndex(GridFunctions.flooredGridPoint(p.pos0, as));
				double d = Math.abs(p.vel[direction] * at / as);
				GroupElement U;
				if(p.vel[direction] > 0) {
					U = s.grid.getU(cellIndexNew, direction).getAlgebraElement().mult(d).getLink();
				} else {
					U = s.grid.getU(cellIndexNew, direction).getAlgebraElement().mult(d).getLink().adj();
				}
				p.evolve(U);
			} else {
				// two cell move
				int cellIndexOld = s.grid.getCellIndex(GridFunctions.flooredGridPoint(p.pos0, as));
				int cellIndexNew = s.grid.getCellIndex(GridFunctions.flooredGridPoint(p.pos1, as));

				if(longitudinalIndexOld < longitudinalIndexNew) {
					// right move
					// path is split into two parts
					double d0 = Math.abs(longitudinalIndexNew - p.pos0[direction] / as);
					double d1 = Math.abs(longitudinalIndexNew - p.pos1[direction] / as);

					GroupElement U0 = s.grid.getU(cellIndexOld, direction).getAlgebraElement().mult(d0).getLink();
					GroupElement U1 = s.grid.getU(cellIndexNew, direction).getAlgebraElement().mult(d1).getLink();
					GroupElement U = U0.mult(U1);

					p.evolve(U);
				} else {
					// left move
					// path is split into two parts
					double d0 = Math.abs(longitudinalIndexOld - p.pos0[direction] / as);
					double d1 = Math.abs(longitudinalIndexOld - p.pos1[direction] / as);

					GroupElement U0 = s.grid.getU(cellIndexOld, direction).getAlgebraElement().mult(d0).getLink();
					GroupElement U1 = s.grid.getU(cellIndexNew, direction).getAlgebraElement().mult(d1).getLink();
					GroupElement U = U0.mult(U1);

					p.evolve(U.adj());
				}
			}
			//totalCharge += p.Q1.square();
		}
		//totalCharge /= particles.size();
		//System.out.println(totalCharge + ", ");
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
			// 1) Charge interpolation

			// "Floored" grid points of the particle
			int[] gridPosOld = GridFunctions.flooredGridPoint(p.pos0, as);
			int[] gridPosNew = GridFunctions.flooredGridPoint(p.pos1, as);

			// Cell indices
			int cellIndex0Old = s.grid.getCellIndex(gridPosOld);
			int cellIndex1Old = s.grid.shift(cellIndex0Old, direction, 1);

			int cellIndex0New = s.grid.getCellIndex(gridPosNew);
			int cellIndex1New = s.grid.shift(cellIndex0New, direction, 1);

			// Relative distances to the lattice sites
			double d0New = p.pos1[direction] / as - gridPosNew[direction];
			double d1New = 1 - d0New;
			double d0Old = p.pos0[direction] / as - gridPosOld[direction];
			double d1Old = 1 - d0Old;

			// Links at old and new position
			GroupElement UOld = s.grid.getUnext(cellIndex0Old, direction);
			GroupElement UNew = s.grid.getU(cellIndex0New, direction);

			// Interpolated gauge links
			GroupElement U0New = UNew.getAlgebraElement().mult(d0New).getLink();
			GroupElement U1New = UNew.getAlgebraElement().mult(d1New).getLink().adj();

			// Charge interpolation to neighbouring lattice sites
			AlgebraElement Q0New = p.Q1.act(U0New).mult(d1New);
			AlgebraElement Q1New = p.Q1.act(U1New).mult(d0New);

			s.grid.addRho(cellIndex0New, Q0New);
			s.grid.addRho(cellIndex1New, Q1New);

			// 2) Charge conserving current calculation

			int longitudinalIndexOld = (int) (p.pos0[direction] / as);
			int longitudinalIndexNew = (int) (p.pos1[direction] / as);

			if(longitudinalIndexNew == longitudinalIndexOld) {
				// One-cell move
				GroupElement U0Old = UOld.getAlgebraElement().mult(d0Old).getLink();
				AlgebraElement Q0Old = p.Q0.act(U0Old).mult(d1Old);

				AlgebraElement J = Q0New.sub(Q0Old).mult(-c);
				s.grid.addJ(cellIndex0New, direction, J);

			} else {
				GroupElement U0Old = UOld.getAlgebraElement().mult(d0Old).getLink();
				GroupElement U1Old = UOld.getAlgebraElement().mult(d1Old).getLink().adj();
				AlgebraElement Q0Old = p.Q0.act(U0Old).mult(d1Old);
				AlgebraElement Q1Old = p.Q0.act(U1Old).mult(d0Old);
				if(longitudinalIndexNew > longitudinalIndexOld) {
					// Two-cell move right
					AlgebraElement JOld = Q0Old.mult(c);
					AlgebraElement JNew = JOld.act(s.grid.getU(cellIndex0Old,direction).adj());
					JNew.addAssign(Q0New.sub(Q1Old).mult(-c));

					s.grid.addJ(cellIndex0Old, direction, JOld);
					s.grid.addJ(cellIndex0New, direction, JNew);
				} else {
					// Two-cell move left
					AlgebraElement JNew = Q0New.mult(-c);
					AlgebraElement JOld = JNew.act(UNew.adj());
					JOld.addAssign(Q1New.sub(Q0Old).mult(-c));

					s.grid.addJ(cellIndex0Old, direction, JOld);
					s.grid.addJ(cellIndex0New, direction, JNew);
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
