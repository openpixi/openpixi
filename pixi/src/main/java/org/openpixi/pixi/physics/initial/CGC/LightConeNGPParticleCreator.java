package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
import org.openpixi.pixi.physics.initial.CGC.IParticleCreator;
import org.openpixi.pixi.physics.particles.CGCParticle;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * This particle generator creates particles to correctly interpolate the charge density
 * on the grid according to CGC initial conditions.
 */
public class LightConeNGPParticleCreator implements IParticleCreator {

	/**
	 * Direction of movement of the charge density. Values range from 0 to numberOfDimensions-1.
	 */
	protected int direction;

	/**
	 * Orientation of movement. Values are -1 or 1.
	 */
	protected int orientation;

	/**
	 * Longitudinal width of the charge density.
	 */
	protected double longitudinalWidth;

	/**
	 * Array containing the size of the transversal grid.
	 */
	protected int[] transversalNumCells;

	/**
	 * Gauss constraint..
	 */
	protected AlgebraElement[] gaussConstraint;

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
	 * Number of particles per cell.
	 */
	protected int particlesPerCell = 1;

	/**
	 * Sets the initial Gauss constraint.
	 *
	 * @param gaussConstraint
	 */
	public void setGaussConstraint(AlgebraElement[] gaussConstraint) {
		this.gaussConstraint = gaussConstraint;
	}

	/**
	 * Initializes the particles.
	 *
	 * @param s
	 */
	public void initialize(Simulation s, int direction, int orientation) {
		this.direction = direction;
		this.orientation = orientation;

		// Define some variables.
		particlesPerCell = (int) (s.grid.getLatticeSpacing() / s.getTimeStep());
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();
		transversalNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);

		// Interpolate grid charge and current density.
		initializeParticles(s, particlesPerCell);
	}

	/**
	 * Initializes the particles according to the field initial conditions. The charge density is computed from the Gauss law
	 * violations of the initial fields. The particles are then sampled from this charge density.
	 *
	 * @param s
	 * @param particlesPerLink
	 */
	public void initializeParticles(Simulation s, int particlesPerLink) {
		double cutoffCharge = 10E-22 * Math.pow( g * as, 2) / (Math.pow(as, 3) * particlesPerLink);

		ArrayList<ArrayList<CGCParticle>> longitudinalParticleList = new ArrayList<ArrayList<CGCParticle>>(totalTransversalCells);
		for (int i = 0; i < totalTransversalCells; i++) {
			longitudinalParticleList.add(new ArrayList<CGCParticle>());
		}
		// Traverse through charge density and add particles by sampling the charge distribution
		double t0 = 0.0;	// Particles should be initialized at t = 0 and t = dt.
		double FIX_ROUND_ERRORS = 10E-12 * as;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int j = 0; j < particlesPerCell; j++) {
				double x = (1.0 * j - particlesPerLink/2) / (particlesPerLink);
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


				AlgebraElement charge = this.interpolateChargeFromGrid(s, particlePosition0).mult(1.0 / particlesPerLink);


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

				if(charge.square() > cutoffCharge) {
					s.particles.add(p);

					// Add to extra particle array for charge refinement.
					int transversalIndex = GridFunctions.getCellIndex(GridFunctions.reduceGridPos(gridPos, direction), transversalNumCells);
					longitudinalParticleList.get(transversalIndex).add(p);
				}
			}
		}

		// Charge refinement
		int numberOfIterations = 100;
		for (int i = 0; i < totalTransversalCells; i++) {
			ArrayList<CGCParticle> particleList = longitudinalParticleList.get(i);
			// 2nd order refinement
			for (int iteration = 0; iteration < numberOfIterations; iteration++) {
				for (int j = 0; j < particleList.size(); j++) {
					refine2(j, particleList, particlesPerLink);
				}
			}

			// 4th order refinement
			for (int iteration = 0; iteration < numberOfIterations; iteration++) {
				for (int j = 0; j < particleList.size(); j++) {
					refine4(j, particleList, particlesPerLink);
				}
			}
		}

		// Make sure particle charges Q0 and Q1 are the same.
		for(IParticle p : s.particles) {
			CGCParticle P = (CGCParticle) p;
			P.Q1 = P.Q0.copy();
		}

	}

	private void refine2(int i, ArrayList<CGCParticle> list, int particlesPerLink) {
		int jmod = i % particlesPerLink;
		int n = list.size();
		// Refinement can not be applied to the last charge in an NGP cell.
		if(jmod >= 0 && jmod < particlesPerLink-1)
		{
			int i0 = p(i-1, n);
			int i1 = p(i+0, n);
			int i2 = p(i+1, n);
			int i3 = p(i+2, n);

			AlgebraElement Q0 = list.get(i0).Q0;
			AlgebraElement Q1 = list.get(i1).Q0;
			AlgebraElement Q2 = list.get(i2).Q0;
			AlgebraElement Q3 = list.get(i3).Q0;

			AlgebraElement DQ = Q0.mult(-1);
			DQ.addAssign(Q1.mult(3));
			DQ.addAssign(Q2.mult(-3));
			DQ.addAssign(Q3.mult(1));
			DQ.multAssign(1.0 / 4.0);

			Q1.addAssign(DQ.mult(-1.0));
			Q2.addAssign(DQ.mult(1.0));
		}
	}


	private void refine4(int i, ArrayList<CGCParticle> list, int particlesPerLink) {
		int jmod = i % particlesPerLink;
		int n = list.size();
		// Refinement can not be applied to the last charge in an NGP cell.
		if(jmod >= 0 && jmod < particlesPerLink-1)
		{
			int i0 = p(i-2, n);
			int i1 = p(i-1, n);
			int i2 = p(i+0, n);
			int i3 = p(i+1, n);
			int i4 = p(i+2, n);
			int i5 = p(i+3, n);

			AlgebraElement Q0 = list.get(i0).Q0;
			AlgebraElement Q1 = list.get(i1).Q0;
			AlgebraElement Q2 = list.get(i2).Q0;
			AlgebraElement Q3 = list.get(i3).Q0;
			AlgebraElement Q4 = list.get(i4).Q0;
			AlgebraElement Q5 = list.get(i5).Q0;

			AlgebraElement DQ = Q0.mult(+1);
			DQ.addAssign(Q1.mult(-5));
			DQ.addAssign(Q2.mult(+10));
			DQ.addAssign(Q3.mult(-10));
			DQ.addAssign(Q4.mult(+5));
			DQ.addAssign(Q5.mult(-1));
			DQ.multAssign(1.0 / 12.0);

			Q2.addAssign(DQ.mult(-1.0));
			Q3.addAssign(DQ.mult(1.0));
		}
	}

	private int p(int i, int n) {
		return (i % n + n) % n;
	}

	protected AlgebraElement interpolateChargeFromGrid(Simulation s, double[] particlePosition) {
		int[] ngp = GridFunctions.nearestGridPoint(particlePosition, as);
		return gaussConstraint[s.grid.getCellIndex(ngp)].copy();
	}
}
