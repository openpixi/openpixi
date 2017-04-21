package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;

public class ImplicitTYMSolver extends FieldSolver
{

	private double timeStep;
	private double implicitIterations;
	private double implicitDampingFactor;
	private UpdateLinks linkUpdater = new UpdateLinks();
	private ImplicitBegin implicitBegin = new ImplicitBegin();
	private ImplicitStep implicitStep = new ImplicitStep();
	private ImplicitDamping implicitDamping = new ImplicitDamping();
	private ImplicitEnd implicitEnd = new ImplicitEnd();

	public ImplicitTYMSolver(double implicitIterations, double implicitDampingFactor) {
		this.implicitIterations = implicitIterations;
		this.implicitDampingFactor = implicitDampingFactor;
	}

	@Override
	public FieldSolver clone() {
		ImplicitTYMSolver clone = new ImplicitTYMSolver(implicitIterations, implicitDampingFactor);
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.implicitIterations = implicitIterations;
		clone.implicitDampingFactor = implicitDampingFactor;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {
		Grid implicitGrid = new Grid(grid); // Copy grid.

		implicitBegin.at = timeStep;
		implicitBegin.unitFactor = new double[grid.getNumberOfDimensions()];
		for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
			implicitBegin.unitFactor[i] =  - grid.getLatticeUnitFactor(i) * grid.getTemporalSpacing();
		}
		implicitBegin.implicitGrid = implicitGrid;
		cellIterator.execute(grid, implicitBegin);

		implicitStep.implicitGrid = implicitGrid;
		implicitStep.at = implicitBegin.at;
		implicitStep.unitFactor = implicitBegin.unitFactor;
		for (int i = 0; i < implicitIterations; i++) {
			implicitGrid.storeFields(); // swap U <-> Unext
			cellIterator.execute(grid, implicitStep);
			cellIterator.execute(implicitGrid, implicitDamping);
		}

		implicitEnd.implicitGrid = implicitGrid;
		cellIterator.execute(grid, implicitEnd);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		linkUpdater.at = timeStep;
		cellIterator.execute(grid, linkUpdater);
	}

	private class ImplicitBegin implements CellAction {

		private double at;
		private double[] unitFactor;
		private Grid implicitGrid;

