package org.openpixi.pixi.math;

public class SU2AlgebraElement implements AlgebraElement {

	protected double v0, v1, v2;
	
	public SU2AlgebraElement() {
			
		v0 = 0;
		v1 = 0;
		v2 = 0;
			
	}
	
	public SU2AlgebraElement(double a, double b, double c) {
		
		v0 = a;
		v1 = b;
		v2 = c;
		
	}
	
	public void reset () {
		
		v0 = 0;
		v1 = 0;
		v2 = 0;
		
	}
	
	public AlgebraElement add (AlgebraElement arg) {

		SU2AlgebraElement a = (SU2AlgebraElement) arg;
		SU2AlgebraElement b = new SU2AlgebraElement();

		b.v0 = v0+a.v0;
		b.v1 = v1+a.v1;
		b.v2 = v2+a.v2;
		return b;
		
	}
	
	public void addAssign(AlgebraElement arg) {

		SU2AlgebraElement a = (SU2AlgebraElement) arg;

		v0 += a.v0;
		v1 += a.v1;
		v2 += a.v2;
		
	}
	
	public AlgebraElement sub (AlgebraElement arg) {

		SU2AlgebraElement a = (SU2AlgebraElement) arg;
		
		SU2AlgebraElement b = new SU2AlgebraElement();
		b.v0 = v0-a.v0;
		b.v1 = v1-a.v1;
		b.v2 = v2-a.v2;
		return b;
		
	}
	
	public void set (int j, double value) {
		
		switch (j) {
			case 0:  v0 = value;
				break;
			case 1:  v1 = value;
				break;
			case 2:  v2 = value;
				break;
		}
		
	}
	
	public double get (int j) {

		double b=0;
		switch (j) {
			case 0:  b = v0;
				break;
			case 1:  b = v1;
				break;
			case 2:  b = v2;
				break;
		}

		return b;
		
	}
	
	public double square () {
		
		return v0*v0+v1*v1+v2*v2;
		
	}
	
	public AlgebraElement mult (double number) {
		
		SU2AlgebraElement b = new SU2AlgebraElement();
		b.v0 = v0*number;
		b.v1 = v1*number;
		b.v2 = v2*number;
		return b;
		
	}

	public void multAssign(double number) {

		this.v0 *= number;
		this.v1 *= number;
		this.v2 *= number;

	}

	public double mult (AlgebraElement arg) {
		SU2AlgebraElement a = (SU2AlgebraElement) arg;
		return v0*a.v0+v1*a.v1+v2*a.v2;
	}

	public void set (AlgebraElement arg) {

		SU2AlgebraElement a = (SU2AlgebraElement) arg;
		
		v0 = a.v0;
		v1 = a.v1;
		v2 = a.v2;
		
	}
	
	public GroupElement getLinearizedLink() {
		
		double sum = (v0*v0+v1*v1+v2*v2)/4;
		SU2GroupElement b = new SU2GroupElement(Math.sqrt(1.0 - sum), v0/2, v1/2, v2/2);
		return b;
	}
	
	public GroupElement getLink() {
		
		double sum = v0*v0+v1*v1+v2*v2;
		double mod = Math.sqrt(sum)/2;
		double sinfakt;
		if(mod < 1.E-20) {
			sinfakt = 0.0;
		} else {
			sinfakt = 0.5/mod*Math.sin(mod);
		}
		SU2GroupElement b = new SU2GroupElement(Math.cos(mod), v0*sinfakt, v1*sinfakt, v2*sinfakt);
		return b;
	}

	public double proj(int c)
	{
		return 0.5 * get(c);
	}

	public AlgebraElement act(GroupElement g) {

		SU2GroupElement u = (SU2GroupElement) g;
		SU2GroupElement Xm = new SU2GroupElement();

		Xm.set(0, 0.0);
		Xm.set(1, v0 / 2);
		Xm.set(2, v1 / 2);
		Xm.set(3, v2 / 2);

		Xm = (SU2GroupElement) u.mult(Xm.mult(u.adj()));
		return Xm.proj();

	}

	public void actAssign(GroupElement g) {
		this.set(this.act(g));
	}

	public AlgebraElement copy() {
		return new SU2AlgebraElement(v0, v1, v2);
	}

	public int getNumberOfColors() {return 2;}

	public int getNumberOfComponents() {return 3;}
}
