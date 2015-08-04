package org.openpixi.pixi.physics.grid;

public interface LinkMatrix {

	LinkMatrix add(LinkMatrix a);

	LinkMatrix sub(LinkMatrix a);

	LinkMatrix adj();
	
	void selfadj();

	LinkMatrix mult(double number);

	LinkMatrix mult(LinkMatrix a);

	YMField getAlgebraElement();

	YMField getLinearizedAlgebraElement();

	double getTrace();

	/**
	 * Returns the projection of the matrix onto the generators of the group as a YMField. This is done via the formula
	 *
	 *      u_a = 2 Im {tr t_a U},
	 *
	 * where U is the SU3Matrix, t_a is the a-th generator of the group and u_a is the a-th component of the YMField.
	 *
	 * @return YMField instance of the projection
	 */
	YMField proj();

	void set(LinkMatrix a);
}
