package org.openpixi.pixi.physics.grid;


import org.junit.Assert;
import org.junit.Test;
import org.openpixi.pixi.math.SU2Field;
import org.openpixi.pixi.math.SU2Matrix;

public class SU2FieldTest {


	private final double accuracy = 1.E-13;

	@Test
	public void testGetterAndSetter() {
				/*
			Create random SU2 field.
		 */
		double[] vec = new double[3];
		double square = 0.0;
		for (int i = 0; i < 3; i++) {
			vec[i] = Math.random() - 0.5;
			square += vec[i] * vec[i];
		}

		/*
			We construct two fields. One from the constructor and another using setter methods.
		 */

		SU2Field firstField = new SU2Field(vec[0], vec[1], vec[2]);
		SU2Field secondField = new SU2Field();
		for (int i = 0; i < 3; i++) {
			secondField.set(i, vec[i]);
		}

		/*
			Now we test if the values have been set correctly.
		 */
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(firstField.get(i), vec[i], accuracy);
			Assert.assertEquals(secondField.get(i), vec[i], accuracy);
		}
		/*
		Here the square method is tested.
	 */
		Assert.assertEquals(firstField.square(), square, accuracy);
		Assert.assertEquals(secondField.square(), square, accuracy);
	}

	@Test
	public void testAdditionAndSubtraction() {
		/*
			Prepare some variables.
		 */
		double[] aVec = new double[3];
		double[] bVec = new double[3];
		double[] rVec1 = new double[3];
		double[] rVec2 = new double[3];

		for (int i = 0; i < 3; i++) {
			aVec[i] = Math.random() - 0.5;
			bVec[i] = Math.random() - 0.5;
		}

		for (int i = 0; i < 3; i++) {
			rVec1[i] = aVec[i] + bVec[i];
			rVec2[i] = aVec[i] - bVec[i];
		}

		/*
			Create two fields.
		 */
		SU2Field a = new SU2Field();
		SU2Field b = new SU2Field();
		for (int i = 0; i < 3; i++) {
			a.set(i, aVec[i]);
			b.set(i, bVec[i]);
		}

		/*
			Use add and sub methods.
		 */
		SU2Field r1 = (SU2Field) a.add(b);
		SU2Field r2 = (SU2Field) a.sub(b);

		/*
			Compare results.
		 */
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(r1.get(i), rVec1[i], accuracy);
			Assert.assertEquals(r2.get(i), rVec2[i], accuracy);
		}
	}

	@Test
	public void testConversionToMatrixAndBack() {

		/*
		Create random SU2 field.
	 */
		double[] vec = new double[3];
		double scaling = 1.0;
		for (int i = 0; i < 3; i++) {
			vec[i] = (Math.random() - 0.5)*scaling;
		}
		SU2Field firstField = new SU2Field(vec[0], vec[1], vec[2]);
		
		/*
		Transform to a SU2 matrix exactly and in linear approximation.
	 */
		SU2Matrix matSimple = (SU2Matrix) firstField.getLinearizedLink();
		SU2Matrix matExact = (SU2Matrix) firstField.getLink();

		/*
		Transform back to a SU2 field exactly and in linear approximation (proj).
	 */
		SU2Field fieldSimple = (SU2Field) matSimple.proj();
		SU2Field fieldExact = (SU2Field) matExact.getAlgebraElement();
		
		/*
		Compare results.
	 */
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(fieldSimple.get(i), firstField.get(i), accuracy);
			Assert.assertEquals(fieldExact.get(i), firstField.get(i), accuracy);
		}
		
	}

	@Test
	public void testScalarMultiplication() {
		int numberOfTests = 10;
		for (int t = 0; t < numberOfTests; t++) {
			/*
				Create a random field.
			 */
			double[] vec1 = new double[3];
			double[] vec2 = new double[3];
			double value = Math.random() - 0.5;
			for (int i = 0; i < 3; i++) {
				vec1[i] = Math.random() - 0.5;
				vec2[i] = value*vec1[i];
			}
			SU2Field firstField = new SU2Field(vec1[0], vec1[1], vec1[2]);
			/*
			Multiply with a scalar.
		 */
			SU2Field secondField = (SU2Field) firstField.mult(value);
			/*
				Compare results.
			 */
			for (int i = 0; i < 3; i++) {
				Assert.assertEquals(secondField.get(i), vec2[i], accuracy);
			}


		}
	}


}
