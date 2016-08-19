package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.CGCSuperParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * This particle generator creates particles to correctly interpolate the charge density
 * on the grid according to CGC initial conditions.
 */
public class LightConeNGPSuperParticleCreator implements IParticleCreator {

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
	 * Initializes the particles according to the field initial conditions. The charge density is computed from the
	 * Gauss law violations of the initial fields. The particles are then sampled from this charge density.
	 * Particles in transverse planes which cross NGP boundaries at the same time during the simulation are consolidated
	 * in super particles.
	 *
	 * @param s
	 * @param particlesPerLink
	 */
	public void initializeParticles(Simulation s, int particlesPerLink) {
		/*
		Detect particle 'block': Find region where Gauss constraint is strongly violated (i.e. where charges are to be
		placed) and ignore the rest. This reduces the total number of particle charges we need to describe the initial
		condition, because most of space will be empty anyways. Note: this introduces very small violations of the Gauss
		law at the boundaries of the regions, but these errors are (supposed to be) negligible.
		 */

		// Iterate through grid and find maximum charge in the grid.
		double maxCharge = 0.0;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			double charge = Math.sqrt(gaussConstraint[i].square());
			if(maxCharge < charge) {
				maxCharge = charge;
			}
		}

		// Set cutoff charge to small fraction of maximum charge.
		//double cutoffCharge = 10E-12 * Math.pow( g * as, 1) / (Math.pow(as, 3) * particlesPerLink);
		double cutoffCharge = maxCharge * 10E-12;

		// Iterate through longitudinal sheets and find dimensions of the particle block.
		int zStart = 0;
		int zEnd =  s.grid.getNumCells(direction);
		boolean foundStartOfBlock = false;
		int longitudinalCells = s.grid.getNumCells(direction);
		for (int z = 0; z < longitudinalCells; z++) {
			// Find max charge in transverse plane
			maxCharge = 0;
			for (int k = 0; k < totalTransversalCells; k++) {
				int[] transPos = GridFunctions.getCellPos(k, transversalNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transPos, direction, z);
				int i = s.grid.getCellIndex(gridPos);

				double charge = Math.sqrt(gaussConstraint[i].square());
				if(charge > maxCharge) {
					maxCharge = charge;
				}
			}
			if(!foundStartOfBlock) {
				if(maxCharge > cutoffCharge) {
					zStart = z;
					foundStartOfBlock = true;
				}
			} else {
				if(maxCharge < cutoffCharge) {
					zEnd = z;
					break;
				}
			}
		}

		// Spawn super particles.
		int numberOfParticlesPerSuperParticle = totalTransversalCells * (zEnd - zStart);
		int indexOffset = zStart * totalTransversalCells;
		CGCSuperParticle[] superParticles = new CGCSuperParticle[particlesPerLink];
		for (int j = 0; j < particlesPerCell; j++) {
			superParticles[j] = new CGCSuperParticle(orientation,
					numberOfParticlesPerSuperParticle,
					indexOffset,
					totalTransversalCells,
					j,
					particlesPerCell);
			s.particles.add(superParticles[j]);
		}
		for (int i = 0; i < numberOfParticlesPerSuperParticle; i++) {
			int index = indexOffset + i;
			for (int j = 0; j < particlesPerCell; j++) {
				int ngp = (j < particlesPerCell/2) ? index : s.grid.shift(index, direction, 1);
				AlgebraElement charge = gaussConstraint[ngp].copy();
				charge.multAssign(1.0 / particlesPerCell);
				superParticles[j].Q[i] = charge;
			}
		}


		ArrayList<ArrayList<AlgebraElement>> longitudinalParticleList = new ArrayList<>(totalTransversalCells);
		for (int i = 0; i < totalTransversalCells; i++) {
			longitudinalParticleList.add(new ArrayList<AlgebraElement>());
		}

		// Charge refinement
		int numberOfIterations = 100;
		for (int i = 0; i < totalTransversalCells; i++) {
			ArrayList<AlgebraElement> particleList = longitudinalParticleList.get(i);
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
	}

	private void refine2(int i, ArrayList<AlgebraElement> list, int particlesPerLink) {
		int jmod = i % particlesPerLink;
		int n = list.size();
		// Refinement can not be applied to the last charge in an NGP cell.
		if(jmod >= 0 && jmod < particlesPerLink-1)
		{
			int i0 = p(i-1, n);
			int i1 = p(i+0, n);
			int i2 = p(i+1, n);
			int i3 = p(i+2, n);

			AlgebraElement Q0 = list.get(i0);
			AlgebraElement Q1 = list.get(i1);
			AlgebraElement Q2 = list.get(i2);
			AlgebraElement Q3 = list.get(i3);

			AlgebraElement DQ = Q0.mult(-1);
			DQ.addAssign(Q1.mult(3));
			DQ.addAssign(Q2.mult(-3));
			DQ.addAssign(Q3.mult(1));
			DQ.multAssign(1.0 / 4.0);

			Q1.addAssign(DQ.mult(-1.0));
			Q2.addAssign(DQ.mult(1.0));
		}
	}


	private void refine4(int i, ArrayList<AlgebraElement> list, int particlesPerLink) {
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

			AlgebraElement Q0 = list.get(i0);
			AlgebraElement Q1 = list.get(i1);
			AlgebraElement Q2 = list.get(i2);
			AlgebraElement Q3 = list.get(i3);
			AlgebraElement Q4 = list.get(i4);
			AlgebraElement Q5 = list.get(i5);

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
