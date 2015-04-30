package org.openpixi.pixi.physics.particles;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;

/**
 * Created by dmueller on 4/28/15.
 */
public class ParticleTest
{
    @Test
    public void testParticleClass()
    {
        double accuracy = 1.E-13;

        int numberOfDimensions = 4;
        int numberOfColors = 3;
        int numberOfComponents = 1;
        if(numberOfColors > 1)
            numberOfComponents = numberOfColors * numberOfColors - 1;

        Particle p = new Particle(numberOfDimensions, numberOfColors);

        /*
                Tests for getters and setters
         */

        //  Initialization

        double[] position       = new double[numberOfDimensions];
        double[] prevpos        = new double[numberOfDimensions];
        double[] velocity       = new double[numberOfDimensions];
        double[] acceleration   = new double[numberOfDimensions];

        double[] charge         = new double[numberOfComponents];
        double mass = Math.random()*10.0;

        double[][] E            = new double[numberOfDimensions][numberOfComponents];
        double[][][] F          = new double[numberOfDimensions][numberOfDimensions][numberOfComponents];


        for(int i = 0; i < numberOfDimensions; i++)
        {
            position[i]     = Math.random()*10.0;
            prevpos[i]      = Math.random()*10.0;
            velocity[i]     = Math.random()*10.0;
            acceleration[i] = Math.random()*10.0;

            p.setPosition(i, position[i]);
            p.setPrevPosition(i, prevpos[i]);
            p.setVelocity(i, velocity[i]);
            p.setAcceleration(i, acceleration[i]);
        }

        for(int c = 0; c < numberOfComponents; c++)
        {
            charge[c] = Math.random() * 10.0;
            p.setCharge(c, charge[c]);
        }

        p.setMass(mass);

        for(int i = 0; i < numberOfDimensions; i++)
        {
            for(int c = 0; c < numberOfComponents; c++)
            {
                E[i][c] = Math.random() - 0.5;
                p.setE(i, c, E[i][c]);
            }
        }

        for(int i = 0; i < numberOfDimensions; i++)
        {
            for (int j = 0; j < numberOfDimensions; j++)
            {
                for (int c = 0; c < numberOfComponents; c++)
                {
                    F[i][j][c] = Math.random() - 0.5;
                    p.setF(i, j, c, F[i][j][c]);
                }
            }
        }

        //  Asserts

        Assert.assertEquals(p.getNumberOfDimensions(), numberOfDimensions);
        Assert.assertEquals(p.getNumberOfColors(), numberOfColors);
        Assert.assertEquals(p.getNumberOfComponents(), numberOfComponents);

        for(int i = 0; i < numberOfDimensions; i++)
        {
            Assert.assertEquals(p.getPosition(i), position[i], accuracy);
            Assert.assertEquals(p.getPrevPosition(i), prevpos[i], accuracy);
            Assert.assertEquals(p.getVelocity(i), velocity[i], accuracy);
            Assert.assertEquals(p.getAcceleration(i), acceleration[i], accuracy);
        }

        for(int c = 0; c < numberOfComponents; c++)
        {
            Assert.assertEquals(p.getCharge(c), charge[c], accuracy);
        }

        Assert.assertEquals(p.getMass(), mass, accuracy);

        for(int i = 0; i < numberOfDimensions; i++)
        {
            for(int c = 0; c < numberOfComponents; c++)
            {
                Assert.assertEquals(p.getE(i, c), E[i][c], accuracy);
            }
        }

        for(int i = 0; i < numberOfDimensions; i++)
        {
            for (int j = 0; j < numberOfDimensions; j++)
            {
                for (int c = 0; c < numberOfComponents; c++)
                {
                    Assert.assertEquals(p.getF(i,j,c), F[i][j][c], accuracy);
                }
            }
        }

        /*
                Tests for adding methods
         */

        double[] deltaPosition = new double[numberOfDimensions];
        double[] deltaPrevPos = new double[numberOfDimensions];
        double[] deltaVelocity = new double[numberOfDimensions];
        double[] deltaAcceleration = new double[numberOfDimensions];

        for(int i = 0; i < numberOfDimensions; i++)
        {
            deltaPosition[i] = (Math.random() - 0.5) * 20.0;
            deltaPrevPos[i] = (Math.random() - 0.5) * 20.0;
            deltaVelocity[i] = (Math.random() - 0.5) * 20.0;
            deltaAcceleration[i] = (Math.random() - 0.5) * 20.0;

            p.addPosition(i, deltaPosition[i]);
            p.addPrevPosition(i, deltaPrevPos[i]);
            p.addVelocity(i, deltaVelocity[i]);
            p.addAcceleration(i, deltaAcceleration[i]);
        }

        //  Asserts

        for(int i = 0; i < numberOfDimensions; i++)
        {
            Assert.assertEquals(p.getPosition(i), position[i] + deltaPosition[i], accuracy);
            Assert.assertEquals(p.getPrevPosition(i), prevpos[i] + deltaPrevPos[i], accuracy);
            Assert.assertEquals(p.getVelocity(i), velocity[i] + deltaVelocity[i], accuracy);
            Assert.assertEquals(p.getAcceleration(i), acceleration[i] + deltaAcceleration[i], accuracy);
        }

        /*
                Test for the storePosition() method
         */

        p.storePosition();
        for(int i = 0; i < numberOfDimensions; i++)
        {
            Assert.assertEquals(p.getPrevPosition(i), p.getPosition(i), accuracy);
        }

        /*
                Test for the copy() method
         */

        IParticle p2 = p.copy();

        Assert.assertEquals(p2.getNumberOfDimensions(), p.getNumberOfDimensions());
        Assert.assertEquals(p2.getNumberOfComponents(), p.getNumberOfComponents());
        Assert.assertEquals(p2.getNumberOfColors(), p.getNumberOfColors());

        for(int i = 0; i < numberOfDimensions; i++)
        {
            Assert.assertEquals(p2.getPosition(i), p.getPosition(i), accuracy);
            Assert.assertEquals(p2.getPrevPosition(i), p.getPrevPosition(i), accuracy);
            Assert.assertEquals(p2.getVelocity(i), p.getVelocity(i), accuracy);
            Assert.assertEquals(p2.getAcceleration(i), p.getAcceleration(i), accuracy);

            for(int c = 0; c < numberOfColors; c++)
            {
                Assert.assertEquals(p2.getCharge(c), p.getCharge(c), accuracy);
                Assert.assertEquals(p2.getE(i, c), p.getE(i, c), accuracy);
                for(int j = 0; j < numberOfDimensions; j++)
                {
                    Assert.assertEquals(p2.getF(i, j, c), p.getF(i, j, c), accuracy);
                }
            }
        }

        Assert.assertEquals(p2.getMass(), p.getMass(), accuracy);

        /*
                Test for the display parameters.
         */

        Color color = new Color(5,23,0);
        double radius = Math.random();

        p.setColor(color);
        p.setRadius(radius);

        Assert.assertEquals(p.getRadius(), radius, accuracy);
        Assert.assertEquals(p.getColor().getRGB(), color.getRGB());

    }

}
