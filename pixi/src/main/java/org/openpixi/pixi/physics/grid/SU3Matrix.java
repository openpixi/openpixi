package org.openpixi.pixi.physics.grid;

public class SU3Matrix implements LinkMatrix {

	/* 9 real entries, 9 imaginary entries */

	private double[] e;

	public SU3Matrix() {

		e = new double[18];
		for (int i = 0; i < 18; i++) {
			e[i] = 0.0;
		}
	}

	public SU3Matrix(double[] params) {

		e = new double[18];

		for (int i = 0; i < 18; i++) {
			e[i] = params[i];
		}
	}

	public SU3Matrix(SU3Matrix matrix)
	{
		this();
		this.set(matrix);
	}

	public LinkMatrix add(LinkMatrix arg) {

		SU3Matrix b = new SU3Matrix();
		for (int i = 0; i < 18; i++) {
			b.set(i, e[i] + arg.get(i));
		}
		return b;

	}

	public LinkMatrix sub(LinkMatrix arg) {

		SU3Matrix b = new SU3Matrix();
		for (int i = 0; i < 18; i++) {
			b.set(i, e[i] - arg.get(i));
		}
		return b;
	}

	public void set(LinkMatrix arg) {
		
		for (int i = 0; i < 18; i++) {
			e[i] = arg.get(i);
		}
	}

	public void set(int j, double value) {

		e[j] = value;

	}

	public double get(int j) {

		return e[j];

	}

	public double[] get() {
		return e;
	}

	public LinkMatrix adj() {

		SU3Matrix b = new SU3Matrix(this);
		// real diag.
		b.set(0, e[0]);
		b.set(4, e[4]);
		b.set(8, e[8]);
		// imag. diag.
		b.set(9, -e[9]);
		b.set(13, -e[13]);
		b.set(17, -e[17]);
		// real left/top
		b.set(1, e[3]);
		b.set(3, e[1]);
		// imag. left/top
		b.set(10, -e[12]);
		b.set(12, -e[10]);
		// real corners
		b.set(2, e[6]);
		b.set(6, e[2]);
		// imag. corners
		b.set(11, -e[15]);
		b.set(15, -e[11]);
		// real right/bottom
		b.set(5, e[7]);
		b.set(7, e[5]);
		// imag. right/bottom
		b.set(14, -e[16]);
		b.set(16, -e[14]);
		return b;
	}

	public void selfadj() {
		double temp;
		// real diag is good

		// imag. diag.
		this.set(9, -e[9]);
		this.set(13, -e[13]);
		this.set(17, -e[17]);
		// real left/top
		temp = e[1];
		this.set(1, e[3]);
		this.set(3, temp);
		// imag. left/top
		temp = e[10];
		this.set(10, -e[12]);
		this.set(12, -temp);
		// real corners
		temp = e[2];
		this.set(2, e[6]);
		this.set(6, temp);
		// imag. corners
		temp = e[11];
		this.set(11, -e[15]);
		this.set(15, -temp);
		// real right/bottom
		temp = e[5];
		this.set(5, e[7]);
		this.set(7, temp);
		// imag. right/bottom
		temp = e[14];
		this.set(14, -e[16]);
		this.set(16, -temp);
	}

	public LinkMatrix mult(double number) {

		SU3Matrix b = new SU3Matrix();
		for (int i = 0; i < 18; i++) {
			b.set(i, e[i] * number);
		}
		return b;

	}

