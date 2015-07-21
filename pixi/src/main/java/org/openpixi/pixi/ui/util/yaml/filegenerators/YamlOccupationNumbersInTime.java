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
	 * True/False. Decides whether to output 'colored' occupation numbers or ouput the sum over all colors.
	 */
	public Boolean colorful = false;

	/**
	 * Returns an instance of CoulombGaugeInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public OccupationNumbersInTime getFileGenerator() {
		OccupationNumbersInTime fileGen;
		if(colorful != null) {
			fileGen = new OccupationNumbersInTime(interval, outputType, path, colorful);
		} else {
			fileGen = new OccupationNumbersInTime(interval, outputType, path, false);
		}
		return fileGen;
	}
}
