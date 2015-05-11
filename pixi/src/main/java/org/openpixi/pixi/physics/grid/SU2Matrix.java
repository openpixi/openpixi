package org.openpixi.pixi.physics.grid;

/**
 * This class implements a parametrization of the group SU(2).
 * A SU(2) element U is defined by a normalized four-component vector e_i via
 *
 *      U   =   e_0 * 1 + i e_i \sigma_i,
 *
 * where \sigma_i is the i-th Pauli matrix.
 *
 */
public class SU2Matrix implements LinkMatrix {

	private double[] e;

	/**
	 * Constructs a new SU2Matrix instance with all parameters set to zero.
	 * This is not a valid SU2Matrix since the norm of the parameter vector is zero.
	 */
	public SU2Matrix() {

		e = new double[4];

		e[0] = 1.0;
		for (int i = 1; i < 4; i++) {
			e[i] = 0.0;
		}
	}

	/**
	 * Constructs a new SU2Matrix instance with given parameters.
	 * In order to get a valid SU2Matrix the parameter vector should have norm 1.
	 *
	 * @param a first parameter
	 * @param b second parameter
	 * @param c third parameter
	 * @param d fourth parameter
	 */
	public SU2Matrix(double a, double b, double c, double d) {

		e = new double[4];

		e[0] = a;
		e[1] = b;
		e[2] = c;
		e[3] = d;

	}

	/**
	 * Constructs a new SU2Matrix instance from a given SU2Matrix instance.
	 *
	 * @param matrix    SU2Matrix instance which should be copied.
	 */
	public SU2Matrix(SU2Matrix matrix)
	{
		this();
		this.set(matrix);
	}

	/**
	 * Adds two SU2Matrix instances and returns the result as a copy.
	 * The result is not a valid SU2 matrix since the parameter norm will not be one in general.
	 * This method does not change the original SU2Matrix instance.
	 *
	 * @param arg SU2Matrix which is added to the current SU2Matrix instance.
	 * @return Sum of the SU2Matrix instances.
	 */
	public LinkMatrix add(LinkMatrix arg) {
		
		SU2Matrix b = new SU2Matrix();
		for (int i = 0; i < 4; i++) {
			b.set(i, e[i] + arg.get(i));
		}
		return b;

	}

	/**
	 * Subtracts the passed SU2Matrix instance from the current instance and returns the result as a copy.
	 * The result is not a valid SU2 matrix since the parameter norm will not be one in general.
	 * This method does not change the original SU2Matrix instance.
	 *
	 * @param arg SU2Matrix which is subtracted from the current SU2Matrix instance.
	 * @return Difference of the SU2Matrix instances.
	 */
	public LinkMatrix sub(LinkMatrix arg) {
		
		SU2Matrix b = new SU2Matrix();
		for (int i = 0; i < 4; i++) {
			b.set(i, e[i] - arg.get(i));
		}
		return b;

	}

	/**
	 * Copies values from another SU2Matrix instance to this instance.
	 *
	 * @param arg SU2Matrix to copy from.
	 */
	public void set(LinkMatrix arg) {
		
		for (int i = 0; i < 4; i++) {
			e[i] = arg.get(i);
		}
	}

	/**
	 * Sets the j-th parameter to a certain value. These parameters are specific to the group parametrization used by
	 * SU2Matrix.
	 *
	 * @param j index of the parameter to be set (0-3).
	 * @param value new value of the parameter
	 */
	public void set(int j, double value) {

		e[j] = value;

	}

	/**
	 * Returns the value of j-th parameter. These parameters are specific to the group parametrization used by
	 * SU2Matrix.
	 *
	 * @param j index of the parameter to be read (0-3).
	 * @return value of the j-th parameter.
	 */
	public double get(int j) {

		return e[j];

	}

	/**
	 * Applies hermitian conjugation to the current instance of SU2Matrix and returns a copy.
	 * This method does not change the original SU2Matrix instance.
	 *
	 * @return  Hermitian conjugate of the current instance.
	 */
	public LinkMatrix adj() {
		SU2Matrix b = new SU2Matrix(this);
		for (int i = 1; i < 4; i++)
		{
			b.set(i, -b.get(i));
		}
		return b;
	}
	
