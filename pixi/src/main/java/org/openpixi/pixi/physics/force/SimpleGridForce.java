package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;


public class SimpleGridForce implements Force {


    private int numberOfDimensions;
    private int numberOfColors;
    private int numberOfComponents;

    private double couplingConstant;

    public SimpleGridForce(Simulation s)
    {
        this(s.getNumberOfDimensions(), s.getNumberOfColors(), s.getCouplingConstant());
    }

    public SimpleGridForce(int numberOfDimensions, int numberOfColors, double couplingConstant)
    {
        this.numberOfDimensions = numberOfDimensions;
        this.numberOfColors = numberOfColors;
        this.couplingConstant = couplingConstant;

        if(this.numberOfColors > 1)
        {
            this.numberOfComponents = this.numberOfColors * this.numberOfColors - 1;
        }
        else
        {
            this.numberOfComponents = 1;
        }
    }

    public SimpleGridForce()
    {
        this(3, 1, 1.0);
    }

    public double getForce(int i, IParticle p)
    {
        double f = 0.0;
		/*
        for(int c = 0; c < this.numberOfComponents; c++)
        {
            f += p.getCharge(c) * p.getE(i, c);
            for(int j = 0; j < this.numberOfDimensions; j++)
            {
                f += p.getCharge(c) * p.getVelocity(j) * p.getF(i,j,c);
            }
        }
        f *= this.couplingConstant;
         */
        return f;
    }

}
