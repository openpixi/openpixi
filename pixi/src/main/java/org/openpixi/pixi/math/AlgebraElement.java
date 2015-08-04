package org.openpixi.pixi.math;

public interface AlgebraElement {

	/**
	 * Adds two AlgebraElement instances and returns the result as a copy.
	 * This method does not change the original AlgebraElement instance.
	 *
	 * @param a AlgebraElement which is added to the current AlgebraElement instance.
	 * @return Sum of the AlgebraElement instances.
	 */
	AlgebraElement add (AlgebraElement a);
	
	void addAssign(AlgebraElement a);

	/**
	 * Subtracts the passed AlgebraElement instance from the current instance and returns the result as a copy.
	 * This method does not change the original AlgebraElement instance.
	 *
	 * @param a AlgebraElement which is subtracted from the current AlgebraElement instance.
	 * @return Difference of the AlgebraElement instances.
	 */
	AlgebraElement sub (AlgebraElement a);

	/**
	 * Set coefficient of jth algebra generator to value.
	 *
	 * @param j Index of generator to be set.
	 * @param value Value to set generator coefficient.
	 */
	void set (int j, double value);

	/**
	 * Get coefficient of jth algebra generator.
	 *
	 * @param j Index of generator to get.
	 * @return Value of generator coefficient.
	 */
	double get (int j);

	/**
	 * The square of the algebra element, defined as 2 tr(A^2).
	 *
	 * @return 2 tr(A^2).
	 */
	double square ();

	/**
	 * Computes the scalar product of the AlgebraElement instance with a real number and returns a copy.
	 * This method does not change the original AlgebraElement instance.
	 *
	 * @param number    real number to be multiplied with.
	 * @return          product of the scalar multiplication.
	 */
	AlgebraElement mult (double number);

	void multAssign(double number);
	
	void set (AlgebraElement a);
	
	void reset ();

	/**
	 * Calculate linearized exponential map essentially just using exp(i A) ~ 1 + i A.
	 * Note: not necessarily an SU(n) element!
	 *
	 * @return GroupElement calculated via linearized exponential map.
	 */
	GroupElement getLinearizedLink();

	/**
	 * Calculate exponential map of algebra element to get group element.
	 *
	 * @return GroupElement calculated via exponential map.
	 */
	GroupElement getLink();

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

	AlgebraElement copy();
}
