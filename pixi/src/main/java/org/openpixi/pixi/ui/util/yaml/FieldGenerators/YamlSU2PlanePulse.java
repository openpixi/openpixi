package org.openpixi.pixi.ui.util.yaml.FieldGenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.FieldGenerators.SU2PlanePulse;

import java.util.List;

/**
 * Yaml wrapper for the SU2PlanePulse FieldGenerator.
 */
public class YamlSU2PlanePulse {

	/**
	 * Direction vector of the plane pulse.
	 */
	public List<Double> dir;

	/**
	 * Position of the plane pulse "wave front".
	 */
	public List<Double> pos;

	/**
	 * Spatial amplitude of the plane pulse.
	 */
	public List<Double> aSpatial;

	/**
	 * Amplitude of the plane pulse in color space.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the plane pulse.
	 */
	public Double a;

	/**
	 * Width of the plane pulse.
	 */
	public Double sigma;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (dir.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2PlanePulse: dir vector does not have the right dimensions.");
			return false;
		}

		if (pos.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2PlanePulse: pos vector does not have the right dimensions.");
			return false;
		}

		if (aSpatial.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2PlanePulse: aSpatial vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SU2PlanePulse: aColor vector does not have the right dimensions.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SU2PlanePulse according to the parameters in the YAML file.
	 *
	 * @return Instance of SU2PlanePulse.
	 */
	public SU2PlanePulse getFieldGenerator() {
		int numberOfDimensions = dir.size();
		int numberOfComponents = aColor.size();

        /*
			I'm sure this can be improved. I don't know how to convert a ArrayList<Double> into a double[] quickly, so
            I do it manually.
         */

		double[] dirArray = new double[numberOfDimensions];
		double[] posArray = new double[numberOfDimensions];
		double[] aSpatialArray = new double[numberOfDimensions];
		double[] aColorArray = new double[numberOfComponents];

		for (int i = 0; i < numberOfDimensions; i++) {
			dirArray[i] = dir.get(i);
			posArray[i] = pos.get(i);
			aSpatialArray[i] = aSpatial.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		return new SU2PlanePulse(dirArray, posArray, aSpatialArray, aColorArray, a, sigma);
	}
}
