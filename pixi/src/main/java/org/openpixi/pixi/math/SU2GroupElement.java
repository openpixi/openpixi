package org.openpixi.pixi.math;

/**
 * This class implements a parametrization of the group SU(2).
 * A SU(2) element U is defined by a normalized four-component vector e_i via
 *
 *      U   =   e_0 * 1 + i e_i \sigma_i,
 *
 * where \sigma_i is the i-th Pauli matrix.
 *
 */
public class SU2GroupElement implements GroupElement {

	private double e0, e1, e2, e3;

	/**
	 * Constructs a new SU2GroupElement instance with all parameters set to zero.
	 * This is not a valid SU2GroupElement since the norm of the parameter vector is zero.
	 */
	public SU2GroupElement() {

		e0 = 1.0;
		e1 = 0.0;
		e2 = 0.0;
		e3 = 0.0;
	}

	/**
	 * Constructs a new SU2GroupElement instance with given parameters.
	 * In order to get a valid SU2GroupElement the parameter vector should have norm 1.
	 *
	 * @param a first parameter
	 * @param b second parameter
	 * @param c third parameter
	 * @param d fourth parameter
	 */
	public SU2GroupElement(double a, double b, double c, double d) {

		e0 = a;
		e1 = b;
		e2 = c;
		e3 = d;

	}

	/**
	 * Constructs a new SU2GroupElement instance from a given SU2GroupElement instance.
	 *
	 * @param matrix    SU2GroupElement instance which should be copied.
	 */
	public SU2GroupElement(SU2GroupElement matrix)
	{
		this();
		this.set(matrix);
	}

	public GroupElement add(GroupElement arg) {
		SU2GroupElement b = (SU2GroupElement) this.copy();
		b.addAssign(arg);
		return b;
	}

	public void addAssign(GroupElement arg) {
		SU2GroupElement a = (SU2GroupElement) arg;
		e0 += a.e0;
		e1 += a.e1;
		e2 += a.e2;
		e3 += a.e3;
	}

	public GroupElement sub(GroupElement arg) {
		SU2GroupElement b = (SU2GroupElement) this.copy();
		b.subAssign(arg);
		return b;
	}

	public void subAssign(GroupElement arg) {
		SU2GroupElement a = (SU2GroupElement) arg;
		e0 -= a.e0;
		e1 -= a.e1;
		e2 -= a.e2;
		e3 -= a.e3;
	}

	/**
	 * Copies values from another SU2GroupElement instance to this instance.
	 *
	 * @param arg SU2GroupElement to copy from.
	 */
	public void set(GroupElement arg) {

		SU2GroupElement a = (SU2GroupElement) arg;

		e0 = a.e0;
		e1 = a.e1;
		e2 = a.e2;
		e3 = a.e3;
	}

	/**
	 * Sets the j-th parameter to a certain value. These parameters are specific to the group parametrization used by
	 * SU2GroupElement.
	 *
	 * @param j index of the parameter to be set (0-3).
	 * @param value new value of the parameter
	 */
	public void set(int j, double value) {

		switch (j) {
			case 0:  e0 = value;
				break;
			case 1:  e1 = value;
				break;
			case 2:  e2 = value;
				break;
			case 3:  e3 = value;
				break;
		}

	}

	/**
	 * Returns the value of j-th parameter. These parameters are specific to the group parametrization used by
	 * SU2GroupElement.
	 *
	 * @param j index of the parameter to be read (0-3).
	 * @return value of the j-th parameter.
	 */
	public double get(int j) {

		double b=0;
		switch (j) {
			case 0:  b = e0;
				break;
			case 1:  b = e1;
				break;
			case 2:  b = e2;
				break;
			case 3:  b = e3;
				break;
		}

		return b;

	}

	public GroupElement adj() {
		SU2GroupElement b = new SU2GroupElement(this);
		b.e1 = - this.e1;
		b.e2 = - this.e2;
		b.e3 = - this.e3;
		return b;
	}

	public void adjAssign() {
		this.e1 = - this.e1;
		this.e2 = - this.e2;
		this.e3 = - this.e3;
	}

