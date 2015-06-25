package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.ScreenshotInTime;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlScreenshotInTime {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement interval.
	 */
	public double interval;

	/**
	 * Measurement interval offset.
	 */
	public double offset;

	/**
	 * Display width.
	 */
	public int width;

	/**
	 * Display height.
	 */
	public int height;

	/**
	 * Returns an instance of BulkQuantitiesInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public ScreenshotInTime getFileGenerator() {
		ScreenshotInTime fileGen = new ScreenshotInTime(path, interval, offset, width, height);
		return fileGen;
	}
}