package org.openpixi.pixi.physics.grid;

/**
 * This is a parametrization of SU(3) algebra elements.
 * They are again represented as 3x3 matrices. Due to
 * symmetry of Hermitian matrices, though, there are only
 * nine independent components. The matrix thus looks like
 * 		e[0]        		e[1] + i e[3]		e[2] + i e[6]
 * 		e[1] - i e[3]		e[4]        		e[5] + i e[7]
 * 		e[2] - i e[6]		e[5] - i e[7]		e[8]
 */
public class SU3Field implements YMField {

	protected double[] v;

	public SU3Field() {

		v = new double[9];

		for (int i = 0; i < 9; i++) {
			v[i] = 0;
		}
	}

	public SU3Field(double[] values) {

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
	
	public YMField add (YMField arg) {

		SU3Field a = (SU3Field) arg;
		
		SU3Field b = new SU3Field();
		for (int i = 0; i < 9; i++) {
			b.v[i] = v[i]+a.v[i];
		}
		return b;
		
	}
	
	public void addAssign(YMField arg) {

		SU3Field a = (SU3Field) arg;

		for (int i = 0; i < 9; i++) {
			v[i] += a.v[i];
		}
		
	}
	
	public YMField sub (YMField arg) {

		SU3Field a = (SU3Field) arg;
		
		SU3Field b = new SU3Field();
		for (int i = 0; i < 9; i++) {
			b.v[i] = v[i]-a.v[i];
		}
		return b;
		
	}

		public void set(int j, double value) {
			double proj = get(j);
			double[] generator;

			switch (j) {
				case 0: generator = new double[]{0,1,0,0,0,0,0,0,0};
					break;
				case 1: generator = new double[]{0,0,0,-1,0,0,0,0,0};
					break;
				case 2: generator = new double[]{1,0,0,0,-1,0,0,0,0};
					break;
				case 3: generator = new double[]{0,0,1,0,0,0,0,0,0};
					break;
				case 4: generator = new double[]{0,0,0,0,0,0,-1,0,0};
					break;
				case 5: generator = new double[]{0,0,0,0,0,1,0,0,0};
					break;
				case 6: generator = new double[]{0,0,0,0,0,0,0,-1,0};
					break;
				case 7: generator = new double[]{1/Math.sqrt(3),0,0,0,1/Math.sqrt(3),0,0,0,-2/Math.sqrt(3)};
					break;
				default: System.out.println("Invalid generator set index!");
					generator = new double[]{0,0,0,0,0,0,0,0,0};
			}

			addAssign((new SU3Field(generator)).mult((value - proj)/2));
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

	/**
	 * @return 2 tr(A^2)
	 */
	public double square () {
		return 2*(v[0]*v[0]+v[4]*v[4]+v[8]*v[8]+2*(v[1]*v[1]+v[2]*v[2]+v[3]*v[3]+v[5]*v[5]+v[6]*v[6]+v[7]*v[7]));
		
	}
	
	public YMField mult (double number) {

		SU3Field b = new SU3Field();
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

	public void set (YMField arg) {

		SU3Field a = (SU3Field) arg;

		for (int i = 0; i < 9; i++) {
			v[i] = a.v[i];
		}

	}
	
	public void addfour (YMField a, YMField b, YMField c, YMField d) {

		for (int i = 0; i < 9; i++) {
			v[i] = a.get(i)+b.get(i)+c.get(i)+d.get(i)+this.get(i);
		}
			
	}

	public void FieldFromForwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d) {}

	public void FieldFromBackwardPlaquette (LinkMatrix a, LinkMatrix b, LinkMatrix c, LinkMatrix d) {}

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
			phases[i] = r * Math.cos(ths[i]) - (linTerm * Math.cos(ths[i])) / (3 * r);
		}

		// now use eigenvalues to compute orthonormal eigenvectors
		// (U - \lambda_i)(U - \lambda_j) has columns that are eigenvectors for the remaining eigenvalue \lambda_k
		// we use this result, but we only need one column so we can avoid doing the full multiplication
		// optimized result computed in Mathematica, of course

		// get one eigenvector for each value
		// normalize vectors in place
		double[][] vectors = new double[3][6];
		for (int i = 0; i < 3; i++) {
			// product of other two phases besides phases[i]
			double otherPhaseProduct = phases[0] * phases[1] * phases[2] / phases[i];
			// sum of other two phases besides phases[i]
			double otherPhaseSum = phases[0] + phases[1] + phases[2] - phases[i];

			vectors[i][0] = v[0]*v[0]+v[1]*v[1]+v[2]*v[2]+v[3]*v[3]+v[6]*v[6]+otherPhaseProduct-v[0]*otherPhaseSum;
			vectors[i][1] = v[0]*v[1]+v[2]*v[5]+v[6]*v[7]+v[1]*(v[4]-otherPhaseSum);
			vectors[i][2] = v[0]*v[2]+v[1]*v[5]-v[3]*v[7]+v[2]*(v[8]-otherPhaseSum);
			vectors[i][3] = 0;
			vectors[i][4] = -v[0]*v[3]-v[5]*v[6]+v[2]*v[7]+v[3]*(otherPhaseSum-v[4]);
			vectors[i][5] = -v[3]*v[5]-v[0]*v[6]-v[1]*v[7]+v[6]*(otherPhaseSum-v[8]);

			normalize(vectors[i]);
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
		SU3Matrix unit = new SU3Matrix(new double[]{vectors[0][0],vectors[1][0],vectors[2][0],
													vectors[0][1],vectors[1][1],vectors[2][1],
													vectors[0][2],vectors[1][2],vectors[2][2],
													vectors[0][3],vectors[1][3],vectors[2][3],
													vectors[0][4],vectors[1][4],vectors[2][4],
													vectors[0][5],vectors[1][5],vectors[2][5]});
		SU3Matrix diag = new SU3Matrix(new double[]{valuesRe[0],0,0,
													0,valuesRe[1],0,
													0,0,valuesRe[2],
													valuesIm[0],0,0,
													0,valuesIm[1],0,
													0,0,valuesIm[2]});
		return ((SU3Matrix) unit.mult(diag).mult(unit.adj())).get();
	}



	/**
	 * Essentially just using exp(I v) ~ 1 + I v
	 */
	public LinkMatrix getLinearizedLink() {
		double[] values = new double[]{1,-v[3],-v[6],v[3],1,-v[7],v[6],v[7],1,v[0],v[1],v[2],v[1],v[4],v[5],v[2],v[5],v[8]};
		return new SU3Matrix(values);
	}
	
	public LinkMatrix getLink() {
		return new SU3Matrix(groupElementDecompositionMethod());
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
}
