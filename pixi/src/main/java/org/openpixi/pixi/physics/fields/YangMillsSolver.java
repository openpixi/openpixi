package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.grid.LinkMatrix;

public class YangMillsSolver extends FieldSolver {
        
	private double timeStep;
	private UpdateFields fieldUpdater = new UpdateFields();
	private UpdateLinks linkUpdater = new UpdateLinks();

	@Override
	public FieldSolver clone() {
		YangMillsSolver clone = new YangMillsSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.fieldUpdater = fieldUpdater;
		clone.linkUpdater = linkUpdater;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		fieldUpdater.at = timeStep;
		fieldUpdater.as = grid.getLatticeSpacing();
		fieldUpdater.setFactor();
		cellIterator.execute(grid, fieldUpdater);
		cellIterator.execute(grid, linkUpdater);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		cellIterator.execute(grid, linkUpdater);
	}

	
	private class UpdateFields implements CellAction {
                
		private double at;
		private double as;
		private double factor;
		private YMField a1,a2,a3,a4,res;
		
		public void setFactor() {
			factor = at*at/as/as;      					//A factor of g*at*as is included in my E-field definition!!
		}
		/**
		 * Updates the electric fields at a given coordinate
		 *
		 * @param index  Lattice index
		 */
		public void execute(Grid grid, int index) {
			
			if(grid.getNumberOfDimensions() == 3) {
				
				for(int k=0;k<grid.getNumberOfDimensions();k++) {
	
					switch (k)
					{

					case 0:
						a1 = grid.FieldFromForwardPlaquette(index, 0, 1).mult(factor);

						a2 = grid.FieldFromBackwardPlaquette(index, 0, 1).mult(factor);

						a3 = grid.FieldFromForwardPlaquette(index, 0, 2).mult(factor);

						a4 = grid.FieldFromBackwardPlaquette(index, 0, 2).mult(factor);
						break;

					case 1:
						a1 = grid.FieldFromForwardPlaquette(index, 1, 2).mult(factor);

						a2 = grid.FieldFromBackwardPlaquette(index, 1, 2).mult(factor);

						a3 = grid.FieldFromForwardPlaquette(index, 1, 0).mult(factor);

						a4 = grid.FieldFromBackwardPlaquette(index, 1, 0).mult(factor);
						break;

					case 2:
						a1 = grid.FieldFromForwardPlaquette(index, 2, 0).mult(factor);

						a2 = grid.FieldFromBackwardPlaquette(index, 2, 0).mult(factor);

						a3 = grid.FieldFromForwardPlaquette(index, 2, 1).mult(factor);

						a4 = grid.FieldFromBackwardPlaquette(index, 2, 1).mult(factor);
						break;

					}
	
					res = grid.getE(index, k);
					res.addfour(a1, a2, a3, a4);
					grid.setE(index, k, res);
				
				}
			} else {System.out.println("Yang-Mills solver only available for 3 spatial dimensions!");}	
		}
	}

	private class UpdateLinks implements CellAction {
		/**
		 * Updates the links matrices at a given coordinate
		 *
		 * @param index  Lattice coordinate
		 */
		public void execute(Grid grid, int index) {
			
			LinkMatrix V;
				
			for(int k=0;k<grid.getNumberOfDimensions();k++) {
				
				V = grid.getE(index, k).getLinkExact();
				V.selfadj();
				grid.setUnext( index, k, V.mult( grid.getU(index, k) ) );
				
			}
		}
	}

}