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

	private class EFieldSquared implements CellAction {

		private double sum;

        public void reset() {
        	sum = 0;
        }
        
        public double getSum() {
        	return sum;
        }

        public void execute(Grid grid, int[] coor) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double res = 0.0;
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				res += grid.getE(coor, i).square();
				//res += grid.getEsquaredFromLinks(coor, i);
			}
			
			double result = res/norm;
			synchronized(this) {
			       sum += result;   // Synchronisierte Summenbildung
			}
		}
	}

	private class BFieldSquared implements CellAction {

		private double sum;

        public void reset() {
        	sum = 0;
        }
        
        public double getSum() {
        	return sum;
        }
        
        public void execute(Grid grid, int[] coor) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double res = 0;
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				//res += grid.getB(coor, i).square();
				res += grid.getBsquaredFromLinks(coor, i);
			}
			double result = res/norm;
			synchronized(this) {
			       sum += result;   // Synchronisierte Summenbildung
			}
		}
	}
	
}