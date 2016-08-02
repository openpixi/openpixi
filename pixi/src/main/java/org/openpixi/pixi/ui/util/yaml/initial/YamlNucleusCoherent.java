package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.initial.CGC.CGCInitialCondition;
import org.openpixi.pixi.physics.initial.CGC.IInitialChargeDensity;
import org.openpixi.pixi.physics.initial.CGC.NucleusCoherent;
import org.openpixi.pixi.physics.initial.IInitialCondition;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;
import java.util.Random;

public class YamlNucleusCoherent {
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
	public Double ultravioletCutoffLongitudinal = 4.44;

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

		IInitialChargeDensity chargeDensity = new NucleusCoherent(direction, orientation, longitudinalLocation, locationTransverse, longitudinalWidth,
				mu, useSeed, randomSeed, numberOfNucleons, useConstituentQuarks, transversalRadius, surfaceThickness,
				nucleonWidth, partonWidth, ultravioletCutoffTransverse, ultravioletCutoffLongitudinal, infraredCoefficient);

		CGCInitialCondition initialCondition = new CGCInitialCondition();
		initialCondition.setInitialChargeDensity(chargeDensity);

		return initialCondition;
	}

}
