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
            particle.setPosition(i, (particle.getPosition(i) + simulationBox.getSize(i)) % simulationBox.getSize(i));
            //particle.setPrevPosition(i, (particle.getPrevPosition(i) + simulationBox.getSize(i)) % simulationBox.getSize(i));
        }
    }
}
