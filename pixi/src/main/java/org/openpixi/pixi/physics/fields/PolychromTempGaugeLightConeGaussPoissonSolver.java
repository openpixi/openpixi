package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
import org.openpixi.pixi.physics.grid.Grid;

import java.util.ArrayList;

public class PolychromTempGaugeLightConeGaussPoissonSolver extends LightConePoissonSolver {

	private static int dir;
	private static double[][] location;
	private static int[] orientation;
	private static double[] width;
	private static Erf erf;
	private static int numberOfInstances;

	public PolychromTempGaugeLightConeGaussPoissonSolver() {	}

	public void saveValues(int totalNumOfInstances, int numInstance, double[] position, int direction, int orientation, double width) {
		if(numInstance == 0) {
			numberOfInstances = totalNumOfInstances;
			this.location = new double[totalNumOfInstances][position.length];
			this.orientation = new int[totalNumOfInstances];
			this.width = new double[totalNumOfInstances];
			this.dir = direction;
		}

		if( direction != this.dir ) {
			System.out.println("PolychromTempGaugeLightConeGaussPoissonSolver: Polychromatic Gaussian currents with DIFFERENT DIRECTIONS are not yet supported!.");
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

	public void polySolver(int surfaceIndex,Grid g) {

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
			AlgebraElement[][] phiList = new AlgebraElement[size[0]][size[1]];
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					phiList[j][w] = factory.algebraZero(colors);
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
					}
				}
				//perform Fourier transformation
				DoubleFFT_2D fft = new DoubleFFT_2D(size[0], size[1]);
				fft.complexForward(charge);
				//perform computation in Fourier space
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						double psqr = (4 - 2 * Math.cos((2 * Math.PI * j) / size[0]) - 2 * Math.cos((2 * Math.PI * w) / size[1]))/norm;
						if( (j+w) != 0 ) {
							charge[j][2*w] = charge[j][2*w]/psqr;
							charge[j][2*w + 1] = charge[j][2*w + 1]/psqr;
						}
					}
				}
				charge[0][0] = 0.0;
				charge[0][1] = 0.0;
				//perform inverse Fourier transform
				fft.complexInverse(charge, true);
				//compute the values of the electric potential
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;
						phiList[j][w].set(i, charge[j][2 * w]);
					}
				}
			}
			//set the values of the gauge field perpendicular to the current and save them as Unext link matrices
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

						A0.set(phiList[j][w].mult(-1.0 * g.getGaugeCoupling() * shapeErf(positionCurrent[dir], surfaceIndex, z * as) / gaussNormFactorCharge));
						A1.set(phiList[j][w].mult(-1.0 * g.getGaugeCoupling() * shapeErf(positionCurrent[dir] + orientation[surfaceIndex]*at, surfaceIndex, z * as) / gaussNormFactorCharge));

						g.setUnext(cellIndex, signature[0], A0.getLink());
						g.setUnext(cellIndex, signature[1], A1.getLink());
					}
				}
			}
			//compute the derivatives of the link matrices stored in Unext, determine and set the electric fields from the temporal derivative and set the gauge links (U's)
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			AlgebraElement A0next, A1next;
			A0next = factory.algebraZero(colors);
			A1next = factory.algebraZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int w = 0; w < size[1]; w++) {
					gaugePos[signature[1]] = w;
					for (int z = 0; z < dirMax; z++) {
						gaugePos[dir] = z;
						cellIndex = g.getCellIndex(gaugePos);

						//fields at t=0
						A0.set( g.getUnext(cellIndex, signature[0]).mult(g.getUnext(g.shift(cellIndex, signature[0], 1), signature[0]).adj().sub(g.getUnext(cellIndex, signature[0]).adj())).mult(1.0/as/g.getGaugeCoupling()).getAlgebraElement() );
						A1.set( g.getUnext(cellIndex, signature[0]).mult(g.getUnext(g.shift(cellIndex, signature[1], 1), signature[0]).adj().sub(g.getUnext(cellIndex, signature[0]).adj())).mult(1.0/as/g.getGaugeCoupling()).getAlgebraElement() );
						//fields at t=-at
						A0next.set( g.getUnext(cellIndex, signature[1]).mult(g.getUnext(g.shift(cellIndex, signature[0], 1), signature[1]).adj().sub(g.getUnext(cellIndex, signature[1]).adj())).mult(1.0/as/g.getGaugeCoupling()).getAlgebraElement() );
						A1next.set( g.getUnext(cellIndex, signature[1]).mult(g.getUnext(g.shift(cellIndex, signature[1], 1), signature[1]).adj().sub(g.getUnext(cellIndex, signature[1]).adj())).mult(1.0/as/g.getGaugeCoupling()).getAlgebraElement() );
						//setting the electric fields
						g.addE(cellIndex, signature[0], A0next.sub(A0).mult(-1.0 / at).mult(g.getGaugeCoupling()*as));
						g.addE(cellIndex, signature[1], A1next.sub(A1).mult(-1.0 / at).mult(g.getGaugeCoupling()*as));

						//setting the gauge links
						A0.multAssign(g.getGaugeCoupling()*as);
						A1.multAssign(g.getGaugeCoupling() * as);
						A0.addAssign(g.getU(cellIndex, signature[0]).getAlgebraElement());
						A1.addAssign(g.getU(cellIndex, signature[1]).getAlgebraElement());
						g.setU(cellIndex, signature[0], A0.getLink());
						g.setU(cellIndex, signature[1], A1.getLink());
					}
				}
			}
			//the Unext matrices are being cleared in order to be used for time evolution
			g.resetUnext();

		} else if(size.length == 1) {
			double[] charge = new double[2 * size[0]];
			AlgebraElement[] phiList = new AlgebraElement[size[0]];
			for(int j = 0; j < size[0]; j++) {
				phiList[j] = factory.algebraZero(colors);
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;

					cellIndex = g.getCellIndex(pos);

					charge[2*j] = g.getRho(cellIndex).get(i);
					charge[2*j + 1] = 0.0;
				}
				//perform Fourier transformation
				DoubleFFT_1D fft = new DoubleFFT_1D(size[0]);
				fft.complexForward(charge);
				//perform computation in Fourier space
				for(int j = 0; j < size[0]; j++) {
					double psqr = (2 - 2 * Math.cos((2 * Math.PI * j) / size[0]))/norm;
						if( j != 0 ) {
							charge[2*j] = charge[2*j]/psqr;
							charge[2*j + 1] = charge[2*j + 1]/psqr;
						}
				}
				charge[0] = 0.0;
				charge[1] = 0.0;
				//perform inverse Fourier transform
				fft.complexInverse(charge, true);
				//compute the values of the gauge field in the direction of the current and the values of the electric field
				for (int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;
					phiList[j].set(i, charge[2 * j]);
				}
			}
			//set the values of the gauge field perpendicular to the current and save them as Unext link matrices
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			AlgebraElement A0, A1;
			A0 = factory.algebraZero(colors);
			A1 = factory.algebraZero(colors);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int z = 0; z < dirMax; z++) {
					gaugePos[dir] = z;
					cellIndex = g.getCellIndex(gaugePos);

					A0.set(phiList[j].mult(-1.0 * g.getGaugeCoupling() * shapeErf(positionCurrent[dir], surfaceIndex, z * as) / gaussNormFactorCharge));
					A1.set(phiList[j].mult(-1.0 * g.getGaugeCoupling() * shapeErf(positionCurrent[dir] + orientation[surfaceIndex] * at, surfaceIndex, z * as) / gaussNormFactorCharge));

					g.setUnext(cellIndex, 0, A0.getLink());
					g.setUnext(cellIndex, 1, A1.getLink());
				}
			}
			//compute the derivatives of the link matrices stored in Unext, determine and set the electric fields from the temporal derivative and set the gauge links (U's)
			System.arraycopy(pos, 0, gaugePos, 0, arrayLength);
			for(int j = 0; j < size[0]; j++) {
				gaugePos[signature[0]] = j;
				for (int z = 0; z < dirMax; z++) {
					gaugePos[dir] = z;
					cellIndex = g.getCellIndex(gaugePos);

					//fields at t=0
					A0.set( g.getUnext(cellIndex, 0).mult(g.getUnext(g.shift(cellIndex, signature[0], 1), 0).adj().sub(g.getUnext(cellIndex, 0).adj())).mult(1.0/as/g.getGaugeCoupling()).getAlgebraElement() );
					//fields at t=-at
					A1.set( g.getUnext(cellIndex, 1).mult(g.getUnext(g.shift(cellIndex, signature[0], 1), 1).adj().sub(g.getUnext(cellIndex, 1).adj())).mult(1.0/as/g.getGaugeCoupling()).getAlgebraElement() );
					//setting the electric fields
					g.addE(cellIndex, signature[0], A1.sub(A0).mult(-1.0 / at).mult(g.getGaugeCoupling()*as));

					//setting the gauge links
					A0.multAssign(g.getGaugeCoupling()*as);
					A0.addAssign(g.getU(cellIndex, signature[0]).getAlgebraElement());
					g.setU(cellIndex, signature[0], A0.getLink());
				}
			}
			//the Unext matrices are being cleared in order to be used for time evolution
			g.resetUnext();

		} else {
			System.out.println("PolychromTempGaugeLightConeGaussPoissonSolver: Poisson solver not applicable!");
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
