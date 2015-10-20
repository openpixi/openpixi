package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;

import java.util.ArrayList;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlBulkQuantitiesInTime {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement interval.
	 */
	public double interval;

	/**
	 * Grid position defining the first point of the restricted region.
	 */
	public ArrayList<Integer> regionPoint1;

	/**
	 * Grid position defining the second point of the restricted region.
	 */
	public ArrayList<Integer> regionPoint2;


	/**
	 * Returns an instance of BulkQuantitiesInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public BulkQuantitiesInTime getFileGenerator() {
		if(regionPoint1 != null && regionPoint2 != null && regionPoint1.size() == regionPoint2.size()) {
			int[] regionPoint1Array = new int[regionPoint1.size()];
			int[] regionPoint2Array = new int[regionPoint2.size()];

			for (int i = 0; i < regionPoint1.size(); i++) {
				regionPoint1Array[i] = regionPoint1.get(i);
				regionPoint2Array[i] = regionPoint2.get(i);
			}

			BulkQuantitiesInTime fileGen = new BulkQuantitiesInTime(path, interval, false, regionPoint1Array, regionPoint2Array);
			return fileGen;
		} else {
			BulkQuantitiesInTime fileGen = new BulkQuantitiesInTime(path, interval);
			return fileGen;
		}
	}
}