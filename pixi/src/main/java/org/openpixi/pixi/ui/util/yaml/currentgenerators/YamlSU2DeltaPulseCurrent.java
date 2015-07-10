package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.currentgenerators.SU2DeltaPulseCurrent;

import java.util.List;

/**
 * Yaml wrapper for the SU2WireCurrent CurrentGenerator.
 */
public class YamlSU2DeltaPulseCurrent {

	/**
	 * Direction of the current.
	 */
	public Integer direction;

	/**
	 * Starting point for the current pulse on the grid.
	 */
	public List<Integer> location;

	/**
	 * Direction of the current in color space.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the current.
	 */
	public Double a;

	/**
	 * Speed of the pulse in terms of the speed of light.
	 */
	public Double v;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (direction >= settings.getNumberOfDimensions()) {
			System.out.println("SU2DeltaPulseCurrent: direction index exceeds the dimensions of the system.");
			return false;
		}

		if (location.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2DeltaPulseCurrent: location vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SU2DeltaPulseCurrent: aColor vector does not have the right dimensions.");
			return false;
		}

		if (Math.abs(v) >= settings.getSpeedOfLight()) {
			System.out.println("SU2DeltaPulseCurrent: v exceeds the chosen speed of light.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SU2DeltaPulseCurrent according to the parameters in the YAML file.
	 *
	 * @return Instance of SU2DeltaPulseCurrent.
	 */
	public SU2DeltaPulseCurrent getCurrentGenerator() {
		int numberOfDimensions = location.size();
		int numberOfComponents = aColor.size();

        /*
			I'm sure this can be improved. I don't know how to convert a ArrayList<Double> into a double[] quickly, so
            I do it manually.
         */

		int[] locationArray = new int[numberOfDimensions];
		double[] aColorArray = new double[numberOfComponents];

		for (int i = 0; i < numberOfDimensions; i++) {
			locationArray[i] = location.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		return new SU2DeltaPulseCurrent(direction, locationArray, aColorArray, a, v);
	}
}
