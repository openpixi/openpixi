package org.openpixi.pixi.physics.initial.YM;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.initial.IInitialCondition;

public class SU2PlaneWave implements IInitialCondition {

	private int numberOfDimensions;
	private int numberOfComponents;
	private double[] k;
	private double[] amplitudeSpatialDirection;
	private double[] amplitudeColorDirection;
	private double amplitudeMagnitude;
	private Simulation s;
	private Grid grid;
	private double timeStep;

	public SU2PlaneWave(double[] k, double[] amplitudeSpatialDirection, double[] amplitudeColorDirection, double amplitudeMagnitude) {
		this.numberOfDimensions = k.length;
		this.numberOfComponents = amplitudeColorDirection.length;

		this.k = k;

		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeSpatialDirection = this.normalizeVector(amplitudeSpatialDirection);
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

		this.amplitudeMagnitude = amplitudeMagnitude;
	}

	public void applyInitialCondition(Simulation s) {
		this.s = s;
		this.grid = s.grid;
		this.timeStep = s.getTimeStep();

		ElementFactory factory = grid.getElementFactory();
		int colors = grid.getNumberOfColors();

		/*
			Setup the field amplitude for the plane wave.
		 */
		AlgebraElement[] amplitudeYMField = new AlgebraElement[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			amplitudeYMField[i] = factory.algebraZero(colors);
			for (int j = 0; j < this.numberOfComponents; j++) {
				amplitudeYMField[i].set(j,this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[j]);
			}
		}

		int numberOfCells = grid.getTotalNumberOfCells();

		/*
			Cycle through each cell and apply the plane wave configuration to the links and electric fields.
		 */
		for (int c = 0; c < numberOfCells; c++) {
			int[] cellPosition = grid.getCellPos(c);
			double[] position = getPosition(cellPosition);

			double kx = 0.0;
			double omega = 0.0;
			for (int i = 0; i < this.numberOfDimensions; i++) {
				kx += this.k[i] * position[i];
				omega += this.k[i] * this.k[i];
			}
			omega = s.getSpeedOfLight() * Math.sqrt(omega);

			//Factor of the plane wave at t = 0 (for electric fields)
			double factorForE = - omega * Math.sin(kx);
			//Phase of the plane wave at t = - dt/2 (for links)
			double factorForU = Math.cos(- omega * timeStep / 2.0 - kx);


			Cell currentCell = grid.getCell(c);

			for (int i = 0; i < this.numberOfDimensions; i++) {
				double unitFactor = s.grid.getLatticeUnitFactor(i);
				//Setup the gauge links
				SU2GroupElement U = (SU2GroupElement) currentCell.getU(i).mult(amplitudeYMField[i].mult(factorForU * unitFactor).getLink());
				currentCell.setU(i, U);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(factorForE * unitFactor));
			}
		}

	}

	private double[] normalizeVector(double[] vector) {
		double norm = 0.0;
		double[] output = new double[vector.length];
		for (int i = 0; i < vector.length; i++) {
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < vector.length; i++) {
			output[i] = vector[i] / norm;
		}
		return output;
	}

	private double[] getPosition(int[] cellPosition) {
		double[] position = new double[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			position[i] = cellPosition[i] * grid.getLatticeSpacing(i);
		}
		return position;
	}
}
