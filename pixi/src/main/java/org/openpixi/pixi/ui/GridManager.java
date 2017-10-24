package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.grid.Grid;

import java.util.ArrayList;

/**
 * Store list of grids
 */
public class GridManager {
	ArrayList<LabeledGrid> labeledGridList = new ArrayList();

	public LabeledGrid add(String label, Grid grid) {
		return add(label, grid, null);
	}

	public LabeledGrid add(String label, Grid grid, double[][] occupationNumbers) {
		LabeledGrid labeledGrid = new LabeledGrid();
		labeledGrid.label = label;
		labeledGrid.grid = grid;
		labeledGrid.occupationNumbers = occupationNumbers;
		labeledGridList.add(labeledGrid);
		return labeledGrid;
	}

	public void remove(LabeledGrid labeledGrid) {
		labeledGridList.remove(labeledGrid);
	}

	public String[] getLabelList() {
		String[] list = new String[labeledGridList.size()];
		for (int i = 0; i < labeledGridList.size(); i++) {
			list[i] = labeledGridList.get(i).label;
		}
		return list;
	}

	public Grid getGrid(int index) {
		return labeledGridList.get(index).grid;
	}

	public double[][] getOccupationNumbers(int index) {
		return labeledGridList.get(index).occupationNumbers;
	}

	public static class LabeledGrid {
		public String label;
		public Grid grid;
		public double[][] occupationNumbers;
	}
}
