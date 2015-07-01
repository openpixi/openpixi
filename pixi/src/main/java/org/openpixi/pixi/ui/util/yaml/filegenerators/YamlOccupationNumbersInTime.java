package org.openpixi.pixi.ui.util.yaml.filegenerators;
import org.openpixi.pixi.diagnostics.methods.OccupationNumbersInTime;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlOccupationNumbersInTime {

	/**
	 * Measurement interval.
	 */
	public double interval;

	/**
	 * Format type of the output.
	 * Supported types are: "csv"
	 */
	public String outputType;

	/**
	 * Output file path.
	 */
	public String path;

	/**
	 * Returns an instance of CoulombGaugeInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public OccupationNumbersInTime getFileGenerator() {
		OccupationNumbersInTime fileGen = new OccupationNumbersInTime(interval, outputType, path);
		return fileGen;
	}
}
