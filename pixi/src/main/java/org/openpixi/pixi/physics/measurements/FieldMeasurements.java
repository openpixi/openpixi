package org.openpixi.pixi.physics.measurements;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.parallel.cellaccess.CellAction;


public class FieldMeasurements {
	
	EFieldSquared Esquared = new EFieldSquared();
	BFieldSquared Bsquared = new BFieldSquared();
	
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

        public void execute(Grid grid, int[] coor) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double[] res = new double[numDir];
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				res[i] += grid.getE(coor, i).square();
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
        
        public void execute(Grid grid, int[] coor) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double[] res = new double[numDir];
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				//res += grid.getB(coor, i).square();
				res[i] += grid.getBsquaredFromLinks(coor, i);
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
	
}