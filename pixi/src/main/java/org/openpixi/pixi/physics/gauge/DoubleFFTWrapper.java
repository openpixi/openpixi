package org.openpixi.pixi.physics.gauge;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;

/**
 * FFT Wrapper for jtransform for arbitrary dimensions (up to 3).
 *
 */
public class DoubleFFTWrapper {

	private int[] dimensions;
	private List<Integer> dimensionsFFT;

	private DoubleFFT_1D doubleFFT_1D;
	private DoubleFFT_2D doubleFFT_2D;
	private DoubleFFT_3D doubleFFT_3D;

	/**
	 * Constructor of FFT Wrapper
	 * @param dimensions dimension and size in each direction
	 */
	public DoubleFFTWrapper(int[] dimensions) {
		this.dimensions = dimensions;

		dimensionsFFT = getEffectiveDimension(dimensions);

		if (dimensionsFFT.size() < 0 || dimensionsFFT.size() > 3) {
			System.out.println("FFTWrapper: dimension " + dimensionsFFT.size() + " is not allowed.");
		}

		switch(dimensionsFFT.size()) {
		case 1:
			doubleFFT_1D = new DoubleFFT_1D(dimensionsFFT.get(0));
			break;
		case 2:
			doubleFFT_2D = new DoubleFFT_2D(dimensionsFFT.get(0), dimensionsFFT.get(1));
			break;
		case 3:
			doubleFFT_3D = new DoubleFFT_3D(dimensionsFFT.get(0), dimensionsFFT.get(1), dimensionsFFT.get(2));
			break;
		}
	}

	/**
	 * Return a list of dimensions whose size > 1.
	 *
	 * @param dimensions
	 * @return
	 */
	private List<Integer> getEffectiveDimension(int[] dimensions) {
		List<Integer> effectiveDimensions = new ArrayList<Integer>();
		for (int d : dimensions) {
			if (d > 1) {
				effectiveDimensions.add(d);
			} else if (d < 1) {
				System.out.println("FFTWrapper: Dimension < 1 along a direction is not allowed");
			}
		}
		return effectiveDimensions;
	}

	public void complexForward(double[] data) {
		switch(dimensionsFFT.size()) {
		case 1:
			doubleFFT_1D.complexForward(data);
			break;
		case 2:
			doubleFFT_2D.complexForward(data);
			break;
		case 3:
			doubleFFT_3D.complexForward(data);
			break;
		}
	}

	public void complexInverse(double[] data, boolean scale) {
		switch(dimensionsFFT.size()) {
		case 1:
			doubleFFT_1D.complexInverse(data, scale);
			break;
		case 2:
			doubleFFT_2D.complexInverse(data, scale);
			break;
		case 3:
			doubleFFT_3D.complexInverse(data, scale);
			break;
		}
	}

	/**
	 * Return array size for FFT.
	 * Takes into account factor 2 for real and imaginary parts.
	 * @return
	 */
	public int getFFTArraySize() {
		int arraySize = 2; // factor 2 for real and imaginary parts.
		for (int size : dimensions) {
			arraySize *= size;
		}
		return arraySize;
	}

	/**
	 * Return array index for FFT.
	 * Takes into account factor 2 for real and imaginary parts.
	 * Add 0 to obtain index for real part.
	 * Add 1 to obtain index for imaginary part.
	 * @param coordinates
	 * @return
	 */
	public int getFFTArrayIndex(int[] coordinates) {
		int index = coordinates[0];
		for (int i = 1; i < dimensions.length; i++) {
			index *= dimensions[i];
			index += coordinates[i];
		}
		return 2 * index; // factor 2 for real and imaginary parts
	}
}
