package org.openpixi.pixi.physics.grid;


import org.apache.commons.math3.Field;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.junit.Assert;
import org.junit.Test;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.math.SU3Field;
import org.openpixi.pixi.math.SU3Matrix;
import org.openpixi.pixi.math.AlgebraElement;

public class SU3EverythingTest {


	private final double accuracy = 1.E-12;

	@Test
	public void testGetterAndSetter() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create random SU3 field.
			 */
			double[] vec = new double[8];
			double square = 0.0;
			for (int i = 0; i < 8; i++) {
				vec[i] = Math.random() - 0.5;
				square += vec[i] * vec[i];
			}

			/*
				We construct two fields. One from the constructor and another using setter methods.
			 */
			SU3Field firstField = new SU3Field(new double[]{(vec[2]+vec[7]/Math.sqrt(3))/2,vec[0]/2,vec[3]/2,
															-vec[1]/2,(-vec[2]+vec[7]/Math.sqrt(3))/2,vec[5]/2,
															-vec[4]/2,-vec[6]/2,-vec[7]/Math.sqrt(3)});
			SU3Field secondField = new SU3Field();
			for (int i = 0; i < 8; i++) {
				secondField.set(i, vec[i]);
			}

			/*
				Now we test if the values have been set correctly.
			 */
			for (int i = 0; i < 8; i++) {
				Assert.assertEquals(firstField.get(i), vec[i], accuracy);
				Assert.assertEquals(secondField.get(i), vec[i], accuracy);
			}

