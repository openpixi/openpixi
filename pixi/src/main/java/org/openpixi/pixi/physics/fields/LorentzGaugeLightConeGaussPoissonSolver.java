package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.physics.grid.Grid;

public class LorentzGaugeLightConeGaussPoissonSolver extends LightConePoissonSolver {

	private static int dir;
	private static double[][] location;
	private static int[] orientation;
	private static double[] width;
	private static Erf erf;
	private static int numberOfInstances;

	public LorentzGaugeLightConeGaussPoissonSolver() {	}

	public void saveValues(int totalNumOfInstances, int numInstance, double[] position, int direction, int orientation, double width) {
		if(numInstance == 0) {
			numberOfInstances = totalNumOfInstances;
			this.location = new double[totalNumOfInstances][position.length];
			this.orientation = new int[totalNumOfInstances];
			this.width = new double[totalNumOfInstances];
			this.dir = direction;
		}

		if( direction != this.dir ) {
			System.out.println("LorentzGaugeLightConeGaussPoissonSolver: Polychromatic Gaussian currents with DIFFERENT DIRECTIONS are not yet supported!.");
		} else {
			System.arraycopy(position, 0, this.location[numInstance], 0, position.length);
			this.orientation[numInstance] = orientation;
			this.width[numInstance] = width;
		}

		for(int i = 0; i < numInstance; i++) {
			if( this.location[i][dir] == position[dir] ) {
				this.orientation[numInstance] = 0;
			} else {}
		}
	}

	public void solve(Grid g) {
		for(int i = 0; i < numberOfInstances; i++) {
			if( orientation[i] != 0 ) {
				lorentzSolver(i, g);
			} else {}
		}
	}

