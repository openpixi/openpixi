package org.openpixi.pixi.physics.measurements;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.util.GridFunctions;


public class FieldMeasurements {
	
	EFieldSquared Esquared = new EFieldSquared();
	BFieldSquared Bsquared = new BFieldSquared();
	GaussLaw GaussConstraint = new GaussLaw();
	TotalCharge totalCharge = new TotalCharge();

	public double calculateEsquared(Grid grid) {
		Esquared.reset();
		grid.getCellIterator().execute(grid, Esquared);
        return Esquared.getSum();
	}
	
	public double calculateBsquared(Grid grid) {
		Bsquared.reset();
		grid.getCellIterator().execute(grid, Bsquared);
        return Bsquared.getSum();
	}
	
	public double calculateEsquared(Grid grid, int dir) {
		Esquared.reset();
		grid.getCellIterator().execute(grid, Esquared);
        return Esquared.getSum(dir);
	}
	
	public double calculateBsquared(Grid grid, int dir) {
		Bsquared.reset();
		grid.getCellIterator().execute(grid, Bsquared);
        return Bsquared.getSum(dir);
	}
	
	public double calculateGaussConstraint(Grid grid) {
		GaussConstraint.reset();
		grid.getCellIterator().execute(grid, GaussConstraint);
        return GaussConstraint.getSum();
	}

	public double calculateTotalCharge(Grid grid) {
		totalCharge.reset(grid);
		grid.getCellIterator().execute(grid, totalCharge);
		return totalCharge.getSum(grid);
	}

	private class EFieldSquared implements CellAction {

		private double[] sum;

        public void reset() {
        	sum = new double[3];//TODO Make this method d-dimensional!!
        }
        
        public double getSum() {
        	return sum[0]+sum[1]+sum[2];
        }
        
        public double getSum(int dir) {
        	return sum[dir];
        }

        public void execute(Grid grid, int index) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double[] res = new double[numDir];
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				res[i] += grid.getE(index, i).square();
				//res += grid.getEsquaredFromLinks(coor, i);
			}
			
			for (int i = 0; i < numDir; i++) {
				res[i] /= norm;
			}
			synchronized(this) {
				for (int i = 0; i < numDir; i++) {
					sum[i] += res[i];   	// Synchronisierte Summenbildung
				}
			}
		}
	}

	private class BFieldSquared implements CellAction {

		private double[] sum;

        public void reset() {
        	sum = new double[3];
        }
        
        public double getSum() {
        	return sum[0]+sum[1]+sum[2];
        }
        
        public double getSum(int dir) {
        	return sum[dir];
        }
        
        public void execute(Grid grid, int index) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double[] res = new double[numDir];
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				//res += grid.getB(coor, i).square();
				// Averaging B(-dt/2) and B(dt/2) to approximate B(0).
				res[i] += 0.5 * (grid.getBsquaredFromLinks(index, i, 0) + grid.getBsquaredFromLinks(index, i, 1));
			}
			
			for (int i = 0; i < numDir; i++) {
				res[i] /= norm;
			}
			synchronized(this) {
				for (int i = 0; i < numDir; i++) {
					sum[i] += res[i];   	// Synchronisierte Summenbildung
				}
			}
		}
	}
	
	private class GaussLaw implements CellAction {

		private double sum;

        public void reset() {
        	sum = 0.0;
        }
        
        public double getSum() {
        	return sum;
        }
        
        public void execute(Grid grid, int index) {
			int numDir = grid.getNumberOfDimensions();
			double norm = 1.0;
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
			}
			double result = grid.getGaussConstraintSquared(index)/norm;
			synchronized(this) {
			       sum += result;   // Synchronisierte Summenbildung
			}
		}
	}

	private class TotalCharge implements CellAction {

		private AlgebraElement charge;

		public void reset(Grid grid) {
			charge = grid.getElementFactory().algebraZero();
		}

		public double getSum(Grid grid) {
			charge.multAssign(Math.pow(grid.getLatticeSpacing(), grid.getNumberOfDimensions()));
			return Math.sqrt(charge.square());
		}

		public void execute(Grid grid, int index) {
			synchronized(this) {
				charge.addAssign(grid.getRho(index));   // Synchronisierte Summenbildung
			}
		}
	}
	
}