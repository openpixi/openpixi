package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.YMField;

public class TempGaugeLightConeGaussPoissonSolver extends LightConePoissonSolver {

	//private DoubleFFTWrapper fft;
	private int dir;
	private double[] position;
	private int orientation;
	private double width;

	public TempGaugeLightConeGaussPoissonSolver(double[] position, int direction, int orientation, double width) {
		this.position = new double[position.length];
		System.arraycopy(position, 0, this.position, 0, position.length);
		this.dir = direction;
		this.orientation = orientation;
		this.width = width;
	}

	public void solve(Grid g) {

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
		//fft = new DoubleFFTWrapper(size);
		int numberOfComponents = g.getNumberOfColors() * g.getNumberOfColors() - 1;
		int cellIndex;
		int chargeIndex;
		int dirIndex, dirMin, dirMax;

		//double norm = Math.pow(g.getLatticeSpacing(), g.getNumberOfDimensions() - 1);
		double norm = Math.pow(g.getLatticeSpacing(), truesize);
		int[] pos = new int[position.length];
		int gaugePos[] = new int[position.length];
		for (int i = 0; i < position.length; i++) {
			pos[i] = (int) Math.rint(position[i]/g.getLatticeSpacing());
		}
		if(orientation > 0) {
			dirMin = 0;
			dirMax = pos[dir]+1;
		} else {
			dirMin = pos[dir]+1;
			dirMax = g.getNumCells(dir);
		}

		if (size.length > 1) {
			double[][] charge = new double[size[0]][2 * size[1]];
			YMField[][] E0List = new YMField[size[0]][size[1]];
			YMField[][] E1List = new YMField[size[0]][size[1]];
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					E0List[j][w] = new SU2Field();
					E1List[j][w] = new SU2Field();
				}
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;

						cellIndex = g.getCellIndex(pos);
						chargeIndex = cellIndex;
						if(orientation < 0) {
							chargeIndex = g.shift(chargeIndex, dir, 1);
						}
						charge[j][2*w] = g.getRho(chargeIndex).get(i);
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
				//compute the values of the gauge field in the direction of the current and the values of the electric field
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;
						E0List[j][w].set(i, -(charge[(j + 1) % size[0]][2 * w] - charge[j][2*w]) / g.getLatticeSpacing());
						E1List[j][w].set(i, -(charge[j][ 2 * ((w + 1) % size[1]) ] - charge[j][2*w]) / g.getLatticeSpacing());
					}
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					pos[signature[0]] = j;
					pos[signature[1]] = w;

					cellIndex = g.getCellIndex(pos);
					chargeIndex = cellIndex;
					if(orientation < 0) {
						chargeIndex = g.shift(chargeIndex, dir, 1);
					}
					g.setE(chargeIndex, signature[0], E0List[j][w]);
					g.setE(chargeIndex, signature[1], E1List[j][w]);

					System.arraycopy(pos, 0, gaugePos, 0, position.length);
					for (int z = dirMin; z < dirMax; z++) {
						gaugePos[dir] = z;
						dirIndex = g.getCellIndex(gaugePos);
						g.setU(dirIndex, signature[0], (E0List[j][w].mult(-1.0)).getLinkExact());
						g.setU(dirIndex, signature[1], (E1List[j][w].mult(-1.0)).getLinkExact());
					}
				}
			}

		} else if(size.length == 1) {
			double[] charge = new double[2 * size[0]];
			YMField[] E0List = new YMField[size[0]];
			for(int j = 0; j < size[0]; j++) {
				E0List[j] = new SU2Field();
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;

					cellIndex = g.getCellIndex(pos);
					chargeIndex = cellIndex;
					if(orientation < 0) {
						chargeIndex = g.shift(chargeIndex, dir, 1);
					}
					charge[2*j] = g.getRho(chargeIndex).get(i);
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
					E0List[j].set(i, -(charge[ 2*((j + 1) % size[0]) ] - charge[2*j]) / g.getLatticeSpacing());
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			for(int j = 0; j < size[0]; j++) {
				pos[signature[0]] = j;

				cellIndex = g.getCellIndex(pos);
				chargeIndex = cellIndex;
				if(orientation < 0) {
					chargeIndex = g.shift(chargeIndex, dir, 1);
				}
				g.setE(chargeIndex, signature[0], E0List[j]);

				System.arraycopy(pos, 0, gaugePos, 0, position.length);
				for (int z = dirMin; z < dirMax; z++) {
					gaugePos[dir] = z;
					dirIndex = g.getCellIndex(gaugePos);
					g.setU(dirIndex, signature[0], (E0List[j].mult(-1.0)).getLinkExact());
				}
			}
		} else {
			System.out.println("TempGaugeLightConeGaussPoissonSolver: Poisson solver not applicable!");
		}


	}

}
