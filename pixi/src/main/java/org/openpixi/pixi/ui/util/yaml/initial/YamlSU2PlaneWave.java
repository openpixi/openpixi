package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.initial.YM.SU2PlaneWave;

import java.util.List;

/**
 * Yaml wrapper for the SU2PlaneWave FieldGenerator.
 */
public class YamlSU2PlaneWave {

	/**
	 * Wave vector of the plane wave.
	 */
	public List<Double> k;

	/**
	 * Spatial amplitude of the plane wave.
	 */
	public List<Double> aSpatial;

	/**
	 * Amplitude of the plane wave in color space.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the plane wave.
	 */
	public Double a;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (k.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2PlaneWave: dir vector does not have the right dimensions.");
			return false;
		}

		if (aSpatial.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2PlaneWave: aSpatial vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SU2PlaneWave: aColor vector does not have the right dimensions.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SU2PlaneWave according to the parameters in the YAML file.
	 *
	 * @return Instance of SU2PlaneWave.
	 */
	public SU2PlaneWave getFieldGenerator() {
		int numberOfDimensions = k.size();
		int numberOfComponents = aColor.size();

        /*
			I'm sure this can be improved. I don't know how to convert a ArrayList<Double> into a double[] quickly, so
            I do it manually.
         */

		double[] kArray = new double[numberOfDimensions];
		double[] aSpatialArray = new double[numberOfDimensions];
		double[] aColorArray = new double[numberOfComponents];

		for (int i = 0; i < numberOfDimensions; i++) {
			kArray[i] = k.get(i);
			aSpatialArray[i] = aSpatial.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		return new SU2PlaneWave(kArray, aSpatialArray, aColorArray, a);
	}
}
