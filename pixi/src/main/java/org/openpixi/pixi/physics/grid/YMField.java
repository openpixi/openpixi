package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.grid.LinkMatrix;

public abstract class YMField {
	
	protected double[] v;
	
	public abstract YMField add (YMField a);
	
	public abstract void addAssign(YMField a);
	
	public abstract YMField sub (YMField a);
	
	public abstract void set (int j, double value);
	
	public abstract double get (int j);
	
	public abstract double square ();
	
	public abstract YMField mult (double number);

	public abstract void multAssign(double number);
	
	public abstract void set (YMField a);
	
	public abstract void reset ();
	
	public abstract void FieldFromForwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d);
	
	public abstract void FieldFromBackwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d);
	
	public abstract void addfour (YMField a, YMField b, YMField c, YMField d);
	
	public abstract LinkMatrix getLink ();
	
	public abstract LinkMatrix getLinkExact ();

	/**
	 * Returns the projection of the algebra element on to the c'th generator, i.e.
	 *
	 *	tr( A t_c ) = 0.5 A_c,
	 *
	 * if A is the algebra element and t_c is the generator. Remember that tr( t_a t_b ) = 0.5 \delta_{ab}.
	 *
	 * @param c	Number of the generator
	 * @return	Projection onto generator
	 */
	public abstract double proj(int c);

}
