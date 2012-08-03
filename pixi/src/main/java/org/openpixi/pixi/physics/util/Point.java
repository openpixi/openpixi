package org.openpixi.pixi.physics.util;

import java.io.Serializable;

public class Point implements Serializable {
	public int x;
	public int y;


	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}


	@Override
	public boolean equals(Object obj) {
		Point p2 = (Point)obj;
		if (p2.x == this.x && p2.y == this.y) {
			return true;
		}
		else {
			return false;
		}
	}


	@Override
	public String toString() {
		return String.format("[%d,%d]", this.x, this.y);
	}
}
