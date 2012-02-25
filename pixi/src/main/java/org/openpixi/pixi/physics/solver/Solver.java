package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.Force;
import org.openpixi.pixi.physics.Particle2D;

public abstract class Solver {
	
	public Solver()
	{
		
	}
	
	public void step(Particle2D p, Force f, double step)
	{
		
	}
	
	public void prepare(Particle2D p, Force f, double step)
	{
		
	}
	
	public void complete(Particle2D p, Force f, double step)
	{
		
	}
}
