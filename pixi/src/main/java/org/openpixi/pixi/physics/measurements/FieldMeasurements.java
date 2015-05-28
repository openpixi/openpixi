package org.openpixi.pixi.physics.measurements;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.parallel.cellaccess.CellAction;


public class FieldMeasurements {
	
	EFieldSquared Esquared = new EFieldSquared();
	BFieldSquared Bsquared = new BFieldSquared();
	
	public double calculateEsquared(Grid grid) {
		return grid.getCellIterator().calculate(grid, Esquared);
	}
	
	public double calculateBsquared(Grid grid) {
		return grid.getCellIterator().calculate(grid, Bsquared);
	}

	private class EFieldSquared implements CellAction {

		public double calculate(Grid grid, int[] coor) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double res = 0.0;
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				res += grid.getE(coor, i).square();
			}
			return res/norm;
		}
		
		public void execute(Grid grid, int[] coor) {}
	}

	private class BFieldSquared implements CellAction {

		public double calculate(Grid grid, int[] coor) {
			int numDir = grid.getNumberOfDimensions();
			double norm = grid.getLatticeSpacing()*grid.getLatticeSpacing()*grid.getGaugeCoupling()*grid.getGaugeCoupling();
			double res = 0;
			for (int i = 0; i < numDir; i++) {
				norm *= grid.getNumCells(i);
				//TODO Implement the calculation of color magnetic fields!!
				//res += grid.getB(coor, i).square();
			}
			return res/norm;
		}
		
		public void execute(Grid grid, int[] coor) {}
	}
	
}