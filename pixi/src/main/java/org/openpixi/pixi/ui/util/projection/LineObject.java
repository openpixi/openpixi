package org.openpixi.pixi.ui.util.projection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * A 3D object of lines
 */
public class LineObject extends AbstractObject {
	private ArrayList<Point> pointlist = new ArrayList<Point>();

	public LineObject() {
	}

	public void clear() {
		pointlist.clear();
		objectlist.clear();
	}

	/** Add point to pointlist.
	 * @return Index of point */
	public Point addPoint(double x, double y, double z) {
		Point p = new Point();
		p.x = x;
		p.y = y;
		p.z = z;
		pointlist.add(p);
		return p;
	}

	public void addLine(Point p1, Point p2, Color color) {
		Line l = new Line();
		l.p1 = p1;
		l.p2 = p2;
		l.color = color;
		objectlist.add(l);
	}

	/** Add a line from point (x1, y1, z1) to (x2, y2, z2) with color */
	public void addLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
		Point p1 = addPoint(x1, y1, z1);
		Point p2 = addPoint(x2, y2, z2);
		addLine(p1, p2, color);
	}

	/** Add a line from point (x, y, z) to (x+dx, y+dy, z+dz) with color */
	public void addLineDelta(double x, double y, double z, double dx, double dy, double dz, Color color) {
		addLine(x, y, z, x + dx, y + dy, z + dz, color);
	}

	public void addCuboid(double sx, double sy, double sz, Color color) {
		Point p1 = addPoint(0, 0, 0);
		Point p2 = addPoint(0, sy, 0);
		Point p3 = addPoint(sx, sy, 0);
		Point p4 = addPoint(sx, 0, 0);
		Point p5 = addPoint(0, 0, sz);
		Point p6 = addPoint(0, sy, sz);
		Point p7 = addPoint(sx, sy, sz);
		Point p8 = addPoint(sx, 0, sz);
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

	public void applyProjection(Projection projection) {
		for (Point p : pointlist) {
			projection.project(p.x, p.y, p.z);
			p.screenX = projection.screenX;
			p.screenY = projection.screenY;
			p.screenZ = projection.screenZ;
		}
	}

	public class Point {
		double x, y, z;
		double screenX, screenY, screenZ;
	}

	public class Line extends PaintObject {
		Point p1;
		Point p2;
		Color color;

		public void paint(Projection projection, Graphics2D graphics, double sx, double sy) {
			applyProjection(projection);

			double x1 = p1.screenX;
			double y1 = p1.screenY;
			double x2 = p2.screenX;
			double y2 = p2.screenY;
			Color c = color;
			graphics.setColor(c);
			graphics.drawLine((int) (x1 * sx), (int) (y1 * sy), (int) (x2 * sx), (int) (y2 * sy));
		}

		public double getDistance() {
			// Use mean distance of line
			return -(p1.screenZ + p2.screenZ) * 0.5;
		}
	}
}
