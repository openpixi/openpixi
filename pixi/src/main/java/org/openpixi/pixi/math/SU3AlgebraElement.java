package org.openpixi.pixi.math;

/**
 * This is a parametrization of SU(3) algebra elements.
 * They are again represented as 3x3 matrices. Due to
 * symmetry of Hermitian matrices, though, there are only
 * nine independent components. The matrix thus looks like
 * 		e[0]        		e[1] + i e[3]		e[2] + i e[6]
 * 		e[1] - i e[3]		e[4]        		e[5] + i e[7]
 * 		e[2] - i e[6]		e[5] - i e[7]		e[8]
 */
public class SU3AlgebraElement implements AlgebraElement {

	private final double zeroAccuracy = 1.E-12;

	protected double[] v;

	public SU3AlgebraElement() {

		v = new double[9];

		for (int i = 0; i < 9; i++) {
			v[i] = 0;
		}
	}

	public SU3AlgebraElement(double[] values) {

		v = new double[9];

		if (values.length == 9) {
			for (int i = 0; i < 9; i++) {
				v[i] = values[i];
			}
		} else if (values.length == 8) {
			for (int i = 0; i < 8; i++) {
				set(i, values[i]);
			}
		}
	}
	
	public void reset () {

		for (int i = 0; i < 9; i++) {
			v[i] = 0;
		}
		
	}
	
	public AlgebraElement add (AlgebraElement arg) {

		SU3AlgebraElement a = (SU3AlgebraElement) arg;
		
		SU3AlgebraElement b = new SU3AlgebraElement();
		for (int i = 0; i < 9; i++) {
			b.v[i] = v[i]+a.v[i];
		}
		return b;
		
	}
	
	public void addAssign(AlgebraElement arg) {

		SU3AlgebraElement a = (SU3AlgebraElement) arg;

		for (int i = 0; i < 9; i++) {
			v[i] += a.v[i];
		}
		
	}
	
	public AlgebraElement sub (AlgebraElement arg) {

		SU3AlgebraElement a = (SU3AlgebraElement) arg;
		
		SU3AlgebraElement b = new SU3AlgebraElement();
		for (int i = 0; i < 9; i++) {
			b.v[i] = v[i]-a.v[i];
		}
		return b;
		
	}

	public void set(int j, double value) {
		double diff = (value - get(j)) / 2;

		switch (j) {
			case 0: v[1] += diff;
				break;
			case 1: v[3] -= diff;
				break;
			case 2: v[0] += diff; v[4] -= diff;
				break;
			case 3: v[2] += diff;
				break;
			case 4: v[6] -= diff;
				break;
			case 5: v[5] += diff;
				break;
			case 6: v[7] -= diff;
				break;
			case 7: v[0] += diff / Math.sqrt(3); v[4] += diff / Math.sqrt(3); v[8] -= 2 * diff / Math.sqrt(3);
				break;
			default: System.out.println("Invalid generator set index!");
		}
	}

	public double get(int j) {
		switch (j) {
			case 0: return 2 * v[1];
			case 1: return -2 * v[3];
			case 2: return v[0] - v[4];
			case 3: return 2 * v[2];
			case 4: return -2 * v[6];
			case 5: return 2 * v[5];
			case 6: return -2 * v[7];
			case 7: return (v[0] + v[4] - 2 * v[8]) / Math.sqrt(3);
			default: System.out.println("Invalid generator get index!"); return 0;
		}
	}
	
	public double getEntry(int j) {
		
		double b = v[j];
		return b;
		
	}

	public double[] get () {

		return v;

	}

	public AlgebraElement act(GroupElement arg) {

		SU3GroupElement a = (SU3GroupElement) arg;

		// temporarily store algebra element as group element so we can use the multiplication method
		SU3GroupElement temp = new SU3GroupElement(new double[]{v[0],v[1],v[2],v[1],v[4],v[5],v[2],v[5],v[8],0,v[3],v[6],-v[3],0,v[7],-v[6],-v[7],0});

		double[] values = ((SU3GroupElement) a.mult(temp.mult(a.adj()))).get();

		double[] fieldValues = new double[9];

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

		return new SU3AlgebraElement(fieldValues);
	}

	public void actAssign(GroupElement arg) {
		v = ((SU3AlgebraElement) act(arg)).get();
	}

