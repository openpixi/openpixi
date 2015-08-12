package org.openpixi.pixi.ui.util.yaml.fieldgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.fieldgenerators.SUNFocusedGaussianPulse;

import java.util.List;

/**
 * Yaml wrapper for the SUNFocusedGaussianPulse FieldGenerator.
 */
public class YamlSUNFocusedGaussianPulse {

	/**
	 * Direction vector of the pulse.
	 */
	public List<Double> dir;

	/**
	 * Focal point of the pulse (where to pulse converges).
	 */
	public List<Double> pos;

	/**
	 * Polarisation angle of the amplitude.
	 */
	public Double polarisationAngle = 0.0;

	/**
	 * Option whether to use a random polarisation angle.
	 */
	public Boolean useRandomPolarisation = false;

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
			System.out.println("SUNFocusedGaussianPulse: dir vector does not have the right dimensions.");
			return false;
		}

		if (pos.size() != settings.getNumberOfDimensions()) {
			System.out.println("SUNFocusedGaussianPulse: pos vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SUNFocusedGaussianPulse: aColor vector does not have the right dimensions.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SUNFocusedGaussianPulse according to the parameters in the YAML file.
	 *
	 * @return Instance of SUNFocusedGaussianPulse.
	 */
	public SUNFocusedGaussianPulse getFieldGenerator() {
		int numberOfDimensions = dir.size();
		int numberOfComponents = aColor.size();

        /*
			I'm sure this can be improved. I don't know how to convert a ArrayList<Double> into a double[] quickly, so
            I do it manually.
         */

		double[] dirArray = new double[numberOfDimensions];
		double[] posArray = new double[numberOfDimensions];
		double[] aColorArray = new double[numberOfComponents];

		for (int i = 0; i < numberOfDimensions; i++) {
			dirArray[i] = dir.get(i);
			posArray[i] = pos.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		if(useRandomPolarisation) {
			double polAngle = 2.0 * Math.PI * Math.random();
			return new SUNFocusedGaussianPulse(dirArray, posArray, polAngle, aColorArray, a, sigma, angle, distance);

		}
		return new SUNFocusedGaussianPulse(dirArray, posArray, polarisationAngle, aColorArray, a, sigma, angle, distance);
	}
}
