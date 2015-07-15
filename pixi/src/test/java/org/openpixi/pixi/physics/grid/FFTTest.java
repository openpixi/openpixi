package org.openpixi.pixi.physics.grid;
import org.junit.Assert;
import org.junit.Test;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;

public class FFTTest {

	private double accuracy = Math.pow(10, -10);

	@Test
	public void testConstantField() {
		int[] dimensions = {512, 32, 8};
		int totalGridPoints = dimensions[0] * dimensions[1] * dimensions[2];
		DoubleFFTWrapper fft = new DoubleFFTWrapper(dimensions);

		// Constant field
		double[] data = new double[fft.getFFTArraySize()];
		for(int i = 0; i < totalGridPoints; i++) {
			data[fft.getFFTArrayIndex(i)] = 1.0;
			data[fft.getFFTArrayIndex(i) + 1] = 0.0;
		}

		fft.complexForward(data);

		Assert.assertEquals(totalGridPoints, data[0], accuracy);
		Assert.assertEquals(0.0, data[1], accuracy);
		for(int i = 1; i < totalGridPoints; i++) {

			Assert.assertEquals(0.0, data[fft.getFFTArrayIndex(i)], accuracy);
			Assert.assertEquals(0.0, data[fft.getFFTArrayIndex(i)+1], accuracy);
		}
	}
}