	public void lorentzSolver(int surfaceIndex,Grid g) {

		int truesize = 0;
		for(int i = 0; i < g.getNumberOfDimensions(); i++) {
			if( (i != dir) && (g.getNumCells(i) > 1) ) {
				truesize++;
			} else {}
		}
		int[] size = new int[truesize];
		int[] signature = new int[truesize];
		int k = 0;
		for(int i = 0; i < g.getNumberOfDimensions(); i++) {
			if( (i != dir) && (g.getNumCells(i) > 1) ) {
				size[k] = g.getNumCells(i);
				signature[k] = i;
				k++;
			} else {}
		}

		int numberOfComponents = g.getNumberOfColors() * g.getNumberOfColors() - 1;
		double as = g.getLatticeSpacing();
		double at = g.getTemporalSpacing();
		double coupling = g.getGaugeCoupling();
		int cellIndex;
		int dirMax = g.getNumCells(dir);
		int arrayLength = location[surfaceIndex].length;

		double[] positionCurrent = new double[arrayLength];
		System.arraycopy(location[surfaceIndex], 0, positionCurrent, 0, arrayLength);
		positionCurrent[dir] -= orientation[surfaceIndex]*at/2;

		ElementFactory factory = g.getElementFactory();
		int colors = g.getNumberOfColors();

		double norm = Math.pow(as, 2);

		int[] pos = new int[arrayLength];
		int[] gaugePos = new int[arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			pos[i] = (int) Math.rint(location[surfaceIndex][i]/as);
		}

		double gaussNormFactorCharge = shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], pos[dir] * as) * g.getGaugeCoupling() * as;

		if (size.length > 1) {
			double[][] charge = new double[size[0]][2 * size[1]];
			double[][] current = new double[size[0]][2 * size[1]];
			AlgebraElement[][] phiList = new AlgebraElement[size[0]][size[1]];
			AlgebraElement[][] gaugeList = new AlgebraElement[size[0]][size[1]];
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					phiList[j][w] = factory.algebraZero(colors);
					gaugeList[j][w] = factory.algebraZero(colors);
				}
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;
						cellIndex = g.getCellIndex(pos);

						charge[j][2*w] = g.getRho(cellIndex).get(i);
						charge[j][2*w + 1] = 0.0;
						current[j][2*w] = g.getJ(cellIndex, dir).get(i);
						current[j][2*w + 1] = 0.0;
					}
				}
				//perform Fourier transformation
				DoubleFFT_2D fft = new DoubleFFT_2D(size[0], size[1]);
				fft.complexForward(charge);
				fft.complexForward(current);
				//perform computation in Fourier space
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						double psqr = (4 - 2 * Math.cos((2 * Math.PI * j) / size[0]) - 2 * Math.cos((2 * Math.PI * w) / size[1]))/norm;
						if( (j+w) != 0 ) {
							charge[j][2*w] = charge[j][2*w]/psqr;
							charge[j][2*w + 1] = charge[j][2*w + 1]/psqr;
							current[j][2*w] = current[j][2*w]/psqr;
							current[j][2*w + 1] = current[j][2*w + 1]/psqr;
						}
					}
				}
				charge[0][0] = 0.0;
				charge[0][1] = 0.0;
				current[0][0] = 0.0;
				current[0][1] = 0.0;
				//perform inverse Fourier transform
				fft.complexInverse(charge, true);
				fft.complexInverse(current, true);
				//compute the values of the electric potential
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;
						phiList[j][w].set(i, charge[j][2 * w]);
						gaugeList[j][w].set(i, current[j][2*w]);
					}
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			AlgebraElement Aspatial, Atemporal;
			Aspatial = factory.algebraZero(colors);
			Atemporal = factory.algebraZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int w = 0; w < size[1]; w++) {
					gaugePos[signature[1]] = w;
					for (int z = 0; z < dirMax; z++) {
						gaugePos[dir] = z;
						cellIndex = g.getCellIndex(gaugePos);

						Atemporal.set(g.getU0(cellIndex).getAlgebraElement());
						Atemporal.addAssign(phiList[j][w].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));

						Aspatial.set(g.getU(cellIndex, dir).getAlgebraElement());
						Aspatial.addAssign(gaugeList[j][w].mult(shapeGauss(positionCurrent[dir], width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));

						g.setU(cellIndex, dir, Aspatial.getLink());
						g.setU0(cellIndex, Atemporal.getLink());

						Atemporal.set(g.getU0next(cellIndex).getAlgebraElement());
						Atemporal.addAssign(phiList[j][w].mult(shapeGauss(location[surfaceIndex][dir] + orientation[surfaceIndex] * at, width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));

						Aspatial.set(g.getUnext(cellIndex, dir).getAlgebraElement());
						Aspatial.addAssign(gaugeList[j][w].mult(shapeGauss(positionCurrent[dir] + orientation[surfaceIndex]*at, width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));

						g.setUnext(cellIndex, dir, Aspatial.getLink());
						g.setU0next(cellIndex, Atemporal.getLink());
					}
				}
			}

		} else if(size.length == 1) {
			double[] charge = new double[2 * size[0]];
			double[] current = new double[2 * size[0]];
			AlgebraElement[] phiList = new AlgebraElement[size[0]];
			AlgebraElement[] gaugeList = new AlgebraElement[size[0]];
			for(int j = 0; j < size[0]; j++) {
				phiList[j] = factory.algebraZero(colors);
				gaugeList[j] = factory.algebraZero(colors);
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;

					cellIndex = g.getCellIndex(pos);

					charge[2*j] = g.getRho(cellIndex).get(i);
					charge[2*j + 1] = 0.0;
					current[2*j] = g.getJ(cellIndex, dir).get(i);
					current[2*j + 1] = 0.0;
				}
				//perform Fourier transformation
				DoubleFFT_1D fft = new DoubleFFT_1D(size[0]);
				fft.complexForward(charge);
				fft.complexForward(current);
				//perform computation in Fourier space
				for(int j = 0; j < size[0]; j++) {
					double psqr = (2 - 2 * Math.cos((2 * Math.PI * j) / size[0]))/norm;
					if( j != 0 ) {
						charge[2*j] = charge[2*j]/psqr;
						charge[2*j + 1] = charge[2*j + 1]/psqr;
						current[2*j] = current[2*j]/psqr;
						current[2*j + 1] = current[2*j + 1]/psqr;
					}
				}
				charge[0] = 0.0;
				charge[1] = 0.0;
				current[0] = 0.0;
				current[1] = 0.0;
				//perform inverse Fourier transform
				fft.complexInverse(charge, true);
				fft.complexInverse(current, true);
				//compute the values of the gauge field in the direction of the current and the values of the electric field
				for (int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;

					phiList[j].set(i, charge[2 * j]);
					gaugeList[j].set(i, current[2*j]);
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			AlgebraElement Aspatial, Atemporal;
			Aspatial = factory.algebraZero(colors);
			Atemporal = factory.algebraZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int z = 0; z < dirMax; z++) {
					gaugePos[dir] = z;
					cellIndex = g.getCellIndex(gaugePos);

					Atemporal.set(g.getU0(cellIndex).getAlgebraElement());
					Atemporal.addAssign(phiList[j].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));

					Aspatial.set(g.getU(cellIndex, dir).getAlgebraElement());
					Aspatial.addAssign(gaugeList[j].mult(shapeGauss(positionCurrent[dir], width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));

					g.setU(cellIndex, dir, Aspatial.getLink());
					g.setU0(cellIndex, Atemporal.getLink());

					Atemporal.set(g.getU0next(cellIndex).getAlgebraElement());
					Atemporal.addAssign(phiList[j].mult(shapeGauss(location[surfaceIndex][dir] + orientation[surfaceIndex] * at, width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));

					Aspatial.set(g.getUnext(cellIndex, dir).getAlgebraElement());
					Aspatial.addAssign(gaugeList[j].mult(shapeGauss(positionCurrent[dir] + orientation[surfaceIndex]*at, width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));

					g.setUnext(cellIndex, dir, Aspatial.getLink());
					g.setU0next(cellIndex, Atemporal.getLink());
				}
			}

		} else {
			System.out.println("LorentzGaugeLightConeGaussPoissonSolver: Poisson solver not applicable!");
		}

	}

	private double shapeGauss(double mean, double width, double x) {
		Gaussian gauss = new Gaussian(mean, width);
		//Gaussian gauss = new Gaussian(1.0/(width*Math.sqrt(2*Math.PI)), mean, width);
		//double value = Math.exp(-Math.pow(x - mean, 2)/(2*width*width))/(width*Math.sqrt(2*Math.PI));
		return gauss.value(x);
	}

	private double shapeErf(double mean, int index, double x) {
		double arg = orientation[index]*(mean - x)/(width[index]*Math.sqrt(2));
		double value = 0.5 + 0.5*erf.erf(arg);
		return value;
	}

}
