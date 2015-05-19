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


	private class UpdateFields implements CellAction
	{
		private double as;
		private double at;

		/**
		 * Updates the electric fields at a given coordinate
		 *
		 * @param coor  Lattice coordinate
		 */
		public void execute(Grid grid, int[] coor) {
			for(int i = 0; i < grid.getNumberOfDimensions(); i++)
			{
				LinkMatrix[] plaquettes =  new LinkMatrix[grid.getNumberOfDimensions()-1];
				int c = 0;
				for(int j = 0; j < grid.getNumberOfDimensions(); j++)
				{
					if(j != i)
					{
						plaquettes[c] = grid.getPlaquette(coor, i, j, 1 , 1).add(grid.getPlaquette(coor, i, j, 1 , -1));
						c++;
					}
				}
				//now add the plaquettes together.
				for(int p = 1; p < plaquettes.length; p++)
				{
					plaquettes[0] = plaquettes[0].add(plaquettes[p]);
				}

				YMField currentE =  grid.getE(coor, i);
				grid.setE(coor, i, currentE.sub(plaquettes[0].proj().mult(2*at/(as*as))));
			}
		}
	}

	private class UpdateLinks implements CellAction {

		private double as;
		private double at;

		/**
		 * Updates the links matrices at a given coordinate
		 *
		 * @param coor  Lattice coordinate
		 */
		public void execute(Grid grid, int[] coor) {

			LinkMatrix V;

			for(int k=0;k<coor.length;k++) {

				V = grid.getE(coor, k).mult(- at).getLinkExact();	//minus sign takes take of conjugation
				grid.setUnext( coor, k, V.mult( grid.getU(coor, k) ) );

			}
		}
	}
}
