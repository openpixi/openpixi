package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.initial.CGC.CGCInitialCondition;
import org.openpixi.pixi.physics.initial.CGC.IInitialChargeDensity;
import org.openpixi.pixi.physics.initial.CGC.MVModel;
import org.openpixi.pixi.physics.initial.IInitialCondition;

public class YamlMVModel {
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
	 * Longitudinal width of the pulse (Gauss shape)
	 */
	public Double longitudinalWidth;

	/**
	 * \mu parameter of the MV model. This controls the average charge density squared.
	 */
	public Double mu;

	/**
	 * Coefficient used for the transverse UV regulator, which is implemented as a hard cutoff. This parameter is given
	 * in units of inverse lattice spacings. A value of sqrt(2)*PI corresponds to no UV cutoff. A value of 0.0 cuts off
	 * all modes in momentum space.
	 */
	public Double ultravioletCutoffTransverse = 4.44;

	/**
	 * Coefficient used for the longitudinal UV regulator, which is implemented as a soft cutoff. This parameter
	 * describes the longitudinal coherence length inside the nucleus and is given in physical units (E^-1). A value of
	 * 0.0 would theoretically correspond to a delta function, but this will not work obviously. It definitely makes
	 * sense to set this value low than the longitudinalWidth of the nucleus.
	 */
	public Double longitudinalCoherenceLength = 0.0;

	/**
	 * Coefficient infrared regulator in the Poisson solver
	 */
	public Double infraredCoefficient = 0.0;

	/**
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed;

	public CGCInitialCondition getInitialCondition() {
		boolean useSeed = (randomSeed != null);
		if(!useSeed) {
			randomSeed = 0;
		}
		IInitialChargeDensity chargeDensity = new MVModel(direction, orientation, longitudinalLocation,
				longitudinalWidth, mu, useSeed, randomSeed, ultravioletCutoffTransverse, longitudinalCoherenceLength,
				infraredCoefficient);

		CGCInitialCondition initialCondition = new CGCInitialCondition();
		initialCondition.setInitialChargeDensity(chargeDensity);

		return initialCondition;
	}
}
