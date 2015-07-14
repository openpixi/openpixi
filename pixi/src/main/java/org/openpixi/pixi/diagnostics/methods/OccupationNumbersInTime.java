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
import java.util.List;
import java.util.Locale;

public class OccupationNumbersInTime implements Diagnostics {

	private Simulation s;
	public double timeInterval;
	private int stepInterval;
	private String outputType;
	private String outputFileName;
	private boolean colorful;

	/*
		Supported output types.
	 */
	private static final String OUTPUT_CSV = "csv";
	private static final String OUTPUT_CSV_WITH_VECTORS = "csv_with_vectors";
	private static final String OUTPUT_CSV_ONLY_ENERGY = "csv_only_energy";
	private static final String OUTPUT_NONE = "none";
	private String[] supportedOutputTypes = {OUTPUT_CSV, OUTPUT_CSV_ONLY_ENERGY,OUTPUT_CSV_WITH_VECTORS, OUTPUT_NONE};

	private DoubleFFTWrapper fft;
	public double[][] occupationNumbers;
	public double	energyDensity;

	private int computationCounter;
	private int numberOfComponents;
	private int effectiveNumberOfDimensions;
	private double simulationBoxVolume;

	/**
	 * Constructor for the occupation numbers diagnostic.
	 *
	 * @param timeInterval	simulation time between measurements
	 * @param outputType	type of output (e.g. csv files)
	 * @param filename		filename for output
	 * @param colorful		true/false if output should distinguish between color components or sum over them.
	 */
	public OccupationNumbersInTime(double timeInterval, String outputType, String filename, boolean colorful) {
		this.timeInterval = timeInterval;
		this.computationCounter = 0;
		this.outputType = outputType;
		this.outputFileName = filename;
		this.colorful = colorful;

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
		this.numberOfComponents = s.getNumberOfColors() * s.getNumberOfColors() - 1;

		occupationNumbers = new double[s.grid.getTotalNumberOfCells()][numberOfComponents];
		effectiveNumberOfDimensions = getEffectiveNumberOfDimensions(s.grid.getNumCells());

		simulationBoxVolume = 1.0;
		for(int i = 0; i < s.getNumberOfDimensions(); i++) {
			if(s.grid.getNumCells(i) > 1) {
				simulationBoxVolume *= s.getSimulationBoxSize(i);
			}
		}

		if(outputType.equals(OUTPUT_CSV_WITH_VECTORS)) {
			this.writeMomentumVectors(outputFileName);
		}

	}

	/**
	 * Computes the occupation numbers in momentum space and field energy from the occupation numbers.
	 *
	 * @param grid_reference	Reference to the Grid instance.
	 * @param particles    		Reference to the list of particles.
	 * @param steps        		Total simulation steps so far.
	 */
	public void calculate(Grid grid_reference, ArrayList<IParticle> particles, int steps) {
		if (steps % stepInterval == 0) {
			// Apply Coulomb gauge.
			Grid grid = new Grid(grid_reference);	// Copy grid.
			CoulombGauge coulombGauge = new CoulombGauge(grid);
			coulombGauge.applyGaugeTransformation(grid);


			// Fill arrays for FFT.
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
			//
			double fftConversationFactorSquared = Math.pow(s.grid.getLatticeSpacing(), 2* effectiveNumberOfDimensions);

			// Compute occupation numbers and averaged energy density
			energyDensity = 0.0;
			for(int k = 0; k < this.numberOfComponents; k++) {
				for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
					int fftIndex = fft.getFFTArrayIndex(i);
					double eSquared = 0.0;
					double aSquared = 0.0;
					double mixed = 0.0;
					for (int j = 0; j < grid.getNumberOfDimensions(); j++) {
						// Electric part
						eSquared += eFFTdata[j][k][fftIndex] * eFFTdata[j][k][fftIndex]
								+ eFFTdata[j][k][fftIndex + 1] * eFFTdata[j][k][fftIndex + 1];

						// Magnetic part
						aSquared += (aFFTdata[j][k][fftIndex] * aFFTdata[j][k][fftIndex]
								+ aFFTdata[j][k][fftIndex + 1] * aFFTdata[j][k][fftIndex + 1]);

						// Mixed part
						mixed -= 2.0 * (-aFFTdata[j][k][fftIndex + 1] * eFFTdata[j][k][fftIndex]
								+ aFFTdata[j][k][fftIndex] * eFFTdata[j][k][fftIndex + 1]);
					}

					double[] kvec = computeMomentumVectorFromLatticeIndex(i);
					double w = Math.sqrt(this.computeDispersionRelationSquared(kvec));
					occupationNumbers[i][k] = (eSquared + w * w * aSquared + w * mixed) * fftConversationFactorSquared;
					energyDensity += occupationNumbers[i][k];
				}
			}
			// This factor is needed for the energy. Check the CPIC notes if in doubt.
			double normalizationConstant = 1.0 / (2.0 * simulationBoxVolume * simulationBoxVolume);
			energyDensity *= normalizationConstant;

			computationCounter++;

			// Generate output (write to file, terminal, etc..)
			if(this.outputType.equals(OUTPUT_CSV)) {
				this.writeCSVFile(this.outputFileName, true);
			}

			if(this.outputType.equals(OUTPUT_CSV_WITH_VECTORS)) {
				this.writeCSVFile(this.outputFileName, true);
			}

			if(this.outputType.equals(OUTPUT_CSV_ONLY_ENERGY)) {
				this.writeCSVFile(this.outputFileName, false);
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
			w2 += 2.0 * (1.0 - Math.cos(k[i] * a)) / (a * a);
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
			if(delta < 0.5)
			{
				k[i] = 2.0 * delta * Math.PI / s.grid.getLatticeSpacing();
			} else {
				k[i] = 2.0 * (delta - 1.0) * Math.PI / s.grid.getLatticeSpacing();
			}
		}
		return k;
	}

