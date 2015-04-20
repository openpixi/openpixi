package org.openpixi.pixi.physics.particles;

import java.awt.Color;
import java.io.Serializable;

public class Particle implements IParticle, Serializable
{
	protected int     numberOfDimensions;
	protected int     numberOfColors;
	protected int     numberOfComponents;
	protected double  charge[];
	protected double  mass;

	protected double  pos[];
	protected double  prevPos[];
	protected double  vel[];
	protected double  acc[];

	protected double  E[][];
	protected double  F[][][];

	protected double  r;
	protected Color   col;

	/*
			CONSTRUCTOR
	 */

	public Particle()
	{
		this(3, 1);
	}

	public Particle(int numberOfDimensions, int numberOfColors)
	{
		this.setNumberOfDimensions(numberOfDimensions);
		this.setNumberOfColors(numberOfColors);
	}

	/*
			GETTERS
	 */

	public double getPosition(int i)        {   return pos[i];              }
	public double getPrevPosition(int i)    {   return prevPos[i];          }
	public double getVelocity(int i)        {   return vel[i];              }
	public double getAcceleration(int i)    {   return acc[i];              }

	public double getE(int i, int c)        {   return E[i][c];             }
	public double getF(int i, int j, int c) {   return F[i][j][c];          }

	public double getCharge(int c)          {   return charge[c];           }
	public double getMass()                 {   return mass;                }

	public double getRadius()               {   return r;                   }
	public Color getColor()                 {   return col;                 }

	/*
		Legacy getters
	 */

	public double getX()                    {   return getPosition(0);      }
	public double getY()                    {   return getPosition(1);      }
	public double getZ()                    {   return getPosition(2);      }

	public double getPrevX()                {   return getPrevPosition(0);  }
	public double getPrevY()                {   return getPrevPosition(1);  }
	public double getPrevZ()                {   return getPrevPosition(2);  }

	public double getVx()                   {   return getVelocity(0);      }
	public double getVy()                   {   return getVelocity(1);      }
	public double getVz()                   {   return getVelocity(2);      }

	public double getAx()                   {   return getAcceleration(0);  }
	public double getAy()                   {   return getAcceleration(1);  }
	public double getAz()                   {   return getAcceleration(2);  }

	public double getEx()                   {   return getE(0, 0);          }
	public double getEy()                   {   return getE(1, 0);          }
	public double getEz()                   {   return getE(2, 0);          }

	public double getBx()                   {   return getF(2, 1, 0);       }
	public double getBy()                   {   return getF(0, 2, 0);       }
	public double getBz()                   {   return getF(1, 0, 0);       }

	public double getCharge()               {   return getCharge(0);        }

	/*
			SETTERS
	 */

	public void setPosition(int i, double value)                {   this.pos[i] = value;            }
	public void addPosition(int i, double value)                {   this.pos[i] += value;           }

	public void setPrevPosition(int i, double value)            {   this.prevPos[i] = value;        }
	public void addPrevPosition(int i, double value)            {   this.prevPos[i] += value;       }

	public void setVelocity(int i, double value)                {   this.vel[i] = value;            }
	public void addVelocity(int i, double value)                {   this.vel[i] += value;           }

	public void setAcceleration(int i, double value)            {   this.acc[i] = value;            }
	public void addAcceleration(int i, double value)            {   this.acc[i] += value;           }

	public void setE(int i, int c, double E)                    {   this.E[i][c] = E;               }
	public void setF(int i, int j, int c, double F)             {   this.F[i][j][c] = F;            }

	public void setNumberOfColors(int numberOfColors)
	{
		this.numberOfColors = numberOfColors;
		if(this.numberOfColors > 1)
			this.numberOfComponents = numberOfColors * numberOfColors - 1;
		else
			this.numberOfComponents = 1;

		this.charge = new double[this.numberOfComponents];

		if(this.numberOfDimensions > 0)
			initializeFields();
	}

	public void setNumberOfDimensions(int numberOfDimensions)
	{
		this.numberOfDimensions = numberOfDimensions;

		this.pos = new double[this.numberOfDimensions];
		this.prevPos = new double[this.numberOfDimensions];
		this.vel = new double[this.numberOfDimensions];
		this.acc = new double[this.numberOfDimensions];

		if(this.numberOfColors > 0)
			initializeFields();
	}

