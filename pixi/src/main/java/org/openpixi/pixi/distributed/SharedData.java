package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.grid.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the data which needs to be shared between two nodes.
 * Specifically, sends and receives ghost and border particles and cells.
 */
public class SharedData {

	private int neighborID;

	private List<Cell> boundaryCells = new ArrayList<Cell>();
	private List<Cell> borderCells = new ArrayList<Cell>();


	public SharedData(int neighborID) {
		this.neighborID = neighborID;
	}


	public void registerBorderCell(Cell cell) {
		borderCells.add(cell);
	}


	public void registerBoundaryCell(Cell cell) {
		boundaryCells.add(cell);
	}
}
