package org.openpixi.pixi.diagnostics.methods;

import org.openpixi.pixi.diagnostics.GridDataOutput;
import org.openpixi.pixi.physics.grid.Grid;

public interface GridMethod {

	public void calculate(Grid g);
	
	public void getData(GridDataOutput out);
}