	public LinkMatrix mult(LinkMatrix arg) {
		// computed in Mathematica
		// not yet tested
		SU3Matrix b = new SU3Matrix();
		b.set(0, arg.get(0)*e[0]+arg.get(3)*e[1]-arg.get(12)*e[10]-arg.get(15)*e[11]+arg.get(6)*e[2]-arg.get(9)*e[9]);
		b.set(1, arg.get(1)*e[0]+arg.get(4)*e[1]-arg.get(13)*e[10]-arg.get(16)*e[11]+arg.get(7)*e[2]-arg.get(10)*e[9]);
		b.set(2, arg.get(2)*e[0]+arg.get(5)*e[1]-arg.get(14)*e[10]-arg.get(17)*e[11]+arg.get(8)*e[2]-arg.get(11)*e[9]);
		b.set(3, -arg.get(9)*e[12]-arg.get(12)*e[13]-arg.get(15)*e[14]+arg.get(0)*e[3]+arg.get(3)*e[4]+arg.get(6)*e[5]);
		b.set(4, -arg.get(10)*e[12]-arg.get(13)*e[13]-arg.get(16)*e[14]+arg.get(1)*e[3]+arg.get(4)*e[4]+arg.get(7)*e[5]);
		b.set(5, -arg.get(11)*e[12]-arg.get(14)*e[13]-arg.get(17)*e[14]+arg.get(2)*e[3]+arg.get(5)*e[4]+arg.get(8)*e[5]);
		b.set(6, -arg.get(9)*e[15]-arg.get(12)*e[16]-arg.get(15)*e[17]+arg.get(0)*e[6]+arg.get(3)*e[7]+arg.get(6)*e[8]);
		b.set(7, -arg.get(10)*e[15]-arg.get(13)*e[16]-arg.get(16)*e[17]+arg.get(1)*e[6]+arg.get(4)*e[7]+arg.get(7)*e[8]);
		b.set(8, -arg.get(11)*e[15]-arg.get(14)*e[16]-arg.get(17)*e[17]+arg.get(2)*e[6]+arg.get(5)*e[7]+arg.get(8)*e[8]);
		b.set(9, arg.get(9)*e[0]+arg.get(12)*e[1]+arg.get(3)*e[10]+arg.get(6)*e[11]+arg.get(15)*e[2]+arg.get(0)*e[9]);
		b.set(10, arg.get(10)*e[0]+arg.get(13)*e[1]+arg.get(4)*e[10]+arg.get(7)*e[11]+arg.get(16)*e[2]+arg.get(1)*e[9]);
		b.set(11, arg.get(11)*e[0]+arg.get(14)*e[1]+arg.get(5)*e[10]+arg.get(8)*e[11]+arg.get(17)*e[2]+arg.get(2)*e[9]);
		b.set(12, arg.get(0)*e[12]+arg.get(3)*e[13]+arg.get(6)*e[14]+arg.get(9)*e[3]+arg.get(12)*e[4]+arg.get(15)*e[5]);
		b.set(13, arg.get(1)*e[12]+arg.get(4)*e[13]+arg.get(7)*e[14]+arg.get(10)*e[3]+arg.get(13)*e[4]+arg.get(16)*e[5]);
		b.set(14, arg.get(2)*e[12]+arg.get(5)*e[13]+arg.get(8)*e[14]+arg.get(11)*e[3]+arg.get(14)*e[4]+arg.get(17)*e[5]);
		b.set(15, arg.get(0)*e[15]+arg.get(3)*e[16]+arg.get(6)*e[17]+arg.get(9)*e[6]+arg.get(12)*e[7]+arg.get(15)*e[8]);
		b.set(16, arg.get(1)*e[15]+arg.get(4)*e[16]+arg.get(7)*e[17]+arg.get(10)*e[6]+arg.get(13)*e[7]+arg.get(16)*e[8]);
		b.set(17, arg.get(2)*e[15]+arg.get(5)*e[16]+arg.get(8)*e[17]+arg.get(11)*e[6]+arg.get(14)*e[7]+arg.get(17)*e[8]);
		return b;
	}

	public double[] det() {
		// computed in Mathematica
		// not yet tested
		double[] out = new double[2];

		// real part
		out[0] = e[13]*e[15]*e[2]-e[12]*e[16]*e[2]-e[11]*e[16]*e[3]+e[10]*e[17]*e[3]+e[11]*e[15]*e[4]-e[10]*e[15]*e[5]+
				e[11]*e[13]*e[6]-e[10]*e[14]*e[6]-e[2]*e[4]*e[6]-e[11]*e[12]*e[7]+e[2]*e[3]*e[7]+e[10]*e[12]*e[8]+
				e[1]*(-e[14]*e[15]+e[12]*e[17]+e[5]*e[6]-e[3]*e[8])+e[0]*(e[14]*e[16]-e[13]*e[17]-e[5]*e[7]+e[4]*e[8])-
				e[17]*e[4]*e[9]+e[16]*e[5]*e[9]+e[14]*e[7]*e[9]-e[13]*e[8]*e[9];

		// imag. part
		out[1] = -e[1]*e[17]*e[3]+e[16]*e[2]*e[3]+e[0]*e[17]*e[4]-e[15]*e[2]*e[4]+e[1]*e[15]*e[5]-e[0]*e[16]*e[5]+
				e[1]*e[14]*e[6]-e[13]*e[2]*e[6]-e[0]*e[14]*e[7]+e[12]*e[2]*e[7]+e[11]*(e[13]*e[15]-e[12]*e[16]-e[4]*e[6]+e[3]*e[7])-
				e[1]*e[12]*e[8]+e[0]*e[13]*e[8]+e[10]*(-e[14]*e[15]+e[12]*e[17]+e[5]*e[6]-e[3]*e[8])+
				(e[14]*e[16]-e[13]*e[17]-e[5]*e[7]+e[4]*e[8])*e[9];

		return out;
		}