	/**
	 * Computes the first parameter from the other three parameters such that the parameter norm is 1.
	 * If the norm of the other three parameters is already larger then one this will fail.
	 * This method is specific to the group parametrization used by SU2GroupElement.
	 */
	public void computeFirstParameter() {

		double sum = e1 * e1 + e2 * e2 + e3 * e3;
		if (sum > 1) {
			System.out.println("Parameters too large!\n");
		} else {
			e0 = Math.sqrt(1.0 - sum);
		}
	}

	/**
	 * Computes the parameter norm. If the norm is one then the matrix is unitary and has determinant 1.
	 * This method is specific to the group parametrization used by SU2GroupElement.
	 *
	 * @return norm of the parameter vector
	 */
	public double computeParameterNorm() {

		return e0 * e0 + e1 * e1 + e2 * e2 + e3 * e3;

	}

	public GroupElement mult(double number) {

		SU2GroupElement b = new SU2GroupElement();
		b.e0 = e0 * number;
		b.e1 = e1 * number;
		b.e2 = e2 * number;
		b.e3 = e3 * number;
		return b;

	}

	public void multAssign(double number) {
		e0 *= number;
		e1 *= number;
		e2 *= number;
		e3 *= number;
	}

	public GroupElement mult(GroupElement arg) {

		SU2GroupElement a = (SU2GroupElement) arg;

		SU2GroupElement b = new SU2GroupElement();
		b.e0 = e0 * a.e0 - e1 * a.e1 - e2 * a.e2 - e3 * a.e3;
		b.e1 = e0 * a.e1 + e1 * a.e0 - e2 * a.e3 + e3 * a.e2;
		b.e2 = e0 * a.e2 + e2 * a.e0 - e3 * a.e1 + e1 * a.e3;
		b.e3 = e0 * a.e3 + e3 * a.e0 - e1 * a.e2 + e2 * a.e1;
		return b;

	}

	public void multAssign(GroupElement arg) {
		double f0, f1, f2;
		double ae0 = ((SU2GroupElement) arg).e0;
		double ae1 = ((SU2GroupElement) arg).e1;
		double ae2 = ((SU2GroupElement) arg).e2;
		double ae3 = ((SU2GroupElement) arg).e3;
		f0 = e0;
		f1 = e1;
		f2 = e2;
		e0 = e0 * ae0 - e1 * ae1 - e2 * ae2 - e3 * ae3;
		e1 = f0 * ae1 + e1 * ae0 - e2 * ae3 + e3 * ae2;
		e2 = f0 * ae2 + e2 * ae0 - e3 * ae1 + f1 * ae3;
		e3 = f0 * ae3 + e3 * ae0 - f1 * ae2 + f2 * ae1;
	}

	public AlgebraElement getAlgebraElement()
	{
		double norm = 0.0;
		norm = this.e1 * this.e1 + this.e2 * this.e2 + this.e3 * this.e3;
		norm = Math.sqrt(norm);

		SU2AlgebraElement field = new SU2AlgebraElement();
		
		if(norm < 1.E-15) {
			//return new SU2AlgebraElement();
			return this.proj();
		} else {
			field.set(0, 2.0 * Math.asin(norm) / norm * this.e1);
			field.set(1, 2.0 * Math.asin(norm) / norm * this.e2);
			field.set(2, 2.0 * Math.asin(norm) / norm * this.e3);
		}

		return field;
	}

	public AlgebraElement proj()
	{
		SU2AlgebraElement field = new SU2AlgebraElement();

		field.set(0, 2 * this.e1);
		field.set(1, 2 * this.e2);
		field.set(2, 2 * this.e3);

		return field;
	}

	public double getRealTrace() {
		return 2*e0;
	}

	public GroupElement pow(double x) {
		return this.getAlgebraElement().mult(x).getLink();
	}

	public GroupElement copy() {
		return new SU2GroupElement(e0, e1, e2, e3);
	}

	/**
	 * Computes the inverse matrix.
	 * @return inverse matrix
	 */
	public SU2GroupElement inv() {
		double n = computeParameterNorm();
		SU2GroupElement v = (SU2GroupElement) this.adj();

		return (SU2GroupElement) v.mult(1.0/n);
	}

	public int getNumberOfColors() {return 2;}

	public int getAdjointDimension() {return 3;}
}
