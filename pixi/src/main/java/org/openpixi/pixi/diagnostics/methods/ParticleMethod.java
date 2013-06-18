package org.openpixi.pixi.diagnostics.methods;

import java.util.ArrayList;

import org.openpixi.pixi.diagnostics.ParticleDataOutput;
import org.openpixi.pixi.physics.Particle;

public interface ParticleMethod {
	
	public void calculate(ArrayList<Particle> particles);
	
	public void getData(ParticleDataOutput out);

}
