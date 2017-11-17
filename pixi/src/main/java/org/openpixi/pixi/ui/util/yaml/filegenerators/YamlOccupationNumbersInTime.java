package org.openpixi.pixi.ui.util.yaml.filegenerators;
import org.openpixi.pixi.diagnostics.methods.OccupationNumbersInTime;
import java.util.List;

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

	public Boolean useMirroredGrid;
	public Integer mirroredDirection;
	public Boolean useRectangularWindow;
	public Double collisionTime;
	public List<Double> collisionPosition;
	public List<Double> coneVelocity;
	public Boolean useGaussianWindow;
	public Boolean useTukeyWindow;
	public Double tukeyWidth;

	/**
	 * Returns an instance of CoulombGaugeInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public OccupationNumbersInTime getFileGenerator() {
		OccupationNumbersInTime fileGen;
		if (useMirroredGrid != null) {

			double[] collisionPositionArray = getDoubleArray(collisionPosition);
			double[] coneVelocityArray = getDoubleArray(coneVelocity);

			fileGen = new OccupationNumbersInTime(interval, outputType, path, colorful,
					useMirroredGrid, mirroredDirection,
					useRectangularWindow, collisionTime, collisionPositionArray, coneVelocityArray,
					useGaussianWindow,
					useTukeyWindow, tukeyWidth);
		} else if(colorful != null) {
			fileGen = new OccupationNumbersInTime(interval, outputType, path, colorful);
		} else {
			fileGen = new OccupationNumbersInTime(interval, outputType, path, false);
		}
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
