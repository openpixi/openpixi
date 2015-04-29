package org.openpixi.pixi.physics.particles;

/**
 * Created by David on 19.04.2015.
 */
public class ImmobileParticle extends Particle
{
	/*
			CONSTRUCTOR
	 */

    public ImmobileParticle()
    {
        super(3, 1);
    }

    public ImmobileParticle(int numberOfDimensions, int numberOfColors)
    {
        super(numberOfDimensions, numberOfColors);
    }

	public double getVelocity(int i)        {   return 0;              }
	public double getAcceleration(int i)    {   return 0;              }

}
