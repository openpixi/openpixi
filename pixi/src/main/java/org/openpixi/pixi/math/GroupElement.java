package org.openpixi.pixi.math;

public interface GroupElement {

	GroupElement add(GroupElement a);

	GroupElement sub(GroupElement a);

	GroupElement adj();
	
	void selfadj();

	GroupElement mult(double number);

	GroupElement mult(GroupElement a);

	AlgebraElement getAlgebraElement();

	/**
	 * Returns the real trace of the matrix.
	 *
	 * @return	Real part of trace of the matrix.
	 */
	double getRealTrace();

	/**
	 * Returns the projection of the matrix onto the generators of the group as a AlgebraElement. This is done via the formula
	 *
	 *      u_a = 2 Im {tr t_a U},
	 *
	 * where U is the SU3Matrix, t_a is the a-th generator of the group and u_a is the a-th component of the AlgebraElement.
	 *
	 * @return AlgebraElement instance of the projection
	 */
	AlgebraElement proj();

	void set(GroupElement a);

	GroupElement copy();
}
