package org.openpixi.pixi.physics.util;

import java.io.Serializable;

public class IntBox implements Serializable
{

    private int dim;
    private int[] min;
    private int[] max;

    public IntBox(int dim, int[] min, int[] max)
    {
        this.dim = dim;
        this.min = min;
        this.max = max;
    }

	public IntBox(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
    {
        this(3, new int[] {xmin, ymin, zmin}, new int[] {xmax, ymax, zmax});
	}

    public int getDim()
    {
        return this.dim;
    }

    public int getMin(int i)
    {
        return this.min[i];
    }

    public int getMax(int i)
    {
        return this.max[i];
    }

    public int getSize(int i)
    {
        return getMax(i) - getMin(i) + 1;
    }

    /*
        Legacy getters
     */

	public int xmin()               {  return getMin(0);        }
	public int xmax()               {  return getMax(0);        }
	public int xsize()              {  return getSize(0);       }

    public int ymin()               {  return getMin(1);        }
    public int ymax()               {  return getMax(1);        }
    public int ysize()              {  return getSize(1);       }

    public int zmin()               {  return getMin(2);        }
    public int zmax()               {  return getMax(2);        }
    public int zsize()              {  return getSize(2);       }

    public boolean contains(int[] p)
    {
        for(int i = 0; i < this.dim; i++)
        {
            if(p[i] < this.min[i] || p[i] > this.max[i])
                return false;
        }
        return true;
    }

	public boolean contains(int x, int y, int z) {
        return this.contains(new int[] {x,y,z});
	}

	@Override
	public String toString()
    {
        String output = "[";
        for(int i = 0; i < this.dim; i++)
        {
            output += this.min[i] + ", " + this.max[i];
            if(i < this.dim - 1)
                output += ",";
        }
        output += "]";
        return output;
	}
}