	/**
	 * Writes the momentum vectors in order of the cell indices to the header of a file.
	 * @param path	Path to the output file
	 */
	private void writeMomentumVectors(String path) {
		File file = this.getOutputFile(path);
		try {
			FileWriter pw = new FileWriter(file, true);
			for(int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				double[] k = this.computeMomentumVectorFromLatticeIndex(i);
				String kString = "";
				for (int j = 0; j < s.getNumberOfDimensions(); j++) {
					kString += k[j];
					if(j < s.getNumberOfDimensions()-1) {
						kString += ", ";
					}
				}
				pw.write(kString);
				if(i < s.grid.getTotalNumberOfCells()-1) {
					pw.write(", ");
				}
			}
			pw.close();
		} catch (IOException ex) {
			System.out.println("OccupationNumbersInTime: Error writing to file.");
		}
	}

	/**
	 * Writes the output to a CSV formatted file. The output contains the measurement times, the energy computed
	 * from the occupation numbers and the occupation numbers as a 1D array.
	 *
	 * The format is as follows: Each time the diagnostic is called two lines are appended to the output file.
	 * 	Time, Energy
	 * 	n0_0, n1_0, n2_0, ....
	 * 	n0_1, n1_1, n2_1, ....
	 * where nk_c defines the occupation number of momentum k with color component c.
	 *
	 * @param path	Path to the output file
	 */
	private void writeCSVFile(String path, boolean includeOccupationNumbers)
	{
		File file = this.getOutputFile(path);
		try {
			FileWriter pw = new FileWriter(file, true);
			pw.write((computationCounter * timeInterval) + ", " + energyDensity + "\n");
			if(includeOccupationNumbers) {
				pw.write(this.generateCSVString());
			}
			pw.close();
		} catch (IOException ex) {
			System.out.println("OccupationNumbersInTime: Error writing to file.");
		}

	}

	/**
	 * Generates a CSV formatted String based on the computed occupation numbers and the energy.
	 * The format is as follows:
	 * 	Time, Energy
	 * 	n0_0, n1_0, n2_0, ....
	 * 	n0_1, n1_1, n2_1, ....
	 * where nk_c defines the occupation number of momentum k with color component c.
	 *
	 * @return	a csv formatted string containing time, energy and the occupation numbers.
	 */
	private String generateCSVString() {
		String outputString = "";
		if(colorful) {
			for (int k = 0; k < numberOfComponents; k++) {
				for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
					outputString += occupationNumbers[i][k];
					if (i < s.grid.getTotalNumberOfCells() - 1) {
						outputString += ", ";
					}
				}
				outputString += "\n";
			}
		} else {
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++)
			{
				double value = 0.0;
				for(int k = 0; k < numberOfComponents; k++)
				{
					value += occupationNumbers[i][k];
				}
				outputString += value;
				if (i < s.grid.getTotalNumberOfCells() - 1) {
					outputString += ", ";
				}
			}
			outputString += "\n";
		}
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

	/**
	 * Return a list of dimensions whose size > 1.
	 *
	 * @param dimensions
	 * @return
	 */
	private int getEffectiveNumberOfDimensions(int[] dimensions) {
		int effectiveNumberOfDimensions = 0;
		for (int d : dimensions) {
			if (d > 1) {
				effectiveNumberOfDimensions += 1;
			} else if (d < 1) {
				System.out.println("OccupationNumbersInTime: Dimension < 1 along a direction is not allowed");
			}
		}
		return effectiveNumberOfDimensions;
	}
}
