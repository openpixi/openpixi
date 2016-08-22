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

	    // Find max charges in transverse planes for each longitudinal coordinate and global charge maximum.
	    int lnum = s.grid.getNumCells(0);
	    double[] maxCharges = new double[lnum];
	    double globalMax = 0.0;
	    for (int z = 0; z < lnum; z++) {
		    double max = 0.0;
		    for (int j = 0; j < totalTransversalCells; j++) {
			    int index = z * totalTransversalCells + j;
			    double charge = Math.sqrt(gaussConstraint[index].square());
			    if(max < charge) {
				    max = charge;
			    }
		    }
		    maxCharges[z] = max;
		    if(globalMax < max) {
			    globalMax = max;
		    }
	    }
	    double cutoffCharge = globalMax * 10E-10;

	    // Find start of block starting from the left boundary.
	    int zStart = 0;
	    for (int z = 0; z < lnum; z++) {
		    if(maxCharges[z] > cutoffCharge) {
			    zStart = z;
			    break;
		    }
	    }

	    // Find end of block starting from the right boundary.
	    int zEnd = lnum - 1;
	    for (int z = zEnd-1; z >= 0; z--) {
		    if(maxCharges[z] > cutoffCharge) {
			    zEnd = z;
			    break;
		    }
	    }

	    // Set width of particle block.
	    int blockWidth = zEnd - zStart;

        // Spawn super particles.
        int numberOfSubdivisions = Math.min(s.numberOfThreads, blockWidth);   // still need to make this dependent on number of threads.
        int numberOfSuperParticles = numberOfSubdivisions * particlesPerCell;
        int totalNumberOfParticles = totalTransversalCells * blockWidth * particlesPerCell;
        int indexOffset = zStart * totalTransversalCells;
        int widthPerSubdivision = (int) Math.ceil(blockWidth / (1.0 * numberOfSubdivisions));
        int longitudinalParticlesPerSubdivision = numberOfSubdivisions * widthPerSubdivision * particlesPerCell;
        int totalNumberOfCells = s.grid.getTotalNumberOfCells();

        int widthForLastSubdivision, particlesInLastSubdivision;
        if (numberOfSubdivisions > 1) {
            widthForLastSubdivision = blockWidth % widthPerSubdivision;
            particlesInLastSubdivision = widthForLastSubdivision * totalTransversalCells;
        } else {
            widthForLastSubdivision = blockWidth;
            particlesInLastSubdivision = blockWidth * totalTransversalCells;
        }

        // Create lists for particle refinement.
        AlgebraElement[][] longitudinalParticleArray = new AlgebraElement[totalTransversalCells][longitudinalParticlesPerSubdivision];
        for (int i = 0; i < totalTransversalCells; i++) {
            for (int j = 0; j < longitudinalParticlesPerSubdivision; j++) {
                longitudinalParticleArray[i][j] = s.grid.getElementFactory().algebraZero();
            }
        }

        // Spawn super particles.
        CGCSuperParticle[] superParticles = new CGCSuperParticle[numberOfSuperParticles];
        int numberOfParticlesPerSuperParticle = widthPerSubdivision * totalTransversalCells;
        for (int j = 0; j < numberOfSubdivisions; j++) {
            // Initialize super particles for subdivision of the particle block.
            for (int k = 0; k < particlesPerCell; k++) {
                if (j < numberOfSubdivisions - 1) {
                    superParticles[particlesPerCell * j + k] = new CGCSuperParticle(orientation,
                            numberOfParticlesPerSuperParticle,
                            indexOffset,
                            totalTransversalCells,
                            k,
                            particlesPerCell);
                } else {
                    superParticles[particlesPerCell * j + k] = new CGCSuperParticle(orientation,
                            particlesInLastSubdivision,
                            indexOffset,
                            totalTransversalCells,
                            k,
                            particlesPerCell);
                }
                s.particles.add(superParticles[particlesPerCell * j + k]);
            }

            // Set super particle charges for subdivision.
            int maxParticleNum = (j < numberOfSubdivisions - 1) ? numberOfParticlesPerSuperParticle : particlesInLastSubdivision;
            for (int i = 0; i < maxParticleNum; i++) {
                int index = indexOffset + i;
                for (int k = 0; k < particlesPerCell; k++) {
                    int ngp = (k < particlesPerCell / 2) ? index : s.grid.shift(index, 0, 1);
                    AlgebraElement charge = gaussConstraint[ngp].copy();
                    charge.multAssign(1.0 / particlesPerCell);
                    superParticles[j * particlesPerCell + k].Q[i] = charge;

                    int transverseIndex = index % totalTransversalCells;
                    int shiftedIndex = index - zStart * totalTransversalCells;
                    int longitudinalIndex = (int) Math.floor(shiftedIndex / totalTransversalCells) * particlesPerCell + k;
                    longitudinalParticleArray[transverseIndex][longitudinalIndex] = superParticles[j * particlesPerCell + k].Q[i];
                }
            }

            indexOffset += numberOfParticlesPerSuperParticle;
        }

        // Charge refinement
        int numberOfIterations = 100;

        for (int i = 0; i < totalTransversalCells; i++) {
            AlgebraElement[] particleList = longitudinalParticleArray[i];
            // 2nd order refinement
            int jmin = 0;
            int jmax = particleList.length;
            for (int iteration = 0; iteration < numberOfIterations; iteration++) {
                for (int j = jmin; j < jmax; j++) {
                    refine2(j, particleList, particlesPerCell);
                }
            }

            // 4th order refinement
            for (int iteration = 0; iteration < numberOfIterations; iteration++) {
                for (int j = jmin; j < jmax; j++) {
                    refine4(j, particleList, particlesPerCell);
                }
            }
        }
    }

    private void refine2(int i, AlgebraElement[] list, int particlesPerLink) {
        int jmod = (i + particlesPerCell / 2) % particlesPerLink;
        int n = list.length;
        // Refinement can not be applied to the last charge in an NGP cell.
        if (jmod >= 0 && jmod < particlesPerLink - 1) {
            int i0 = p(i - 1, n);
            int i1 = p(i + 0, n);
            int i2 = p(i + 1, n);
            int i3 = p(i + 2, n);

            AlgebraElement Q0 = list[i0];
            AlgebraElement Q1 = list[i1];
            AlgebraElement Q2 = list[i2];
            AlgebraElement Q3 = list[i3];

            AlgebraElement DQ = Q0.mult(-1);
            DQ.addAssign(Q1.mult(3));
            DQ.addAssign(Q2.mult(-3));
            DQ.addAssign(Q3.mult(1));
            DQ.multAssign(1.0 / 4.0);

            Q1.addAssign(DQ.mult(-1.0));
            Q2.addAssign(DQ.mult(1.0));
        }
    }


    private void refine4(int i, AlgebraElement[] list, int particlesPerLink) {
        int jmod = (i + particlesPerCell / 2) % particlesPerLink;
        int n = list.length;
        // Refinement can not be applied to the last charge in an NGP cell.
        if (jmod >= 0 && jmod < particlesPerLink - 1) {
            int i0 = p(i - 2, n);
            int i1 = p(i - 1, n);
            int i2 = p(i + 0, n);
            int i3 = p(i + 1, n);
            int i4 = p(i + 2, n);
            int i5 = p(i + 3, n);

            AlgebraElement Q0 = list[i0];
            AlgebraElement Q1 = list[i1];
            AlgebraElement Q2 = list[i2];
            AlgebraElement Q3 = list[i3];
            AlgebraElement Q4 = list[i4];
            AlgebraElement Q5 = list[i5];

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
