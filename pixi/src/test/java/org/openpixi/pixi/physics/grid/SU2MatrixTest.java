package org.openpixi.pixi.physics.grid;


import org.apache.commons.math3.Field;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.math3.linear.EigenDecomposition;

public class SU2MatrixTest {


	private final double accuracy = 1.E-13;

	@Test
	public void testGetterAndSetter() {
				/*
			Create random SU2 matrix.
		 */
		double[] vec = new double[4];
		double modulus = 0.0;
		for (int i = 0; i < 4; i++) {
			vec[i] = Math.random() - 0.5;
			modulus += vec[i] * vec[i];
		}
		modulus = Math.sqrt(modulus);

		for (int i = 0; i < 4; i++) {
			vec[i] /= modulus;
		}

		/*
			We construct two matrices. One from the constructor and another using setter methods.
		 */

		SU2Matrix firstMatrix = new SU2Matrix(vec[0], vec[1], vec[2], vec[3]);
		SU2Matrix secondMatrix = new SU2Matrix();
		for (int i = 0; i < 4; i++) {
			secondMatrix.set(i, vec[i]);
		}

		/*
			Now we test if the values have been set correctly.
		 */
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(firstMatrix.get(i), vec[i], accuracy);
			Assert.assertEquals(secondMatrix.get(i), vec[i], accuracy);
		}

		/*
			We check for unitarity using the built-in methods.
		 */

