package org.openpixi.pixi.ui.util.yaml.fieldgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2FocusedGaussianPulse;
import org.openpixi.pixi.physics.fields.fieldgenerators.SU2Test;

import java.util.List;

/**
 * Yaml wrapper for the SU2FocusedGaussianPulse FieldGenerator.
 */
public class YamlSU2Test {

	/**
	 * Direction vector of the pulse.
	 */
	public List<Double> dir;

	/**
	 * Spatial magnitude of the pulse.
	 */
	public List<Double> aSpatial;

	/**
	 * Color magnitude of the pulse.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the pulse.
	 */
	public Double a;

	/**
	 * Position of the pulse.
	 */
	public List<Double> pos;

	/**
	 * Eccentricity of the pulse envelope.
	 */
	public Double e;

	/**
	 * Scale size of envelope.
	 */
	public Double size;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (dir.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2Test: dir vector does not have the right dimensions.");
			return false;
		}

		if (aSpatial.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2Test: aSpatial vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SU2Test: aColor vector does not have the right dimensions.");
			return false;
		}

		if (pos.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2Test: pos vector does not have the right dimensions.");
			return false;
		}

		if (e >= 1) {
			System.out.println("SU2Test: eccentricity must be less than one.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SU2FocusedGaussianPulse according to the parameters in the YAML file.
	 *
	 * @return Instance of SU2FocusedGaussianPulse.
	 */
	public SU2Test getFieldGenerator() {
		int numberOfDimensions = dir.size();
		int numberOfComponents = aColor.size();

		double[] dirArray = new double[numberOfDimensions];
		double[] aSpatialArray = new double[numberOfDimensions];
		double[] aColorArray = new double[numberOfComponents];
		double[] posArray = new double[numberOfDimensions];

		for (int i = 0; i < numberOfDimensions; i++) {
			dirArray[i] = dir.get(i);
			aSpatialArray[i] = aSpatial.get(i);
			posArray[i] = pos.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		return new SU2Test(dirArray, aSpatialArray, aColorArray, a, posArray, e, size);
	}
}