	/**
	 * Normalizes (complex) vector in place
	 * Vector is stored as three real components followed by three imag. components
	 * @param vector to be normalized
	 */
	private void normalize(double[] vector) {
		double norm = 0;
		for (int i = 0; i < 6; i++) {
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < 6; i++) {
			vector[i] /= norm;
		}
	}

	/**
	 * Calculates the algebra element by first eigendecomposing into UDU* and then finding log D
	 * WARNING: This decomposition only works for SU(3) matrices due to certain optimizations
	 * @return coefficients to be fed into SU3Field to give algebra element
	 */
	private double[] algebraElementDecompositionMethod() {

		// real and imag. parts of trace
		double trRe, trIm;
		trRe = e[0] + e[4] + e[8];
		trIm = e[13] + e[17] + e[9];

		// real and imag. parts of trace of matrix squared
		double trSqRe, trSqIm;
		trSqRe = e[0]*e[0]-2*e[10]*e[12]-e[13]*e[13]-2*e[11]*e[15]-2*e[14]*e[16]-e[17]*e[17]+2*e[1]*e[3]+e[4]*e[4]+2*e[2]*e[6]+2*e[5]*e[7]+e[8]*e[8]-e[9]*e[9];
		trSqIm = 2*(e[1]*e[12]+e[15]*e[2]+e[10]*e[3]+e[13]*e[4]+e[16]*e[5]+e[11]*e[6]+e[14]*e[7]+e[17]*e[8]+e[0]*e[9]);

		// coefficients in reduced cubic equation
		// \lambda = X + tr(U)/3
		// X^3 + X * (1/6 tr^2(U) - 1/2 tr(U^2)) - (det(U) + 1/6 tr(U^2) tr(U) - 5/54 tr^3(U)) == 0
		double linTermRe, linTermIm, constTermRe, constTermIm;
		linTermRe = (trRe*trRe - trIm*trIm)/6 - trSqRe/2;
		linTermIm = trRe*trIm/3 - trSqIm/2;
		constTermRe = -1 - (trRe*trSqRe - trIm*trSqIm)/6 + 5*trRe*(trRe*trRe - 3*trIm*trIm)/54;
		constTermIm = -(trRe*trSqIm + trIm*trSqRe)/6 + 5*trIm*(3*trRe*trRe - trIm*trIm)/54;

		// cubic is now in form X^3 + p X + q == 0
		// transform to W^6 + q W^3 - 1/27 p^3 == 0
		// then W^3 == (-q + sqrt(q^2 + 4/27 p^3))/2
		// (pick positive solution)
		// preRad is real and imag. parts of radical in solution for W^3

		// old code, see update
//		double preRadRe, preRadIm;
//		preRadRe = constTermRe*constTermRe - constTermIm*constTermIm + 4*linTermRe*(linTermRe*linTermRe - 3*linTermIm*linTermIm)/27;
//		preRadIm = 2*constTermRe*constTermIm + 4*linTermIm*(3*linTermRe*linTermRe - linTermIm*linTermIm)/27;
//
//		// compute radical
//		double radRe, radIm, r, th;
//		r = Math.pow(preRadRe*preRadRe + preRadIm*preRadIm, .25);
//		th = Math.atan2(preRadIm, preRadRe)/2;
//		radRe = r * Math.cos(th);
//		radIm = r * Math.sin(th);

		// UPDATE!
		// for some reason the argument under the square root is always real for SU(3) matrices
		// confirmed in Mathematica for one million random SU(3) matrices
		// invariant can be expressed as
		// 		Im{ 1/3 tr(U^2) tr(U) -
		//			5/27 tr^3(U) -
		//			1/54 tr^3(U^2) +
		//			1/108 tr^6(U) -
		//			1/27 tr^4(U) tr(U^2) +
		//			5/108 tr^2(U) tr^2(U^2) } == 0
		// not always true for SU(4)! so why is this true for SU(3)?
		// anyway, we can get rid of all the imaginary computation here and take the direct square root

		double preRad, radRe, radIm;
		preRad = constTermRe*constTermRe - constTermIm*constTermIm + 4*linTermRe*(linTermRe*linTermRe - 3*linTermIm*linTermIm)/27;
		if (preRad >= 0) {
			radRe = Math.sqrt(preRad);
			radIm = 0;
		}
		else {
			radRe = 0;
			radIm = Math.sqrt(-preRad);
		}

		// convert W^3 to polar
		double preOmegaRe, preOmegaIm, r, th;
		preOmegaRe = (-constTermRe + radRe)/2;
		preOmegaIm = (-constTermIm + radIm)/2;
		th = Math.atan2(preOmegaIm, preOmegaRe);
		r = Math.pow(preOmegaRe * preOmegaRe + preOmegaIm * preOmegaIm, 1. / 6);

		// three angles of cube roots of W^3
		double th1, th2, th3;
		th1 = (th + 0*Math.PI)/3;
		th2 = (th + 2*Math.PI)/3;
		th3 = (th + 4*Math.PI)/3;

		// the end is near!
		// X_i = W_i - p / (3 W_i)
		// then \lambda_i = X_i + 1/3 tr(U)
		// this gives us the real and imag. parts of the three eigenvalues to high precision
		double value1Re, value1Im, value2Re, value2Im, value3Re, value3Im;
		value1Re = r * Math.cos(th1) - (linTermRe*Math.cos(th1) + linTermIm*Math.sin(th1))/(3*r) + trRe/3;
		value1Im = r * Math.sin(th1) + (linTermRe*Math.sin(th1) - linTermIm*Math.cos(th1))/(3*r) + trIm/3;
		value2Re = r * Math.cos(th2) - (linTermRe*Math.cos(th2) + linTermIm*Math.sin(th2))/(3*r) + trRe/3;
		value2Im = r * Math.sin(th2) + (linTermRe*Math.sin(th2) - linTermIm*Math.cos(th2))/(3*r) + trIm/3;
		value3Re = r * Math.cos(th3) - (linTermRe*Math.cos(th3) + linTermIm*Math.sin(th3))/(3*r) + trRe/3;
		value3Im = r * Math.sin(th3) + (linTermRe*Math.sin(th3) - linTermIm*Math.cos(th3))/(3*r) + trIm/3;

		// now use eigenvalues to compute orthonormal eigenvectors
		// (U - \lambda_i)(U - \lambda_j) has columns that are eigenvectors for the remaining eigenvalue \lambda_k
		// we use this result, but we only need one column so we can avoid doing the full multiplication
		// optimized result computed in Mathematica, of course

		// get one eigenvector for each value
		// normalize vectors in place
		// not yet tested
		double[] vector1, vector2, vector3;

		vector1 = new double[6];
		vector1[0] = value2Re*(value3Re-e[0])-value3Re*e[0]+e[0]*e[0]-e[10]*e[12]-e[11]*e[15]+e[1]*e[3]+e[2]*e[6]+value3Im*e[9]-e[9]*e[9]+value2Im*(-value3Im+e[9]);
		vector1[1] = value2Im*e[12]+value3Im*e[12]-e[12]*e[13]-e[14]*e[15]-value2Re*e[3]-value3Re*e[3]+e[0]*e[3]+e[3]*e[4]+e[5]*e[6]-e[12]*e[9];
		vector1[2] = value2Im*e[15]+value3Im*e[15]-e[12]*e[16]-e[15]*e[17]-value2Re*e[6]-value3Re*e[6]+e[0]*e[6]+e[3]*e[7]+e[6]*e[8]-e[15]*e[9];
		vector1[3] = value2Im*(value3Re-e[0])-value3Im*e[0]+e[1]*e[12]+e[15]*e[2]+e[10]*e[3]+e[11]*e[6]+value2Re*(value3Im-e[9])-value3Re*e[9]+2*e[0]*e[9];
		vector1[4] = -value2Re*e[12]-value3Re*e[12]+e[0]*e[12]-value2Im*e[3]-value3Im*e[3]+e[13]*e[3]+e[12]*e[4]+e[15]*e[5]+e[14]*e[6]+e[3]*e[9];
		vector1[5] = -value2Re*e[15]-value3Re*e[15]+e[0]*e[15]+e[16]*e[3]-value2Im*e[6]-value3Im*e[6]+e[17]*e[6]+e[12]*e[7]+e[15]*e[8]+e[6]*e[9];

		vector2 = new double[6];
		vector2[0] = value1Re*(value3Re-e[0])-value3Re*e[0]+e[0]*e[0]-e[10]*e[12]-e[11]*e[15]+e[1]*e[3]+e[2]*e[6]+value3Im*e[9]-e[9]*e[9]+value1Im*(-value3Im+e[9]);
		vector2[1] = value1Im*e[12]+value3Im*e[12]-e[12]*e[13]-e[14]*e[15]-value1Re*e[3]-value3Re*e[3]+e[0]*e[3]+e[3]*e[4]+e[5]*e[6]-e[12]*e[9];
		vector2[2] = value1Im*e[15]+value3Im*e[15]-e[12]*e[16]-e[15]*e[17]-value1Re*e[6]-value3Re*e[6]+e[0]*e[6]+e[3]*e[7]+e[6]*e[8]-e[15]*e[9];
		vector2[3] = value1Im*(value3Re-e[0])-value3Im*e[0]+e[1]*e[12]+e[15]*e[2]+e[10]*e[3]+e[11]*e[6]+value1Re*(value3Im-e[9])-value3Re*e[9]+2*e[0]*e[9];
		vector2[4] = -value1Re*e[12]-value3Re*e[12]+e[0]*e[12]-value1Im*e[3]-value3Im*e[3]+e[13]*e[3]+e[12]*e[4]+e[15]*e[5]+e[14]*e[6]+e[3]*e[9];
		vector2[5] = -value1Re*e[15]-value3Re*e[15]+e[0]*e[15]+e[16]*e[3]-value1Im*e[6]-value3Im*e[6]+e[17]*e[6]+e[12]*e[7]+e[15]*e[8]+e[6]*e[9];

		vector3 = new double[6];
		vector3[0] = value1Re*(value2Re-e[0])-value2Re*e[0]+e[0]*e[0]-e[10]*e[12]-e[11]*e[15]+e[1]*e[3]+e[2]*e[6]+value2Im*e[9]-e[9]*e[9]+value1Im*(-value2Im+e[9]);
		vector3[1] = value1Im*e[12]+value2Im*e[12]-e[12]*e[13]-e[14]*e[15]-value1Re*e[3]-value2Re*e[3]+e[0]*e[3]+e[3]*e[4]+e[5]*e[6]-e[12]*e[9];
		vector3[2] = value1Im*e[15]+value2Im*e[15]-e[12]*e[16]-e[15]*e[17]-value1Re*e[6]-value2Re*e[6]+e[0]*e[6]+e[3]*e[7]+e[6]*e[8]-e[15]*e[9];
		vector3[3] = value1Im*(value2Re-e[0])-value2Im*e[0]+e[1]*e[12]+e[15]*e[2]+e[10]*e[3]+e[11]*e[6]+value1Re*(value2Im-e[9])-value2Re*e[9]+2*e[0]*e[9];
		vector3[4] = -value1Re*e[12]-value2Re*e[12]+e[0]*e[12]-value1Im*e[3]-value2Im*e[3]+e[13]*e[3]+e[12]*e[4]+e[15]*e[5]+e[14]*e[6]+e[3]*e[9];
		vector3[5] = -value1Re*e[15]-value2Re*e[15]+e[0]*e[15]+e[16]*e[3]-value1Im*e[6]-value2Im*e[6]+e[17]*e[6]+e[12]*e[7]+e[15]*e[8]+e[6]*e[9];

		normalize(vector1);
		normalize(vector2);
		normalize(vector3);

		// take log of eigenvalue matrix
		double phase1, phase2, phase3;
		phase1 = Math.atan2(value1Im,value1Re);
		phase2 = Math.atan2(value2Im,value2Re);
		phase3 = Math.atan2(value3Im,value3Re);

		if (Math.abs(phase1+phase2+phase3) >= 1.e-13) {
			phase3 -= phase1+phase2+phase3;
		}

		// multiply U log(D) U* to get algebra element
		// log(D) is just a real diagonal matrix so multipication is included in construction of U
		SU3Matrix ULnD = new SU3Matrix(new double[]{vector1[0]*phase1,vector2[0]*phase2,vector3[0]*phase3,
													vector1[1]*phase1,vector2[1]*phase2,vector3[1]*phase3,
													vector1[2]*phase1,vector2[2]*phase2,vector3[2]*phase3,
													vector1[3]*phase1,vector2[3]*phase2,vector3[3]*phase3,
													vector1[4]*phase1,vector2[4]*phase2,vector3[4]*phase3,
													vector1[5]*phase1,vector2[5]*phase2,vector3[5]*phase3});
		SU3Matrix UAdj = new SU3Matrix(new double[]{ vector1[0], vector1[1], vector1[2],
													 vector2[0], vector2[1], vector2[2],
													 vector3[0], vector3[1], vector3[2],
													-vector1[3],-vector1[4],-vector1[5],
													-vector2[3],-vector2[4],-vector2[5],
													-vector3[3],-vector3[4],-vector3[5]});

		double[] values = ((SU3Matrix) ULnD.mult(UAdj)).get();
		// now normalize to ensure hermiticity!
		return hermiticize(values);
	}

