package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.PlanarFields;

public class YamlPlanarFields {
	/**
	 * Measurement interval.
	 */
	public Double interval;

	/**
	 * Measurement starting time.
	 */
	public Double startingTime;

	/**
	 * Measurement stopping time.
	 */
	public Double finalTime;

	/**
	 * Output file path.
	 */
	public String path;

	/**
	 * Direction orthogonal the plane.
	 */
	public Integer direction;

	/**
	 * Index of the plane in the chosen direction;
	 */
	public Integer planarIndex;


	public PlanarFields getFileGenerator() {
		if(startingTime == null) {
			startingTime = 0.0;
		}

		if(finalTime == null) {
			finalTime = Double.MAX_VALUE;
		}

		return new PlanarFields(interval, path, startingTime, finalTime, direction, planarIndex);
	}
}
