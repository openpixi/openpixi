package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.currentgenerators.NewLCCurrent;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmueller on 9/3/15.
 */
public class YamlNewLCCurrent {

	/**
	 * Direction of the current pulse (0 to d)
	 */
	public Integer direction;

	/**
	 * Orientation of the current pulse (-1 or 1)
	 */
	public Integer orientation;

	/**
	 * Starting location of the pulse
	 */
	public Double location;

	/**
	 * Longitudinal width of the pulse (Gauss shape)
	 */
	public Double longitudinalWidth;

	/**
	 * List of charges on the pulse plane
	 */
	public ArrayList<YamlPointCharge> charges = new ArrayList<YamlPointCharge>();

	/**
	 * Checks input for errors.
	 *
	 * @param settings Settings class. Important: numberOfDimensions and numberOfColors must be defined.
	 * @return Returns true if everything looks alright.
	 */
	public boolean checkConsistency(Settings settings) {
		if (direction >= settings.getNumberOfDimensions()) {
			System.out.println("NewLCCurrent: direction index exceeds the dimensions of the system.");
			return false;
		}

		if(Math.abs(orientation) != 1) {
			System.out.println("NewLCCurrent: orientation must be either -1 or 1.");
			return false;
		}

		int numberOfComponents = settings.getNumberOfColors() * settings.getNumberOfColors() - 1;
		int effDim = GridFunctions.getEffectiveNumberOfDimensions(settings.getGridCells());

		for(YamlPointCharge c : charges) {
			// Check color vectors
			if (c.amplitudeColorDirection.size() != numberOfComponents) {
				System.out.println("NewLCCurrent: aColor vector does not have the right dimensions.");
				return false;
			}
			// Check location vectors
			if(c.location.size() != effDim) {
				System.out.println("NewLCCurrent: location vector does not have the right dimensions.");
				return false;
			}

		}


		return true;
	}


	public NewLCCurrent getCurrentGenerator() {
		NewLCCurrent generator = new NewLCCurrent(direction, orientation, location, longitudinalWidth);

		for(YamlPointCharge c: charges) {
			double[] chargeLocation = new double[c.location.size()];
			double[] chargeColorDirection = new double[c.amplitudeColorDirection.size()];
			for (int i = 0; i < c.location.size(); i++) {
				chargeLocation[i] = c.location.get(i);
			}
			for (int i = 0; i < c.amplitudeColorDirection.size(); i++) {
				chargeColorDirection[i] = c.amplitudeColorDirection.get(i);
			}
			generator.addCharge(chargeLocation, chargeColorDirection, c.magnitude);
		}

		return generator;
	}

}
