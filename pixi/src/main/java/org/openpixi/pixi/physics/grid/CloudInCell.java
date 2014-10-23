package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.particles.Particle;

public class CloudInCell implements InterpolatorAlgorithm {

	@Override
	public void interpolateToGrid(Particle p, Grid g, double tstep) {
		/**X index of the grid point that is left from or at the x position of the particle*/
		int i;
		/**Y index of the grid point that is below or at the y position of the particle*/
		int j;		
		/**Normalized distance to the left cell boundary*/
		double a;
		/**Normalized distance to the right cell boundary*/
		double b;
		/**Normalized distance to the lower cell boundary*/
		double c;
		/**Normalized distance to the upper cell boundary*/
		double d;
		
		a = p.getX() / g.getCellWidth();
		i = (int) Math.floor(a);
		a -= i;
		b = 1 - a;
		
		c = p.getY() / g.getCellHeight();
		j = (int) Math.floor(c);
		c -= j;
		d = 1 - c;
		
		//The Jx-field is located in the middle of the left cell boundary.
		//This means that the Jx-field-grid is shifted upwards by half a cell height.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the vertical plane. All changes are reversed
		//after the calculation.
		if( c < 0.5 ){
			j -= 1;
			c += 0.5;
			d -= 0.5;
			
			g.addJx(i,		j,		p.getCharge() * p.getVx() * b * d);
			g.addJx(i,		j + 1,	p.getCharge() * p.getVx() * b * c);
			g.addJx(i + 1,	j + 1,	p.getCharge() * p.getVx() * a * c);
			g.addJx(i + 1,	j,		p.getCharge() * p.getVx() * a * d);
			
			c -= 0.5;
			d += 0.5;
			j += 1;
		} else {
			c -= 0.5;
			d += 0.5;
			
			g.addJx(i,		j,		p.getCharge() * p.getVx() * b * d);
			g.addJx(i,		j + 1,	p.getCharge() * p.getVx() * b * c);
			g.addJx(i + 1,	j + 1,	p.getCharge() * p.getVx() * a * c);
			g.addJx(i + 1,	j,		p.getCharge() * p.getVx() * a * d);
			
			c += 0.5;
			d -= 0.5;
		}

		//The Jy-field is located in the middle of the lower cell boundary.
		//This means that the Jy-field-grid is shifted to the right by half a cell width.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the horizontal plane.
		if( a < 0.5 ){
			i -= 1;
			a += 0.5;
			b -= 0.5;
			
			g.addJy(i,		j,		p.getCharge() * p.getVy() * b * d);
			g.addJy(i,		j + 1,	p.getCharge() * p.getVy() * b * c);
			g.addJy(i + 1,	j + 1,	p.getCharge() * p.getVy() * a * c);
			g.addJy(i + 1,	j,		p.getCharge() * p.getVy() * a * d);
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.
		} else {
			a -= 0.5;
			b += 0.5;
			
			g.addJy(i,		j,		p.getCharge() * p.getVy() * b * d);
			g.addJy(i,		j + 1,	p.getCharge() * p.getVy() * b * c);
			g.addJy(i + 1,	j + 1,	p.getCharge() * p.getVy() * a * c);
			g.addJy(i + 1,	j,		p.getCharge() * p.getVy() * a * d);
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.			
		}
	}

	@Override
	public void interpolateChargedensity(Particle p, Grid g) {
		/**X index of the grid point that is left from or at the x position of the particle*/
		int i;
		/**Y index of the grid point that is below or at the y position of the particle*/
		int j;		
		/**Normalized distance to the left cell boundary*/
		double a;
		/**Normalized distance to the right cell boundary*/
		double b;
		/**Normalized distance to the lower cell boundary*/
		double c;
		/**Normalized distance to the upper cell boundary*/
		double d;
		//DELETED THE SHIFT
		//The -0.5 is there to shift the grid
		//a = p.getX() / g.getCellWidth() - 0.5;
		a = p.getX() /g.getCellWidth();
                i = (int) Math.floor(a);
		a -= i;
		b = 1 - a;

		//The -0.5 is there to shift the grid
                //c = p.getY() / g.getCellHeight() - 0.5;
		c = p.getY() / g.getCellHeight();
		j = (int) Math.floor(c);
		c -= j;
		d = 1 - c;	

		//Assign a portion of the charge to the four surrounding points depending on
		//the distance.
		g.addRho(i,		j,		p.getCharge() * b * d);
		g.addRho(i,		j + 1,	p.getCharge() * b * c);
		g.addRho(i + 1,	j + 1,	p.getCharge() * a * c);
		g.addRho(i + 1,	j,		p.getCharge() * a * d);
	}
	
