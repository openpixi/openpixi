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

    public int getDim()
    {
        return this.dim;
    }
    
    public int getNumCells()
    {
        int num = 1;
        for(int i=0;i<dim;i++) {
        	num *= getSize(i);
        }
        return num;
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

	public IntBox copy(){
		IntBox copy = new IntBox(this.dim, this.min.clone(), this.max.clone());
		return copy;
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
