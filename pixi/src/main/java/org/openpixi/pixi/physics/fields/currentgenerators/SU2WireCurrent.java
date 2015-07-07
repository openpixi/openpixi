package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.SU2Matrix;

public class SU2WireCurrent implements ICurrentGenerator {

	private int numberOfDimensions;
	private int numberOfComponents;
	private int direction;
	private int[] location;
	private double[] amplitudeColorDirection;
	private double magnitude;
	private Simulation s;
	private Grid grid;

	public SU2WireCurrent(int direction, int[] location, double[] amplitudeColorDirection, double magnitude) {
		this.numberOfDimensions = location.length;
		this.numberOfComponents = amplitudeColorDirection.length;

		this.direction = direction;
		this.location = location;

		/*
			Amplitude directions should be normalized.
		 */
		this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

		this.magnitude = magnitude;
	}

	public void applyCurrent(Simulation s) {
		this.s = s;
		this.grid = s.grid;
		double as = grid.getLatticeSpacing();
		double g = s.getCouplingConstant();
		int numberOfCells = grid.getTotalNumberOfCells();

		/*
			Setup the field amplitude for the current.
		 */
		SU2Field fieldAmplitude = new SU2Field(
				this.magnitude * this.amplitudeColorDirection[0],
				this.magnitude * this.amplitudeColorDirection[1],
				this.magnitude * this.amplitudeColorDirection[2]);


		/*
			Cycle through each cell and apply the current configuration to the cell currents.
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
			double factorForE = -g * as * omega * Math.sin(kx);
			//Phase of the plane wave at t = dt/2 (for links)
			double factorForU = g * as * Math.cos(omega * timeStep / 2.0 - kx);


			Cell currentCell = grid.getCell(c);

			for (int i = 0; i < this.numberOfDimensions; i++) {
				//Setup the gauge links
				SU2Matrix U = (SU2Matrix) currentCell.getU(i).mult(amplitudeYMField[i].mult(factorForU).getLinkExact());
				currentCell.setU(i, U);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(factorForE));
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
			position[i] = cellPosition[i] * grid.getLatticeSpacing();
		}
		return position;
	}
}
