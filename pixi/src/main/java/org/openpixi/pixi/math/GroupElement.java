package org.openpixi.pixi.math;

public interface GroupElement {

	/**
	 * Adds two GroupElement instances and returns the result as a copy.
	 * The result is not a valid SU(n) matrix in general.
	 * This method does not change the original GroupElement instance.
	 *
	 * @param a GroupElement which is added to the current GroupElement instance.
	 * @return Sum of the GroupElement instances.
	 */
	GroupElement add(GroupElement a);

	void addAssign(GroupElement a);

	/**
	 * Subtracts the passed GroupElement instance from the current instance and returns the result as a copy.
	 * The result is not a valid SU(n) matrix in general.
	 * This method does not change the original GroupElement instance.
	 *
	 * @param a GroupElement which is subtracted from the current GroupElement instance.
	 * @return Difference of the GroupElement instances.
	 */
	GroupElement sub(GroupElement a);

	void subAssign(GroupElement a);

	/**
	 * Applies hermitian conjugation to the current instance of GroupElement and returns a copy.
	 * This method does not change the original GroupElement instance.
	 *
	 * @return  Hermitian conjugate of the current instance.
	 */
	GroupElement adj();

	/**
	 * Applies hermitian conjugation to the current instance of GroupElement without returning a copy.
	 * This method changes the original GroupElement instance.
	 *
	 * @return  Hermitian conjugate of the current instance.
	 */
	void adjAssign();

	/**
	 * Computes the scalar product of the GroupElement instance with a real number and returns a copy.
	 * This method does not change the original GroupElement instance.
	 *
	 * @param number    real number to be multiplied with.
	 * @return          product of the scalar multiplication.
	 */
	GroupElement mult(double number);

	void multAssign(double number);

	/**
	 * Computes the matrix product of the GroupElement instance with another GroupElement instance.
	 * Let A be the GroupElement instance which the method is applied to and let B the GroupElement instance
	 * which is passed as an argument. The result of the multiplication is A.B (post-multiply or multiplication from
	 * the right).
	 * This method does not change the original GroupElement instance.
	 *
	 * @param a GroupElement instance used for post-multiplication.
	 * @return  Result of the multiplication.
	 */
	GroupElement mult(GroupElement a);

	void multAssign(GroupElement a);

	/**
	 * Returns the exact algebra element of the group element. The algebra element generates the group element
	 * via the exponential map.
	 * This also works far from identity but is not always continuous.
	 *
	 * @return  Algebra element of the GroupElement.
	 */
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
	 * where U is the GroupElement, t_a is the a-th generator of the group and u_a is the a-th component of the AlgebraElement.
	 *
	 * This method can also be used to give a linearized algebra element.
	 *
	 * @return AlgebraElement instance of the projection
	 */
	AlgebraElement proj();

	/**
	 * Returns x'th matrix power of the group element. This works for diagonalizable matrices.
	 * The most simple implementation of this uses getAlgebraElement() and getLink().
	 * g.pow(x) should effectively be the same as g.getAlgebraElement().mult(x).getLink().
	 *
	 * @param x	exponent
	 * @return x'th matrix power of the group element
	 */
	GroupElement pow(double x);

	void set(GroupElement a);

	GroupElement copy();

	/**
	 * Returns the number of colors associated with the gauge group, i.e. the N in SU(N).
	 * @return  number of colors of the gauge group
	 */
	int getNumberOfColors();

	/**
	 * Returns the dimension of the adjoint representation of the gauge group, i.e. N^2-1 for SU(N)
	 * @return dimension of the adjoint representation
	 */
	int getAdjointDimension();
}
