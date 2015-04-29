package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Created by David on 20.04.2015.
 */
public class PeriodicParticleBoundaryConditions implements IParticleBoundaryConditions
{

    private DoubleBox simulationBox;
    private int numberOfDimensions;

    public PeriodicParticleBoundaryConditions(DoubleBox simulationBox, int numberOfDimensions)
    {
        this.simulationBox = simulationBox;
        this.numberOfDimensions = numberOfDimensions;
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
