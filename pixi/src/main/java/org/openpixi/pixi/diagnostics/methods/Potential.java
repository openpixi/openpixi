/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openpixi.pixi.diagnostics.methods;

import java.util.ArrayList;

import org.openpixi.pixi.diagnostics.DataOutput;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.Particle;

import edu.emory.mathcs.jtransforms.fft.*;

/**
 * Calculates the electrostatic potential with the Fast Fourier Transform.
 * THIS IS A GLOBAL METHOD! IT NEEDS THE WHOLE GRID TO FUNCTION PROPERLY.
 * Here we assume periodic boundary conditions!
 */
public class Potential implements Diagnostics {
	
	/** Storage */
	double[][] phi;
	
	/** Period of calculation */
	private int calculationPeriod;
	/** The next iteration when this diagnostic should be performed */
	private int nextIteration = 0;
	/** Determines whether there is new data. I.e. whether calculate was called but the
	 * new data not yet extracted with getData.
	 */
	private boolean newData = false;
	
	public Potential(int calculationPeriod) {
		this.calculationPeriod = calculationPeriod;
	}
	
	public void calculate(Grid g, ArrayList<Particle> particles) {
		
		//size of the array to be transformed
		int columns = g.getNumCellsX();
		int rows = g.getNumCellsY();
		double cellArea = g.getCellWidth() * g.getCellHeight();
		//JTransform saves the imaginary part as a second row entry
		//therefore there must be twice as many rows
		double[][] trho = new double[columns][2*rows];
		double[][] tphi = new double[columns][2*rows];

		DoubleFFT_2D fft = new DoubleFFT_2D(columns, rows);
		
		//prepare input for fft
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				trho[i][2*j] = g.getRho(i,j);
				trho[i][2*j+1] = 0;
			}
		}
		
		//perform Fourier transformation
		fft.complexForward(trho);
		
		//Solve Poisson equation in Fourier space
		//We omit the term with i,j=0 where d would become 0. This term only contributes a constant term
		//to the potential and can therefore be chosen arbitrarily.
		for(int i = 1; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				double d = (4 - 2 * Math.cos((2 * Math.PI * i) / columns) - 2 * Math.cos((2 * Math.PI * j) / rows));
				tphi[i][2*j] = (cellArea * trho[i][2*j]) / d;
				tphi[i][2*j+1] = (cellArea * trho[i][2*j+1]) / d;
			}
		}
		//i=0 but j!=0
		for(int j = 1; j < rows; j++) {
			double d = (2 - 2 * Math.cos((2 * Math.PI * j) / rows));
			tphi[0][2*j] = (cellArea * trho[0][2*j]) / d;
			tphi[0][2*j+1] = (cellArea * trho[0][2*j+1]) / d;		
		}
		
		//perform inverse Fourier transform
		fft.complexInverse(tphi, true);
		
		phi = new double[columns][rows];
		
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				phi[i][j] = tphi[i][2*j];
			}
		}
		
		// Bookkeeping
		nextIteration += calculationPeriod;
		newData = true;
	}
	
	public int getNextIteration() {
		return nextIteration;
	}
	
	public boolean checkIfNewData() {
		return newData;
	}
	
	public void getData(DataOutput out) {
		out.potential(phi);
		newData = false;
	}
	
}
