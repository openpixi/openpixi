package org.openpixi.pixi.physics.grid;


import org.apache.commons.math3.Field;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.junit.Assert;
import org.junit.Test;

public class SU3MatrixTest {


	private final double accuracy = 1.E-13;

	@Test
	public void testGetterAndSetter() {
	}

	@Test
	public void SU() {
	}

	@Test
	public void testAdditionAndSubtraction() {
	}

	@Test
	public void testBasicMatrixConversionStuff() {
	}

	@Test
	public void testMultiplication() {
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

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				Assert.assertEquals(cMatrix.getEntry(i, j).getReal(),
						cMatrix2.getEntry(i, j).getReal(),
						accuracy);

				Assert.assertEquals(cMatrix.getEntry(i, j).getImaginary(),
						cMatrix2.getEntry(i, j).getImaginary(),
						accuracy);
			}
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

	private SU3Matrix createRandomSU3Matrix() {
		double[] v1 = new double[6];
		double[] v2 = new double[6];
		double[] v3 = new double[6];
		for (int i = 0; i < 6; i++) {
			v1[i] = Math.random();
			v2[i] = Math.random();
			v3[i] = Math.random();
		}
		double norm = v1[0]*v1[0]+v1[1]*v1[1]+v1[2]*v1[2]+v1[3]*v1[3]+v1[4]*v1[4]+v1[5]*v1[5];
		norm = Math.sqrt(norm);
		for (int i = 0; i < 6; i++) {
			v1[i] /= norm;
		}
		v2 = new double[]{(1-v1[0]*v1[0]+v1[3]*v1[3])*v2[0]+v1[3]*(v1[4]*v2[1]+v1[5]*v2[2]-v1[1]*v2[4]-v1[2]*v2[5])-v1[0]*(v1[1]*v2[1]+v1[2]*v2[2]+2*v1[3]*v2[3]+v1[4]*v2[4]+v1[5]*v2[5]),
				v1[3]*v1[4]*v2[0]+v2[1]-v1[1]*v1[1]*v2[1]+v1[4]*v1[4]*v2[1]-v1[1]*v1[2]*v2[2]+v1[4]*v1[5]*v2[2]-v1[1]*v1[3]*v2[3]-v1[0]*(v1[1]*v2[0]+v1[4]*v2[3])-2*v1[1]*v1[4]*v2[4]-v1[2]*v1[4]*v2[5]-v1[1]*v1[5]*v2[5],
				v1[3]*v1[5]*v2[0]-v1[1]*v1[2]*v2[1]+v1[4]*v1[5]*v2[1]+v2[2]-v1[2]*v1[2]*v2[2]+v1[5]*v1[5]*v2[2]-v1[2]*v1[3]*v2[3]-v1[0]*(v1[2]*v2[0]+v1[5]*v2[3])-v1[2]*v1[4]*v2[4]-v1[1]*v1[5]*v2[4]-2*v1[2]*v1[5]*v2[5],
				-v1[1]*v1[3]*v2[1]-v1[2]*v1[3]*v2[2]+v2[3]+v1[0]*v1[0]*v2[3]-v1[3]*v1[3]*v2[3]-v1[3]*v1[4]*v2[4]-v1[3]*v1[5]*v2[5]+v1[0]*(-2*v1[3]*v2[0]-v1[4]*v2[1]-v1[5]*v2[2]+v1[1]*v2[4]+v1[2]*v2[5]),
				-v1[0]*v1[4]*v2[0]-v1[2]*v1[4]*v2[2]-v1[3]*v1[4]*v2[3]+v2[4]+v1[1]*v1[1]*v2[4]-v1[4]*v1[4]*v2[4]-v1[4]*v1[5]*v2[5]+v1[1]*(-v1[3]*v2[0]-2*v1[4]*v2[1]-v1[5]*v2[2]+v1[0]*v2[3]+v1[2]*v2[5]),
				-v1[0]*v1[5]*v2[0]-v1[1]*v1[5]*v2[1]-v1[3]*v1[5]*v2[3]-v1[4]*v1[5]*v2[4]+v1[2]*(-v1[3]*v2[0]-v1[4]*v2[1]-2*v1[5]*v2[2]+v1[0]*v2[3]+v1[1]*v2[4])+v2[5]+v1[2]*v1[2]*v2[5]-v1[5]*v1[5]*v2[5]};
		norm = v2[0]*v2[0]+v2[1]*v2[1]+v2[2]*v2[2]+v2[3]*v2[3]+v2[4]*v2[4]+v2[5]*v2[5];
		norm = Math.sqrt(norm);
		for (int i = 0; i < 6; i++) {
			v2[i] /= norm;
		}
			return new SU3Matrix();
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

	private Array2DRowFieldMatrix<Complex> convertToMatrix(LinkMatrix arg) {
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


}
