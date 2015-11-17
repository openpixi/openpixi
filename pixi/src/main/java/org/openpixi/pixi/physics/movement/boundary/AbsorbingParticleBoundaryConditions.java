package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * This type of boundary conditions removes particles once they leave the simulation volume.
 */
public class AbsorbingParticleBoundaryConditions implements IParticleBoundaryConditions
{

    private DoubleBox simulationBox;
    private int numberOfDimensions;
	private Simulation s;

    public AbsorbingParticleBoundaryConditions(Simulation s)
    {
		double[] min = new double[s.getNumberOfDimensions()];
		double[] max = new double[s.getNumberOfDimensions()];
		for (int i = 0; i < s.getNumberOfDimensions(); i++) {
			min[i] = 0.0;
			max[i] = s.getSimulationBoxSize(i);
		}

		this.simulationBox = new DoubleBox(numberOfDimensions, min, max);
		this.numberOfDimensions = s.grid.getNumberOfDimensions();
		this.s = s;
    }

    public void applyOnParticle(IParticle particle)
    {
        for(int i = 0; i < numberOfDimensions; i++)
        {
			double x = particle.getPosition(i);
			if(x < simulationBox.getMin(i) || x > simulationBox.getMax(i)) {
				s.particles.remove(particle);
			}
        }
    }
}
