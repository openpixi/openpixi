package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.PointChargeLCCurrent;
import org.openpixi.pixi.physics.fields.currentgenerators.SphericalProtonLCCurrent;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomTemporalParticleColorCurrentSphericalProton {
	/**
	 * Direction of the current pulse (0 to d)
	 */
	public Integer direction;

	/**
	 * Orientation of the current pulse (-1 or 1)
	 */
	public Integer orientation;

	/**
	 * Starting location of the pulse on the longitudinal line
	 */
	public Double longitudinalLocation;

	/**
	 *	Center of the charge distribution in the transversal plane
	 */
	public ArrayList<Double> transversalLocation;

	/**
	 * Longitudinal width of the pulse (Gauss shape)
	 */
	public Double longitudinalWidth;

	/**
	 * Transversal width of the random charge distribution
	 */
	public Double transversalWidth;

	/**
	 * Number of point-like charges in the distribution
	 */
	public Integer numberOfCharges;

	/**
	 * Width of the Gaussian distribution from which the random charges are sampled
	 */
	public Double colorDistributionWidth;

	/**
	 * Number of colors
	 */
	public Integer numberOfColors;

	/**
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed = null;

	/**
	 * Option whether to use the dipole removal method.
	 */
	public Boolean useDipoleRemoval = true;


	public SphericalProtonLCCurrent getCurrentGenerator() {
		SphericalProtonLCCurrent generator = new SphericalProtonLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth, true, useDipoleRemoval);

		double[] locationTransverse = new double[transversalLocation.size()];
		for (int i = 0; i < transversalLocation.size(); i++) {
			locationTransverse[i] = transversalLocation.get(i);
		}

		int numberOfComponents = numberOfColors * numberOfColors - 1;
		if(numberOfColors == 1) {
			numberOfComponents = 1;
		}

		for(int i = 0; i < numberOfCharges; i++) {
			generator.addCharge(locationTransverse, transversalWidth);
		}

		return generator;
	}


}
