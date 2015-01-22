package org.openpixi.pixi.ui.util.projection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A 3D object of spheres
 */
public class SphereObject {
	private ArrayList<Sphere> spherelist = new ArrayList<Sphere>();
	private ArrayList<Distance> distancelist = new ArrayList<Distance>();

	public SphereObject() {
	}

	public void clear() {
		spherelist.clear();
	}

	public void addSphere(double x, double y, double z, double r, Color color) {
		Sphere s = new Sphere();
		s.x = x;
		s.y = y;
		s.z = z;
		s.r = r;
		s.color = color;
		spherelist.add(s);
	}

	private void applyProjection(Projection projection) {
		for (Sphere s : spherelist) {
			projection.project(s.x, s.y, s.z);
			s.screenX = projection.screenX;
			s.screenY = projection.screenY;
			s.screenZ = projection.screenZ;
			s.screenScale = projection.screenScale;
		}
	}

	public void sort() {
		distancelist.clear();
		for (Sphere s : spherelist) {
			Distance d = new Distance();
			d.distance = -s.screenZ;
			d.sphere = s;
			distancelist.add(d);
		}

		Collections.sort(distancelist, new DistanceComparator());
	}

	public void paint(Projection projection, Graphics2D graphics, double sx, double sy) {
		applyProjection(projection);
		sort();
		for (Distance d : distancelist) {
			Sphere s = d.sphere;
			double radius = s.screenScale * s.r;
			int width = (int) (2*sx*radius);
			int height = (int) (2*sy*radius);
			graphics.setColor(s.color);
			if(width > 2 && height > 2) {
				graphics.fillOval((int) (s.screenX*sx) - width/2, (int) (s.screenY*sy) - height/2,  width,  height);
			}
			else {
				graphics.drawRect((int) (s.screenX*sx), (int) (s.screenY*sy), 0, 0);
			}
		}
	}

	private class Sphere {
		double x, y, z;
		double r;
		Color color;
		double screenX, screenY, screenZ, screenScale;
	}

	private class Distance {
		double distance;
		Sphere sphere;
	}

	private class DistanceComparator implements Comparator<Distance> {
		@Override
		public int compare(Distance s1, Distance s2) {
			return Double.compare(s1.distance, s2.distance);
		}
	}
}
