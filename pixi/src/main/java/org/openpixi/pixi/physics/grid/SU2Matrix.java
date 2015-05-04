package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.YMField;
import org.openpixi.pixi.physics.grid.LinkMatrix;

public class SU2Matrix extends LinkMatrix {

	private double[] e;
	
	public SU2Matrix () {

		e = new double[4];

		for(int i = 0; i < 4; i++)
		{
			e[i] = 0.0;
		}
	}
	
	public SU2Matrix (double a, double b, double c, double d) {

		e = new double[4];

		e[0] = a;
		e[1] = b;
		e[2] = c;
		e[3] = d;
		
	}

	public LinkMatrix add (LinkMatrix a) {
		
		SU2Matrix b = new SU2Matrix();
		for(int i=0; i < 4; i++)
		{
			b.set(i, e[i] + a.get(i));
		}
		return b;
		
	}
	
	public LinkMatrix sub (LinkMatrix a) {
		
		SU2Matrix b = new SU2Matrix();
		for(int i=0; i < 4; i++)
		{
			b.set(i, e[i] - a.get(i));
		}
		return b;
		
	}
	
	public void set (int j, double value) {
		
		e[j] = value;
		
	}
	
	public double get (int j) {

		return e[j];
		
	}
	
	public void adj () {

		for(int i = 1; i < 4; i++)
		{
			e[i] = -e[i];
		}
	}
	
	public void computeFirstParameter() {
		
		double sum = e[1]*e[1]+e[2]*e[2]+e[3]*e[3];
		if (sum>1)
		{
			System.out.println("Parameters too large!\n");
		}
		else
		{
			e[0]=Math.sqrt(1.0-sum);
		}
	}
	
	public double computeParameterNorm() {
		
		return e[0]*e[0]+e[1]*e[1]+e[2]*e[2]+e[3]*e[3];
		
	}
	
	public LinkMatrix mult (double number) {
		
		SU2Matrix b = new SU2Matrix();
		for(int i = 0; i < 4; i++)
		{
			b.set(i, e[i] * number);
		}
		return b;
		
	}
	
	public LinkMatrix mult (LinkMatrix a) {
		
		SU2Matrix b = new SU2Matrix();
		b.e[0] = e[0]*a.get(0)-e[1]*a.get(1)-e[2]*a.get(2)-e[3]*a.get(3);
		b.e[1] = e[0]*a.get(1)+e[1]*a.get(0)-e[2]*a.get(3)+e[3]*a.get(2);
		b.e[2] = e[0]*a.get(2)+e[2]*a.get(0)-e[3]*a.get(1)+e[1]*a.get(3);
		b.e[3] = e[0]*a.get(3)+e[3]*a.get(0)-e[1]*a.get(2)+e[2]*a.get(1);
		return b;
		
	}
	
	public YMField getField () {
		return new SU2Field(e[1]*2, e[2]*2, e[3]*2);
	}
	
	public void set (LinkMatrix a) {

		for(int i = 0; i < 4; i++)
		{
			e[i] = a.get(i);
		}
	}

}