	/**
	 * (anti)symmetrizes matrix to ensure hermiticity
	 * @param values list of 18 values as in SU3Matrix
	 * @return list of 9 values as in SU3Field
	 */
	private double[] hermiticize(double[] values) {
		double[] fieldValues = new double[9];
		// diagonal is just the real diagonal of result
		fieldValues[0] = values[0];
		fieldValues[4] = values[4];
		fieldValues[8] = values[8];
		// off-diagonal real values are symmetric averages of pairs
		fieldValues[1] = (values[1] + values[3])/2;
		fieldValues[2] = (values[2] + values[6])/2;
		fieldValues[5] = (values[5] + values[7])/2;
		// off-diagonal imag. values are asymmetric averages of pairs
		fieldValues[3] = (values[10] - values[12])/2;
		fieldValues[6] = (values[11] - values[15])/2;
		fieldValues[7] = (values[14] - values[16])/2;
		return fieldValues;
	}


	// Mostly same as proj, but multiplies by 2 since tr(t_i t_j) = 1/2 \delta_ij
	public YMField getLinearizedAlgebraElement() {
		double[] values = new double[]{(2*e[9]-e[13]-e[17])/3,
				e[10],
				e[11],
				e[12],
				(2*e[13]-e[17]-e[9])/3,
				e[14],
				e[15],
				e[16],
				(2*e[17]-e[9]-e[13])/3,
				(e[4]+e[8]-2*e[0])/3,
				-e[1],
				-e[2],
				-e[3],
				(e[0]+e[8]-2*e[4])/3,
				-e[5],
				-e[6],
				-e[7],
				(e[0]+e[4]-2*e[8])/3};
		double[] fieldValues = hermiticize(values);
		return new SU3Field(fieldValues);
	}

