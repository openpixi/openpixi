package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.grid.Grid;

public class NumericPolychromTempGaugeLightConeGaussPoissonSolver extends LightConePoissonSolver {

	private static int dir;
	private static double[][] location;
	private static int[] orientation;
	private static double[] width;
	private static Erf erf;
	private static int numberOfInstances;

	public NumericPolychromTempGaugeLightConeGaussPoissonSolver() {	}

	public void saveValues(int totalNumOfInstances, int numInstance, double[] position, int direction, int orientation, double width) {
		if(numInstance == 0) {
			numberOfInstances = totalNumOfInstances;
			this.location = new double[totalNumOfInstances][position.length];
			this.orientation = new int[totalNumOfInstances];
			this.width = new double[totalNumOfInstances];
			this.dir = direction;
		}

		if( direction != this.dir ) {
			System.out.println("NumericPolychromTempGaugeLightConeGaussPoissonSolver: Polychromatic Gaussian currents with DIFFERENT DIRECTIONS are not yet supported!.");
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
				polySolver(i, g);
			} else {}
		}
	}

	public void polySolver(int surfaceIndex, Grid g) {

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

		double gaussNormFactorCharge = shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], pos[dir] * as) * coupling * as;

		if (size.length > 1) {
			double[][] charge = new double[size[0]][2 * size[1]];
			double[][] current = new double[size[0]][2 * size[1]];
			AlgebraElement[][] phiList = new AlgebraElement[size[0]][size[1]];
			AlgebraElement[][] gaugeList = new AlgebraElement[size[0]][size[1]];
			AlgebraElement[][] E0List = new AlgebraElement[size[0]][size[1]];
			AlgebraElement[][] E1List = new AlgebraElement[size[0]][size[1]];
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					phiList[j][w] = factory.algebraZero(colors);
					gaugeList[j][w] = factory.algebraZero(colors);
					E0List[j][w] = factory.algebraZero(colors);
					E1List[j][w] = factory.algebraZero(colors);
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
						E0List[j][w].set(i, -(charge[(j + 1) % size[0]][2 * w] - charge[j][2*w]) / as);
						E1List[j][w].set(i, -(charge[j][ 2 * ((w + 1) % size[1]) ] - charge[j][2*w]) / as);
					}
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			AlgebraElement A0, A1;
			A0 = factory.algebraZero(colors);
			A1 = factory.algebraZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int w = 0; w < size[1]; w++) {
					gaugePos[signature[1]] = w;
					for (int z = 0; z < dirMax; z++) {
						gaugePos[dir] = z;
						cellIndex = g.getCellIndex(gaugePos);

						g.addE(cellIndex, signature[0], E0List[j][w].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));
						g.addE(cellIndex, signature[1], E1List[j][w].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));

						A0.set(g.getU(cellIndex, signature[0]).getAlgebraElement());
						A1.set(g.getU(cellIndex, signature[1]).getAlgebraElement());
						A0.addAssign(E0List[j][w].mult(shapeGauss(positionCurrent[dir], width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));
						A1.addAssign(E1List[j][w].mult(shapeGauss(positionCurrent[dir], width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));

						g.setU(cellIndex, signature[0], A0.getLink());
						g.setU(cellIndex, signature[1], A1.getLink());
					}
				}
			}
			//compute the transformation matrices and store them in Unext
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int w = 0; w < size[1]; w++) {
					gaugePos[signature[1]] = w;
					for (int z = 0; z < dirMax; z++) {
						gaugePos[dir] = z;
						cellIndex = g.getCellIndex(gaugePos);
						//computing transformation matrices
						g.setUnext(cellIndex, 0, phiList[j][w].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * at).getLink());
						g.setUnext(cellIndex, 0, g.getUnext(cellIndex, 0).adj());
					}
				}
			}

			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			GroupElement U0, U1, U2;
			U0 = factory.groupZero(colors);
			U1 = factory.groupZero(colors);
			U2 = factory.groupZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int w = 0; w < size[1]; w++) {
					gaugePos[signature[1]] = w;
					for (int z = 0; z < dirMax; z++) {
						gaugePos[dir] = z;
						cellIndex = g.getCellIndex(gaugePos);

						//transforming the gauge links
						U0.set( g.getUnext(cellIndex, 0).mult( g.getU(g.shift(cellIndex, signature[0], 1), signature[0]).mult(g.getUnext(cellIndex, 0).adj()) ) );
						U1.set( g.getUnext(cellIndex, 0).mult( g.getU(g.shift(cellIndex, signature[1], 1), signature[1]).mult(g.getUnext(cellIndex, 0).adj()) ) );
						U2.set( g.getUnext(cellIndex, 0).mult( g.getU(g.shift(cellIndex, dir, 1), dir).mult(g.getUnext(cellIndex, 0).adj()) ) );
						g.setU(cellIndex, signature[0], U0);
						g.setU(cellIndex, signature[1], U1);
						g.setU(cellIndex, dir, U2);
						//transforming the electric fields
						g.setE( cellIndex, signature[0], g.getE(cellIndex, signature[0]).act(g.getUnext(cellIndex, 0)) );
						g.setE( cellIndex, signature[1], g.getE(cellIndex, signature[1]).act(g.getUnext(cellIndex, 0)) );
						g.setE( cellIndex, dir, g.getE(cellIndex, dir).act(g.getUnext(cellIndex, 0)) );
					}
				}
			}
			//the Unext matrices are being cleared in order to be used for time evolution
			g.resetUnext();

		} else if(size.length == 1) {
			double[] charge = new double[2 * size[0]];
			double[] current = new double[2 * size[0]];
			AlgebraElement[] phiList = new AlgebraElement[size[0]];
			AlgebraElement[] gaugeList = new AlgebraElement[size[0]];
			AlgebraElement[] E0List = new AlgebraElement[size[0]];
			for(int j = 0; j < size[0]; j++) {
				phiList[j] = factory.algebraZero(colors);
				gaugeList[j] = factory.algebraZero(colors);
				E0List[j] = factory.algebraZero(colors);
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
					E0List[j].set(i, -(charge[ 2*((j + 1) % size[0]) ] - charge[2*j]) / as);
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			AlgebraElement A0, A1;
			A0 = factory.algebraZero(colors);
			A1 = factory.algebraZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int z = 0; z < dirMax; z++) {
					gaugePos[dir] = z;
					cellIndex = g.getCellIndex(gaugePos);

					g.addE(cellIndex, signature[0], E0List[j].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * as));

					A0.set(g.getU(cellIndex, signature[0]).getAlgebraElement());
					A0.addAssign(E0List[j].mult(shapeGauss(positionCurrent[dir], width[surfaceIndex], z * as /*- as/2*/) / gaussNormFactorCharge * coupling * as));

					g.setU(cellIndex, signature[0], A0.getLink());
				}
			}
			//compute the transformation matrices and store them in Unext
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int z = 0; z < dirMax; z++) {
					gaugePos[dir] = z;
					cellIndex = g.getCellIndex(gaugePos);

					//computing transformation matrices
					g.setUnext(cellIndex, 0, phiList[j].mult(shapeGauss(location[surfaceIndex][dir], width[surfaceIndex], z * as) / gaussNormFactorCharge * coupling * at).getLink());
					g.setUnext(cellIndex, 0, g.getUnext(cellIndex, 0).adj());
				}
			}

			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			GroupElement U0, U1, U2;
			U0 = factory.groupZero(colors);
			U1 = factory.groupZero(colors);
			U2 = factory.groupZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int z = 0; z < dirMax; z++) {
					gaugePos[dir] = z;
					cellIndex = g.getCellIndex(gaugePos);

					//transforming the gauge links
					U0.set( g.getUnext(cellIndex, 0).mult( g.getU(g.shift(cellIndex, signature[0], 1), signature[0]).mult(g.getUnext(cellIndex, 0).adj()) ) );
					U2.set( g.getUnext(cellIndex, 0).mult( g.getU(g.shift(cellIndex, dir, 1), dir).mult(g.getUnext(cellIndex, 0).adj()) ) );
					g.setU(cellIndex, signature[0], U0);
					g.setU(cellIndex, dir, U2);
					//transforming the electric fields
					g.setE( cellIndex, signature[0], g.getE(cellIndex, signature[0]).act(g.getUnext(cellIndex, 0)) );
					g.setE( cellIndex, dir, g.getE(cellIndex, dir).act(g.getUnext(cellIndex, 0)) );
				}
			}
			//the Unext matrices are being cleared in order to be used for time evolution
			g.resetUnext();

		} else {
			System.out.println("NumericPolychromTempGaugeLightConeGaussPoissonSolver: Poisson solver not applicable!");
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
