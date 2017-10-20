package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.grid.Grid;

import java.util.ArrayList;

/**
 * Store list of grids
 */
public class GridManager {
	ArrayList<LabeledGrid> labeledGridList = new ArrayList();

	public LabeledGrid add(String label, Grid grid) {
		LabeledGrid labeledGrid = new LabeledGrid();
		labeledGrid.label = label;
		labeledGrid.grid = grid;
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

	public static class LabeledGrid {
		public String label;
		public Grid grid;
	}
}
