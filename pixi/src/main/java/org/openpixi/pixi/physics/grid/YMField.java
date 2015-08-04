package org.openpixi.pixi.physics.grid;

public interface YMField {
	
	YMField add (YMField a);
	
	void addAssign(YMField a);
	
	YMField sub (YMField a);

	/**
	 * Set coefficient of jth algebra generator to value
	 * @param j
	 * @param value
	 */
	void set (int j, double value);

	/**
	 * Get coefficient of jth algebra generator
	 * @param j
	 * @return
	 */
	double get (int j);
	
	double square ();
	
	YMField mult (double number);

	void multAssign(double number);
	
	void set (YMField a);
	
	void reset ();
	
	LinkMatrix getLinearizedLink();
	
	LinkMatrix getLink();

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
	double proj(int c);

}
