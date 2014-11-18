package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

public class SimpleSolver extends FieldSolver {
        
	private double timeStep;
	private SolveForE solveForE = new SolveForE();
	private SolveForB solveForB = new SolveForB();

	@Override
	public FieldSolver clone() {
		SimpleSolver clone = new SimpleSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.solveForE = solveForE;
		clone.solveForB = solveForB;
		return clone;
	}

	/**A simple LeapFrog algorithm
	 * @param grid before the update: E(t), B(t+dt/2);
	 * 						after the update: E(t+dt), B(t+3dt/2)
	*/
	@Override
	public void step(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		solveForE.timeStep = timeStep;
		solveForB.timeStep = timeStep;
		cellIterator.execute(grid, solveForE);
		cellIterator.execute(grid, solveForB);
	}


	private class SolveForE implements CellAction {
                private double eps0 = 1.0/(4*Math.PI);
                //private double eps0 = 1;
                //Suppose c=1:
                private double mue0 = 4*Math.PI;
                //private double mue0 = 1;
                
		private double timeStep;
		public void execute(Cell cell) {
			throw new UnsupportedOperationException();
		}

		public void execute(Grid grid, int x, int y) {
			/**Curl of the B field using forward difference.
			 * Because we are using a FDTD grid E(x,y) is in between of B(x,y) and B(x+1,y)
			 * (same for y). Therefore this is something like a center difference.*/
			//double cx = (grid.getBz(x, y+1) - grid.getBz(x, y)) / grid.getCellHeight();
			//double cy = -(grid.getBz(x+1, y) - grid.getBz(x, y)) / grid.getCellWidth();
                        double cx = (grid.getBz(x, y) - grid.getBz(x, (y+grid.getNumCellsY()- 1)%grid.getNumCellsY())) / grid.getCellHeight();
			double cy = -(grid.getBz(x, y) - grid.getBz((x+grid.getNumCellsX()-1)%grid.getNumCellsX(), y)) / grid.getCellWidth();
			/**Maxwell equations*/
			grid.addEx(x, y, timeStep * (1/(mue0*eps0)*cx - 1/eps0*grid.getJx(x, y)));
			grid.addEy(x, y, timeStep * (1/(mue0*eps0)*cy - 1/eps0*grid.getJy(x, y)));
		}
	}

	private class SolveForB implements CellAction {

		private double timeStep;
		public void execute(Cell cell) {
			throw new UnsupportedOperationException();
		}

		public void execute(Grid grid, int x, int y) {
			/**Curl of the E field using forward difference.
			 * Because we are using a FDTD grid B(x,y) is in between of E(x,y) and E(x-1,y)
			 * (same for y). Therefore this is something like a center difference.*/
			//double cz = (grid.getEy(x, y) - grid.getEy(x-1, y)) / grid.getCellWidth() -
			//		(grid.getEx(x, y) - grid.getEx(x, y-1)) / grid.getCellHeight();
                        double cz = (grid.getEy((x+1)%grid.getNumCellsX(), y) - grid.getEy(x, y)) / grid.getCellWidth() -
					(grid.getEx(x, (y+1)%grid.getNumCellsY()) - grid.getEx(x, y)) / grid.getCellHeight();

			/**Maxwell equation*/
			grid.addBz(x, y, -timeStep * cz);
		}
	}

}
