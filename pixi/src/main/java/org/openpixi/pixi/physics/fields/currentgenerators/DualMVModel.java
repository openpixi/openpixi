package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.diagnostics.FileFunctions;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.math.SU2AlgebraElement;
import org.openpixi.pixi.math.SU2GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DualMVModel implements ICurrentGenerator {

	/**
	 * Direction of movement of the charge density. Values range from 0 to numberOfDimensions-1.
	 */
	private int direction;

	/**
	 * Longitudinal location of the initial charge density in the simulation box.
	 */
	private double location;

	/**
	 * Longitudinal width of the charge density.
	 */
	private double longitudinalWidth;

	/**
	 * \mu parameter of the MV model.
	 */
	private double mu;

	/**
	 * Seed for the random variables.
	 */
	private boolean useSeed = false;
	private int seed1;
	private int seed2;


	/**
	 * Low pass filter for the Poisson solver.
	 */
	private double lowPassCoefficient;

	/**
	 * Initial condition output
	 */
	private String outputFile;
	private boolean createInitialConditionsOutput;

	/**
	 * Option whether to use the \mu^2 (true) or the g^2 \mu^2 (false) normalization for the Gaussian
	 * probability distribution of the color charge densities.
	 */
	private boolean useAlternativeNormalization;

	private MVModel mv1;
	private MVModel mv2;


	public DualMVModel(int direction, double location, double longitudinalWidth, double mu, double lowPassCoefficient,
					   boolean useSeed, int seed1, int seed2, boolean createInitialConditionsOutput, String outputFile,
					   boolean useAlternativeNormalization){
		this.direction = direction;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.mu = mu;
		this.lowPassCoefficient = lowPassCoefficient;

		this.useSeed = useSeed;
		this.seed1 = seed1;
		this.seed2 = seed2;

		this.createInitialConditionsOutput = createInitialConditionsOutput;
		this.outputFile = outputFile;

		this.useAlternativeNormalization = useAlternativeNormalization;
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		if(useSeed){
			mv1 = new MVModel(direction, 1, location, longitudinalWidth, mu, seed1, lowPassCoefficient, useAlternativeNormalization);
			mv2 = new MVModel(direction, -1, -(location+1), longitudinalWidth, mu, seed2, lowPassCoefficient, useAlternativeNormalization);
		} else {
			mv1 = new MVModel(direction, 1, location, longitudinalWidth, mu, lowPassCoefficient, useAlternativeNormalization);
			mv2 = new MVModel(direction, -1, -(location+1), longitudinalWidth, mu, lowPassCoefficient, useAlternativeNormalization);
		}

		mv1.initializeCurrent(s, totalInstances);
		mv2.initializeCurrent(s, totalInstances);

		double g = s.getCouplingConstant();

		// Compute boost-invariant initial conditions. Note: this only works for SU2.
		if(createInitialConditionsOutput && s.getNumberOfColors() == 2) {

			double normalizationFactor = 1.0 / s.getCouplingConstant() * s.grid.getLatticeSpacing();
			int[] transNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
			int transverseNumberOfCells = GridFunctions.getTotalNumberOfCells(transNumCells);
			int effDimensions = GridFunctions.getEffectiveNumberOfDimensions(s.grid.getNumCells());

			GroupElement zero = (SU2GroupElement) s.grid.getElementFactory().groupZero(2);
			GroupElement identity = (SU2GroupElement) s.grid.getElementFactory().groupIdentity(2);

			// Transverse links
			SU2GroupElement[][] transverseLinks = new SU2GroupElement[effDimensions - 1][transverseNumberOfCells];
			SU2AlgebraElement[][] transverseFields = new SU2AlgebraElement[effDimensions - 1][transverseNumberOfCells];

			// Longitudinal E-Fields
			SU2AlgebraElement[] longitudinalFields = new SU2AlgebraElement[transverseNumberOfCells];

			NewLCPoissonSolver ps1 = mv1.particleLCCurrent.poissonSolver;
			NewLCPoissonSolver ps2 = mv2.particleLCCurrent.poissonSolver;

			// Compute transverse link elements
			for (int i = 0; i < transverseNumberOfCells; i++) {
				int ts = 0;
				for (int j = 0; j < effDimensions; j++) {
					if(j != direction) {
						// Compute initial condition for transverse fields
						int is = GridFunctions.shift(i, ts, 1, transNumCells);
						SU2GroupElement U1 = (SU2GroupElement) ps1.getV(i).mult(ps1.getV(is).adj());
						SU2GroupElement U2 = (SU2GroupElement) ps2.getV(i).mult(ps2.getV(is).adj());

						SU2GroupElement sum = (SU2GroupElement) U1.add(U2);
						SU2GroupElement sumInv = ((SU2GroupElement) sum.adj()).inv();

						transverseLinks[ts][i] = (SU2GroupElement) sum.mult(sumInv);
						transverseFields[ts][i] = (SU2AlgebraElement) transverseLinks[ts][i].getAlgebraElement().mult(normalizationFactor);
						ts++;
					}
				}
			}

			// Compute longitudinal fields
			for (int i = 0; i < transverseNumberOfCells; i++) {
				SU2GroupElement temp = (SU2GroupElement) zero.copy();
				//SU2AlgebraElement temp2 = (SU2AlgebraElement) s.grid.getElementFactory().algebraZero(2);

				int ts = 0;
				for (int j = 0; j < effDimensions; j++) {
					if(j != direction) {
						int is = GridFunctions.shift(i, ts, -1, transNumCells);
						SU2GroupElement Um1 = (SU2GroupElement) transverseLinks[ts][i].adj().sub(identity);
						SU2GroupElement diff1 = (SU2GroupElement) ps1.getU(i, ts).sub(ps2.getU(i, ts));
						SU2GroupElement Um2 = (SU2GroupElement) transverseLinks[ts][is].adj().sub(identity);
						SU2GroupElement diff2 = (SU2GroupElement) ps1.getU(is, ts).sub(ps2.getU(is, ts));
						temp.addAssign(diff1.mult(Um1).sub(Um2.mult(diff2)));

						ts++;
					}
				}

				longitudinalFields[i] = (SU2AlgebraElement) temp.proj().mult(normalizationFactor / (2.0 * g));
			}

			// File output ((d-1)x3 transversal gauge field components, 1x3 longitudinal electric field component)
			FileFunctions.clearFile("output/" + outputFile);
			File file = FileFunctions.getFile("output/" + outputFile);
			try {
				FileWriter pw = new FileWriter(file, true);

				// Transverse fields
				for (int i = 0; i < effDimensions-1; i++) {
					double[][] output = convertToDoubleArray(transverseFields[i]);
					pw.write(generateTSVString(output[0]) + "\n");
					pw.write(generateTSVString(output[1]) + "\n");
					pw.write(generateTSVString(output[2]) + "\n");
				}

				// Longitudinal fields
				double[][] output = convertToDoubleArray(longitudinalFields);
				pw.write(generateTSVString(output[0]) + "\n");
				pw.write(generateTSVString(output[1]) + "\n");
				pw.write(generateTSVString(output[2]) + "\n");

				pw.close();
			} catch (IOException ex) {
				System.out.println("DualMVModel: Error writing to file.");
			}

		}
	}

	public void applyCurrent(Simulation s) {
		mv1.applyCurrent(s);
		mv2.applyCurrent(s);
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

	private double[][] convertToDoubleArray(AlgebraElement[] array) {
		double[][] output = new double[3][array.length];
		for (int i = 0; i < array.length; i++) {
			output[0][i] = array[i].get(0);
			output[1][i] = array[i].get(1);
			output[2][i] = array[i].get(2);
		}
		return output;
	}

}