		Assert.assertEquals(firstMatrix.computeParameterNorm(), 1.0, accuracy);
		Assert.assertEquals(secondMatrix.computeParameterNorm(), 1.0, accuracy);
	}

	@Test
	public void testUnitarity() {
		/*
			Create random SU2 matrix. Should be unitary.
		 */
		SU2Matrix matrix = createRandomSU2Matrix();

		/*
			We check for unitarity using the built-in methods.
		 */
		Assert.assertEquals(matrix.computeParameterNorm(), 1.0, accuracy);

		/*
			Now we make a change to the matrix and see if unitarity can be restored.
		 */
		matrix.set(3, 0.0);
		matrix.computeFirstParameter();
		Assert.assertEquals(matrix.computeParameterNorm(), 1.0, accuracy);

	}

	@Test
	public void testAdditionAndSubtraction() {
		/*
			Prepare some variables.
		 */
		double[] aVec = new double[4];
		double[] bVec = new double[4];
		double[] rVec1 = new double[4];
		double[] rVec2 = new double[4];

		double aMod = 0.0;
		double bMod = 0.0;

		for (int i = 0; i < 4; i++) {
			aVec[i] = Math.random() - 0.5;
			bVec[i] = Math.random() - 0.5;

			aMod += aVec[i] * aVec[i];
			bMod += bVec[i] * bVec[i];
		}

		aMod = Math.sqrt(aMod);
		bMod = Math.sqrt(bMod);

		for (int i = 0; i < 4; i++) {
			aVec[i] /= aMod;
			bVec[i] /= bMod;
		}

		for (int i = 0; i < 4; i++) {
			rVec1[i] = aVec[i] + bVec[i];
			rVec2[i] = aVec[i] - bVec[i];
		}

		/*
			Create two matrices.
		 */
		SU2Matrix a = new SU2Matrix();
		SU2Matrix b = new SU2Matrix();
		for (int i = 0; i < 4; i++) {
			a.set(i, aVec[i]);
			b.set(i, bVec[i]);
		}

		/*
			Use add and sub methods.
		 */
		SU2Matrix r1 = (SU2Matrix) a.add(b);
		SU2Matrix r2 = (SU2Matrix) a.sub(b);

		/*
			Compare results.
		 */
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(r1.get(i), rVec1[i], accuracy);
			Assert.assertEquals(r2.get(i), rVec2[i], accuracy);
		}
	}

	@Test
	public void testBasicMatrixConversionStuff() {

		Field<Complex> field = ComplexField.getInstance();
		Complex imaginaryUnit = new Complex(0.0, 1.0);

		/*
			Definition of unity and Pauli matrices
		 */

		// Unity
		Array2DRowFieldMatrix<Complex> s0 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s0.setEntry(0, 0, new Complex(1.0, 0.0));
		s0.setEntry(1, 1, new Complex(1.0, 0.0));

		// Pauli x
		Array2DRowFieldMatrix<Complex> s1 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s1.setEntry(0, 1, new Complex(1.0, 0.0));
		s1.setEntry(1, 0, new Complex(1.0, 0.0));
		s1 = (Array2DRowFieldMatrix<Complex>) s1.scalarMultiply(imaginaryUnit);

		// Pauli y
		Array2DRowFieldMatrix<Complex> s2 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s2.setEntry(0, 1, new Complex(0.0, -1.0));
		s2.setEntry(1, 0, new Complex(0.0, 1.0));
		s2 = (Array2DRowFieldMatrix<Complex>) s2.scalarMultiply(imaginaryUnit);

		// Pauli z
		Array2DRowFieldMatrix<Complex> s3 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s3.setEntry(0, 0, new Complex(1.0, 0.0));
		s3.setEntry(1, 1, new Complex(-1.0, 0.0));
		s3 = (Array2DRowFieldMatrix<Complex>) s3.scalarMultiply(imaginaryUnit);


		/*
			Create unit matrix.
		 */
		SU2Matrix a = new SU2Matrix(1.0, 0.0, 0.0, 0.0);
		Array2DRowFieldMatrix<Complex> aMatrix = convertToMatrix(a);
		compareMatrices(aMatrix, s0);

		/*
			Create Pauli x matrix times i.
		 */
		SU2Matrix b = new SU2Matrix(0.0, 1.0, 0.0, 0.0);
		Array2DRowFieldMatrix<Complex> bMatrix = convertToMatrix(b);
		compareMatrices(bMatrix, s1);

		/*
			Create Pauli y matrix times i.
		 */
		SU2Matrix c = new SU2Matrix(0.0, 0.0, 1.0, 0.0);
		Array2DRowFieldMatrix<Complex> cMatrix = convertToMatrix(c);
		compareMatrices(cMatrix, s2);

		/*
			Create Pauli y matrix times i.
		 */
		SU2Matrix d = new SU2Matrix(0.0, 0.0, 0.0, 1.0);
		Array2DRowFieldMatrix<Complex> dMatrix = convertToMatrix(d);
		compareMatrices(dMatrix, s3);

		/*
			Test matrix multiplication between Pauli matrices.
		 */

		LinkMatrix r;
		Array2DRowFieldMatrix<Complex> rMatrix;
		Array2DRowFieldMatrix<Complex> rMatrix2;

		r = b.mult(c);
		rMatrix = convertToMatrix(r);
		rMatrix2 = bMatrix.multiply(cMatrix);
		compareMatrices(rMatrix, rMatrix2);

		r = c.mult(d);
		rMatrix = convertToMatrix(r);
		rMatrix2 = cMatrix.multiply(dMatrix);
		compareMatrices(rMatrix, rMatrix2);

		r = b.mult(d);
		rMatrix = convertToMatrix(r);
		rMatrix2 = bMatrix.multiply(dMatrix);
		compareMatrices(rMatrix, rMatrix2);


	}

	@Test
	public void testMultiplication() {
		SU2Matrix a = createRandomSU2Matrix();
		SU2Matrix b = createRandomSU2Matrix();

		Array2DRowFieldMatrix<Complex> aMatrix = convertToMatrix(a);
		Array2DRowFieldMatrix<Complex> bMatrix = convertToMatrix(b);

		/*
			Do the multiplication.
		 */
		LinkMatrix c = a.mult(b);
		Array2DRowFieldMatrix<Complex> cMatrix = (Array2DRowFieldMatrix<Complex>) aMatrix.multiply(bMatrix).copy();

		/*
			Compare results.
		 */
		Array2DRowFieldMatrix<Complex> cMatrix2 = convertToMatrix(c);

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
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
			SU2Matrix m1 = createRandomSU2Matrix();
			Array2DRowFieldMatrix<Complex> m2 = convertToMatrix(m1);

			/*
				Multiply with real scalar.
			 */
			double rand = Math.random() - 0.5;

			m1 = (SU2Matrix) m1.mult(rand);
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
			SU2Matrix m1 = createRandomSU2Matrix();
			Array2DRowFieldMatrix<Complex> m2 = convertToMatrix(m1);

			/*
				Apply hermitian conjugation.
			 */
			m1 = (SU2Matrix) m1.adj();
			m2 = (Array2DRowFieldMatrix<Complex>) m2.transpose();
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
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

	private SU2Matrix createRandomSU2Matrix() {
		/*
			Create random SU2 matrix.
		 */
		double[] vec = new double[4];
		double modulus = 0.0;
		for (int i = 0; i < 4; i++) {
			vec[i] = Math.random() - 0.5;
			modulus += vec[i] * vec[i];
		}
		modulus = Math.sqrt(modulus);

		for (int i = 0; i < 4; i++) {
			vec[i] /= modulus;
		}

		SU2Matrix m = new SU2Matrix(vec[0], vec[1], vec[2], vec[3]);
		Assert.assertEquals(m.computeParameterNorm(), 1.0, accuracy);

		return m;
	}

	private void compareMatrices(Array2DRowFieldMatrix<Complex> a, Array2DRowFieldMatrix<Complex> b) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
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
		/*
			This is very, very tedious. I'm sorry.
		 */

		SU2Matrix input = (SU2Matrix) arg;

		Field<Complex> field = ComplexField.getInstance();
		Complex imaginaryUnit = new Complex(0.0, 1.0);
		Array2DRowFieldMatrix<Complex> output = new Array2DRowFieldMatrix<Complex>(field, 2, 2);

		/*
			Definition of unity and Pauli matrices
		 */

		// Unity
		Array2DRowFieldMatrix<Complex> s0 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s0.setEntry(0, 0, new Complex(1.0, 0.0));
		s0.setEntry(1, 1, new Complex(1.0, 0.0));

		// Pauli x
		Array2DRowFieldMatrix<Complex> s1 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s1.setEntry(0, 1, new Complex(1.0, 0.0));
		s1.setEntry(1, 0, new Complex(1.0, 0.0));

		// Pauli y
		Array2DRowFieldMatrix<Complex> s2 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s2.setEntry(0, 1, new Complex(0.0, -1.0));
		s2.setEntry(1, 0, new Complex(0.0, 1.0));

		// Pauli z
		Array2DRowFieldMatrix<Complex> s3 = new Array2DRowFieldMatrix<Complex>(field, 2, 2);
		s3.setEntry(0, 0, new Complex(1.0, 0.0));
		s3.setEntry(1, 1, new Complex(-1.0, 0.0));

		/*
			Representation of a SU(2) matrix using Pauli matrices.

			U = I * a_0 + i \sigma_j a_j
		 */

		s0 = (Array2DRowFieldMatrix<Complex>) s0.scalarMultiply(new Complex(input.get(0)));
		s1 = (Array2DRowFieldMatrix<Complex>) s1.scalarMultiply(new Complex(input.get(1)));
		s2 = (Array2DRowFieldMatrix<Complex>) s2.scalarMultiply(new Complex(input.get(2)));
		s3 = (Array2DRowFieldMatrix<Complex>) s3.scalarMultiply(new Complex(input.get(3)));

		output = output.add(s0);
		output = output.add((Array2DRowFieldMatrix<Complex>) s1.scalarMultiply(imaginaryUnit));
		output = output.add((Array2DRowFieldMatrix<Complex>) s2.scalarMultiply(imaginaryUnit));
		output = output.add((Array2DRowFieldMatrix<Complex>) s3.scalarMultiply(imaginaryUnit));

		return output;
	}


}
