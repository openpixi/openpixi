package org.openpixi.pixi.physics.util;

/**
 * Created by monol on 2015-09-02.
 */
public class GridFunctions {

	/**
	 * Computes the effective number of dimensions for a given grid.
	 * Example: A grid described by the grid sizes [16, 16, 16] has effective dimension 3.
	 * A grid described by [16, 16, 1] is effectively two-dimensional.
	 *
	 * @param numCells array of grid sizes
	 * @return         effective number of dimensions of the grid
	 */
	public static int getEffectiveNumberOfDimensions(int[] numCells) {
		int count = 0;
		for (int i = 0; i < numCells.length; i++) {
			if(numCells[i] > 1) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Reduces a grid given by its grid sizes to the effectively lower dimensional grid.
	 * Example: [16, 16, 1] is reduced to [16, 16].
	 *
	 * @param numCells array of grid sizes
	 * @return         array of grid sizes for the reduced grid
	 */
	public static int[] getEffectiveNumCells(int[] numCells) {
		int effDim = getEffectiveNumberOfDimensions(numCells);
		int[] effNumCells = new int[effDim];
		int count = 0;
		for (int i = 0; i < numCells.length; i++) {
			if(numCells[i] > 1) {
				effNumCells[count] = numCells[i];
				count++;
			}
		}
		return effNumCells;
	}

	/**
	 * Counts the total number of cells for a given grid size.
	 *
	 * @param numCells array of grid sizes
	 * @return         total number of cells in the grid
	 */
	public static int getTotalNumberOfCells(int[] numCells) {
		int count = 1;
		for (int i = 0; i < numCells.length; i++) {
			count *= numCells[i];
		}
		return count;
	}

	/**
	 * Given a double vector in the simulation box the nearest grid point is returned.
	 *
	 * @param pos position in the simulation box
	 * @param as  lattice spacing of the grid
	 * @return    grid position of the nearest grid point
	 */
	public static int[] nearestGridPoint(double[] pos, double as) {
		int[] roundedGridPosition = new int[pos.length];
		for (int i = 0; i < pos.length; i++) {
			roundedGridPosition[i] = (int) Math.rint(pos[i] / as);
		}
		return roundedGridPosition;
	}

	/**
	 * Given a double vector in the simulation box the nearest grid point is returned.
	 *
	 * @param pos position in the simulation box
	 * @param as  lattice spacings of the grid
	 * @return    grid position of the nearest grid point
	 */
	public static int[] nearestGridPoint(double[] pos, double[] as) {
		int[] roundedGridPosition = new int[pos.length];
		for (int i = 0; i < pos.length; i++) {
			roundedGridPosition[i] = (int) Math.rint(pos[i] / as[i]);
		}
		return roundedGridPosition;
	}

	/**
	 * Given a double vector in the simulation box the "floored" grid point is returned.
	 *
	 * @param pos position in the simulation box
	 * @param as  lattice spacing of the grid
	 * @return    grid position of the nearest grid point
	 */
	public static int[] flooredGridPoint(double[] pos, double as) {
		int[] roundedGridPosition = new int[pos.length];
		for (int i = 0; i < pos.length; i++) {
			roundedGridPosition[i] = (int) Math.floor(pos[i] / as);
		}
		return roundedGridPosition;
	}

	/**
	 * Returns the grid position of cell index.
	 *
	 * @param index    cell index
	 * @param numCells array of grid sizes
	 * @return         grid position of the cell
	 */
	public static int[] getCellPos(int index, int[] numCells)
	{
		int numDim = numCells.length;
		int[] pos = new int[numDim];

		for(int i = numDim-1; i >= 0; i--)
		{
			pos[i] = index % numCells[i];
			index -= pos[i];
			index /= numCells[i];
		}

		return pos;
	}

	/**
	 * Returns the cell index of a given grid position.
	 *
	 * @param coordinates grid position of a cell
	 * @param numCells    array of grid sizes
	 * @return            cell index of the grid position
	 */
	public static int getCellIndex(int[] coordinates, int[] numCells) {
		int cellIndex;
		// Make periodic
		int[] periodicCoordinates = new int[coordinates.length];
		for (int i = 0; i < numCells.length; i++) {
			periodicCoordinates[i] = (coordinates[i] % numCells[i] + numCells[i]) % numCells[i];
		}
		// Compute cell index
		cellIndex = periodicCoordinates[0];
		for (int i = 1; i < coordinates.length; i++) {
			cellIndex *= numCells[i];
			cellIndex += periodicCoordinates[i];
		}
		return cellIndex;
	}

	/**
	 * Eliminates a coordinate of a grid position vector.
	 * Example: reduceGridPos([16, 16, 8], 2) returns the reduced (projected) grid position [16, 16]
	 *
	 * @param gridPos grid position
	 * @param dir     direction which should be eliminated
	 * @return        reduced grid position
	 */
	public static int[] reduceGridPos(int[] gridPos, int dir) {
		int[] projGridPos = new int[gridPos.length-1];
		int count = 0;
		for (int i = 0; i < gridPos.length; i++) {
			if(i != dir) {
				projGridPos[count] = gridPos[i];
				count++;
			}
		}
		return projGridPos;
	}

	/**
	 * Eliminates a coordinate of a position vector.
	 * Example: reducePos([16.0, 16.0, 8.0], 2) returns the reduced (projected) position [16.0, 16.0]
	 *
	 * @param pos position
	 * @param dir direction which should be eliminated
	 * @return    reduced position
	 */
	public static double[] reducePos(double[] pos, int dir) {
		double[] projPos = new double[pos.length-1];
		int count = 0;
		for (int i = 0; i < pos.length; i++) {
			if(i != dir) {
				projPos[count] = pos[i];
				count++;
			}
		}
		return projPos;
	}

	/**
	 * Inserts a coordinate for a given reduced grid position and direction.
	 * Example: insertGridPos([16, 16], 2, 8) returns [16, 16, 8].
	 *
	 * @param gridPos grid position
	 * @param dir     direction which should be inserted
	 * @param value   value of the grid coordinate to be inserted
	 * @return        grid position with the new coordinate inserted
	 */
	public static int[] insertGridPos(int[] gridPos, int dir, int value) {
		int[] newGridPos = new int[gridPos.length + 1];
		int count = 0;
		for (int i = 0; i < newGridPos.length; i++) {
			if(i != dir) {
				newGridPos[i] = gridPos[count];
				count++;
			} else {
				newGridPos[i] = value;
			}
		}
		return newGridPos;
	}

	/**
	 * Shifts a cell index in the given direction with given orientation.
	 * @param index cell index
	 * @param d direction of the shift
	 * @param o orienatation of the shift
	 * @param numCells gridsize of the array
	 * @return
	 */
	public static int shift(int index, int d, int o, int[] numCells) {

		int[] gpos = getCellPos(index, numCells);
		gpos[d] += o;
		gpos[d] = (gpos[d] % numCells[d] + numCells[d])  % numCells[d];

		return GridFunctions.getCellIndex(gpos, numCells);
	}


}