	public void setCharge(int c, double q)
	{
		this.charge[c] = q;
	}

	public void setMass(double m)
	{
		this.mass = m;
	}

	public void setRadius(double r)
	{
		this.r = r;
	}

	public void setColor(Color color)
	{
		this.col = color;
	}
	
	/*
		Legacy setters
	 */

	public void setX(double x)              {   this.setPosition(0, x);                 }
	public void addX(double x)              {   this.addPosition(0, x);                 }
	public void setPrevX(double x)          {   this.setPrevPosition(0, x);             }
	public void addPrevX(double x)          {   this.addPrevPosition(0, x);             }

	public void setY(double y)              {   this.setPosition(1, y);                 }
	public void addY(double y)              {   this.addPosition(1, y);                 }
	public void setPrevY(double prevY)      {   this.setPrevPosition(1, prevY);         }
	public void addPrevY(double prevY)      {   this.addPrevPosition(1, prevY);         }

	public void setZ(double z)              {   this.setPosition(2, z);                 }
	public void addZ(double z)              {   this.addPosition(2, z);                 }
	public void setPrevZ(double prevZ)      {   this.setPrevPosition(2, prevZ);         }
	public void addPrevZ(double prevZ)      {   this.addPrevPosition(2, prevZ);         }

	public void setVx(double vx)            {   this.setVelocity(0, vx);                }
	public void setVy(double vy)            {   this.setVelocity(1, vy);                }
	public void setVz(double vz)            {   this.setVelocity(2, vz);                }

	public void setAx(double ax)            {   this.setAcceleration(0, ax);            }
	public void setAy(double ay)            {   this.setAcceleration(1, ay);            }
	public void setAz(double az)            {   this.setAcceleration(2, az);            }

	public void setCharge(double q)         {    this.setCharge(0, q);                  }

	public void setEx(double Ex)            {   this.setE(0, 0, Ex);                    }
	public void setEy(double Ey)            {   this.setE(1, 0, Ey);                    }
	public void setEz(double Ez)            {   this.setE(2, 0, Ez);                    }

	public void setBx(double Bx)
	{
		this.setF(2, 1, 0, Bx);
		this.setF(1, 2, 0, -Bx);
	}
	public void setBy(double By)
	{
		this.setF(0, 2, 0, By);
		this.setF(2, 0, 0, -By);
	}
	public void setBz(double Bz)
	{
		this.setF(1, 0, 0, Bz);
		this.setF(0, 1, 0, -Bz);
	}

	public void storePosition()
	{
		prevPos = pos.clone();
	}

	public IParticle copy()
	{
		Particle p = new Particle();

		p.setNumberOfDimensions(this.numberOfDimensions);
		p.setNumberOfColors(this.numberOfColors);

		for (int i = 0; i < this.numberOfDimensions; i++)
		{
			p.setPosition(i, this.pos[i]);
			p.setPrevPosition(i, this.prevPos[i]);
			p.setVelocity(i, this.vel[i]);
			p.setAcceleration(i, this.acc[i]);
		}



		p.setMass(this.mass);
		p.setRadius(this.r);
		p.setColor(this.col);

		for (int c = 0; c < this.numberOfComponents; c++)
		{
			p.setCharge(c, this.charge[c]);
			for(int i = 0; i < this.numberOfDimensions; i++)
			{
				p.setE(i, c, this.E[i][c]);
				for(int j = 0; j < this.numberOfDimensions; j++)
				{
					p.setF(i, j, c, this.F[i][j][c]);
				}
			}
		}

		return p;
	}

	public String toString()
	{
		String output = "[";
		for(int i = 0; i < this.numberOfDimensions; i++)
		{
			output += this.pos[i];
			if(i < this.numberOfDimensions - 1)
				output += ",";
		}
		output += "]";
		return output;
	}

	private void initializeFields()
	{
		this.E = new double[this.numberOfDimensions][this.numberOfComponents];
		this.F = new double[this.numberOfDimensions][this.numberOfDimensions][this.numberOfComponents];
	}
}
