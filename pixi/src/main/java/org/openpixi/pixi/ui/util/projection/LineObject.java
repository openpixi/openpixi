package org.openpixi.pixi.ui.util.projection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * A 3D object of lines
 */
public class LineObject {
	private int pointcount = 0;
	private ArrayList<Double> pointlist = new ArrayList<Double>();
	private ArrayList<Integer> linelist = new ArrayList<Integer>();
	private ArrayList<Color> colorlist = new ArrayList<Color>();

	private ArrayList<Double> transformedpointlist = new ArrayList<Double>();

	public LineObject() {
	}

	public void clear() {
		pointcount = 0;
		pointlist.clear();
		linelist.clear();
	}

	/** Add point to pointlist.
	 * @return Index of point */
	public int addPoint(double x, double y, double z) {
		pointlist.add(x);
		pointlist.add(y);
		pointlist.add(z);
		return pointcount++;
	}

	public void addLine(int index1, int index2, Color color) {
		linelist.add(index1);
		linelist.add(index2);
		colorlist.add(color);
	}

	public void addCube(double size, Color color) {
		int p1 = addPoint(0, 0, 0);
		int p2 = addPoint(0, size, 0);
		int p3 = addPoint(size, size, 0);
		int p4 = addPoint(size, 0, 0);
		int p5 = addPoint(0, 0, size);
		int p6 = addPoint(0, size, size);
		int p7 = addPoint(size, size, size);
		int p8 = addPoint(size, 0, size);
		addLine(p1, p2, color);
		addLine(p2, p3, color);
		addLine(p3, p4, color);
		addLine(p4, p1, color);
		addLine(p5, p6, color);
		addLine(p6, p7, color);
		addLine(p7, p8, color);
		addLine(p8, p5, color);
		addLine(p1, p5, color);
		addLine(p2, p6, color);
		addLine(p3, p7, color);
		addLine(p4, p8, color);
	}

	private void applyProjection(Projection projection) {
		transformedpointlist.clear();
		int i = 0;
		while (i < pointlist.size()) {
			double x = pointlist.get(i++);
			double y = pointlist.get(i++);
			double z = pointlist.get(i++);
			projection.project(x, y, z);
			transformedpointlist.add(projection.screenX);
			transformedpointlist.add(projection.screenY);
		}
	}

	public void paint(Projection projection, Graphics2D graphics, double sx, double sy) {
		applyProjection(projection);

		for (int i = 0; i < colorlist.size(); i++) {
			int j = 2 * i; // index in linelist
			int p1 = 2*linelist.get(j++); // first index in transformedpointlist
			int p2 = 2*linelist.get(j); // first index in transformedpointlist
			double x1 = transformedpointlist.get(p1++);
			double y1 = transformedpointlist.get(p1);
			double x2 = transformedpointlist.get(p2++);
			double y2 = transformedpointlist.get(p2);
			Color c = colorlist.get(i);
			graphics.setColor(c);
			graphics.drawLine((int) (x1 * sx), (int) (y1 * sy), (int) (x2 * sx), (int) (y2 * sy));
		}
	}
}
