package org.openpixi.pixi.diagnostics;

import org.openpixi.pixi.physics.grid.Grid;

public interface GridMethod {

	public void calculate(Grid g);
	
	public void getData(GridDataOutput out);
}
