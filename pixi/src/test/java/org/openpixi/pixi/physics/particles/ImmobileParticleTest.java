package org.openpixi.pixi.physics.particles;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dmueller on 4/28/15.
 */
public class ImmobileParticleTest
{
    @Test
    public void testImmobileParticleClass()
    {
        double accuracy = 1.E-13;

        int numberOfDimensions = 4;
        int numberOfColors = 3;
        int numberOfComponents = 1;
        if(numberOfColors > 1)
            numberOfComponents = numberOfColors * numberOfColors - 1;

        ImmobileParticle p = new ImmobileParticle(numberOfDimensions, numberOfColors);

        /*
                Tests for getters and setters
         */

        //  Initialization

        double[] velocity       = new double[numberOfDimensions];
        double[] acceleration   = new double[numberOfDimensions];

        for(int i = 0; i < numberOfDimensions; i++)
        {
            velocity[i]     = Math.random()*10.0;
            acceleration[i] = Math.random()*10.0;

            p.setVelocity(i, velocity[i]);
            p.setAcceleration(i, acceleration[i]);
        }

        for(int i = 0; i < numberOfDimensions; i++)
        {
            Assert.assertEquals(p.getVelocity(i), 0.0, accuracy);
            Assert.assertEquals(p.getAcceleration(i), 0.0, accuracy);
        }
    }
}
