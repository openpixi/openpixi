package org.openpixi.pixi.physics.util;

/**
 * Created by monol on 2015-09-02.
 */
public class GridFunctions {

	public static int getEffectiveNumberOfDimensions(int[] numCells) {
		int count = 0;
		for (int i = 0; i < numCells.length; i++) {
			if(numCells[i] > 1) {
				count++;
			}
		}
		return count;
	}

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

	public static int getTotalNumberOfCells(int[] numCells) {
		int count = 1;
		for (int i = 0; i < numCells.length; i++) {
			count *= numCells[i];
		}
		return count;
	}

	public static int[] roundGridPos(double[] pos, double as) {
		int[] roundedGridPosition = new int[pos.length];
		for (int i = 0; i < pos.length; i++) {
			roundedGridPosition[i] = (int) Math.rint(pos[i] / as);
		}
		return roundedGridPosition;
	}

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

	public static int getCellIndex(int[] coordinates, int[] numCells) {
		int cellIndex;
		// Make periodic
		int[] periodicCoordinates = new int[coordinates.length];
		System.arraycopy(coordinates, 0, periodicCoordinates, 0, coordinates.length);
		for (int i = 0; i < numCells.length; i++) {
			periodicCoordinates[i] = (coordinates[i] + numCells[i]) % numCells[i];
		}
		// Compute cell index
		cellIndex = periodicCoordinates[0];
		for (int i = 0; i < coordinates.length; i++) {
			cellIndex *= numCells[i];
			cellIndex += periodicCoordinates[i];
		}
		return cellIndex;
	}

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


}
