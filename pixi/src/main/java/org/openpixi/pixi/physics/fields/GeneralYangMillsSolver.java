package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.grid.LinkMatrix;

public class GeneralYangMillsSolver extends FieldSolver
{

	private double timeStep;
	private UpdateFields fieldUpdater = new UpdateFields();
	private UpdateLinks linkUpdater = new UpdateLinks();

	@Override
	public FieldSolver clone() {
		GeneralYangMillsSolver clone = new GeneralYangMillsSolver();
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
		linkUpdater.at = timeStep;
		linkUpdater.as = grid.getLatticeSpacing();
		cellIterator.execute(grid, fieldUpdater);
		cellIterator.execute(grid, linkUpdater);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		linkUpdater.at = timeStep;
		linkUpdater.as = grid.getLatticeSpacing();
		cellIterator.execute(grid, linkUpdater);
	}


	private class UpdateFields implements CellAction
	{
		private double as;
		private double at;
		private double g;

		/**
		 * Updates the electric fields at a given coordinate
		 *
		 * @param index  Lattice index
		 */
		public void execute(Grid grid, int index) {
			for(int i = 0; i < grid.getNumberOfDimensions(); i++)
			{
				LinkMatrix[] plaquettes =  new LinkMatrix[grid.getNumberOfDimensions()-1];
				int c = 0;
				for(int j = 0; j < grid.getNumberOfDimensions(); j++)
				{
					if(j != i)
					{
						plaquettes[c] = grid.getPlaquette(index, i, j, 1 , 1, 0).add(grid.getPlaquette(index, i, j, 1 , -1, 0));
						c++;
					}
				}
				//now add the plaquettes together.
				for(int p = 1; p < plaquettes.length; p++)
				{
					plaquettes[0] = plaquettes[0].add(plaquettes[p]);
				}

				YMField currentE = grid.getE(index, i).add(plaquettes[0].proj().mult(at / (as * as )));
				currentE.addAssign(grid.getJ(index, i).mult(-at));
				grid.setE(index, i, currentE);
			}
		}
	}

	private class UpdateLinks implements CellAction {

		private double as;
		private double at;

		/**
		 * Updates the links matrices in a given cell.
		 * @param grid	Reference to the grid
		 * @param index	Cell index
		 */
		public void execute(Grid grid, int index) {

			LinkMatrix V;

			for(int k=0;k<grid.getNumberOfDimensions();k++) {

				V = grid.getE(index, k).mult(-at).getLink();	//minus sign takes take of conjugation
				grid.setUnext( index, k, V.mult(grid.getU(index, k)) );

			}
		}
	}
}