	@Override
	public void interpolateToParticle(Particle p, Grid g) {

		int xCellPosition = (int) Math.floor(p.getX() / g.getCellWidth());
		int yCellPosition = (int) Math.floor(p.getY() / g.getCellHeight());

		int xCellPosition2 = xCellPosition + 1;
		int yCellPosition2 = yCellPosition + 1;

		if (Debug.asserts) {
			// Assert conditions for interpolation
			assert xCellPosition2 * g.getCellWidth() > p.getX() : p.getX();
			assert p.getX() > (xCellPosition2 - 1) * g.getCellWidth() : p.getX();
			assert yCellPosition2 * g.getCellHeight() > p.getY() : p.getY();
			assert p.getY() > (yCellPosition2 - 1) * g.getCellHeight() : p.getY();
		}
                
                //Adaption since the electric field is stored in the edges of the cells
                if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
                {
                    xCellPosition--;
                    xCellPosition2--;
                }

                p.setEx((g.getEx(xCellPosition, yCellPosition) *
                                ((xCellPosition+1.5) * g.getCellWidth() - p.getX()) *
                                (yCellPosition2 * g.getCellHeight() - p.getY()) +
                                g.getEx(xCellPosition + 1, yCellPosition) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                (yCellPosition2 * g.getCellHeight() - p.getY()) +
                                g.getEx(xCellPosition, yCellPosition + 1) *
                                ((xCellPosition+1.5) * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
                                g.getEx(xCellPosition + 1, yCellPosition + 1) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 - 1) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));

                if(p.getX()/g.getCellWidth() - xCellPosition >1)
                {
                    xCellPosition++;
                    xCellPosition2++;
                }
                
                //redo the adaption for the x-component
                
                if(p.getY()/g.getCellHeight()-yCellPosition <0.5)
                {
                    yCellPosition--;
                    yCellPosition2--;
                }
                
                p.setEy((g.getEy(xCellPosition, yCellPosition) *
                                (xCellPosition2 * g.getCellWidth() - p.getX()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getEy(xCellPosition + 1, yCellPosition) *
                                (p.getX() - (xCellPosition2 - 1) * g.getCellWidth()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getEy(xCellPosition, yCellPosition + 1) *
                                (xCellPosition2 * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight()) +
                                g.getEy(xCellPosition + 1, yCellPosition + 1) *
                                (p.getX() - (xCellPosition2 - 1) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));

                //adapt the values of x/y CellPosition (2) since the B-field is located in the middle of the grid
                if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
                {
                    xCellPosition--;
                    xCellPosition2--;
                }
                
                p.setBz((g.getBz(xCellPosition, yCellPosition) *
                                ((xCellPosition2+0.5) * g.getCellWidth() - p.getX()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getBz(xCellPosition + 1, yCellPosition) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getBz(xCellPosition, yCellPosition + 1) *
                                ((xCellPosition2+0.5) * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight()) +
                                g.getBz(xCellPosition + 1, yCellPosition + 1) *
                                (p.getX() - (xCellPosition2 -0.5) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 -0.5) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));
                //p.setBz(0);
	}


	//@Override
	public void interpolateToParticle_oldmaster(Particle p, Grid g) {
		/**X index of the grid point that is left from or at the x position of the particle*/
		int i;
		/**Y index of the grid point that is below or at the y position of the particle*/
		int j;		
		/**Normalized distance to the left cell boundary*/
		double a;
		/**Normalized distance to the right cell boundary*/
		double b;
		/**Normalized distance to the lower cell boundary*/
		double c;
		/**Normalized distance to the upper cell boundary*/
		double d;
		
		a = p.getX() / g.getCellWidth();
		i = (int) Math.floor(a);
		a -= i;
		b = 1 - a;
		
		c = p.getY() / g.getCellHeight();
		j = (int) Math.floor(c);
		c -= j;
		d = 1 - c;
		
		//Bz as given by the FDTD field solver is defined half a timestep ahead of particle
		//time. Therefore we have to average over the old Bz (that is half a timestep behind)
		//and the current Bz. The magnetic field is located at the grid points. 
		//No adjustments to the grid are necessary.
		p.setBz((formFactor(
				g.getBzo(i, j), g.getBzo(i, j+1), g.getBzo(i+1, j+1), g.getBzo(i+1, j),
				a, b, c, d) + 
				formFactor(g.getBz(i, j), g.getBz(i, j+1), g.getBz(i+1, j+1), g.getBz(i+1, j),
				a, b, c, d)) / 2);
		
		//The Ex-field is located in the middle of the left cell boundary.
		//This means that the Ex-field-grid is shifted upwards by half a cell height.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the vertical plane. All changes are reversed
		//after the calculation.
		if( c < 0.5 ){
			j -= 1;
			c += 0.5;
			d -= 0.5;
			
			p.setEx(formFactor(
					g.getEx(i, j), g.getEx(i, j+1), g.getEx(i+1, j+1), g.getEx(i+1, j),
					a, b, c, d));
			
			c -= 0.5;
			d += 0.5;
			j += 1;
		} else {
			c -= 0.5;
			d += 0.5;
			
			p.setEx(formFactor(
					g.getEx(i, j), g.getEx(i, j+1), g.getEx(i+1, j+1), g.getEx(i+1, j),
					a, b, c, d));
			
			c += 0.5;
			d -= 0.5;
		}
		
		//The Ey-field is located in the middle of the lower cell boundary.
		//This means that the Ey-field-grid is shifted to the right by half a cell width.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the horizontal plane.
		if( a < 0.5 ){
			i -= 1;
			a += 0.5;
			b -= 0.5;
			
			p.setEy(formFactor(
					g.getEy(i, j), g.getEy(i, j+1), g.getEy(i+1, j+1), g.getEy(i+1, j),
					a, b, c, d));
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.
		} else {
			a -= 0.5;
			b += 0.5;
			
			p.setEy(formFactor(
					g.getEy(i, j), g.getEy(i, j+1), g.getEy(i+1, j+1), g.getEy(i+1, j),
					a, b, c, d));
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.		
		}
	}

	/**Defines how grid values are weighted. Characteristic of interpolation algorithm.
	 * <p>This is linear interpolation.</p>
	 * @param A value at the lower left grid point
	 * @param B value at the upper left grid point
	 * @param C value at the upper right grid point
	 * @param D value at the lower right grid point
	 * @param a normalized distance to the left cell boundary
	 * @param b normalized distance to the right cell boundary
	 * @param c normalized distance to the lower cell boundary
	 * @param d normalized distance to the upper cell boundary
	 * @return Total value at the particle position
	 */
	private double formFactor(double A, double B, double C, double D,
			double a, double b, double c, double d){
		return A*b*d + B*b*c + C*a*c + D*a*d;
	}

}