	/**
	 * Applies hermitian conjugation to the current instance of SU2Matrix without returning a copy.
	 * This method changes the original SU2Matrix instance.
	 *
	 * @return  Hermitian conjugate of the current instance.
	 */
	public void selfadj() {
		for (int i = 1; i < 4; i++)
		{
			this.set(i, -this.get(i));
		}
	}

	/**
	 * Computes the first parameter from the other three parameters such that the parameter norm is 1.
	 * If the norm of the other three parameters is already larger then one this will fail.
	 * This method is specific to the group parametrization used by SU2Matrix.
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
	 * This method is specific to the group parametrization used by SU2Matrix.
	 *
	 * @return norm of the parameter vector
	 */
	public double computeParameterNorm() {

		return e[0] * e[0] + e[1] * e[1] + e[2] * e[2] + e[3] * e[3];

	}

	/**
	 * Computes the scalar product of the SU2Matrix instance with a real number and returns a copy.
	 * This method does not change the original SU2Matrix instance.
	 *
	 * @param number    real number to be multiplied with.
	 * @return          product of the scalar multiplication.
	 */
	public LinkMatrix mult(double number) {

		SU2Matrix b = new SU2Matrix();
		for (int i = 0; i < 4; i++) {
			b.set(i, e[i] * number);
		}
		return b;

	}

	/**
	 * Computes the matrix product of the SU2Matrix instance with another SU2Matrix instance.
	 * Let A be the SU2Matrix instance which the method is applied to and let B the SU2matrix instance
	 * which is passed as an argument. The result of the multiplication is A.B (post-multiply or multiplication from
	 * the right).
	 * This method does not change the original SU2Matrix instance.
	 *
	 * @param arg SU2Matrix instance used for post-multiplication.
	 * @return  Result of the multiplication.
	 */
	public LinkMatrix mult(LinkMatrix arg) {

		SU2Matrix b = new SU2Matrix();
		b.e[0] = e[0] * arg.get(0) - e[1] * arg.get(1) - e[2] * arg.get(2) - e[3] * arg.get(3);
		b.e[1] = e[0] * arg.get(1) + e[1] * arg.get(0) - e[2] * arg.get(3) + e[3] * arg.get(2);
		b.e[2] = e[0] * arg.get(2) + e[2] * arg.get(0) - e[3] * arg.get(1) + e[1] * arg.get(3);
		b.e[3] = e[0] * arg.get(3) + e[3] * arg.get(0) - e[1] * arg.get(2) + e[2] * arg.get(1);
		return b;

	}

	/**
	 * Returns approximate algebra element of the group element. The algebra element generates the group element via
	 * the exponential map.
	 * This only works if the group element is close to identity.
	 *
	 * @return  Approximate algebra element of the SU2Matrix.
	 */
	public YMField getLinearizedAlgebraElement()
	{
		return new SU2Field(e[1] * 2, e[2] * 2, e[3] * 2);
	}

	/**
	 * Returns the exact algebra element of the group element. The algebra element generates the group element
	 * via the exponential map.
	 * This also works far from identity but is not always continuous.
	 *
	 * @return  Algebra element of the SU2Matrix.
	 */
	public YMField getAlgebraElement()
	{
		double norm = 0.0;
		for(int i = 1; i < 4; i++)
		{
			norm += this.e[i] * this.e[i];
		}
		norm = Math.sqrt(norm);

		SU2Field field = new SU2Field();

		for(int i = 0; i < 3; i++)
		{
			field.set(i, 2.0 * Math.asin(norm) / norm * this.e[i+1]);
		}

		return field;
	}

	/**
	 * Returns the projection of the matrix onto the generators of the group as a YMField. This is done via the formula
	 *
	 *      u_a = - i tr (t_a U),
	 *
	 * where U is the SU2Matrix, t_a is the a-th generator of the group and u_a is the a-th component of the YMField.
	 *
	 * @return YMField instance of the projection
	 */
	public YMField proj()
	{
		SU2Field field = new SU2Field();

		field.set(0, this.e[1]);
		field.set(1, this.e[2]);
		field.set(2, this.e[3]);

		return field;
	}



}
