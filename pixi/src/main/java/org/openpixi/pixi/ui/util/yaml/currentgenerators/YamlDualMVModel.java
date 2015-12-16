package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.DualMVModel;
import org.openpixi.pixi.physics.fields.currentgenerators.MVModel;

public class YamlDualMVModel {
	/**
	 * Direction of the current pulse (0 to d)
	 */
	public Integer direction;

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
	 * Seeds to use for the random number generator
	 */
	public Integer randomSeed1 = null;
	public Integer randomSeed2 = null;

	/**
	 * Option for writing boost-invariant collision initial conditins to file.
	 */
	public Boolean createInitialConditionsOutput =  false;

	/**
	 * Path to the output file.
	 */
	public String outputFile = null;


	public DualMVModel getCurrentGenerator() {
		if(randomSeed1 != null && randomSeed2 != null) {
			return new DualMVModel(direction, longitudinalLocation, longitudinalWidth, mu, true, randomSeed1, randomSeed2, createInitialConditionsOutput, outputFile);
		}
		return new DualMVModel(direction, longitudinalLocation, longitudinalWidth, mu, false, 0, 0, createInitialConditionsOutput, outputFile);
	}

}
