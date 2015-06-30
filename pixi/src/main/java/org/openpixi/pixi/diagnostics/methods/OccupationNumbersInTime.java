package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class OccupationNumbersInTime implements Diagnostics {

	private Simulation s;
	private double timeInterval;
	private int stepInterval;
	private String outputType;
	private String outputFileName;

	/*
		Supported output types.
	 */
	private static final String OUTPUT_CSV = "csv";

	private String[] supportedOutputTypes = {OUTPUT_CSV};

	private DoubleFFTWrapper fft;
	public double[] occupationNumbers;
	public double	energy;

	private int computationCounter;

	public OccupationNumbersInTime(double timeInterval, String outputType, String filename) {
		this.timeInterval = timeInterval;
		this.computationCounter = 0;
		this.outputType = outputType;
		this.outputFileName = filename;

		this.clear(filename);

		if (!Arrays.asList(supportedOutputTypes).contains(outputType)) {
			System.out.print("OccupationNumbersInTime: unsupported output type. Allowed types are ");
			for(String t : supportedOutputTypes) {
				System.out.print(t + " ");
			}
			System.out.println();
		}
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

			// Fill arrays for FFT.
			int numberOfComponents = grid.getNumberOfColors() * grid.getNumberOfColors() - 1;
			double gainv = 1.0 / (grid.getLatticeSpacing() * grid.getGaugeCoupling());
			double[][][] eFFTdata = new double[grid.getNumberOfDimensions()][numberOfComponents][fft.getFFTArraySize()];
			double[][][] aFFTdata = new double[grid.getNumberOfDimensions()][numberOfComponents][fft.getFFTArraySize()];
			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				int fftIndex = fft.getFFTArrayIndex(i);
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					for (int k = 0; k < numberOfComponents; k++) {
						// Electric field
						double electricFieldComponent = 2.0 * grid.getE(i, j).proj(k) * gainv;
						eFFTdata[j][k][fftIndex] = electricFieldComponent;
						eFFTdata[j][k][fftIndex + 1] = 0.0;

						// Gauge fields need to be averaged over two time-steps.
						YMField gaugeFieldAsAlgebraElement0 = grid.getU(i, j).getLinearizedAlgebraElement();
						YMField gaugeFieldAsAlgebraElement1 = grid.getUnext(i, j).getLinearizedAlgebraElement();
						double gaugeFieldComponent0 = 2.0 * gaugeFieldAsAlgebraElement0.proj(k) * gainv;
						double gaugeFieldComponent1 = 2.0 * gaugeFieldAsAlgebraElement1.proj(k) * gainv;
						aFFTdata[j][k][fftIndex] = 0.5 * (gaugeFieldComponent0 + gaugeFieldComponent1);
						aFFTdata[j][k][fftIndex + 1] = 0.0;
					}
				}
			}

			// Compute FTs of electric field and gauge field.
			for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
				for (int k = 0; k < numberOfComponents; k++) {
					fft.complexForward(eFFTdata[j][k]);
					fft.complexForward(aFFTdata[j][k]);
				}
			}

			// Compute occupation numbers and energy
			double volume = 1.0;
			for(int i = 0; i < s.getNumberOfDimensions(); i++) {
				volume *= s.getSimulationBoxSize(i);
			}
			double prefactor = 1.0 / (2.0 * Math.pow(2.0 * Math.PI, 3));
			occupationNumbers[0] = 0.0; // Zero modes of the electric field have divergent occupation number.
			energy = 0.0;
			for (int i = 1; i < grid.getTotalNumberOfCells(); i++) {
				int fftIndex = fft.getFFTArrayIndex(i);
				double eSquared = 0.0;
				double aSquared = 0.0;
				double mixed = 0.0;
				for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
					for (int k = 0; k < numberOfComponents; k++) {
						// Electric part
						eSquared += eFFTdata[j][k][fftIndex] * eFFTdata[j][k][fftIndex]
								+ eFFTdata[j][k][fftIndex + 1] * eFFTdata[j][k][fftIndex + 1];

						// Magnetic part
						aSquared += (aFFTdata[j][k][fftIndex] * aFFTdata[j][k][fftIndex]
								+ aFFTdata[j][k][fftIndex + 1] * aFFTdata[j][k][fftIndex + 1]);

						// Mixed part
						mixed -= 2.0 * (- aFFTdata[j][k][fftIndex+1] * eFFTdata[j][k][fftIndex]
								+ aFFTdata[j][k][fftIndex] * eFFTdata[j][k][fftIndex + 1]);
					}
				}

				double[] k = computeMomentumVectorFromLatticeIndex(i);
				double w = Math.sqrt(this.computeDispersionRelationSquared(k));
				occupationNumbers[i] = prefactor * (eSquared +  Math.pow(w, 2.0) * aSquared + w * mixed);
				energy += occupationNumbers[i];
			}

			/* The summation above sums over the whole spectrum found by the FFT. Since this spectrum contains two times
			the actual spectrum we have to divide the energy by a factor of two. This is fishy will almost certainly not
			work for more than one dimension. */
			//energy *= 0.5;

			computationCounter++;

			// Generate output (write to file, terminal, etc..)
			if(this.outputType == OUTPUT_CSV) {
				this.writeCSVFile(this.outputFileName);
			}

		}
	}

	/**
	 * Computes the lattice dispersion relation for a momentum vector k assuming abelian plane waves.
	 *
	 * @param k			momentum vector
	 * @return			energy of the mode
	 */
	private double computeDispersionRelationSquared(double[] k)
	{
		double w2 = 0.0;
		double a = s.grid.getLatticeSpacing();
		for(int i = 0; i < s.getNumberOfDimensions(); i++)
		{
			w2 += 2.0 * (1.0 - Math.cos(k[i] * a) / (a * a));
		}
		return w2;
	}

	/**
	 * Computes the continuum dispersion relation for a momentum vector k assuming abelian plane waves.
	 *
	 * @param k			momentum vector
	 * @return			energy of the mode
	 */
	private double computeContinuumDispersionRelationSquared(double[] k)
	{
		double w2 = 0.0;
		for(int i = 0; i < s.getNumberOfDimensions(); i++)
		{
			w2 += k[i] * k[i];
		}
		return w2;
	}

	/**
	 * Auxiliary function to convert lattice indices in the FFT spectra to physical momenta.
	 *
	 * @param index	Lattice index in the spectrum
	 * @return		Momentum vector associated with the index
	 */
	private double[] computeMomentumVectorFromLatticeIndex(int index)
	{
		int[] coordinate = s.grid.getCellPos(index);
		/* In one dimension: smallest possible value of the coordinate is 0. This should be mapped to zero momentum.
		In the middle of the Fourier spectrum we find the Nyquist mode. This should be mapped to the maximum momentum
		Pi / gridStep. The second half of the spectrum contains the negative k vectors beginning at the minimum momentum
		vector -Pi / gridStep and ending at the negative zero vector.
		 */

		double[] k = new double[s.getNumberOfDimensions()];
		for(int i = 0; i < s.getNumberOfDimensions(); i++)
		{
			double delta = coordinate[i] / ((double) s.grid.getNumCells(i));
			if(coordinate[i] < s.grid.getNumCells(i) / 2)
			{
				k[i] = 2.0 * delta * Math.PI / s.grid.getLatticeSpacing();
			} else {
				k[i] = 2.0 * (delta - 1.0) * Math.PI / s.grid.getLatticeSpacing();
			}
		}
		return k;
	}

	/**
	 * Writes the output to a CSV formatted file. The output contains the measurement times, the energy computed
	 * from the occupation numbers and the occupation numbers as a 1D array.
	 *
	 * The format is as follows: Each time the diagnostic is called two lines are appended to the output file.
	 * 	Time, Energy
	 * 	n0, n1, n2, ....
	 *
	 * @param path	Path to the output file
	 */
	private void writeCSVFile(String path)
	{
		File file = this.getOutputFile(path);
		try {
			FileWriter pw = new FileWriter(file, true);
			pw.write((computationCounter * timeInterval) + ", " + energy + "\n");
			pw.write(this.generateCSVString());
			pw.close();
		} catch (IOException ex) {
			System.out.println("OccupationNumbersInTime: Error writing to file.");
		}

	}

	/**
	 * Generates a CSV formatted String based on the computed occupation numbers and the energy.
	 * The format is as follows:
	 * 	Time, Energy
	 * 	n0, n1, n2, ....
	 *
	 * @return	a csv formatted string containing time, energy and the occupation numbers.
	 */
	private String generateCSVString() {
		String outputString = "";
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++)
		{
			outputString += occupationNumbers[i];
			if(i < s.grid.getTotalNumberOfCells() - 1) {
				outputString += ", ";
			}
		}
		outputString += "\n";
		return outputString;
	}

	/**
	 * 	Checks if the files are already existent and deletes them
	 */
	public void clear(String path) {
		File file = getOutputFile(path);
		if(file.exists()) {
			file.delete();
		}
	}

	/**
	 * Creates a file with a given name in the output folder.
	 * @param filename	Filename of the output file.
	 * @return			File object of the opened file.
	 */
	private File getOutputFile(String filename) {
		// Default output path is
		// 'output/' + filename
		File fullpath = new File("output");
		if(!fullpath.exists()) fullpath.mkdir();

		return new File(fullpath, filename);
	}
}
