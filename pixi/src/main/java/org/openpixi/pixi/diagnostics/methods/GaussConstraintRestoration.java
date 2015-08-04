package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.IOException;
import java.util.ArrayList;

public class GaussConstraintRestoration implements Diagnostics {

	private double timeInterval;
	private int stepInterval;
	private double timeOffset;
	private int stepOffset;
	private double gamma = 0.5;
	private double accuracy = 10e-4;
	private int maxIterations = 100;


	private LinkMatrix[] gaussViolation;
	private double totalGaussViolation;
	private double oldTotalGaussViolation;

	public GaussConstraintRestoration(double timeInterval, double timeOffset, double gamma, int maxIterations, double accuracy) {
		this.timeInterval = timeInterval;
		this.timeOffset = timeOffset;
		this.gamma = gamma;
		this.maxIterations = maxIterations;
		this.accuracy = accuracy;
	}

	public void initialize(Simulation s) {
		this.stepInterval = (int) (timeInterval / s.getTimeStep());
		this.stepOffset = (int) (timeOffset / s.getTimeStep());
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if ((steps - stepOffset) % stepInterval == 0) {
			iterateRestorationAlgorithm(grid);
		}
	}

	public void iterateRestorationAlgorithm(Grid grid) {
		computeGaussViolation(grid);
		oldTotalGaussViolation = totalGaussViolation;
		for(int i = 0 ; i < maxIterations; i++) {
			applyCorrection(grid, gamma);
			computeGaussViolation(grid);
			double x = (oldTotalGaussViolation - totalGaussViolation) / oldTotalGaussViolation;
			if(Math.abs(x) < accuracy) {
				System.out.println("GaussConstraintRestoration: Reached accuracy goal at step #" + i);
				break;
			}
			if(x < 0) {
				System.out.println("GaussConstraintRestoration: Reached instability at step #" + i);
				break;
			}
			oldTotalGaussViolation = totalGaussViolation;
		}
	}

	public void computeGaussViolation(Grid grid) {
		// Compute Gauss violation at every point and save it to an array.
		int numberOfCells = grid.getTotalNumberOfCells();
		double dt = grid.getTemporalSpacing();
		double a = grid.getLatticeSpacing();
		double factor = 1.0 / (dt * a);
		LinkMatrix zero = grid.getU(0,0).mult(0.0); // hack to get a zero matrix.
		gaussViolation = new LinkMatrix[numberOfCells];
		totalGaussViolation = 0.0;
		for(int i = 0; i < numberOfCells; i++) {
			LinkMatrix C = zero.mult(1.0); // hack to get a copy of a matrix.
			for(int j = 0; j < grid.getNumberOfDimensions(); j++) {
				int k = grid.shift(i, j, -1);
				LinkMatrix C1 = grid.getUnext(i, j).mult(grid.getU(i, j).adj());
				LinkMatrix C2 = grid.getU(k, j).adj().mult(grid.getUnext(k, j));
				C = C.add(C1.sub(C2));
			}

			gaussViolation[i] = C.mult(factor);
			totalGaussViolation += gaussViolation[i].proj().sub(grid.getRho(i)).square();
		}
	}

	public void applyCorrection(Grid grid, double gamma) {
		int numberOfCells = grid.getTotalNumberOfCells();

		// Apply correction to electric fields
		double factor2 = - 1.0 * grid.getLatticeSpacing() * gamma;
		for(int i = 0; i < numberOfCells; i++) {
			for(int j = 0; j < grid.getNumberOfDimensions(); j++) {
				YMField E = grid.getE(i, j);
				int k = grid.shift(i, j, 1);
				LinkMatrix C1 = grid.getU(i, j).mult(gaussViolation[k]).mult(grid.getU(i, j).adj());
				LinkMatrix C2 = gaussViolation[i];
				grid.setE(i, j, E.add(C1.sub(C2).proj().mult(factor2)));
			}
		}

		// Since the electric fields have changed, the Unext links have to be recalculated.
		grid.updateLinks(grid.getTemporalSpacing());
	}
}