			/*
				Here the square method is tested.
			 */
			Assert.assertEquals(firstField.square(), square, accuracy);
			Assert.assertEquals(secondField.square(), square, accuracy);
		}
	}

	@Test
	public void SU() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create a random matrix.
			 */
			SU3Matrix m1 = createRandomSU3Matrix();

			/*
				Test determinant
			 */
			double[] d = m1.det();
			Assert.assertArrayEquals(new double[]{1,0}, d, accuracy);

			/*
				Test if U U* is I
			 */
			SU3Matrix m2 = (SU3Matrix) m1.mult(m1.adj());
			Array2DRowFieldMatrix<Complex> s0 = convertToMatrix(m2);
			Field<Complex> field = ComplexField.getInstance();
			Array2DRowFieldMatrix<Complex> s1 = new Array2DRowFieldMatrix<Complex>(field, 3, 3);
			s1.setEntry(0, 0, new Complex(1.0, 0.0));
			s1.setEntry(1, 1, new Complex(1.0, 0.0));
			s1.setEntry(2, 2, new Complex(1.0, 0.0));
			compareMatrices(s0, s1);
		}
	}

	@Test
	public void testAdditionAndSubtraction() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			SU3Matrix a = createRandomSU3Matrix();
			SU3Matrix b = createRandomSU3Matrix();

			Array2DRowFieldMatrix<Complex> aMatrix = convertToMatrix(a);
			Array2DRowFieldMatrix<Complex> bMatrix = convertToMatrix(b);

			/*
				Do the addition.
			 */
			SU3Matrix c = (SU3Matrix) a.add(b);
			Array2DRowFieldMatrix<Complex> cMatrix = (Array2DRowFieldMatrix<Complex>) aMatrix.add(bMatrix).copy();

			/*
				Compare results.
			 */
			Array2DRowFieldMatrix<Complex> cMatrix2 = convertToMatrix(c);
			compareMatrices(cMatrix, cMatrix2);

			/*
				Do the subtraction.
			 */
			c = (SU3Matrix) a.sub(b);
			cMatrix = (Array2DRowFieldMatrix<Complex>) aMatrix.subtract(bMatrix).copy();

			/*
				Compare results.
			 */
			cMatrix2 = convertToMatrix(c);
			compareMatrices(cMatrix, cMatrix2);

			/*
				Now repeat for algebra element.
			 */
			SU3Field fa = (SU3Field) a.getAlgebraElement();
			SU3Field fb = (SU3Field) b.getAlgebraElement();
			aMatrix = convertToMatrix(fa);
			bMatrix = convertToMatrix(fb);
			SU3Field fc = (SU3Field) fa.add(fb);
			cMatrix = (Array2DRowFieldMatrix<Complex>) aMatrix.add(bMatrix).copy();
			cMatrix2 = convertToMatrix(fc);
			compareMatrices(cMatrix, cMatrix2);
			fc = (SU3Field) fa.sub(fb);
			cMatrix = (Array2DRowFieldMatrix<Complex>) aMatrix.subtract(bMatrix).copy();
			cMatrix2 = convertToMatrix(fc);
			compareMatrices(cMatrix, cMatrix2);
		}
	}

	@Test
	public void testMultiplication() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			SU3Matrix a = createRandomSU3Matrix();
			SU3Matrix b = createRandomSU3Matrix();

			Array2DRowFieldMatrix<Complex> aMatrix = convertToMatrix(a);
			Array2DRowFieldMatrix<Complex> bMatrix = convertToMatrix(b);

			/*
				Do the multiplication.
			 */
			SU3Matrix c = (SU3Matrix) a.mult(b);
			Array2DRowFieldMatrix<Complex> cMatrix = (Array2DRowFieldMatrix<Complex>) aMatrix.multiply(bMatrix).copy();

			/*
				Compare results.
			 */
			Array2DRowFieldMatrix<Complex> cMatrix2 = convertToMatrix(c);

			compareMatrices(cMatrix, cMatrix2);
		}
	}

	@Test
	public void testScalarMultiplication() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create a random matrix.
			 */
			SU3Matrix m1 = createRandomSU3Matrix();
			Array2DRowFieldMatrix<Complex> m2 = convertToMatrix(m1);

			/*
				Multiply with real scalar.
			 */
			double rand = Math.random() - 0.5;

			m1 = (SU3Matrix) m1.mult(rand);
			m2 = (Array2DRowFieldMatrix<Complex>) m2.scalarMultiply(new Complex(rand));

			/*
				Compare results.
			 */
			Array2DRowFieldMatrix<Complex> m3 = convertToMatrix(m1);
			compareMatrices(m2, m3);

			/*
				Test corresponding algebra element
			 */
			SU3Field f1 = (SU3Field) m1.getAlgebraElement();
			Array2DRowFieldMatrix<Complex> f2 = convertToMatrix(f1);
			rand = Math.random() - 0.5;
			f1 = (SU3Field) f1.mult(rand);
			f2 = (Array2DRowFieldMatrix<Complex>) f2.scalarMultiply(new Complex(rand));
			Array2DRowFieldMatrix<Complex> f3 = convertToMatrix(f1);
			compareMatrices(f2, f3);
		}
	}

	@Test
	public void testConjugation() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create a random matrix.
			 */
			SU3Matrix m1 = createRandomSU3Matrix();
			Array2DRowFieldMatrix<Complex> m2 = convertToMatrix(m1);

			/*
				Apply hermitian conjugation.
			 */
			m1 = (SU3Matrix) m1.adj();
			m2 = (Array2DRowFieldMatrix<Complex>) m2.transpose();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					Complex v = m2.getEntry(i, j).conjugate();
					m2.setEntry(i, j, v);
				}
			}

			/*
				Compare results.
			 */
			Array2DRowFieldMatrix<Complex> m3 = convertToMatrix(m1);
			compareMatrices(m2, m3);
		}
	}

	@Test
	public void testAlgebraGroupElements() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create a random matrix.
			 */
			SU3Matrix m1 = createRandomSU3Matrix();
			Array2DRowFieldMatrix<Complex> m2 = convertToMatrix(m1);

			SU3Field f1 = (SU3Field) m1.getAlgebraElement();
			// mm1 is algebra element expressed as Array2DRowFieldMatrix
			Array2DRowFieldMatrix<Complex> mm1 = convertToMatrix(f1);

			SU3Matrix m3 = (SU3Matrix) m1.getAlgebraElement().getLink();
			// m4 is group element again, which should be same as m2
			Array2DRowFieldMatrix<Complex> m4 = convertToMatrix(m3);

			Array2DRowFieldMatrix<Complex> arg = (Array2DRowFieldMatrix<Complex>) mm1.scalarMultiply(new Complex(0,1));

			/*
				Exponentiate i*mm1; should be original SU3Matrix m2
			 */
			Field<Complex> field = ComplexField.getInstance();
			// initialize summand to 3x3 identity matrix
			Array2DRowFieldMatrix<Complex> summand = new Array2DRowFieldMatrix<Complex>(field, 3, 3);
			summand.setEntry(0, 0, new Complex(1,0));
			summand.setEntry(1, 1, new Complex(1,0));
			summand.setEntry(2, 2, new Complex(1,0));
			Array2DRowFieldMatrix<Complex> mm2 = (Array2DRowFieldMatrix<Complex>) summand.copy();
			// power series
			for (int i = 1; i <= 100; i++) {
				summand = summand.multiply(arg);
				summand = (Array2DRowFieldMatrix<Complex>) summand.scalarMultiply(new Complex(1./i,0));
				mm2 = mm2.add(summand);
			}

			compareMatrices(m2,mm2);
			compareMatrices(m2,m4);
		}
	}

	@Test
	public void testHermiticity() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create a random matrix.
			 */
			SU3Matrix m1 = createRandomSU3Matrix();

			/*
				Get algebra element
			 */
			SU3Field f1 = (SU3Field) m1.getAlgebraElement();
			Array2DRowFieldMatrix<Complex> ff1 = convertToMatrix(f1);

			/*
				Get adjoint
			 */
			Array2DRowFieldMatrix<Complex> ff2 = (Array2DRowFieldMatrix<Complex>) ff1.transpose();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					Complex v = ff2.getEntry(i, j).conjugate();
					ff2.setEntry(i, j, v);
				}
			}

			/*
				Test Hermiticity
			 */
			compareMatrices(ff1,ff2);

			/*
				Test if traceless
			 */
			double trReal = ff1.getEntry(0,0).getReal() + ff1.getEntry(1,1).getReal() + ff1.getEntry(2,2).getReal();
			double trImag = ff1.getEntry(0,0).getImaginary() + ff1.getEntry(1,1).getImaginary() + ff1.getEntry(2,2).getImaginary();
			Assert.assertEquals(0, trReal, accuracy);
			Assert.assertEquals(0, trImag, accuracy);
		}
	}

	private double[] proj(double[] v1, double[] v2) {
		double[] prod = new double[2];
		prod[0] = v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2]+v1[3]*v2[3]+v1[4]*v2[4]+v1[5]*v2[5];
		prod[1] = v1[0]*v2[3]+v1[1]*v2[4]+v1[2]*v2[5]-v1[3]*v2[0]-v1[4]*v2[1]-v1[5]*v2[2];
		double[] out = new double[6];
		out[0] = prod[0]*v1[0] - prod[1]*v1[3];
		out[1] = prod[0]*v1[1] - prod[1]*v1[4];
		out[2] = prod[0]*v1[2] - prod[1]*v1[5];
		out[3] = prod[0]*v1[3] + prod[1]*v1[0];
		out[4] = prod[0]*v1[4] + prod[1]*v1[1];
		out[5] = prod[0]*v1[5] + prod[1]*v1[2];
		return out;
	}

	// uses Gram-Schmidt on random matrix (-> unitary) and then normalizes to set determinant (-> SU)
	public SU3Matrix createRandomSU3Matrix() {
		double[] v1 = new double[6];
		double[] v2 = new double[6];
		double[] v3 = new double[6];
		for (int i = 0; i < 6; i++) {
			v1[i] = Math.random() - .5;
			v2[i] = Math.random() - .5;
			v3[i] = Math.random() - .5;
		}
		double norm = v1[0]*v1[0]+v1[1]*v1[1]+v1[2]*v1[2]+v1[3]*v1[3]+v1[4]*v1[4]+v1[5]*v1[5];
		norm = Math.sqrt(norm);
		for (int i = 0; i < 6; i++) {
			v1[i] /= norm;
		}
		double[] projVector;
		projVector = proj(v1,v2);
		for (int i = 0; i < 6; i++) {
			v2[i] -= projVector[i];
		}
		norm = v2[0]*v2[0]+v2[1]*v2[1]+v2[2]*v2[2]+v2[3]*v2[3]+v2[4]*v2[4]+v2[5]*v2[5];
		norm = Math.sqrt(norm);
		for (int i = 0; i < 6; i++) {
			v2[i] /= norm;
		}
		projVector = proj(v1, v3);
		for (int i = 0; i < 6; i++) {
			v3[i] -= projVector[i];
		}
		projVector = proj(v2,v3);
		for (int i = 0; i < 6; i++) {
			v3[i] -= projVector[i];
		}
		norm = v3[0]*v3[0]+v3[1]*v3[1]+v3[2]*v3[2]+v3[3]*v3[3]+v3[4]*v3[4]+v3[5]*v3[5];
		norm = Math.sqrt(norm);
		for (int i = 0; i < 6; i++) {
			v3[i] /= norm;
		}
		SU3Matrix u = new SU3Matrix(new double[]{v1[0],v1[1],v1[2],v2[0],v2[1],v2[2],v3[0],v3[1],v3[2],v1[3],v1[4],v1[5],v2[3],v2[4],v2[5],v3[3],v3[4],v3[5]});
		double[] d = u.det();
		double r, th;
		r = Math.pow(d[0]*d[0] + d[1]*d[1],-1./6);
		th = -Math.atan2(d[1], d[0])/3;
		d[0] = r*Math.cos(th);
		d[1] = r*Math.sin(th);
		for (int i = 0; i < 9; i++) {
			double ure, uim;
			ure = u.get(i);
			uim = u.get(i+9);
			u.set(i,ure*d[0]-uim*d[1]);
			u.set(i+9,ure*d[1]+uim*d[0]);
		}
		return u;
	}

	private void compareMatrices(Array2DRowFieldMatrix<Complex> a, Array2DRowFieldMatrix<Complex> b) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				Assert.assertEquals(a.getEntry(i, j).getReal(),
						b.getEntry(i, j).getReal(),
						accuracy);
				Assert.assertEquals(a.getEntry(i, j).getImaginary(),
						b.getEntry(i, j).getImaginary(),
						accuracy);
			}
		}
	}

	private Array2DRowFieldMatrix<Complex> convertToMatrix(GroupElement arg) {
		SU3Matrix input = (SU3Matrix) arg;

		Field<Complex> field = ComplexField.getInstance();
		Array2DRowFieldMatrix<Complex> output = new Array2DRowFieldMatrix<Complex>(field, 3, 3);

		output.setEntry(0, 0, new Complex(input.get(0),input.get(9)));
		output.setEntry(0, 1, new Complex(input.get(1),input.get(10)));
		output.setEntry(0, 2, new Complex(input.get(2),input.get(11)));
		output.setEntry(1, 0, new Complex(input.get(3),input.get(12)));
		output.setEntry(1, 1, new Complex(input.get(4),input.get(13)));
		output.setEntry(1, 2, new Complex(input.get(5),input.get(14)));
		output.setEntry(2, 0, new Complex(input.get(6),input.get(15)));
		output.setEntry(2, 1, new Complex(input.get(7),input.get(16)));
		output.setEntry(2, 2, new Complex(input.get(8),input.get(17)));

		return output;
	}

	private Array2DRowFieldMatrix<Complex> convertToMatrix(AlgebraElement arg) {
		SU3Field input = (SU3Field) arg;

		Field<Complex> field = ComplexField.getInstance();
		Array2DRowFieldMatrix<Complex> output = new Array2DRowFieldMatrix<Complex>(field, 3, 3);

		output.setEntry(0, 0, new Complex(input.getEntry(0),0));
		output.setEntry(0, 1, new Complex(input.getEntry(1),input.getEntry(3)));
		output.setEntry(0, 2, new Complex(input.getEntry(2),input.getEntry(6)));
		output.setEntry(1, 0, new Complex(input.getEntry(1),-input.getEntry(3)));
		output.setEntry(1, 1, new Complex(input.getEntry(4),0));
		output.setEntry(1, 2, new Complex(input.getEntry(5),input.getEntry(7)));
		output.setEntry(2, 0, new Complex(input.getEntry(2),-input.getEntry(6)));
		output.setEntry(2, 1, new Complex(input.getEntry(5),-input.getEntry(7)));
		output.setEntry(2, 2, new Complex(input.getEntry(8),0));

		return output;
	}
}
