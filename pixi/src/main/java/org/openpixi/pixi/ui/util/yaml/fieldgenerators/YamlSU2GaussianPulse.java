package org.openpixi.pixi.ui.util.yaml.fieldgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2GaussianPulse;

import java.util.List;

/**
 * Yaml wrapper for the SU2GaussianPulse FieldGenerator.
 */
public class YamlSU2GaussianPulse {

	/**
	 * Direction vector of the gaussian pulse.
	 */
	public List<Double> dir;

	/**
	 * Position of the gaussian pulse "wave front".
	 */
	public List<Double> pos;

	/**
	 * Spatial amplitude of the gaussian pulse.
	 */
	public List<Double> aSpatial;

	/**
	 * Amplitude of the gaussian pulse in color space.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the gaussian pulse.
	 */
	public Double a;

	/**
	 * Width of the gaussian pulse.
	 */
	public List<Double> sigma;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (dir.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2GaussianPulse: dir vector does not have the right dimensions.");
			return false;
		}

		if (pos.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2GaussianPulse: pos vector does not have the right dimensions.");
			return false;
		}

		if (aSpatial.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2GaussianPulse: aSpatial vector does not have the right dimensions.");
			return false;
		}

		if (sigma.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2GaussianPulse: sigma vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SU2GaussianPulse: aColor vector does not have the right dimensions.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SU2GaussianPulse according to the parameters in the YAML file.
	 *
	 * @return Instance of SU2GaussianPulse.
	 */
	public SU2GaussianPulse getFieldGenerator() {
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
		double[] sigmaArray = new double[numberOfDimensions];

		for (int i = 0; i < numberOfDimensions; i++) {
			dirArray[i] = dir.get(i);
			posArray[i] = pos.get(i);
			aSpatialArray[i] = aSpatial.get(i);
			sigmaArray[i] = sigma.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		return new SU2GaussianPulse(dirArray, posArray, aSpatialArray, aColorArray, a, sigmaArray);
	}
}
