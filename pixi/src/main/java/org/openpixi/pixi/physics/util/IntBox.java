package org.openpixi.pixi.physics.util;

import java.io.Serializable;

public class IntBox implements Serializable {
	private int xmin;
	private int xmax;
	private int ymin;
	private int ymax;
	private int zmin;
	private int zmax;

	public IntBox(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zmin = zmin;
		this.zmax = zmax;
	}

	public int xmin() {
		return xmin;
	}

	public int xsize() {
		return xmax - xmin + 1;
	}

	public int xmax() {
		return xmax;
	}

	public int ymin() {
		return ymin;
	}

	public int ymax() {
		return ymax;
	}

	public int ysize() {
		return ymax - ymin + 1;
	}
	
	public int zmin() {
		return zmin;
	}

	public int zmax() {
		return zmax;
	}

	public int zsize() {
		return zmax - zmin + 1;
	}

	public boolean contains(int x, int y, int z) {
		if (xmin <= x && x <= xmax && ymin <= y && y <= ymax && zmin <= z && z <= zmax) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns distance of a given point to the border of this box.
	 * Point lying on xmin, xmax, ymin or ymax has distance 0.
	 * Does not distinguish whether the point lies outside the box or inside.
	 */
	public int distanceFromBorder(int x, int y, int z) {
		int xmind = Math.min(Math.abs(x - xmin), Math.abs(x - xmax));
		int ymind = Math.min(Math.abs(y - ymin), Math.abs(y - ymax));
		int zmind = Math.min(Math.abs(z - zmin), Math.abs(z - zmax));
		return Math.min(xmind, Math.min(ymind, zmind) );
	}

	@Override
	public String toString() {
		return String.format("[%d,%d,%d,%d,%d,%d]", xmin, xmax, ymin, ymax, zmin, zmax);
	}
}
