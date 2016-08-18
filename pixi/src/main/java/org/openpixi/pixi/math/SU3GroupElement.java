package org.openpixi.pixi.math;

/**
 * This is a parametrization of SU(3) group elements.
 * For lack of a better method, the elements are represented explicitly
 * as 3x3 complex matrices. The first nine components are the real parts,
 * the last nine components are the imaginary parts. The matrix thus looks like
 * 		e[0] + i e[9]		e[1] + i e[10]		e[2] + i e[11]
 * 		e[3] + i e[12]		e[4] + i e[13]		e[5] + i e[14]
 * 		e[6] + i e[15]		e[7] + i e[16]		e[8] + i e[17]
 */
public class SU3GroupElement implements GroupElement {

	// thresholds for getAlgebraElement method to use Taylor series
	private final double degeneracyCutoff = 1.E-4;
	private final double unityCutoff = 3 - 1.E-2;

	// number of iterations to use in Taylor series for each case
	private final int taylorSeriesUnityIterations = 15;
	private final int taylorSeriesDegenerateIterations = 50;

	// threshold to determine zero vectors in normalize method
	private final double normalizationAccuracy = 1.E-12;

	private double[] e;

	public SU3GroupElement() {

		e = new double[18];
		for (int i = 0; i < 18; i++) {
			e[i] = 0.0;
		}
	}

	public SU3GroupElement(double[] params) {

		e = new double[18];

		for (int i = 0; i < 18; i++) {
			e[i] = params[i];
		}
	}

	public SU3GroupElement(SU3GroupElement matrix)
	{
		this();
		this.set(matrix);
	}

	public GroupElement add(GroupElement arg) {
		SU3GroupElement b = (SU3GroupElement) this.copy();
		b.addAssign(arg);
		return b;
	}

	public void addAssign(GroupElement arg) {
		SU3GroupElement a = (SU3GroupElement) arg;
		for (int i = 0; i < 18; i++) {
			e[i] += a.get(i);
		}
	}

	public GroupElement sub(GroupElement arg) {
		SU3GroupElement b = (SU3GroupElement) this.copy();
		b.subAssign(arg);
		return b;
	}

	public void subAssign(GroupElement arg) {
		SU3GroupElement a = (SU3GroupElement) arg;
		for (int i = 0; i < 18; i++) {
			e[i] -= a.get(i);
		}
	}

