package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
	private boolean useMirroredGrid;
	private int mirroredDirection;

	private String separator = ", ";
	private String linebreak = "\n";

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


		if(!outputType.equals(OUTPUT_NONE)) {
			FileFunctions.clearFile("output/" + outputFileName);
		}

		if (!Arrays.asList(supportedOutputTypes).contains(outputType)) {
			System.out.print("OccupationNumbersInTime: unsupported output type. Allowed types are ");
			for(String t : supportedOutputTypes) {
				System.out.print(t + " ");
			}
			System.out.println();
		}
	}

	public OccupationNumbersInTime(double timeInterval, String outputType, String filename, boolean colorful, int mirroredDirection) {
		this(timeInterval, outputType, filename, colorful);

		this.useMirroredGrid = true;
		this.mirroredDirection = mirroredDirection;
	}

	public void initialize(Simulation s) {
		this.s = s;
		this.stepInterval = (int) (this.timeInterval / s.getTimeStep());
		this.numberOfComponents = s.getNumberOfColors() * s.getNumberOfColors() - 1;
		effectiveNumberOfDimensions = getEffectiveNumberOfDimensions(s.grid.getNumCells());

		simulationBoxVolume = 1.0;
		for(int i = 0; i < s.getNumberOfDimensions(); i++) {
			if(s.grid.getNumCells(i) > 1) {
				simulationBoxVolume *= s.getSimulationBoxSize(i);
			}
		}

		if(useMirroredGrid) {
			int[] numCells = s.grid.getNumCells().clone();
			numCells[mirroredDirection] *= 2;
			this.fft = new DoubleFFTWrapper(numCells);
			occupationNumbers = new double[2 * s.grid.getTotalNumberOfCells()][numberOfComponents];
			simulationBoxVolume *= 2;
		} else {
			this.fft = new DoubleFFTWrapper(s.grid.getNumCells());
			occupationNumbers = new double[s.grid.getTotalNumberOfCells()][numberOfComponents];
		}




		// Write header
		if(!outputType.equals(OUTPUT_NONE)) {
			this.writeHeader(outputFileName);
		}

		// Include lattice momentum vectors (optional)
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
			Grid grid;
			if(useMirroredGrid) {
				grid = new MirroredGrid(grid_reference, mirroredDirection);
			} else {
				grid = new Grid(grid_reference);	// Copy grid.
			}
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
						double electricFieldComponent = grid.getE(i, j).get(k) * gainv;
						eFFTdata[j][k][fftIndex] = electricFieldComponent;
						eFFTdata[j][k][fftIndex + 1] = 0.0;

						// Gauge fields need to be averaged over two time-steps.
						AlgebraElement gaugeFieldAsAlgebraElement0 = grid.getU(i, j).getAlgebraElement();
						AlgebraElement gaugeFieldAsAlgebraElement1 = grid.getUnext(i, j).getAlgebraElement();
						double gaugeFieldComponent0 = gaugeFieldAsAlgebraElement0.get(k) * gainv;
						double gaugeFieldComponent1 = gaugeFieldAsAlgebraElement1.get(k) * gainv;
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


			computationCounter++;
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
	 * Writes the basic header of the csv file. The header includes the size of the grid.
	 *
	 * @param path	Path to output file
	 */
	public void writeHeader(String path) {
		File file = FileFunctions.getFile("output/" + path);

		try {
			FileWriter pw = new FileWriter(file, true);
			for (int i = 0; i < s.getNumberOfDimensions(); i++) {
				pw.write(String.valueOf(s.grid.getNumCells(i)));
				if(i < s.getNumberOfDimensions() -1) {
					pw.write(separator);
				}
			}
			pw.write(linebreak);
			pw.close();

		} catch (IOException ex) {
			System.out.println("OccupationNumbersInTime: Error writing to file.");
		}
	}

	/**
	 * Writes the momentum vectors in order of the cell indices to the header of a file.
	 * In 3D the output is as follows:
	 * k0_x, k0_y, k0_z, k1_x, k1_y, k1_y, k2_x, k2_y, k2_z, ...
	 *
	 * The three numbers (kn_x, kn_y, kn_z) define the momentum vector associated with the index n.
	 *
	 * @param path	Path to the output file
	 */
	private void writeMomentumVectors(String path) {
		File file = FileFunctions.getFile("output/" + path);
		try {
			FileWriter pw = new FileWriter(file, true);
			for(int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				double[] k = this.computeMomentumVectorFromLatticeIndex(i);
				String kString = "";
				for (int j = 0; j < s.getNumberOfDimensions(); j++) {
					kString += k[j];
					if(j < s.getNumberOfDimensions()-1) {
						kString += separator;
					}
				}
				pw.write(kString);
				if(i < s.grid.getTotalNumberOfCells()-1) {
					pw.write(separator);
				}
			}
			pw.write(linebreak);
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
	 * where nk_c defines the occupation number of momentum k (in terms of grid indices) with color component c.
	 *
	 * @param path	Path to the output file
	 */
	private void writeCSVFile(String path, boolean includeOccupationNumbers)
	{
		File file = FileFunctions.getFile("output/" + path);
		try {
			FileWriter pw = new FileWriter(file, true);
			pw.write(this.generateCSVString(includeOccupationNumbers));
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
	private String generateCSVString(boolean includeOccupationNumbers) {
		StringBuilder outputStringBuilder = new StringBuilder();
		outputStringBuilder.append((computationCounter * timeInterval) + separator + energyDensity + linebreak);
		if(includeOccupationNumbers) {
			if (colorful) {
				for (int k = 0; k < numberOfComponents; k++) {
					for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
						outputStringBuilder.append(occupationNumbers[i][k]);
						if (i < s.grid.getTotalNumberOfCells() - 1) {
							outputStringBuilder.append(separator);
						}
					}
					outputStringBuilder.append(linebreak);
				}
			} else {
				for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
					double value = 0.0;
					for (int k = 0; k < numberOfComponents; k++) {
						value += occupationNumbers[i][k];
					}
					outputStringBuilder.append(value);
					if (i < s.grid.getTotalNumberOfCells() - 1) {
						outputStringBuilder.append(separator);
					}
				}
				outputStringBuilder.append(linebreak);
			}
		}
		return outputStringBuilder.toString();
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

	private class MirroredGrid extends Grid {
		public MirroredGrid(Grid grid, int mirroredDirection) {
			super(grid);
			this.numCells[mirroredDirection] *= 2;
			createGrid();
			this.cellIterator.setNormalMode(numCells);

			// Copy and mirror cells.
			for (int i = 0; i < grid.getTotalNumberOfCells(); i++) {
				int[] cellPos = grid.getCellPos(i);
				int newGridIndex = this.getCellIndex(cellPos);

				int[] newMirroredGridPos = cellPos.clone();
				newMirroredGridPos[mirroredDirection] = 2 * grid.getNumCells(mirroredDirection) - cellPos[mirroredDirection] - 1;
				int mirroredIndex = this.getCellIndex(newMirroredGridPos);

				cells[newGridIndex] = grid.getCell(i).copy();
				cells[mirroredIndex] = grid.getCell(i).copy();

				// Switch gauge links in the mirrored direction.
				// TODO: This shift of the gauge links is a bit ambiguous. Not sure what would be the correct way to do it.
				if(cellPos[mirroredDirection] > 0) {
					cellPos[mirroredDirection]--;
				}
				int shiftedIndex = grid.getCellIndex(cellPos);
				cells[mirroredIndex].setU(mirroredDirection, grid.getU(shiftedIndex, mirroredDirection).copy());
			}



		}
	}
}
