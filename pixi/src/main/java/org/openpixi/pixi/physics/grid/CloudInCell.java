package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.particles.Particle;

public class CloudInCell implements InterpolatorAlgorithm {

	@Deprecated
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
		
		if(g.getNumCellsZ() > 1) {
			interpolateChargedensity3D(p, g);
			return;
		}
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
		g.addRho( (i + g.getNumCellsX())%g.getNumCellsX(),		(j + g.getNumCellsY())%g.getNumCellsY(),		p.getCharge() * b * d);
		g.addRho( (i + g.getNumCellsX())%g.getNumCellsX(),		(j + 1 + g.getNumCellsY())%g.getNumCellsY(),	p.getCharge() * b * c);
		g.addRho( (i + 1 + g.getNumCellsX())%g.getNumCellsX(),	(j + 1 + g.getNumCellsY())%g.getNumCellsY(),	p.getCharge() * a * c);
		g.addRho( (i + 1 + g.getNumCellsX())%g.getNumCellsX(),	(j + g.getNumCellsY())%g.getNumCellsY(),		p.getCharge() * a * d);
	}
	
	@Override
	public void interpolateToParticle(Particle p, Grid g) {
		
		if(g.getNumCellsZ() > 1) {
			interpolateToParticle3D(p, g);
			return;
		}

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

                p.setEx((g.getEx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY() ) *
                                ((xCellPosition+1.5) * g.getCellWidth() - p.getX()) *
                                (yCellPosition2 * g.getCellHeight() - p.getY()) +
                                g.getEx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY() ) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                (yCellPosition2 * g.getCellHeight() - p.getY()) +
                                g.getEx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY() ) *
                                ((xCellPosition+1.5) * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 1) * g.getCellHeight()) +
                                g.getEx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY() ) *
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
                
                p.setEy((g.getEy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY() ) *
                                (xCellPosition2 * g.getCellWidth() - p.getX()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getEy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY() ) *
                                (p.getX() - (xCellPosition2 - 1) * g.getCellWidth()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getEy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY() ) *
                                (xCellPosition2 * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight()) +
                                g.getEy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY() ) *
                                (p.getX() - (xCellPosition2 - 1) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));
                /*System.out.println("position:");System.out.println(p.getX());System.out.println(p.getY());
                System.out.println(xCellPosition);System.out.println(yCellPosition);System.out.println(":");System.out.println(g.getEy(xCellPosition, yCellPosition));
                System.out.println(xCellPosition+1);System.out.println(yCellPosition);System.out.println(":");System.out.println(g.getEy(xCellPosition+1, yCellPosition));
                System.out.println(xCellPosition);System.out.println(yCellPosition+1);System.out.println(":");System.out.println(g.getEy(xCellPosition, yCellPosition+1));
                System.out.println(xCellPosition+1);System.out.println(yCellPosition+1);System.out.println(":");System.out.println(g.getEy(xCellPosition+1, yCellPosition+1));
                */
                //adapt the values of x/y CellPosition (2) since the B-field is located in the middle of the grid
                if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
                {
                    xCellPosition--;
                    xCellPosition2--;
                }
                
                p.setBz((g.getBz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY() ) *
                                ((xCellPosition2+0.5) * g.getCellWidth() - p.getX()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getBz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY() ) *
                                (p.getX() - (xCellPosition2 - 0.5) * g.getCellWidth()) *
                                ((yCellPosition2+0.5) * g.getCellHeight() - p.getY()) +
                                g.getBz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY() ) *
                                ((xCellPosition2+0.5) * g.getCellWidth() - p.getX()) *
                                (p.getY() - (yCellPosition2 - 0.5) * g.getCellHeight()) +
                                g.getBz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY() ) *
                                (p.getX() - (xCellPosition2 -0.5) * g.getCellWidth()) *
                                (p.getY() - (yCellPosition2 -0.5) * g.getCellHeight())) /
                                (g.getCellWidth() * g.getCellHeight()));
                //p.setBz(0);
	}

