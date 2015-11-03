package org.openpixi.pixi.ui.util.yaml;

import java.util.ArrayList;
import java.util.List;

public class YamlRegion {
	public Boolean enabled;
	public ArrayList<Integer> point1;
	public ArrayList<Integer> point2;

	public boolean checkPoints(int numberOfDimensions) {
		if(point1 != null && point2 != null) {
			if (point1.size() == numberOfDimensions && point2.size() == numberOfDimensions) {
				return true;
			}
		}
		return false;
	}

	public int[] getPoint1(List<Integer> gridCells) {
		int[] point = convertList(point1);
		for (int i = 0; i < gridCells.size(); i++) {
			if(point[i] < 0) {
				point[i] += gridCells.get(i);
			}
		}
		return point;
	}

	public int[] getPoint2(List<Integer> gridCells) {
		int[] point = convertList(point2);
		for (int i = 0; i < gridCells.size(); i++) {
			if(point[i] < 0) {
				point[i] += gridCells.get(i);
			}
		}
		return point;
	}

	private int[] convertList(List<Integer> list) {
		int[] array = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
}