package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PlanarFields implements Diagnostics {

	private double timeInterval;
	private int stepInterval;
	private String outputName;
	private double startingTime;
	private int startingStep;
	private double finalTime;
	private int finalStep;

	private int direction;
	private int planarIndex;

	private int[] transNumCells;
	private int totalTransCells;

	private int effDimensions;

	private Simulation s;
	private int numberOfComponents;

	public PlanarFields(double timeInterval, String outputName, double startingTime, double finalTime, int direction, int planarIndex) {
		this.timeInterval = timeInterval;
		this.outputName = outputName;
		this.startingTime =  startingTime;
		this.finalTime = finalTime;
		this.direction = direction;
		this.planarIndex = planarIndex;

	}

	public void initialize(Simulation s) {
		FileFunctions.clearFile(outputName);

		this.s = s;
		this.transNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		this.totalTransCells =  GridFunctions.getTotalNumberOfCells(transNumCells);

		this.stepInterval = (int) Math.max(Math.round((timeInterval / s.getTimeStep())), 1);
		this.startingStep = (int) (startingTime / s.getTimeStep());
		this.finalStep = (int) (finalTime / s.getTimeStep());
		this.effDimensions = GridFunctions.getEffectiveNumberOfDimensions(s.grid.getNumCells());

		this.numberOfComponents = s.grid.getElementFactory().numberOfComponents;
	}


	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(steps > startingStep && steps < finalStep) {
			if (steps % stepInterval == 0) {
				// do stuff
				double normalizationFactor = 1.0 / s.getCouplingConstant() * grid.getLatticeSpacing();
				AlgebraElement[][]  transverseGaugeFields = new AlgebraElement[effDimensions][totalTransCells];
				AlgebraElement[]  longitudinalElectricFields = new AlgebraElement[totalTransCells];
				for (int i = 0; i < totalTransCells; i++) {
					int[] gridPos = GridFunctions.insertGridPos(GridFunctions.getCellPos(i, transNumCells), direction, planarIndex);
					int cellIndex = grid.getCellIndex(gridPos);

					int ts = 0;
					for (int j = 0; j < effDimensions; j++) {
						if(j == direction) {
							longitudinalElectricFields[i] = grid.getE(cellIndex, j).mult(normalizationFactor);
						} else {
							transverseGaugeFields[ts][i] = grid.getLink(cellIndex, j, 1, 0).getAlgebraElement().mult(normalizationFactor);
							ts++;
						}
					}
				}

				File file = FileFunctions.getFile(outputName);
				try {
					FileWriter pw = new FileWriter(file, true);

					//pw.write(s.getIterations() + "\t" + s.getIterations() * s.getTimeStep() + "\n");

					// Transverse fields
					for (int i = 0; i < effDimensions-1; i++) {
						double[][] output = convertToDoubleArray(transverseGaugeFields[i], numberOfComponents);
						for (int j = 0; j < numberOfComponents; j++) {
							pw.write(generateTSVString(output[j]) + "\n");
						}
					}

					// Longitudinal fields
					double[][] output = convertToDoubleArray(longitudinalElectricFields, numberOfComponents);
					for (int j = 0; j < numberOfComponents; j++) {
						pw.write(generateTSVString(output[j]) + "\n");
					}

					pw.close();
				} catch (IOException ex) {
					System.out.println("PlanarFields: Error writing to file.");
				}

			}
		}
	}

	private String generateTSVString(double[] array) {
		StringBuilder outputStringBuilder = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("0.################E0");
		for (int i = 0; i < array.length; i++) {
			outputStringBuilder.append(formatter.format(array[i]));
			if(i < array.length - 1) {
				outputStringBuilder.append("\t");
			}
		}
		return outputStringBuilder.toString();
	}

	private double[][] convertToDoubleArray(AlgebraElement[] array, int numberOfComponents) {
		double[][] output = new double[3][array.length];
		for (int i = 0; i < array.length; i++) {
			AlgebraElement a = array[i];
			for (int j = 0; j < numberOfComponents; j++) {
				output[j][i] = a.get(j);
			}
		}
		return output;
	}

}
