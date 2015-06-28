package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.particles.IParticle;

import java.util.ArrayList;
import java.util.Locale;

public class OccupationNumbersInTime implements Diagnostics {

	private Simulation s;
	private double timeInterval;
	private int stepInterval;
	private DoubleFFTWrapper fft;

	public double[] occupationNumbers;

	public OccupationNumbersInTime(double timeInterval) {
		this.timeInterval = timeInterval;
	}

	public void initialize(Simulation s) {
		this.s = s;
		this.stepInterval = (int) (this.timeInterval / s.getTimeStep());
		this.fft = new DoubleFFTWrapper(s.grid.getNumCells());

		occupationNumbers = new double[s.grid.getTotalNumberOfCells()];
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) {
		if (steps % stepInterval == 0) {
			// Apply Coulomb gauge.
			CoulombGauge coulombGauge = new CoulombGauge(grid);
			coulombGauge.applyGaugeTransformation(grid);

			int numberOfComponents = grid.getNumberOfColors() * grid.getNumberOfColors() - 1;
			double gainv = 1.0 / (grid.getLatticeSpacing() * grid.getGaugeCoupling());

			// Fill arrays for FFT.
			double[][][] eFFTdata = new double[grid.getNumberOfDimensions()][numberOfComponents][fft.getFFTArraySize()];
			double[][][] aFFTdata = new double[grid.getNumberOfDimensions()][numberOfComponents][fft.getFFTArraySize()];
			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				int fftIndex = fft.getFFTArrayIndex(i);
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					for (int k = 0; k < numberOfComponents; k++) {
						double electricFieldComponent = 2.0 * grid.getE(i, j).proj(k) * gainv;
						eFFTdata[j][k][fftIndex] = electricFieldComponent;
						eFFTdata[j][k][fftIndex + 1] = 0.0;
						YMField gaugeFieldAsAlgebraElement = grid.getU(i, j).getLinearizedAlgebraElement();
						double gaugeFieldComponent = 2.0 * gaugeFieldAsAlgebraElement.proj(k) * gainv;
						aFFTdata[j][k][fftIndex] = gaugeFieldComponent;
						aFFTdata[j][k][fftIndex + 1] = 0.0;
					}
				}
			}

			// Compute FTs.
			for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
				for (int k = 0; k < numberOfComponents; k++) {
					fft.complexForward(eFFTdata[j][k]);
					fft.complexForward(aFFTdata[j][k]);
				}
			}

			// Compute occupation numbers
			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				int fftIndex = fft.getFFTArrayIndex(i);
				double eSquared = 0.0;
				double aSquared = 0.0;
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					for (int k = 0; k < numberOfComponents; k++) {
						// Electric part
						eSquared += eFFTdata[j][k][fftIndex] * eFFTdata[j][k][fftIndex]
								+ eFFTdata[j][k][fftIndex + 1] * eFFTdata[j][k][fftIndex + 1];

						// Magnetic part
						aSquared += (aFFTdata[j][k][fftIndex] * aFFTdata[j][k][fftIndex]
								+ aFFTdata[j][k][fftIndex + 1] * aFFTdata[j][k][fftIndex + 1]);
					}
				}

				int[] k = grid.getCellPos(i);
				occupationNumbers[i] = eSquared + this.computeDispersionRelationSquared(k, s.getSimulationBoxSize()) * aSquared;

			}



			// print
			double energy = 0.0;
			for(int i = 0; i < grid.getTotalNumberOfCells() / 2; i++)
			{
				System.out.print(String.format(Locale.ENGLISH, "%.4f", (occupationNumbers[i])));
				System.out.print(", ");
				int[] k = grid.getCellPos(i);
				energy += occupationNumbers[i] * Math.sqrt(Math.abs(this.computeDispersionRelationSquared(k, s.getSimulationBoxSize())));
			}
			System.out.println();
			System.out.println(energy);


		}
	}

	private double computeDispersionRelationSquared(int[] k, double[] boxSize)
	{
		double w2 = 0.0;
		for(int i = 0; i < boxSize.length; i++)
		{
			w2 += 2.0 * (1.0 - Math.cos(2.0 * Math.PI * k[i] / boxSize[i]));
		}
		return w2;
	}
}