private void interpolateChargedensity3D(Particle p, Grid g) {
		
		/**X index of the grid point that is left from or at the x position of the particle*/
		int i;
		/**Y index of the grid point that is below or at the y position of the particle*/
		int j;
		/**Z index of the grid point that is below or at the z position of the particle*/
		int k;
		/**Normalized distance to the left cell boundary*/
		double a;
		/**Normalized distance to the right cell boundary*/
		double b;
		/**Normalized distance to the lower cell boundary*/
		double c;
		/**Normalized distance to the upper cell boundary*/
		double d;
		/**Normalized distance to the nearer cell boundary*/
		double e;
		/**Normalized distance to the farther cell boundary*/
		double f;

		a = p.getX() /g.getCellWidth();
        i = (int) Math.floor(a);
		a -= i;
		b = 1 - a;

		c = p.getY() / g.getCellHeight();
		j = (int) Math.floor(c);
		c -= j;
		d = 1 - c;
		
		e = p.getZ() / g.getCellDepth();
		k = (int) Math.floor(e);
		e -= k;
		f = 1 - e;

		//Assign a portion of the charge to the eight surrounding points depending on
		//the distance.
		g.addRho( (i + g.getNumCellsX())%g.getNumCellsX(),		(j + g.getNumCellsY())%g.getNumCellsY(),		(k + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * b * d * f);
		g.addRho( (i + g.getNumCellsX())%g.getNumCellsX(),		(j + 1 + g.getNumCellsY())%g.getNumCellsY(),	(k + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * b * c * f);
		g.addRho( (i + 1 + g.getNumCellsX())%g.getNumCellsX(),	(j + 1 + g.getNumCellsY())%g.getNumCellsY(),	(k + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * a * c * f);
		g.addRho( (i + 1 + g.getNumCellsX())%g.getNumCellsX(),	(j + g.getNumCellsY())%g.getNumCellsY(),		(k + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * a * d * f);
		g.addRho( (i + g.getNumCellsX())%g.getNumCellsX(),		(j + g.getNumCellsY())%g.getNumCellsY(),		(k + 1 + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * b * d * e);
		g.addRho( (i + g.getNumCellsX())%g.getNumCellsX(),		(j + 1 + g.getNumCellsY())%g.getNumCellsY(),	(k + 1 + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * b * c * e);
		g.addRho( (i + 1 + g.getNumCellsX())%g.getNumCellsX(),	(j + 1 + g.getNumCellsY())%g.getNumCellsY(),	(k + 1 + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * a * c * e);
		g.addRho( (i + 1 + g.getNumCellsX())%g.getNumCellsX(),	(j + g.getNumCellsY())%g.getNumCellsY(),		(k + 1 + g.getNumCellsZ())%g.getNumCellsZ(),	p.getCharge() * a * d * e);
	}

private void interpolateToParticle3D(Particle p, Grid g) {
	/**Normalized distance to the left cell boundary*/
	double a;
	/**Normalized distance to the right cell boundary*/
	double b;
	/**Normalized distance to the lower cell boundary*/
	double c;
	/**Normalized distance to the upper cell boundary*/
	double d;
	/**Normalized distance to the nearer cell boundary*/
	double e;
	/**Normalized distance to the farther cell boundary*/
	double f;

	int xCellPosition = (int) Math.floor(p.getX() / g.getCellWidth());
	int yCellPosition = (int) Math.floor(p.getY() / g.getCellHeight());
	int zCellPosition = (int) Math.floor(p.getZ() / g.getCellDepth());
            
            //Adaption since the electric field is stored in the edges of the cells
            if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
            {
                xCellPosition--;
            }

    		a = p.getX() /g.getCellWidth();
    		a -= xCellPosition + 0.5;
    		b = 1 - a;

    		c = p.getY() / g.getCellHeight();
    		c -= yCellPosition;
    		d = 1 - c;
    		
    		e = p.getZ() / g.getCellDepth();
    		e -= zCellPosition;
    		f = 1 - e;

            p.setEx(g.getEx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
            				(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * f +
            			
                    g.getEx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                    		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * f +
                        
                    g.getEx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                    		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * f +
                    		 
                    g.getEx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                    		(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * f +
                    		
                    		g.getEx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                    				(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * e +
                    			
                            g.getEx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                            		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * e +
                                
                            g.getEx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                            		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * e +
                            		 
                            g.getEx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                            		(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * e
                            		 
            		);

          //redo the adaption for the x-component
            if(p.getX()/g.getCellWidth() - xCellPosition > 1)
            {
                xCellPosition++;
            }
            
          //Adaption since the electric field is stored in the edges of the cells
            if(p.getY()/g.getCellHeight()-yCellPosition <0.5)
            {
                yCellPosition--;
            }

    		a = p.getX() /g.getCellWidth();
    		a -= xCellPosition;
    		b = 1 - a;

    		c = p.getY() / g.getCellHeight();
    		c -= yCellPosition + 0.5;
    		d = 1 - c;
    		
    		e = p.getZ() / g.getCellDepth();
    		e -= zCellPosition;
    		f = 1 - e;
    		
    		p.setEy(g.getEy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
    				(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * f +
    			
            g.getEy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
            		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * f +
                
            g.getEy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
            		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * f +
            		 
            g.getEy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
            		(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * f +
            		
            		g.getEy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
            				(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * e +
            			
                    g.getEy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                    		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * e +
                        
                    g.getEy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                    		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * e +
                    		 
                    g.getEy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                    		(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * e
                    		 
    		);

            //redo the adaption for the y-component
              if(p.getY()/g.getCellHeight() - yCellPosition > 1)
              {
                  yCellPosition++;
              }
              
            //Adaption since the electric field is stored in the edges of the cells
              if(p.getZ()/g.getCellDepth()-zCellPosition <0.5)
              {
                  zCellPosition--;
              }

      		a = p.getX() /g.getCellWidth();
      		a -= xCellPosition;
      		b = 1 - a;

      		c = p.getY() / g.getCellHeight();
      		c -= yCellPosition;
      		d = 1 - c;
      		
      		e = p.getZ() / g.getCellDepth();
      		e -= zCellPosition + 0.5;
      		f = 1 - e;
      		
      		p.setEz(g.getEz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
      				(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * f +
      			
              g.getEz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * f +
                  
              g.getEz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * f +
              		 
              g.getEz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              		(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * f +
              		
              		g.getEz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              				(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * e +
              			
                      g.getEz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * e +
                          
                      g.getEz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * e +
                      		 
                      g.getEz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                      		(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * e
                      		 
      		);
      		
      	//redo the adaption for the z-component
            if(p.getZ()/g.getCellDepth() - zCellPosition > 1)
            {
                zCellPosition++;
            }
           
            //adapt the values of y/z CellPosition since the Bx-field is located in the middle of the grid
            if(p.getZ()/g.getCellDepth()-zCellPosition<0.5)
            {
                zCellPosition--;
            }
            if(p.getY()/g.getCellHeight()-yCellPosition<0.5)
            {
                yCellPosition--;
            }

      		a = p.getX() /g.getCellWidth();
      		a -= xCellPosition;
      		b = 1 - a;

      		c = p.getY() / g.getCellHeight();
      		c -= yCellPosition + 0.5;
      		d = 1 - c;
      		
      		e = p.getZ() / g.getCellDepth();
      		e -= zCellPosition + 0.5;
      		f = 1 - e;
      		
      		p.setBx(g.getBx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
      				(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * f +
      			
              g.getBx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * f +
                  
              g.getBx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * f +
              		 
              g.getBx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              		(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * f +
              		
              		g.getBx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              				(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * e +
              			
                      g.getBx( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * e +
                          
                      g.getBx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * e +
                      		 
                      g.getBx( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                      		(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * e
                      		 
      		);
      		
      	//redo the adaption for the y/z-components
            if(p.getZ()/g.getCellDepth() - zCellPosition > 1)
            {
                zCellPosition++;
            }
            if(p.getY()/g.getCellHeight() - yCellPosition > 1)
            {
                yCellPosition++;
            }
            
          //adapt the values of x/z CellPosition since the By-field is located in the middle of the grid
            if(p.getZ()/g.getCellDepth()-zCellPosition<0.5)
            {
                zCellPosition--;
            }
            if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
            {
                xCellPosition--;
            }

      		a = p.getX() /g.getCellWidth();
      		a -= xCellPosition + 0.5;
      		b = 1 - a;

      		c = p.getY() / g.getCellHeight();
      		c -= yCellPosition;
      		d = 1 - c;
      		
      		e = p.getZ() / g.getCellDepth();
      		e -= zCellPosition + 0.5;
      		f = 1 - e;
      		
      		p.setBy(g.getBy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
      				(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * f +
      			
              g.getBy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * f +
                  
              g.getBy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * f +
              		 
              g.getBy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              		(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * f +
              		
              		g.getBy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              				(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * e +
              			
                      g.getBy( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * e +
                          
                      g.getBy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * e +
                      		 
                      g.getBy( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                      		(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * e
                      		 
      		);
      		
      	//redo the adaption for the x/z-components
            if(p.getZ()/g.getCellDepth() - zCellPosition > 1)
            {
                zCellPosition++;
            }
            if(p.getX()/g.getCellWidth() - xCellPosition > 1)
            {
                xCellPosition++;
            }
           
            //adapt the values of x/y CellPosition since the Bz-field is located in the middle of the grid
            if(p.getX()/g.getCellWidth()-xCellPosition<0.5)
            {
                xCellPosition--;
            }
            if(p.getY()/g.getCellHeight()-yCellPosition<0.5)
            {
                yCellPosition--;
            }

      		a = p.getX() /g.getCellWidth();
      		a -= xCellPosition + 0.5;
      		b = 1 - a;

      		c = p.getY() / g.getCellHeight();
      		c -= yCellPosition + 0.5;
      		d = 1 - c;
      		
      		e = p.getZ() / g.getCellDepth();
      		e -= zCellPosition;
      		f = 1 - e;
      		
      		p.setBz(g.getBz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
      				(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * f +
      			
              g.getBz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * f +
                  
              g.getBz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
              		 (zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * f +
              		 
              g.getBz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              		(zCellPosition + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * f +
              		
              		g.getBz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
              				(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * d * e +
              			
                      g.getBz( (xCellPosition + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * b * c * e +
                          
                      g.getBz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + 1 + g.getNumCellsY())%g.getNumCellsY(),
                      		 (zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * c * e +
                      		 
                      g.getBz( (xCellPosition + 1 + g.getNumCellsX())%g.getNumCellsX(), (yCellPosition + g.getNumCellsY())%g.getNumCellsY(),
                      		(zCellPosition + 1 + g.getNumCellsZ())%g.getNumCellsZ() ) * a * d * e
                      		 
      		);
	}

}
