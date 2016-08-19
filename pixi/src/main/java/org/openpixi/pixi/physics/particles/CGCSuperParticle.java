package org.openpixi.pixi.physics.particles;

import org.openpixi.pixi.math.AlgebraElement;

import java.awt.*;

/**
 * This particle class describes not single particles, but larger collections of particles as 'super particles'.
 * It enables us to make use of optimizations specific to CGC simulations with fixed particle trajectories.
 */
public class CGCSuperParticle implements IParticle{

	/**
	 * Array of charges containing all the particle charges associated with this super particle.
	 */
	public AlgebraElement[] Q;

	/**
	 * Orientation of the super particle (-1 or +1).
	 */
	public int orientation;

	/**
	 * Total number of particles described by super particle.
	 */
	public int numberOfParticles;

	/**
	 * Longitudinal offset from z = 0 in terms of lattice indices.
	 */
	public int indexOffset;

	/**
	 * Number of particles within a transverse plane.
	 */
	public int particlesPerPlane;

	/**
	 * Offset/shift of the particles within a cell.
	 */
	public int subLatticeShift;

	/**
	 * Number of particles per cell. Needed for relative position of super particles.
	 */
	public int particlePerCell;

	public CGCSuperParticle(int orientation,
							int numberOfParticles,
							int indexOffset,
	                        int particlesPerPlane,
	                        int subLatticeShift,
	                        int particlePerCell) {
		this.orientation = orientation;
		this.numberOfParticles = numberOfParticles;
		this.indexOffset = indexOffset;
		this.particlesPerPlane = particlesPerPlane;
		this.subLatticeShift = subLatticeShift;
		this.particlePerCell = particlePerCell;

		this.Q = new AlgebraElement[numberOfParticles];

	}

	/**
	 * Returns index offset at simulation time t. Add the particle index to get the cell index.
	 * @param t     current simulation step
	 * @return      index offset since beginning of simulation.
	 */
	public int getCurrentOffset(int t) {
		if(orientation > 0) {
			return indexOffset + ((t + subLatticeShift) / particlePerCell) * particlesPerPlane;
		}
		int shift = ((int) Math.floor((- t + subLatticeShift) / (1.0 * particlePerCell))) * particlesPerPlane;
		return indexOffset + shift;
	}

	/**
	 * Returns the ngp index offset at simulation time t. Add the particle index to get the current ngp index.
	 * @param t     current simulation step
	 * @return      ngp index offset since beginning of simulation.
	 */
	public int getCurrentNGPOffset(int t) {
		int currentOffset = getCurrentOffset(t);
		if(orientation > 0) {
			return currentOffset + ((((t+subLatticeShift) % particlePerCell) < particlePerCell/2) ? 0 : particlesPerPlane);
		}
		int shift = ((((t - subLatticeShift + particlePerCell + particlePerCell/2 - 1) % particlePerCell) < particlePerCell/2) ? 0 : particlesPerPlane);
		return currentOffset + shift;
	}

	/**
	 * Checks if particle will cross an ngp boundary in the *next* simulation step.
	 * @param t     current simulation step
	 * @return      true/false if particle needs an update or needs to be interpolated to the grid.
	 */
	public boolean needsUpdate(int t) {
		if(orientation > 0) {
			return 0 == (t + subLatticeShift + particlePerCell / 2 + 1) % particlePerCell;
		}
		return 0 == (t - subLatticeShift + particlePerCell / 2 ) % particlePerCell;
	}

	// GETTERS

	public double getPosition(int i) {
		return 0;
	}

	public double getPrevPosition(int i) {
		return 0;
	}

	public double getVelocity(int i) {
		return 0;
	}

	public double[] getPosition() {
		return new double[]{0};
	}

	public double[] getPrevPosition() {
		return new double[]{0};
	}

	public double[] getVelocity() {
		return new double[]{0};
	}

	public double getRadius() {
		return 0;
	}

	public Color getDisplayColor() {
		return Color.BLACK;
	}

	public int getNumberOfDimensions() {
		return 0;
	}

	// SETTERS

	public void setPosition(int i, double value) {

	}

	public void addPosition(int i, double value) {

	}

	public void setPrevPosition(int i, double value) {

	}

	public void addPrevPosition(int i, double value) {

	}

	public void setVelocity(int i, double value) {

	}

	public void addVelocity(int i, double value) {

	}

	public void setNumberOfDimensions(int numberOfDimensions) {
	}

	public void setRadius(double r) {

	}

	public void setDisplayColor(Color color) {

	}

	public void reassignValues() {
	}

	public IParticle copy() {
		throw new RuntimeException("copy() method of CGCSuperParticle not implemented.");
	}
}
