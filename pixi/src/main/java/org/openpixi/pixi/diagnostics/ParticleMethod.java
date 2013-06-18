package org.openpixi.pixi.diagnostics;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Grid;

public interface ParticleMethod {
	
	public void calculate(ArrayList<Particle> particles);
	
	public void getData(ParticleDataOutput out);

}
