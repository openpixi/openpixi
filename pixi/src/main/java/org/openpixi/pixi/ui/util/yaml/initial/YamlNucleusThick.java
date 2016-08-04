package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.initial.CGC.CGCInitialCondition;
import org.openpixi.pixi.physics.initial.CGC.IInitialChargeDensity;
import org.openpixi.pixi.physics.initial.CGC.Nucleus;
import org.openpixi.pixi.physics.initial.CGC.NucleusThick;

import java.util.ArrayList;

public class YamlNucleusThick {
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
	 * Gamma factor determining the length contraction of the charge density
	 */
	public Double gammaFactor;

	/**
	 * Transversal radius of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	public Double transversalRadius;

	/**
	 * Transversal surface thickness of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	public Double surfaceThickness;

	/**
	 * Number of nucleons in the nucleus
	 */
	public Integer numberOfNucleons;

	/**
	 * Width of the Gaussian distribution of the valence quark charge
	 */
	public Double partonWidth;

	/**
	 * Width of the Gaussian distribution of the nucleons
	 */
	public Double nucleonWidth;

	/**
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed = null;

	/**
	 * Option whether to use the constituent quark model for the nucleons.
	 */
	public Boolean useConstituentQuarks = true;

	/**
	 * Color charge density, similar to the \mu parameter of the MV model. This controls the average charge density squared.
	 */
	public Double mu;

	/**
	 * Coefficient used for the transverse UV regulator, which is implemented as a hard cutoff. This parameter is given
	 * in units of inverse lattice spacings. A value of sqrt(2)*PI corresponds to no UV cutoff. A value of 0.0 cuts off
	 * all modes in momentum space.
	 */
	public Double ultravioletCutoffTransverse = 4.44;

	/**
	 * Coefficient used for the longitudinal UV regulator, which is implemented as a hard cutoff. This parameter is
	 * given in units of inverse lattice spacings. A value of sqrt(2)*PI corresponds to no UV cutoff. A value of 0.0
	 * cuts off all modes in momentum space.
	 */
	//public Double ultravioletCutoffLongitudinal = 4.44;

	/**
	 * Coefficient used for the longitudinal UV regulator, which is implemented as a soft cutoff. This parameter
	 * describes the longitudinal coherence length inside the nucleus and is given in physical units (E^-1). A value of
	 * 0.0 would theoretically correspond to a delta function, but this will not work obviously. It definitely makes
	 * sense to set this value lower than the longitudinalWidth of the nucleus.
	 */
	public Double longitudinalCoherenceLength = 0.0;

	/**
	 * Coefficient infrared regulator in the Poisson solver
	 */
	public Double infraredCoefficient = 0.0;


	public CGCInitialCondition getInitialCondition() {
		boolean useSeed = (randomSeed != null);
		if(!useSeed) {
			randomSeed = 0;
		}

		double[] locationTransverse = new double[transversalLocation.size()];
		for (int j = 0; j < transversalLocation.size(); j++) {
			locationTransverse[j] = transversalLocation.get(j);
		}

		IInitialChargeDensity chargeDensity = new NucleusThick(direction, orientation, longitudinalLocation, locationTransverse, gammaFactor,
				mu, useSeed, randomSeed, numberOfNucleons, useConstituentQuarks, transversalRadius, surfaceThickness,
				nucleonWidth, partonWidth, ultravioletCutoffTransverse, longitudinalCoherenceLength, infraredCoefficient);

		CGCInitialCondition initialCondition = new CGCInitialCondition();
		initialCondition.setInitialChargeDensity(chargeDensity);

		return initialCondition;
	}

}
