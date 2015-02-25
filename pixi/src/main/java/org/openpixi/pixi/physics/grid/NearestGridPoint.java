package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.particles.Particle;

public class NearestGridPoint implements InterpolatorAlgorithm {
	
	@Override
	public void interpolateToGrid(Particle p, Grid g, double tstep) {
		
		/**X index of the nearest grid point BEFORE particle push*/
		int xStart = (int) Math.rint( p.getPrevX()/g.getCellWidth() );
		/**Y index of the nearest grid point BEFORE particle push*/
		int yStart = (int) Math.rint( p.getPrevY()/g.getCellHeight() );
		/**Z index of the nearest grid point BEFORE particle push*/
		int zStart = (int) Math.rint( p.getPrevZ()/g.getCellDepth() );
		/**X index of the nearest grid point AFTER particle push*/
		int xEnd = (int) Math.rint( p.getX()/g.getCellWidth() );
		/**Y index of the nearest grid point AFTER particle push*/
		int yEnd = (int) Math.rint( p.getY()/g.getCellHeight() );
		/**Z index of the nearest grid point AFTER particle push*/
		int zEnd = (int) Math.rint( p.getZ()/g.getCellDepth() );
		
		double cellVolume = g.getCellWidth() * g.getCellHeight() * g.getCellDepth();
		
		if( (xEnd % g.getNumCellsX()) != (xStart % g.getNumCellsX()) ) {
			
			g.addJx( Math.min(xStart, xEnd)%g.getNumCellsX(), Math.min(yStart, yEnd)%g.getNumCellsY(), Math.min(zStart, zEnd)%g.getNumCellsZ(), (xEnd - xStart) * p.getCharge() * g.getCellWidth() / cellVolume / tstep);
			
		} else {}
		
		if( (yEnd % g.getNumCellsY()) != (yStart % g.getNumCellsY()) ) {
			
			g.addJy( Math.min(xStart, xEnd)%g.getNumCellsX(), Math.min(yStart, yEnd)%g.getNumCellsY(), Math.min(zStart, zEnd)%g.getNumCellsZ(), (yEnd - yStart) * p.getCharge() * g.getCellHeight() / cellVolume / tstep);
			
		} else {}
		
		if( (zEnd % g.getNumCellsZ()) != (zStart % g.getNumCellsZ()) ) {
			
			g.addJz( Math.min(xStart, xEnd)%g.getNumCellsX(), Math.min(yStart, yEnd)%g.getNumCellsY(), Math.min(zStart, zEnd)%g.getNumCellsZ(), (zEnd - zStart) * p.getCharge() * g.getCellDepth() / cellVolume / tstep);
			
		} else {}
		
	}
	
	@Override
	public void interpolateChargedensity(Particle p, Grid g) {
		
		/**X index of the nearest grid point*/
		int i = (int) Math.rint( p.getX()/g.getCellWidth() );
		/**Y index of the nearest grid point*/
		int j = (int) Math.rint( p.getY()/g.getCellHeight() );
		/**Z index of the nearest grid point*/
		int k = (int) Math.rint( p.getZ()/g.getCellDepth() );
		
		g.addRho( i%g.getNumCellsX(), j%g.getNumCellsY(), k%g.getNumCellsZ(), p.getCharge() );
		
	}
	
	@Override
	public void interpolateToParticle(Particle p, Grid g) {
		
		/**X index of the nearest grid point*/
		int i;
		/**Y index of the nearest grid point*/
		int j;
		/**Z index of the nearest grid point*/
		int k;
		
		i = (int) Math.rint( p.getX()/g.getCellWidth() - 0.5 ) % g.getNumCellsX();
		j = (int) Math.rint( p.getY()/g.getCellHeight() ) % g.getNumCellsY();
		k = (int) Math.rint( p.getZ()/g.getCellDepth() ) % g.getNumCellsZ();
		
		p.setEx( g.getEx(i, j, k) );
		
		i = (int) Math.rint( p.getX()/g.getCellWidth() ) % g.getNumCellsX();
		j = (int) Math.rint( p.getY()/g.getCellHeight() - 0.5 ) % g.getNumCellsY();
		k = (int) Math.rint( p.getZ()/g.getCellDepth() ) % g.getNumCellsZ();
		
		p.setEy( g.getEy(i, j, k) );
		
		i = (int) Math.rint( p.getX()/g.getCellWidth() ) % g.getNumCellsX();
		j = (int) Math.rint( p.getY()/g.getCellHeight() ) % g.getNumCellsY();
		k = (int) Math.rint( p.getZ()/g.getCellDepth() - 0.5 ) % g.getNumCellsZ();
		
		p.setEz( g.getEz(i, j, k) );
		
		i = (int) Math.rint( p.getX()/g.getCellWidth() ) % g.getNumCellsX();
		j = (int) Math.rint( p.getY()/g.getCellHeight() - 0.5 ) % g.getNumCellsY();
		k = (int) Math.rint( p.getZ()/g.getCellDepth() - 0.5 ) % g.getNumCellsZ();
		
		p.setBx( g.getBx(i, j, k) );
		
		i = (int) Math.rint( p.getX()/g.getCellWidth() - 0.5 ) % g.getNumCellsX();
		j = (int) Math.rint( p.getY()/g.getCellHeight() ) % g.getNumCellsY();
		k = (int) Math.rint( p.getZ()/g.getCellDepth() - 0.5 ) % g.getNumCellsZ();
		
		p.setBy( g.getBy(i, j, k) );
		
		i = (int) Math.rint( p.getX()/g.getCellWidth() - 0.5 ) % g.getNumCellsX();
		j = (int) Math.rint( p.getY()/g.getCellHeight() - 0.5 ) % g.getNumCellsY();
		k = (int) Math.rint( p.getZ()/g.getCellDepth() ) % g.getNumCellsZ();
		
		p.setBz( g.getBz(i, j, k) );
		
	}
	
}