	public YMField getAlgebraElement() {
		return new SU3Field(algebraElementDecompositionMethod());
	}

	/**
	 * Returns the projection of the matrix onto the generators of the group as a YMField. This is done via the formula
	 *
	 *      u_a = - i tr (t_a U),
	 *
	 * where U is the SU3Matrix, t_a is the a-th generator of the group and u_a is the a-th component of the YMField.
	 *
	 * Computed in Mathematica by calculating u_a and then finding explicit matrix
	 * as sum of Gell-Mann matrices with weights u_a
	 *
	 * @return YMField instance of the projection
	 */
	public YMField proj() {
		double[] values = new double[]{(2*e[9]-e[13]-e[17])/6,
				e[10]/2,
				e[11]/2,
				e[12]/2,
				(2*e[13]-e[17]-e[9])/6,
				e[14]/2,
				e[15]/2,
				e[16]/2,
				(2*e[17]-e[9]-e[13])/6,
				(e[4]+e[8]-2*e[0])/6,
				-e[1]/2,
				-e[2]/2,
				-e[3]/2,
				(e[0]+e[8]-2*e[4])/6,
				-e[5]/2,
				-e[6]/2,
				-e[7]/2,
				(e[0]+e[4]-2*e[8])/6};
		double[] fieldValues = hermiticize(values);
		return new SU3Field(fieldValues);
	}

	/**
	 * Returns the trace of the matrix.
	 *
	 * @return	Trace of the matrix.
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * Not necessarily real for SU(3) so not implemented!!!!!!
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 */
	public double getTrace() {
		System.out.println("Trace not double for SU3!");
		return 0;
	}
}
