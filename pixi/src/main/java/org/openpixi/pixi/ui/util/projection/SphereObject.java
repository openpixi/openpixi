package org.openpixi.pixi.ui.util.projection;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A 3D object of spheres
 */
public class SphereObject extends AbstractObject {

	public SphereObject() {
	}

	public void clear() {
		objectlist.clear();
	}

	public void addSphere(double x, double y, double z, double r, Color color) {
		Sphere s = new Sphere();
		s.x = x;
		s.y = y;
		s.z = z;
		s.r = r;
		s.color = color;
		objectlist.add(s);
	}

	public void applyProjection(Projection projection) {
		for (PaintObject o : objectlist) {
			Sphere s = (Sphere) o;
			projection.project(s.x, s.y, s.z);
			s.screenX = projection.screenX;
			s.screenY = projection.screenY;
			s.screenZ = projection.screenZ;
			s.screenScale = projection.screenScale;
		}
	}

	public class Sphere extends PaintObject {
		double x, y, z;
		double r;
		Color color;
		double screenX, screenY, screenZ, screenScale;

		public void paint(Projection projection, Graphics2D graphics) {
			double radius = screenScale * r;
			int width = (int) (2*radius);
			int height = (int) (2*radius);
			graphics.setColor(color);
			if(width > 2 && height > 2) {
				graphics.fillOval((int) (screenX) - width/2, (int) (screenY) - height/2,  width,  height);
			}
			else {
				graphics.drawRect((int) (screenX), (int) (screenY), 0, 0);
			}
		}

		public double getDistance() {
			return -screenZ; // TODO: subtract radius?
		}
	}

}