	public void set(GroupElement arg) {

		SU3GroupElement a = (SU3GroupElement) arg;

		for (int i = 0; i < 18; i++) {
			e[i] = a.get(i);
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

	public GroupElement adj() {

		SU3GroupElement b = new SU3GroupElement(this);
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

	public void adjAssign() {
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

	public GroupElement mult(double number) {

		SU3GroupElement b = new SU3GroupElement();
		for (int i = 0; i < 18; i++) {
			b.set(i, e[i] * number);
		}
		return b;

	}

	public GroupElement mult(GroupElement arg) {

		SU3GroupElement a = (SU3GroupElement) arg;

		// computed in Mathematica
		SU3GroupElement b = new SU3GroupElement();
		b.set(0, a.get(0)*e[0]+a.get(3)*e[1]-a.get(12)*e[10]-a.get(15)*e[11]+a.get(6)*e[2]-a.get(9)*e[9]);
		b.set(1, a.get(1) * e[0] + a.get(4) * e[1] - a.get(13) * e[10] - a.get(16) * e[11] + a.get(7) * e[2] - a.get(10) * e[9]);
		b.set(2, a.get(2) * e[0] + a.get(5) * e[1] - a.get(14) * e[10] - a.get(17) * e[11] + a.get(8) * e[2] - a.get(11) * e[9]);
		b.set(3, -a.get(9)*e[12]-a.get(12)*e[13]-a.get(15)*e[14]+a.get(0)*e[3]+a.get(3)*e[4]+a.get(6)*e[5]);
		b.set(4, -a.get(10) * e[12] - a.get(13) * e[13] - a.get(16) * e[14] + a.get(1) * e[3] + a.get(4) * e[4] + a.get(7) * e[5]);
		b.set(5, -a.get(11) * e[12] - a.get(14) * e[13] - a.get(17) * e[14] + a.get(2) * e[3] + a.get(5) * e[4] + a.get(8) * e[5]);
		b.set(6, -a.get(9)*e[15]-a.get(12)*e[16]-a.get(15)*e[17]+a.get(0)*e[6]+a.get(3)*e[7]+a.get(6)*e[8]);
		b.set(7, -a.get(10) * e[15] - a.get(13) * e[16] - a.get(16) * e[17] + a.get(1) * e[6] + a.get(4) * e[7] + a.get(7) * e[8]);
		b.set(8, -a.get(11) * e[15] - a.get(14) * e[16] - a.get(17) * e[17] + a.get(2) * e[6] + a.get(5) * e[7] + a.get(8) * e[8]);
		b.set(9, a.get(9)*e[0]+a.get(12)*e[1]+a.get(3)*e[10]+a.get(6)*e[11]+a.get(15)*e[2]+a.get(0)*e[9]);
		b.set(10, a.get(10) * e[0] + a.get(13) * e[1] + a.get(4) * e[10] + a.get(7) * e[11] + a.get(16) * e[2] + a.get(1) * e[9]);
		b.set(11, a.get(11) * e[0] + a.get(14) * e[1] + a.get(5) * e[10] + a.get(8) * e[11] + a.get(17) * e[2] + a.get(2) * e[9]);
		b.set(12, a.get(0)*e[12]+a.get(3)*e[13]+a.get(6)*e[14]+a.get(9)*e[3]+a.get(12)*e[4]+a.get(15)*e[5]);
		b.set(13, a.get(1) * e[12] + a.get(4) * e[13] + a.get(7) * e[14] + a.get(10) * e[3] + a.get(13) * e[4] + a.get(16) * e[5]);
		b.set(14, a.get(2) * e[12] + a.get(5) * e[13] + a.get(8) * e[14] + a.get(11) * e[3] + a.get(14) * e[4] + a.get(17) * e[5]);
		b.set(15, a.get(0)*e[15]+a.get(3)*e[16]+a.get(6)*e[17]+a.get(9)*e[6]+a.get(12)*e[7]+a.get(15)*e[8]);
		b.set(16, a.get(1) * e[15] + a.get(4) * e[16] + a.get(7) * e[17] + a.get(10) * e[6] + a.get(13) * e[7] + a.get(16) * e[8]);
		b.set(17, a.get(2) * e[15] + a.get(5) * e[16] + a.get(8) * e[17] + a.get(11) * e[6] + a.get(14) * e[7] + a.get(17) * e[8]);
		return b;
	}

	public void multAssign(GroupElement arg) {
		GroupElement U = this.mult(arg);
		this.set(U);
	}

	public double[] det() {
		// computed in Mathematica
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
	private boolean normalize(double[] vector) {
		double norm = 0;
		for (int i = 0; i < 6; i++) {
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
		if (Math.abs(norm) < normalizationAccuracy) {
			return false;
		} else {
			for (int i = 0; i < 6; i++) {
				vector[i] /= norm;
			}
			return true;
		}
	}

	/**
	 * Calculates the algebra element by first eigendecomposing into UDU* and then finding log D
	 * WARNING: This decomposition only works for SU(3) matrices due to certain optimizations
	 * @return coefficients to be fed into SU3AlgebraElement to give algebra element
	 */
	private double[] algebraElementDecompositionMethod() {

		// real and imag. parts of trace
		double trRe, trIm;
		trRe = e[0] + e[4] + e[8];
		trIm = e[13] + e[17] + e[9];

		// if matrix is too close to unity, use taylor series for better accuracy
		if (trRe >= unityCutoff) {
			return new double[]{taylorSeriesUnityIterations};
		}

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

		double preRad, radRe;
		preRad = constTermRe*constTermRe - constTermIm*constTermIm + 4*linTermRe*(linTermRe*linTermRe - 3*linTermIm*linTermIm)/27;
		if (preRad < 0) {
			preRad = 0;
		}
		radRe = Math.sqrt(preRad);

		// convert W^3 to polar
		double preOmegaRe, preOmegaIm, r, th;
		preOmegaRe = (-constTermRe + radRe)/2;
		preOmegaIm = -constTermIm / 2;
		th = Math.atan2(preOmegaIm, preOmegaRe);
		r = Math.pow(preOmegaRe * preOmegaRe + preOmegaIm * preOmegaIm, 1. / 6);

		// three angles of cube roots of W^3
		double[] ths = new double[3];
		for (int i = 0; i < 3; i++) {
			ths[i] = (th + 2 * Math.PI * i) / 3;
		}

		// the end is near!
		// X_i = W_i - p / (3 W_i)
		// then \lambda_i = X_i + 1/3 tr(U)
		// this gives us the real and imag. parts of the three eigenvalues to high precision
		double[] valuesRe = new double[3];
		double[] valuesIm = new double[3];
		for (int i = 0; i < 3; i++) {
			if (Math.abs(r) == 0) {
				valuesRe[i] = trRe / 3;
				valuesIm[i] = trIm / 3;
			} else {
				valuesRe[i] = r * Math.cos(ths[i]) - (linTermRe * Math.cos(ths[i]) + linTermIm * Math.sin(ths[i])) / (3 * r) + trRe / 3;
				valuesIm[i] = r * Math.sin(ths[i]) + (linTermRe * Math.sin(ths[i]) - linTermIm * Math.cos(ths[i])) / (3 * r) + trIm / 3;
			}
		}

		// now use eigenvalues to compute orthonormal eigenvectors
		// (U - \lambda_i)(U - \lambda_j) has columns that are eigenvectors for the remaining eigenvalue \lambda_k
		// we use this result, but we only need one column so we can avoid doing the full multiplication
		// optimized result computed in Mathematica, of course

		// if there are degenerate eigenvalues, use taylor series
		if (Math.abs(1 - valuesRe[0] / valuesRe[1]) < degeneracyCutoff && Math.abs(1 - valuesIm[0] / valuesIm[1]) < degeneracyCutoff) {
			return new double[]{taylorSeriesDegenerateIterations};
		} else if (Math.abs(1 - valuesRe[0] / valuesRe[2]) < degeneracyCutoff && Math.abs(1 - valuesIm[0] / valuesIm[2]) < degeneracyCutoff) {
			return new double[]{taylorSeriesDegenerateIterations};
		} else if (Math.abs(1 - valuesRe[1] / valuesRe[2]) < degeneracyCutoff && Math.abs(1 - valuesIm[1] / valuesIm[2]) < degeneracyCutoff) {
			return new double[]{taylorSeriesDegenerateIterations};
		}

		// get one eigenvector for each value
		// normalize vectors in place
		double[][] vectors = new double[3][6];
		for (int i = 0; i < 3; i++) {
			// product of other two valuesRe besides valuesRe[i]
			double otherValueReProduct = valuesRe[(i + 1) % 3] * valuesRe[(i + 2) % 3];
			// sum of other two valuesRe besides valuesRe[i]
			double otherValueReSum = valuesRe[0] + valuesRe[1] + valuesRe[2] - valuesRe[i];
			// product of other two valuesIm besides valuesIm[i]
			double otherValueImProduct = valuesIm[(i + 1) % 3] * valuesIm[(i + 2) % 3];
			// sum of other two valuesIm besides valuesIm[i]
			double otherValueImSum = valuesIm[0] + valuesIm[1] + valuesIm[2] - valuesIm[i];
			//  sum of products of valuesIm and valuesRe for other two values
			double otherReImSum = (valuesRe[0]*valuesIm[0] + valuesRe[1]*valuesIm[1] + valuesRe[2]*valuesIm[2] - valuesRe[i]*valuesIm[i]);

			vectors[i][0] = (e[0]-otherValueReSum)*e[0]+(otherValueImSum-e[9])*e[9]+otherValueReProduct-otherValueImProduct-e[10]*e[12]-e[11]*e[15]+e[1]*e[3]+e[2]*e[6];
			vectors[i][1] = (e[0]+e[4]-otherValueReSum)*e[3]+e[6]*e[5]+(otherValueImSum-e[9]-e[13])*e[12]-e[15]*e[14];
			vectors[i][2] = (e[0]+e[8]-otherValueReSum)*e[6]+e[3]*e[7]+(otherValueImSum-e[9]-e[17])*e[15]-e[16]*e[12];
			vectors[i][3] = (otherValueReSum-e[0])*(otherValueImSum-e[9])-otherReImSum+e[1]*e[12]+e[15]*e[2]+e[10]*e[3]+e[11]*e[6]+e[0]*e[9];
			vectors[i][4] = (e[0]+e[4]-otherValueReSum)*e[12]+e[15]*e[5]+(e[9]+e[13]-otherValueImSum)*e[3]+e[6]*e[14];
			vectors[i][5] = (e[0]+e[8]-otherValueReSum)*e[15]+e[12]*e[7]+(e[9]+e[17]-otherValueImSum)*e[6]+e[3]*e[16];

			boolean done = normalize(vectors[i]);

			if (!done) {
				vectors[i][0] = (e[0]+e[4]-otherValueReSum)*e[1]+e[7]*e[2]+(otherValueImSum-e[9]-e[13])*e[10]-e[16]*e[11];
				vectors[i][1] = (e[4]-otherValueReSum)*e[4]+(otherValueImSum-e[13])*e[13]+otherValueReProduct-otherValueImProduct-e[10]*e[12]-e[14]*e[16]+e[1]*e[3]+e[5]*e[7];
				vectors[i][2] = (e[4]+e[8]-otherValueReSum)*e[7]+e[1]*e[6]+(otherValueImSum-e[13]-e[17])*e[16]-e[10]*e[15];
				vectors[i][3] = (e[0]+e[4]-otherValueReSum)*e[10]+e[16]*e[2]+(e[9]+e[13]-otherValueImSum)*e[1]+e[7]*e[11];
				vectors[i][4] = (otherValueReSum-e[4])*(otherValueImSum-e[13])-otherReImSum+e[1]*e[12]+e[16]*e[5]+e[10]*e[3]+e[14]*e[7]+e[4]*e[13];
				vectors[i][5] = (e[4]+e[8]-otherValueReSum)*e[16]+e[10]*e[6]+(e[13]+e[17]-otherValueImSum)*e[7]+e[1]*e[15];

				done = normalize(vectors[i]);

				if (!done) {
					vectors[i][0] = (e[0]+e[8]-otherValueReSum)*e[2]+e[1]*e[5]+(otherValueImSum-e[9]-e[17])*e[11]-e[10]*e[14];
					vectors[i][1] = (e[4]+e[8]-otherValueReSum)*e[5]+e[2]*e[3]+(otherValueImSum-e[13]-e[17])*e[14]-e[11]*e[12];
					vectors[i][2] = (e[8]-otherValueReSum)*e[8]+(otherValueImSum-e[17])*e[17]+otherValueReProduct-otherValueImProduct-e[14]*e[16]-e[11]*e[15]+e[5]*e[7]+e[2]*e[6];
					vectors[i][3] = (e[0]+e[8]-otherValueReSum)*e[11]+e[14]*e[1]+(e[9]+e[17]-otherValueImSum)*e[2]+e[5]*e[10];
					vectors[i][4] = (e[4]+e[8]-otherValueReSum)*e[14]+e[11]*e[3]+(e[13]+e[17]-otherValueImSum)*e[5]+e[2]*e[12];
					vectors[i][5] = (otherValueReSum-e[8])*(otherValueImSum-e[17])-otherReImSum+e[7]*e[14]+e[15]*e[2]+e[16]*e[5]+e[11]*e[6]+e[8]*e[17];

					done = normalize(vectors[i]);

					if (!done) {
						for (int j = 0; j < 6; j++) {
							vectors[i][j] = 0;
						}
						vectors[i][i] = 1;
					}
				}
			}
		}

		// take log of eigenvalue matrix
		double[] phases = new double[3];
		for (int i = 0; i < 3; i++) {
			phases[i] = Math.atan2(valuesIm[i],valuesRe[i]);
		}

		// ensure algebra element is traceless!
		// make phases sum to zero
		double phaseSum = phases[0] + phases[1] + phases[2];
		phases[0] -= phaseSum;


		// multiply U log(D) U* to get algebra element
		// log(D) is just a real diagonal matrix so multiplication is included in construction of U
		SU3GroupElement ULnD = new SU3GroupElement(new double[]{vectors[0][0]*phases[0],vectors[1][0]*phases[1],vectors[2][0]*phases[2],
		                                                        vectors[0][1]*phases[0],vectors[1][1]*phases[1],vectors[2][1]*phases[2],
		                                                        vectors[0][2]*phases[0],vectors[1][2]*phases[1],vectors[2][2]*phases[2],
		                                                        vectors[0][3]*phases[0],vectors[1][3]*phases[1],vectors[2][3]*phases[2],
		                                                        vectors[0][4]*phases[0],vectors[1][4]*phases[1],vectors[2][4]*phases[2],
		                                                        vectors[0][5]*phases[0],vectors[1][5]*phases[1],vectors[2][5]*phases[2]});
		SU3GroupElement UAdj = new SU3GroupElement(new double[]{vectors[0][0], vectors[0][1], vectors[0][2],
		                                                        vectors[1][0], vectors[1][1], vectors[1][2],
		                                                        vectors[2][0], vectors[2][1], vectors[2][2],
		                                                       -vectors[0][3],-vectors[0][4],-vectors[0][5],
		                                                       -vectors[1][3],-vectors[1][4],-vectors[1][5],
		                                                       -vectors[2][3],-vectors[2][4],-vectors[2][5]});

		double[] values = ((SU3GroupElement) ULnD.mult(UAdj)).get();
		// now normalize to ensure hermiticity!
		return hermiticize(values);
	}

	/**
	 * Calculates the group element using the taylor series expansion of exp.
	 * WARNING: This decomposition only works well for "small" matrices!
	 * @return coefficients to be fed into SU3GroupElement to give group element
	 */
	private double[] algebraElementTaylorSeries(double iterations) {
		SU3GroupElement result = new SU3GroupElement();
		SU3GroupElement intermediate = new SU3GroupElement(new double[]{-1,0,0,0,-1,0,0,0,-1,0,0,0,0,0,0,0,0,0});
		// series for log(1+x), so subtract I from A
		SU3GroupElement multiplier = (SU3GroupElement) this.add(intermediate).mult(-1);

		for (int i = 1; i <= iterations; i++) {
			intermediate = (SU3GroupElement) intermediate.mult(multiplier);
			result = (SU3GroupElement) result.add(intermediate.mult(1.0 / i));
		}

		double[] values = new double[18];

		for (int i = 0; i < 9; i++) {
			values[i] = result.get(i + 9);
			values[i + 9] = -result.get(i);
		}

		return hermiticize(values);
	}

	/**
	 * (anti)symmetrizes matrix to ensure hermiticity
	 * @param values list of 18 values as in SU3GroupElement
	 * @return list of 9 values as in SU3AlgebraElement
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
		fieldValues[3] = (values[12] - values[10])/2;
		fieldValues[6] = (values[15] - values[11])/2;
		fieldValues[7] = (values[16] - values[14])/2;
		return fieldValues;
	}

	public AlgebraElement getAlgebraElement() {
		// try exact method
		double[] values = algebraElementDecompositionMethod();

		// if values has length 1 the exact method failed and values[0] is the requried taylorSeriesIterations
		if (values.length == 1) {
			values = algebraElementTaylorSeries(values[0]);
		}

		return new SU3AlgebraElement(values);
	}

	/**
	 * Computed in Mathematica by calculating u_a and then finding explicit matrix
	 * as sum of Gell-Mann matrices with weights u_a
	 *
	 * @return AlgebraElement instance of the projection
	 */
	public AlgebraElement proj() {
		double[] fieldValues = new double[]{(2*e[9]-e[13]-e[17])/3,
		                                    (e[10]+e[12])/2,
		                                    (e[11]+e[15])/2,
		                                    (e[1]-e[3])/2,
		                                    (2*e[13]-e[17]-e[9])/3,
		                                    (e[14]+e[16])/2,
		                                    (e[2]-e[6])/2,
		                                    (e[5]-e[7])/2,
		                                    (2*e[17]-e[9]-e[13])/3};
		return new SU3AlgebraElement(fieldValues);
	}

	/**
	 * Returns the real trace of the matrix.
	 *
	 * @return	Real part of trace of the matrix.
	 */
	public double getRealTrace() {
		return e[0] + e[4] + e[8];
	}

	public GroupElement pow(double x) {
		return this.getAlgebraElement().mult(x).getLink();
	}

	public GroupElement copy() {
		return new SU3GroupElement(get());
	}

	public int getNumberOfColors() {return 3;}

	public int getNumberOfComponents() {return 18;}
}
