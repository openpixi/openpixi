package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.currentgenerators.SU2LightConeGaussPulseCurrent;

import java.util.List;

/**
 * Yaml wrapper for the SU2LightConeDeltaPulseCurrent CurrentGenerator.
 */
public class YamlSU2LightConeGaussPulseCurrent {

	/**
	 * Direction of the current.
	 */
	public Integer direction;

	/**
	 * Starting point for the current pulse on the grid.
	 */
	public List<Double> location;

	/**
	 * Direction of the current in color space.
	 */
	public List<Double> aColor;

	/**
	 * Magnitude of the current.
	 */
	public Double a;

	/**
	 * Orientation of the current.
	 */
	public Integer v;

	/**
	 * Width of the Gauss pulse.
	 */
	public Double width;

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (direction >= settings.getNumberOfDimensions()) {
			System.out.println("SU2LightConeGaussPulseCurrent: direction index exceeds the dimensions of the system.");
			return false;
		}

		if (location.size() != settings.getNumberOfDimensions()) {
			System.out.println("SU2LightConeGaussPulseCurrent: location vector does not have the right dimensions.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		if (aColor.size() != numberOfComponents) {
			System.out.println("SU2LightConeGaussPulseCurrent: aColor vector does not have the right dimensions.");
			return false;
		}

		if (v == 0) {
			System.out.println("SU2LightConeGaussPulseCurrent: Orientation has to be non-zero.");
			return false;
		}

		if (width <= 0) {
			System.out.println("SU2LightConeGaussPulseCurrent: Width has to be a non-zero positive number.");
			return false;
		}
		return true;
	}

	/**
	 * Returns an instance of SU2LightConeDeltaPulseCurrent according to the parameters in the YAML file.
	 *
	 * @return Instance of SU2LightConeDeltaPulseCurrent.
	 */
	public SU2LightConeGaussPulseCurrent getCurrentGenerator() {
		int numberOfDimensions = location.size();
		int numberOfComponents = aColor.size();
        /*
			I'm sure this can be improved. I don't know how to convert a ArrayList<Double> into a double[] quickly, so
            I do it manually.
         */

		double[] locationArray = new double[numberOfDimensions];
		double[] aColorArray = new double[numberOfComponents];

		for (int i = 0; i < numberOfDimensions; i++) {
			locationArray[i] = location.get(i);
		}

		for (int c = 0; c < numberOfComponents; c++) {
			aColorArray[c] = aColor.get(c);
		}

		return new SU2LightConeGaussPulseCurrent(direction, locationArray, width, aColorArray, a, v);
	}
}
