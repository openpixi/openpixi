package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

import java.util.ArrayList;

/**
 * Created by dmueller on 8/27/15.
 */
public class BoundaryFixer implements Diagnostics {

	private int loopIndex1;
	private int loopIndex2;
	private int fixedIndex;
	private int fixedIndexPosition;
	private int N1;
	private int N2;
	private Cell[][] savedCells;

	public BoundaryFixer(int loopIndex1, int loopIndex2, int fixedIndex, int fixedIndexPosition) {
		this.loopIndex1 = loopIndex1;
		this.loopIndex2 = loopIndex2;
		this.fixedIndex = fixedIndex;
		this.fixedIndexPosition = fixedIndexPosition; // 0 or 1 to specify the plane.
	}

	public void initialize(Simulation s) {
		N1 = s.grid.getNumCells(loopIndex1);
		N2 = s.grid.getNumCells(loopIndex2);
		if(fixedIndexPosition == 1) {
			fixedIndexPosition = s.grid.getNumCells(fixedIndex) - 1;
		}
		//s.grid.updateLinks(-s.getTimeStep());
		savedCells = new Cell[N1][N2];
		for(int i = 0; i < N1; i++) {
			for (int j = 0; j < N2; j++) {
				int[] pos = getGridPos(i, j);
				int cellIndex = s.grid.getCellIndex(pos);
				savedCells[i][j] = s.grid.getCell(cellIndex).copy();
			}
		}
		//s.grid.updateLinks(s.getTimeStep());
	}

	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps) {
		for(int i = 0; i < N1; i++) {
			for (int j = 0; j < N2; j++) {
				int[] pos = getGridPos(i, j);
				int cellIndex = grid.getCellIndex(pos);
				grid.getCell(cellIndex).copyFrom(savedCells[i][j]);
			}
		}
	}

	private int[] getGridPos(int i, int j) {
		int[] pos = new int[3];
		pos[loopIndex1] = i;
		pos[loopIndex2] = j;
		pos[fixedIndex] = fixedIndexPosition;
		return pos;
	}
}