		/**
		 * Combined update of fields and links using the sum of staples.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				GroupElement V;
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					GroupElement temp = grid.getU(index, i).mult(grid.getStapleSum(index, i));
					implicitGrid.addE(index, i, temp.proj().mult(at)); // area factors already included in getStapleSum()
					implicitGrid.addE(index, i, grid.getJ(index, i).mult(unitFactor[i]));
					V = implicitGrid.getE(index, i).mult(-at).getLink();
					V.multAssign(grid.getU(index, i));
					implicitGrid.setUnext(index, i, V);
				}
			}
		}
	}

	private class ImplicitStep implements CellAction {

		private double at;
		private double[] unitFactor;
		private int beamdirection = 0;

		private Grid implicitGrid;
		private Grid explicitGrid;

		/**
		 * Combined update of fields and links using the sum of staples.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			explicitGrid = grid;
			if(grid.isActive(index)) {
				GroupElement V;
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					// Start from previous E
					implicitGrid.setE(index, i, grid.getE(index,i));

					// add 1/2 of future contributions:
					GroupElement temp = getStapleSum(index, i, beamdirection, +1);
					implicitGrid.addE(index, i, temp.proj().mult(at * 0.5)); // area factors already included in getStapleSum()

					// add 1/2 of past contributions:
					temp = getStapleSum(index, i, beamdirection, -1);
					implicitGrid.addE(index, i, temp.proj().mult(at * 0.5)); // area factors already included in getStapleSum()

					// add current:
					implicitGrid.addE(index, i, grid.getJ(index, i).mult(unitFactor[i]));
					V = implicitGrid.getE(index, i).mult(-at).getLink();
					V.multAssign(explicitGrid.getU(index, i));
					implicitGrid.setUnext(index, i, V);
				}
			}
		}


		/**
		 * Computes the sum of staples surrounding a particular gauge link given by a lattice index and a direction,
		 * but uses links at different time steps.
		 * This is used for the field equations of motion.
		 * <b>Note that this routine closes the plaquettes along the central line (different from
		 * the behavior of the corresponding routine Grid.getStapleSum()).</b>
		 * @param index Lattice index
		 * @param d     Direction
		 * @param beamdirection Beam direction
		 * @param time -1 or +1
		 * @return      Sum of all surrounding staples
		 */
		public GroupElement getStapleSum(int index, int d, int beamdirection, int time) {
			GroupElement S = explicitGrid.getElementFactory().groupZero();
			int ci1 = explicitGrid.shift(index, d, 1);
			int ci2, ci3, ci4;
			for (int i = 0; i < explicitGrid.getNumberOfDimensions(); i++) {
				boolean inTransversePlane = (i != beamdirection) && (d != beamdirection);
				if ((i != d)) {
					// explicit time by default
					int time_d = 0;
					int time_i = 0;
					if (inTransversePlane) {
						// implicit time in all directions
						time_d = time;
						time_i = time;
					} else {
						// implicit time in beamdirection
						if (d == beamdirection) {
							time_d = time;
						} else if (i == beamdirection) {
							time_i = time;
						}
					}
					ci2 = explicitGrid.shift(index, i, 1);
					ci3 = explicitGrid.shift(ci1, i, -1);
					ci4 = explicitGrid.shift(index, i, -1);
					GroupElement U1 = getU(ci1, i, time_i).mult(getU(ci2, d, time_d).adj());
					U1.multAssign(getU(index, i, time_i).adj());
					GroupElement U2 = getU(ci4, d, time_d).mult(getU(ci3, i, time_i));
					U2.adjAssign();
					U2.multAssign(getU(ci4, i, time_i));
					double areaFactor = 1.0 / Math.pow(explicitGrid.getLatticeSpacing(i), 2);
					U1.addAssign(U2);
					U1 = getU(index, d, time_d).mult(U1); // close plaquettes
					U1.multAssign(areaFactor);
					S.addAssign(U1);
				}
			}
			return S;
		}

		/**
		 * Returns the gauge link at time (t) at a given lattice index in a given direction.
		 * @param index Lattice index of the gauge link
		 * @param dir   Direction of the gauge link
		 * @param time  Time = -1, 0, or 1.
		 * @return      Instance of the gauge link
		 */
		public GroupElement getU(int index, int dir, int time) {
			switch(time) {
				case -1:
					// (Note that grid.getUnext contains the old U previous to grid.E)
					return explicitGrid.getUnext(index, dir);
				case 0:
					return explicitGrid.getU(index, dir);
				case 1:
					return implicitGrid.getU(index, dir);
				default:
					throw new RuntimeException("Invalid time step " + time);
			}
		}

	}

	private class ImplicitDamping implements CellAction {

		/**
		 * Damping of the fields of the implicit grid.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					GroupElement U1 = grid.getUnext(index,  i).pow(1 - implicitDampingFactor);
					GroupElement U2 = grid.getU(index, i).pow(implicitDampingFactor);
					grid.setUnext(index, i, U2.mult(U1));
				}
			}
		}
	}

	private class ImplicitEnd implements CellAction {

		private Grid implicitGrid;

		/**
		 * Combined update of fields and links using the sum of staples.
		 * @param grid
		 * @param index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				for (int i = 0; i < grid.getNumberOfDimensions(); i++) {
					grid.setE(index, i, implicitGrid.getE(index, i));
					grid.setUnext(index, i, implicitGrid.getUnext(index, i));
				}
			}
		}
	}

	private class UpdateLinks implements CellAction {
		private double at;

		/**
		 * Updates the links matrices in a given cell.
		 * @param grid	Reference to the grid
		 * @param index	Cell index
		 */
		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				GroupElement V;
				for (int k = 0; k < grid.getNumberOfDimensions(); k++) {
					V = grid.getE(index, k).mult(-at).getLink();
					V.multAssign(grid.getU(index, k));
					grid.setUnext(index, k, V);

				}
			}
		}
	}
}
