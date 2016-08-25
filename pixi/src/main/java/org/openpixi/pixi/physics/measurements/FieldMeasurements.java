package org.openpixi.pixi.physics.measurements;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.parallel.cellaccess.CellAction;


public class FieldMeasurements {

	private EFieldSquared Esquared;
	private BFieldSquared Bsquared;
	private GaussLaw GaussConstraint;
	private TotalCharge totalCharge;
	private TotalChargeSquared totalChargeSquared;
	/**
	 * Empty constructor for standard usage.
	 */
	public FieldMeasurements() {
		Esquared = new EFieldSquared();
		Bsquared = new BFieldSquared();
		GaussConstraint = new GaussLaw();
		totalCharge = new TotalCharge();
		totalChargeSquared = new TotalChargeSquared();
	}

	public double calculateEsquared(Grid grid) {
		Esquared.reset(grid);
		grid.getCellIterator().execute(grid, Esquared);
        return Esquared.getSum(grid);
	}
	
	public double calculateBsquared(Grid grid) {
		Bsquared.reset(grid);
		grid.getCellIterator().execute(grid, Bsquared);
        return Bsquared.getSum(grid);
	}
	
	public double calculateEsquared(Grid grid, int dir) {
		Esquared.reset(grid);
		grid.getCellIterator().execute(grid, Esquared);
        return Esquared.getSum(grid, dir);
	}
	
	public double calculateBsquared(Grid grid, int dir) {
		Bsquared.reset(grid);
		grid.getCellIterator().execute(grid, Bsquared);
        return Bsquared.getSum(grid, dir);
	}
	
	public double calculateGaussConstraint(Grid grid) {
		GaussConstraint.reset();
		grid.getCellIterator().execute(grid, GaussConstraint);
        return GaussConstraint.getSum(grid);
	}

	public double calculateTotalCharge(Grid grid) {
		totalCharge.reset(grid);
		grid.getCellIterator().execute(grid, totalCharge);
		return totalCharge.getSum(grid);
	}

	public double calculateTotalChargeSquared(Grid grid) {
		totalChargeSquared.reset(grid);
		grid.getCellIterator().execute(grid, totalChargeSquared);
		return totalChargeSquared.getSum(grid);
	}

	private class EFieldSquared implements CellAction {
		private double[] values;
		private double[] unitFactors;

        public void reset(Grid g)
		{
        	values = new double[g.getNumberOfDimensions()];
			unitFactors = new double[g.getNumberOfDimensions()];

			for (int i = 0; i < g.getNumberOfDimensions(); i++) {
				values[i] = 0.0;
				unitFactors[i] = Math.pow(g.getLatticeUnitFactor(i), -2);
			}
		}
        
        public double getSum(Grid grid) {
        	double sum = 0.0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
			}
			return sum;
        }
        
        public double getSum(Grid grid, int dir) {
        	return values[dir];
        }

        public void execute(Grid grid, int index) {
			if(grid.isEvaluatable(index)) {
				int numDir = grid.getNumberOfDimensions();
				double[] res = new double[numDir];
				for (int i = 0; i < numDir; i++) {
					res[i] += grid.getE(index, i).square() * unitFactors[i];
				}

				// Synchronized summation.
				synchronized (this) {
					for (int i = 0; i < numDir; i++) {
						values[i] += res[i];
					}
				}
			}
		}
	}

	private class BFieldSquared implements CellAction {
		private double[] values;
		private double[] unitFactors;

		public void reset(Grid g)
		{
			values = new double[g.getNumberOfDimensions()];
			unitFactors = new double[g.getNumberOfDimensions()];

			for (int i = 0; i < g.getNumberOfDimensions(); i++) {
				values[i] = 0.0;
				unitFactors[i] = Math.pow(g.getLatticeUnitFactor(i), -2);
			}
		}

		public double getSum(Grid grid) {
			double sum = 0.0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
			}
			return sum;
		}

		public double getSum(Grid grid, int dir) {
			return values[dir];
		}

		public void execute(Grid grid, int index) {
			if(grid.isEvaluatable(index)) {
				int numDir = grid.getNumberOfDimensions();
				double[] res = new double[numDir];
				for (int i = 0; i < numDir; i++) {
					res[i] += 0.5 * (grid.getB(index, i, 0).square() + grid.getB(index, i, 1).square()) * unitFactors[i];
				}

				// Synchronized summation.
				synchronized (this) {
					for (int i = 0; i < numDir; i++) {
						values[i] += res[i];
					}
				}
			}
		}
	}

	private class GaussLaw implements CellAction {

		private double sum;

        public void reset() {
        	sum = 0.0;
        }
        
        public double getSum(Grid grid) {
			return sum;
        }
        
        public void execute(Grid grid, int index) {
			if(grid.isEvaluatable(index)) {
				double result = grid.getGaussConstraintSquared(index);
				synchronized (this) {
					sum += result;   // Synchronisierte Summenbildung
				}
			}
		}
	}

	private class TotalCharge implements CellAction  {

		private AlgebraElement charge;

		public void reset(Grid grid) {
			charge = grid.getElementFactory().algebraZero();
		}

		public double getSum(Grid grid) {
			return Math.sqrt(charge.square());
		}

		public void execute(Grid grid, int index) {
			if(grid.isEvaluatable(index)) {
				synchronized (this) {
					charge.addAssign(grid.getRho(index));   // Synchronisierte Summenbildung
				}
			}
		}
	}

	private class TotalChargeSquared implements CellAction  {

		private double charge;

		public void reset(Grid grid) {
			charge = 0.0;
		}

		public double getSum(Grid grid) {
			return Math.sqrt(charge);
		}

		public void execute(Grid grid, int index) {
			if(grid.isEvaluatable(index)) {
				synchronized (this) {
					charge += grid.getRho(index).square();   // Synchronisierte Summenbildung
				}
			}
		}
	}
}