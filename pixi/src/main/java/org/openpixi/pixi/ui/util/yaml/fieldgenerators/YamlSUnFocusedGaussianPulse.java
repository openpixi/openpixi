package org.openpixi.pixi.ui.util.yaml.fieldgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.fieldgenerators.SUnFocusedGaussianPulse;

import java.util.List;

/**
 * Yaml wrapper for the SUnFocusedGaussianPulse FieldGenerator.
 */
public class YamlSUnFocusedGaussianPulse {

	/**
	 * Direction vector of the pulse.
	 */
	public List<Double> dir;

	/**
	 * Focal point of the pulse (where to pulse converges).
	 */
	public List<Double> pos;

	/**
	 * Spatial amplitude of the pulse.
	 */
	public List<Double> aSpatial;

	/**
	 * Amplitude of the pulse in color space.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the pulse.
	 */
	public Double a;

	/**
	 * Radial width of the pulse.
	 */
	public Double sigma;

	/**
	 * Angular width of the pulse.
	 */
	public Double angle;

	/**
	 * Radial distance from the focal point.
	 */
	public Double distance;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (dir.size() != settings.getNumberOfDimensions()) {
			System.out.println("SUnFocusedGaussianPulse: dir vector does not have the right dimensions.");
			return false;
		}

		if (pos.size() != settings.getNumberOfDimensions()) {
			System.out.println("SUnFocusedGaussianPulse: pos vector does not have the right dimensions.");
			return false;
		}

		if (aSpatial.size() != settings.getNumberOfDimensions()) {
			System.out.println("SUnFocusedGaussianPulse: aSpatial vector does not have the right dimensions.");
			return false;
		}
		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SUnFocusedGaussianPulse: aColor vector does not have the right dimensions.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SUnFocusedGaussianPulse according to the parameters in the YAML file.
	 *
	 * @return Instance of SUnFocusedGaussianPulse.
	 */
	public SUnFocusedGaussianPulse getFieldGenerator() {
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

		return new SUnFocusedGaussianPulse(dirArray, posArray, aSpatialArray, aColorArray, a, sigma, angle, distance);
	}
}
