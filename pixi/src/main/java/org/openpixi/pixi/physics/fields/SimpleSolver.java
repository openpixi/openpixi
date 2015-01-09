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

		public void execute(Grid grid, int x, int y, int z) {
			/**Curl of the B field using backward difference.
			 * Because we are using a FDTD grid E(x,y) is in between of B(x,y) and B(x-1,y)
			 * (same for y). Therefore this is something like a center difference.*/
                        double cx = (grid.getBz(x, y, z) - grid.getBz(x, (y+grid.getNumCellsY()- 1)%grid.getNumCellsY(), z)) / grid.getCellHeight()
                        		- (grid.getBy(x, y, z) - grid.getBy(x, y, (z+grid.getNumCellsZ()- 1)%grid.getNumCellsZ())) / grid.getCellDepth();
                        double cy = (grid.getBx(x, y, z) - grid.getBx(x, y, (z+grid.getNumCellsZ()- 1)%grid.getNumCellsZ())) / grid.getCellDepth()
                        		- (grid.getBz(x, y, z) - grid.getBz((x+grid.getNumCellsX()-1)%grid.getNumCellsX(), y, z)) / grid.getCellWidth();
                        double cz = (grid.getBy(x, y, z) - grid.getBy((x+grid.getNumCellsX()-1)%grid.getNumCellsX(), y, z)) / grid.getCellWidth()
                        		- (grid.getBx(x, y, z) - grid.getBx(x, (y+grid.getNumCellsY()- 1)%grid.getNumCellsY(), z)) / grid.getCellHeight();
			/**Maxwell equations*/
			grid.addEx(x, y, z, timeStep * (1/(mue0*eps0)*cx - 1/eps0*grid.getJx(x, y, z)));
			grid.addEy(x, y, z, timeStep * (1/(mue0*eps0)*cy - 1/eps0*grid.getJy(x, y, z)));
			grid.addEz(x, y, z, timeStep * (1/(mue0*eps0)*cz - 1/eps0*grid.getJz(x, y, z)));
		}
	}

	private class SolveForB implements CellAction {

		private double timeStep;
		public void execute(Cell cell) {
			throw new UnsupportedOperationException();
		}

		public void execute(Grid grid, int x, int y, int z) {
			/**Curl of the E field using forward difference.
			 * Because we are using a FDTD grid B(x,y) is in between of E(x+1,y) and E(x,y)
			 * (same for y). Therefore this is something like a center difference.*/
						double cx = (grid.getEz(x, (y+1)%grid.getNumCellsY(), z) - grid.getEz(x, y, z)) / grid.getCellHeight()
									- (grid.getEy(x, y, (z+1)%grid.getNumCellsZ()) - grid.getEy(x, y, z)) / grid.getCellDepth();
						double cy = (grid.getEx(x, y, (z+1)%grid.getNumCellsZ()) - grid.getEx(x, y, z)) / grid.getCellDepth()
									- (grid.getEz((x+1)%grid.getNumCellsX(), y, z) - grid.getEz(x, y, z)) / grid.getCellWidth();
                        double cz = (grid.getEy((x+1)%grid.getNumCellsX(), y, z) - grid.getEy(x, y, z)) / grid.getCellWidth()
                        		 	- (grid.getEx(x, (y+1)%grid.getNumCellsY(), z) - grid.getEx(x, y, z)) / grid.getCellHeight();

			/**Maxwell equation*/
            grid.addBx(x, y, z, -timeStep * cx);
			grid.addBy(x, y, z, -timeStep * cy);
			grid.addBz(x, y, z, -timeStep * cz);
		}
	}

}
