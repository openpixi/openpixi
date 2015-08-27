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

	private double[] e;

	/**
	 * Constructs a new SU2GroupElement instance with all parameters set to zero.
	 * This is not a valid SU2GroupElement since the norm of the parameter vector is zero.
	 */
	public SU2GroupElement() {

		e = new double[4];

		e[0] = 1.0;
		for (int i = 1; i < 4; i++) {
			e[i] = 0.0;
		}
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

		e = new double[4];

		e[0] = a;
		e[1] = b;
		e[2] = c;
		e[3] = d;

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

		SU2GroupElement a = (SU2GroupElement) arg;

		SU2GroupElement b = new SU2GroupElement();
		for (int i = 0; i < 4; i++) {
			b.set(i, e[i] + a.get(i));
		}
		return b;

	}

	public GroupElement sub(GroupElement arg) {

		SU2GroupElement a = (SU2GroupElement) arg;

		SU2GroupElement b = new SU2GroupElement();
		for (int i = 0; i < 4; i++) {
			b.set(i, e[i] - a.get(i));
		}
		return b;

	}

	/**
	 * Copies values from another SU2GroupElement instance to this instance.
	 *
	 * @param arg SU2GroupElement to copy from.
	 */
	public void set(GroupElement arg) {

		SU2GroupElement a = (SU2GroupElement) arg;

		for (int i = 0; i < 4; i++) {
			e[i] = a.get(i);
		}
	}

	/**
	 * Sets the j-th parameter to a certain value. These parameters are specific to the group parametrization used by
	 * SU2GroupElement.
	 *
	 * @param j index of the parameter to be set (0-3).
	 * @param value new value of the parameter
	 */
	public void set(int j, double value) {

		e[j] = value;

	}

	/**
	 * Returns the value of j-th parameter. These parameters are specific to the group parametrization used by
	 * SU2GroupElement.
	 *
	 * @param j index of the parameter to be read (0-3).
	 * @return value of the j-th parameter.
	 */
	public double get(int j) {

		return e[j];

	}

	public GroupElement adj() {
		SU2GroupElement b = new SU2GroupElement(this);
		for (int i = 1; i < 4; i++)
		{
			b.set(i, -b.get(i));
		}
		return b;
	}

	public void adjAssign() {
		for (int i = 1; i < 4; i++)
		{
			this.set(i, -this.get(i));
		}
	}

	/**
	 * Computes the first parameter from the other three parameters such that the parameter norm is 1.
	 * If the norm of the other three parameters is already larger then one this will fail.
	 * This method is specific to the group parametrization used by SU2GroupElement.
	 */
	public void computeFirstParameter() {

		double sum = e[1] * e[1] + e[2] * e[2] + e[3] * e[3];
		if (sum > 1) {
			System.out.println("Parameters too large!\n");
		} else {
			e[0] = Math.sqrt(1.0 - sum);
		}
	}

	/**
	 * Computes the parameter norm. If the norm is one then the matrix is unitary and has determinant 1.
	 * This method is specific to the group parametrization used by SU2GroupElement.
	 *
	 * @return norm of the parameter vector
	 */
	public double computeParameterNorm() {

		return e[0] * e[0] + e[1] * e[1] + e[2] * e[2] + e[3] * e[3];

	}

	public GroupElement mult(double number) {

		SU2GroupElement b = new SU2GroupElement();
		for (int i = 0; i < 4; i++) {
			b.set(i, e[i] * number);
		}
		return b;

	}

	public GroupElement mult(GroupElement arg) {

		SU2GroupElement a = (SU2GroupElement) arg;

		SU2GroupElement b = new SU2GroupElement();
		b.e[0] = e[0] * a.get(0) - e[1] * a.get(1) - e[2] * a.get(2) - e[3] * a.get(3);
		b.e[1] = e[0] * a.get(1) + e[1] * a.get(0) - e[2] * a.get(3) + e[3] * a.get(2);
		b.e[2] = e[0] * a.get(2) + e[2] * a.get(0) - e[3] * a.get(1) + e[1] * a.get(3);
		b.e[3] = e[0] * a.get(3) + e[3] * a.get(0) - e[1] * a.get(2) + e[2] * a.get(1);
		return b;

	}

	public AlgebraElement getAlgebraElement()
	{
		double norm = 0.0;
		for(int i = 1; i < 4; i++)
		{
			norm += this.e[i] * this.e[i];
		}
		norm = Math.sqrt(norm);

		SU2AlgebraElement field = new SU2AlgebraElement();
		
		if(norm < 1.E-15) {
			//return new SU2AlgebraElement();
			return this.proj();
		} else {
			for(int i = 0; i < 3; i++)
			{
				field.set(i, 2.0 * Math.asin(norm) / norm * this.e[i+1]);
			}
		}

		return field;
	}

	public AlgebraElement proj()
	{
		SU2AlgebraElement field = new SU2AlgebraElement();

		field.set(0, 2 * this.e[1]);
		field.set(1, 2 * this.e[2]);
		field.set(2, 2 * this.e[3]);

		return field;
	}

	public double getRealTrace() {
		return 2*e[0];
	}

	public GroupElement copy() {
		return new SU2GroupElement(e[0], e[1], e[2], e[3]);
	}
}
