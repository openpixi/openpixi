package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;

public class ConstantForce implements Force {


    private int numberOfDimensions;
    private int numberOfColors;
    private int numberOfComponents;

    private double couplingConstant;

    public double[][] E;
    public double[][][] F;

    /*
        Primary constructor
     */

    public ConstantForce(int numberOfDimensions, int numberOfColors, double couplingConstant)
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

        this.E = new double[this.numberOfDimensions][this.numberOfComponents];
        this.F = new double[this.numberOfDimensions][this.numberOfDimensions][this.numberOfComponents];

        reset();
    }

    public ConstantForce(int numberOfDimensions, int numberOfColors, double couplingConstant, double[][] E, double[][][] F)
    {
        this(numberOfDimensions, numberOfColors, couplingConstant);

        this.E = E;
        this.F = F;
    }

    public ConstantForce(Simulation s)
    {
        this(s.getNumberOfDimensions(), s.getNumberOfColors(), s.getCouplingConstant());
    }

	/** New empty force */
	public ConstantForce()
	{
        this(3, 1, 1.0);
	}

    public double getForce(int i, IParticle p)
    {
        double f = 0.0;
        for(int c = 0; c < this.numberOfComponents; c++)
        {
            f += p.getCharge(c) * E[c][i];
            for(int j = 0; j < this.numberOfDimensions; j++)
            {
                f += p.getCharge(c) * p.getVelocity(j) * F[i][j][c];
            }
        }
        f *= this.couplingConstant;
        return f;
    }

	public double getForceX(IParticle p) {
		return  getForce(0, p);
	}

	public double getForceY(IParticle p) {
        return  getForce(1, p);
	}
	
	public double getForceZ(IParticle p) {
        return  getForce(2, p);
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
