package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This diagnostic implements an algorithm to restore the Gauss constraint. It is taken from arXiv:hep-ph/9603384.
 */
public class GaussConstraintRestoration implements Diagnostics {

	private double timeInterval;
	private int stepInterval;
	private double timeOffset;
	private int stepOffset;
	private double gamma;
	private double absoluteValue;
	private int maxIterations;
	private boolean applyOnlyOnce;
	private boolean alreadyApplied;


	private GroupElement[] gaussViolation;
	private double totalGaussViolation;
	private double oldTotalGaussViolation;

	private Grid oldGrid;

	/**
	 * Creates an instance of the diagnostic.
	 * @param timeInterval      time interval at which the diagnostic should run.
	 * @param timeOffset        time offset for the diagnostic so that it does not run in the first simulation step.
	 * @param gamma             parameter controlling the convergence of the algorithm. Smaller values lead to better convergence but need more iterations.
	 * @param maxIterations     maximum number of iterations before the algorithm stops.
	 * @param absoluteValue     absoluteValue goal for the algorithm. if the absolute value goal is reached the iteration stops.
	 * @param applyOnlyOnce     apply the algorithm only once at time offset.
	 */
	public GaussConstraintRestoration(double timeInterval, double timeOffset, double gamma, int maxIterations, double absoluteValue, boolean applyOnlyOnce) {
		this.timeInterval = timeInterval;
		this.timeOffset = timeOffset;
		this.gamma = gamma;
		this.maxIterations = maxIterations;
		this.absoluteValue = absoluteValue;
		this.applyOnlyOnce = applyOnlyOnce;
		this.alreadyApplied = false;
	}

	public void initialize(Simulation s) {
		this.stepInterval = (int) (timeInterval / s.getTimeStep());
		this.stepOffset = (int) (timeOffset / s.getTimeStep());
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) throws IOException {
		if(!applyOnlyOnce){
			if ((steps - stepOffset) % stepInterval == 0) {
				iterateRestorationAlgorithm(grid);
			}
		} else {
			if(!alreadyApplied) {
				if(steps == stepOffset) {
					iterateRestorationAlgorithm(grid);
					alreadyApplied = true;
				}
			}
		}
	}

	/**
	 * Starts the iteration of the algorithm.
	 * @param grid  reference to the grid
	 */
	public void iterateRestorationAlgorithm(Grid grid) {
		computeGaussViolation(grid);
		oldTotalGaussViolation = totalGaussViolation;
		for(int i = 0 ; i < maxIterations; i++) {

			if(totalGaussViolation < absoluteValue) {
				// The algorithm finished because the desired absolute value goal was reached.
				System.out.println("GaussConstraintRestoration: Reached absolute value at step #" + i);
				break;
			}

			// Create backup of the grid.
			backupGrid(grid);

			applyCorrection(grid, gamma);
			computeGaussViolation(grid);
			double x = oldTotalGaussViolation - totalGaussViolation;
			if(x < 0) {
				// The algorithm finished because the last iteration lead to a worsening of the Gauss law violation.
				restoreGrid(grid);
				System.out.println("GaussConstraintRestoration: Reached instability at step #" + i);
				break;
			}

			oldTotalGaussViolation = totalGaussViolation;

			if(i == maxIterations-1) {
				// The algorithm finished because the maximum number of iterations was reached.
				System.out.println("GaussConstraintRestoration: Reached maximum number of iterations at step #" + i);
			}
		}

	}

	/**
	 * Computes the gauss violation at every point on the grid.
	 * @param grid  reference to the grid
	 */
	public void computeGaussViolation(Grid grid) {
		// Compute Gauss violation at every point and save it to an array.
		int numberOfCells = grid.getTotalNumberOfCells();
		double dt = grid.getTemporalSpacing();
		double a = grid.getLatticeSpacing();
		double factor = 1.0 / (dt * a);
		GroupElement zero = grid.getU(0,0).mult(0.0); // hack to get a zero matrix.
		gaussViolation = new GroupElement[numberOfCells];
		totalGaussViolation = 0.0;
		for(int i = 0; i < numberOfCells; i++) {
			GroupElement C = zero.copy();
			for(int j = 0; j < grid.getNumberOfDimensions(); j++) {
				int k = grid.shift(i, j, -1);
				GroupElement C1 = grid.getUnext(i, j).mult(grid.getU(i, j).adj());
				GroupElement C2 = grid.getU(k, j).adj().mult(grid.getUnext(k, j));
				C = C.add(C1.sub(C2));
			}

			gaussViolation[i] = C.mult(factor);
			totalGaussViolation += gaussViolation[i].proj().sub(grid.getRho(i)).square();
		}
		totalGaussViolation /= numberOfCells;
	}

	/**
	 * Applies the correction according to the violation computed with computeGaussViolation().
	 * @param grid      reference to the grid
	 * @param gamma     parameter controlling the convergence
	 */
	public void applyCorrection(Grid grid, double gamma) {
		int numberOfCells = grid.getTotalNumberOfCells();

		// Apply correction to electric fields
		double factor2 = - 0.5 * grid.getLatticeSpacing() * gamma;
		for(int i = 0; i < numberOfCells; i++) {
			for(int j = 0; j < grid.getNumberOfDimensions(); j++) {
				AlgebraElement E = grid.getE(i, j);
				int k = grid.shift(i, j, 1);
				GroupElement C1 = grid.getU(i, j).mult(gaussViolation[k]).mult(grid.getU(i, j).adj());
				GroupElement C2 = gaussViolation[i];
				grid.setE(i, j, E.add(C1.sub(C2).proj().mult(factor2)));
			}
		}

		// Since the electric fields have changed, the Unext links have to be recalculated.
		grid.updateLinks(grid.getTemporalSpacing());
	}

	/**
	 * Creates a backup of the grid.
	 * @param grid  reference to the grid
	 */
	public void backupGrid(Grid grid) {
		oldGrid = new Grid(grid);
	}

	/**
	 * Restores the backup of the grid.
	 * @param grid  reference to the grid
	 */
	public void restoreGrid(Grid grid) {
		grid.copyValuesFrom(oldGrid);
	}
}
