package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.RelativisticVelocity;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.force.Force;

public class ConstantForceRelativistic implements Force {


    private int numberOfDimensions;
    private int numberOfColors;
    private int numberOfComponents;

    private double couplingConstant;
    private double speedOfLight;

    private RelativisticVelocity relVelocity;

    public double[][] E;
    public double[][][] F;

    /*
        Primary constructor
     */

    public ConstantForceRelativistic(int numberOfDimensions, int numberOfColors, double couplingConstant, double speedOfLight)
    {
        this.numberOfDimensions = numberOfDimensions;
        this.numberOfColors = numberOfColors;
        this.couplingConstant = couplingConstant;
        this.speedOfLight = speedOfLight;

        if(this.numberOfColors > 1)
        {
            this.numberOfComponents = this.numberOfColors * this.numberOfColors - 1;
        }
        else
        {
            this.numberOfComponents = 1;
        }

        this.relVelocity = new RelativisticVelocity(this.speedOfLight);


        this.E = new double[this.numberOfDimensions][this.numberOfComponents];
        this.F = new double[this.numberOfDimensions][this.numberOfDimensions][this.numberOfComponents];

        reset();
    }

    public ConstantForceRelativistic(int numberOfDimensions, int numberOfColors, double couplingConstant, double speedOfLight, double[][] E, double[][][] F)
    {
        this(numberOfDimensions, numberOfColors, couplingConstant, speedOfLight);

        this.E = E;
        this.F = F;
    }

    public ConstantForceRelativistic(Simulation s)
    {
        this(s.getNumberOfDimensions(), s.getNumberOfColors(), s.getCouplingConstant(), s.getSpeedOfLight());
    }

    /** New empty force */
    public ConstantForceRelativistic()
    {
        this(3, 1, 1.0, 1.0);
    }

    public double getForce(int i, IParticle p)
    {
        double f = 0.0;
		/*
		 double gamma = relVelocity.calculateGamma(p);
        for(int c = 0; c < this.numberOfComponents; c++)
        {
            f += p.getCharge(c) * E[c][i];
            for(int j = 0; j < this.numberOfDimensions; j++)
            {
                f += p.getCharge(c) * p.getVelocity(j) * F[i][j][c] / gamma;
            }
        }
        f *= this.couplingConstant;
        */
        return f;
    }

    public void reset()
    {
        for(int c = 0; c < this.numberOfColors; c++)
        {
            for(int i = 0; i < this.numberOfComponents; i++)
            {
                this.E[i][c] = 0;

                for(int j = 0; j < this.numberOfComponents; j++)
                {
                    this.F[i][j][c] = 0.0;
                }
            }
        }

    }

}
