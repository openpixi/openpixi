package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.DoubleBox;

public class PeriodicParticleBoundaryConditions implements IParticleBoundaryConditions
{

    private DoubleBox simulationBox;
    private int numberOfDimensions;

    public PeriodicParticleBoundaryConditions(Simulation s)
    {
		double[] min = new double[s.getNumberOfDimensions()];
		double[] max = new double[s.getNumberOfDimensions()];
		for (int i = 0; i < s.getNumberOfDimensions(); i++) {
			min[i] = 0.0;
			max[i] = s.getSimulationBoxSize(i);
		}

		this.simulationBox = new DoubleBox(numberOfDimensions, min, max);
		this.numberOfDimensions = s.getNumberOfDimensions();
    }

    public void applyOnParticle(IParticle particle)
    {
        for(int i = 0; i < numberOfDimensions; i++)
        {
            double positionShift = (particle.getPosition(i) + simulationBox.getSize(i)) % simulationBox.getSize(i) - particle.getPosition(i);
            particle.addPosition(i, positionShift);

            /*
                If the particle position gets shifted due to periodic boundary conditions, then the previous position should get shifted as well.
                This ensures that
                                dx = x(t) - x(t-dt)
                stays the same during a shift because of the boundary conditions, even if x(t-dt) becomes negative.
             */

            particle.addPrevPosition(i, positionShift);
        }
    }
}
