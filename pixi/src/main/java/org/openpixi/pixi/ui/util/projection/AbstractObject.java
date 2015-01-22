package org.openpixi.pixi.ui.util.projection;

import java.util.ArrayList;

public class AbstractObject {

	/** Contains a list of all objects that can be painted. */
	protected ArrayList<PaintObject> objectlist = new ArrayList<PaintObject>();

	public void applyProjection(Projection projection) {
	}

	public ArrayList<PaintObject> getObjects() {
		return objectlist;
	}
}