	public double square () {
		return 2*(v[0]*v[0]+v[4]*v[4]+v[8]*v[8]+2*(v[1]*v[1]+v[2]*v[2]+v[3]*v[3]+v[5]*v[5]+v[6]*v[6]+v[7]*v[7]));
		
	}
	
	public AlgebraElement mult (double number) {

		SU3AlgebraElement b = new SU3AlgebraElement();
		for (int i = 0; i < 9; i++) {
			b.v[i] = v[i]*number;
		}
		return b;

	}

	public void multAssign(double number) {

		for (int i = 0; i < 9; i++) {
			this.v[i] = v[i]*number;
		}

	}

	public void set (AlgebraElement arg) {

		SU3AlgebraElement a = (SU3AlgebraElement) arg;

		for (int i = 0; i < 9; i++) {
			v[i] = a.v[i];
		}

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

		if (Math.abs(norm) < zeroAccuracy) {
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
	private double[] groupElementDecompositionMethod() {
		// trace of matrix squared, using square method
		double trSq = square() / 2;

		// real determinant
		double det = -v[2]*v[2]*v[4]+v[6]*(2*v[3]*v[5]-v[4]*v[6]+2*v[1]*v[7])+2*v[2]*(v[1]*v[5]-v[3]*v[7])-
				(v[1]*v[1]+v[3]*v[3])*v[8]-v[0]*(v[5]*v[5]+v[7]*v[7]-v[4]*v[8]);

		// coefficients in reduced cubic equation
		// \lambda = X
		// X^3 - X * 1/2 tr(U^2) - det(U) == 0
		double linTerm = -trSq/2;

		// cubic is now in form X^3 + p X + q == 0
		// transform to W^6 + q W^3 - 1/27 p^3 == 0
		// then W^3 == (-q + sqrt(q^2 + 4/27 p^3))/2
		// (pick positive solution)
		// preRad is always negative and real here so rad = i radIm = i sqrt(-preRad)
		double preRad, radIm;
		preRad = det*det + Math.pow(linTerm,3)*4/27;
		if (preRad > 0) {
			preRad = 0;
		}
		radIm = Math.sqrt(-preRad);

		// convert W^3 to polar
		double preOmegaRe, preOmegaIm, r, th;
		preOmegaRe = det/2;
		preOmegaIm = radIm/2;
		th = Math.atan2(preOmegaIm, preOmegaRe);
		r = Math.pow(preOmegaRe * preOmegaRe + preOmegaIm * preOmegaIm, 1. / 6);

		// three angles of cube roots of W^3
		double[] ths = new double[3];
		for (int i = 0; i < 3; i++) {
			ths[i] = (th + 2 * Math.PI * i) / 3;
		}

		// the end is near!
		// X_i = W_i - p / (3 W_i)
		// then \lambda_i = X_i
		// this gives us the real eigenvalues to high precision
		// (these matrices are hermitian, so eigenvalues better be real)
		double[] phases = new double[3];
		for (int i = 0; i < 3; i++) {
			if (Math.abs(r) == 0) {
				phases[i] = 0;
			} else {
				phases[i] = r * Math.cos(ths[i]) - (linTerm * Math.cos(ths[i])) / (3 * r);
			}
		}

		// now use eigenvalues to compute orthonormal eigenvectors
		// (U - \lambda_i)(U - \lambda_j) has columns that are eigenvectors for the remaining eigenvalue \lambda_k
		// we use this result, but we only need one column so we can avoid doing the full multiplication
		// optimized result computed in Mathematica, of course

		// if there are degenerate eigenvalues, only use vector method for nondegenerate value
		int phaseNum = 3;
		int notDegenerate = 3;
		if (Math.abs(phases[0] - phases[1]) < zeroAccuracy) {
			double temp = phases[2];
			phases[2] = phases[0];
			phases[0] = temp;
			phaseNum = 1;
			notDegenerate = 2;
		} else if (Math.abs(phases[0] - phases[2]) < zeroAccuracy) {
			double temp = phases[1];
			phases[1] = phases[0];
			phases[0] = temp;
			phaseNum = 1;
			notDegenerate  = 1;
		} else if (Math.abs(phases[1] - phases[2]) < zeroAccuracy) {
			phaseNum = 1;
			notDegenerate = 0;
		}

		// get one eigenvector for each value
		// normalize vectors in place
		double[][] vectors = new double[3][6];
		for (int i = 0; i < phaseNum; i++) {
			// product of other two phases besides phases[i]
			double otherPhaseProduct = phases[(i + 1) % 3] * phases[(i + 2) % 3];
			// sum of other two phases besides phases[i]
			double otherPhaseSum = phases[0] + phases[1] + phases[2] - phases[i];

			vectors[i][0] = v[0]*v[0]+v[1]*v[1]+v[2]*v[2]+v[3]*v[3]+v[6]*v[6]+otherPhaseProduct-v[0]*otherPhaseSum;
			vectors[i][1] = v[2]*v[5]+v[6]*v[7]+v[1]*(v[0]+v[4]-otherPhaseSum);
			vectors[i][2] = v[1]*v[5]-v[3]*v[7]+v[2]*(v[0]+v[8]-otherPhaseSum);
			vectors[i][3] = 0;
			vectors[i][4] = -v[5]*v[6]+v[2]*v[7]+v[3]*(otherPhaseSum-v[0]-v[4]);
			vectors[i][5] = -v[3]*v[5]-v[1]*v[7]+v[6]*(otherPhaseSum-v[0]-v[8]);

			boolean done = normalize(vectors[i]);

			if (!done) {
				vectors[i][0] = v[2]*v[5]+v[6]*v[7]+v[1]*(v[0]+v[4]-otherPhaseSum);
				vectors[i][1] = v[1]*v[1]+v[3]*v[3]+v[4]*v[4]+v[5]*v[5]+v[7]*v[7]+otherPhaseProduct-v[4]*otherPhaseSum;
				vectors[i][2] = v[1]*v[2]+v[3]*v[6]+v[5]*(v[4]+v[8]-otherPhaseSum);
				vectors[i][3] = v[5]*v[6]-v[2]*v[7]-v[3]*(otherPhaseSum-v[0]-v[4]);
				vectors[i][4] = 0;
				vectors[i][5] = v[2]*v[3]-v[1]*v[6]+v[7]*(otherPhaseSum-v[4]-v[8]);

				done = normalize(vectors[i]);

				if (!done) {
					vectors[i][0] = v[1]*v[5]-v[3]*v[7]+v[2]*(v[0]+v[8]-otherPhaseSum);
					vectors[i][1] = v[1]*v[2]+v[3]*v[6]+v[5]*(v[4]+v[8]-otherPhaseSum);
					vectors[i][2] = v[2]*v[2]+v[5]*v[5]+v[6]*v[6]+v[7]*v[7]+v[8]*v[8]+otherPhaseProduct-v[8]*otherPhaseSum;
					vectors[i][3] = v[3]*v[5]+v[1]*v[7]-v[6]*(otherPhaseSum-v[0]-v[8]);
					vectors[i][4] = v[1]*v[6]-v[2]*v[3]-v[7]*(otherPhaseSum-v[4]-v[8]);
					vectors[i][5] = 0;

					done = normalize(vectors[i]);

					if (!done) {
						for (int j = 0; j < 6; j++) {
							vectors[i][j] = 0;
						}
						if (notDegenerate == 3) {
							vectors[i][i] = 1;
						} else {
							vectors[i][notDegenerate] = 1;
						}
					}
				}
			}
		}

		// if there are degenerate eigenvalues use row reduction to find two orthogonal eigenvectors
		// for a given row of our hermitian matrix (a0 a1 a2), these vectors are
		//  		-a1    a0     0
		// and		a0*a2  a1*a2  -|a0|^2-|a1|^2
		if (phaseNum == 1) {

			vectors[1][0] = -v[1];
			vectors[1][1] = v[0] - phases[1];
			vectors[1][2] = 0;
			vectors[1][3] = -v[3];
			vectors[1][4] = 0;
			vectors[1][5] = 0;

			vectors[2][0] = v[2] * (v[0] - phases[2]);
			vectors[2][1] = v[1]*v[2] + v[3]*v[6];
			vectors[2][2] = (phases[2] - v[0]) * (v[0] - phases[2]) - v[1]*v[1] - v[3]*v[3];
			vectors[2][3] = v[6] * (v[0] - phases[2]);
			vectors[2][4] = v[1]*v[6] - v[2]*v[3];
			vectors[2][5] = 0;

			boolean done = normalize(vectors[1]) && normalize(vectors[2]);

			if (!done) {
				vectors[1][0] = phases[1] - v[4];
				vectors[1][1] = v[1];
				vectors[1][2] = 0;
				vectors[1][3] = 0;
				vectors[1][4] = -v[3];
				vectors[1][5] = 0;

				vectors[2][0] = v[1]*v[5] - v[3]*v[7];
				vectors[2][1] = v[5] * (v[4] - phases[2]);
				vectors[2][2] = (phases[2] - v[4]) * (v[4] - phases[2]) - v[1]*v[1] - v[3]*v[3];
				vectors[2][3] = v[1]*v[7] + v[3]*v[5];
				vectors[2][4] = v[7] * (v[4] - phases[2]);
				vectors[2][5] = 0;

				done = normalize(vectors[1]) && normalize(vectors[2]);

				if (!done) {
					vectors[1][0] = -v[5];
					vectors[1][1] = v[2];
					vectors[1][2] = 0;
					vectors[1][3] = v[7];
					vectors[1][4] = -v[6];
					vectors[1][5] = 0;

					vectors[2][0] = v[2] * (v[8] - phases[2]);
					vectors[2][1] = v[5] * (v[8] - phases[2]);
					vectors[2][2] = -v[2]*v[2] - v[5]*v[5] - v[6]*v[6] - v[7]*v[7];
					vectors[2][3] = v[6] * (v[8] - phases[2]);
					vectors[2][4] = v[7] * (v[8] - phases[2]);
					vectors[2][5] = 0;

					done = normalize(vectors[1]) && normalize(vectors[2]);

					if (!done) {
						for (int j = 0; j < 6; j++) {
							vectors[1][j] = 0;
							vectors[2][j] = 0;
						}
						vectors[1][(notDegenerate + 1) % 3] = 1;
						vectors[2][(notDegenerate + 2) % 3] = 1;
					}
				}
			}
		}

		// take log of eigenvalue matrix
		double[] valuesRe = new double[3];
		double[] valuesIm = new double[3];
		for (int i = 0; i < 3; i++) {
			valuesRe[i] = Math.cos(phases[i]);
			valuesIm[i] = Math.sin(phases[i]);
		}

		// multiply U exp(D) U* to get algebra element
		// exp(D) is just a (complex) diagonal matrix
		SU3GroupElement unit = new SU3GroupElement(new double[]{vectors[0][0],vectors[1][0],vectors[2][0],
													vectors[0][1],vectors[1][1],vectors[2][1],
													vectors[0][2],vectors[1][2],vectors[2][2],
													vectors[0][3],vectors[1][3],vectors[2][3],
													vectors[0][4],vectors[1][4],vectors[2][4],
													vectors[0][5],vectors[1][5],vectors[2][5]});
		SU3GroupElement diag = new SU3GroupElement(new double[]{valuesRe[0],0,0,
													0,valuesRe[1],0,
													0,0,valuesRe[2],
													valuesIm[0],0,0,
													0,valuesIm[1],0,
													0,0,valuesIm[2]});
		return ((SU3GroupElement) unit.mult(diag).mult(unit.adj())).get();
	}

	public GroupElement getLinearizedLink() {
		double[] values = new double[]{1,-v[3],-v[6],v[3],1,-v[7],v[6],v[7],1,v[0],v[1],v[2],v[1],v[4],v[5],v[2],v[5],v[8]};
		return new SU3GroupElement(values);
	}
	
	public GroupElement getLink() {
		return new SU3GroupElement(groupElementDecompositionMethod());
	}

	public double proj(int c) {
		switch (c) {
			case 0: return 2 * v[1];
			case 1: return -2 * v[3];
			case 2: return v[0] - v[4];
			case 3: return 2 * v[2];
			case 4: return -2 * v[6];
			case 5: return 2 * v[5];
			case 6: return -2 * v[7];
			case 7: return (v[0] + v[4] - 2 * v[8]) / Math.sqrt(3);
			default: System.out.println("Invalid generator index!"); return 0;
		}
	}

	public AlgebraElement copy() {
		return new SU3AlgebraElement(get());
	}
}
