package org.openpixi.pixi.ui.util.yaml.filegenerators;

import java.util.List;

import org.openpixi.pixi.diagnostics.methods.CoulombGaugeInTime;
import org.openpixi.pixi.diagnostics.methods.RandomGaugeInTime;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlRandomGaugeInTime {

	/**
	 * Measurement interval.
	 */
	public double interval;

	/**
	 * Measurement interval offset.
	 */
	public double offset;

	/**
	 * Amplitude of random component in color space.
	 */
	public List<Double> aColor;

	/**
	 * Returns an instance of CoulombGaugeInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public RandomGaugeInTime getFileGenerator() {
		double[] aColorArray = getDoubleArray(aColor);
		RandomGaugeInTime fileGen = new RandomGaugeInTime(interval, offset, aColorArray);
		return fileGen;
	}

	private double[] getDoubleArray(List<Double> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
}
