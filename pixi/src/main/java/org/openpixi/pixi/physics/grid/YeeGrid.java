package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.force.SimpleGridForce;

public class YeeGrid extends Grid {

	/**Creates a 10x10 Grid for a given simulation(area)
	 * and adds a SimpleGridForce to the forces list.
	 * @param s Simulation
	 */
	public YeeGrid(Simulation s) {

		super(s);

		numCellsX = 10;
		numCellsY = 10;
		cellWidth = s.width/numCellsX;
		cellHeight = s.height/numCellsY;

		fsolver = new YeeSolver();
		interp = new ChargeConservingAreaWeighting(this);
		SimpleGridForce force = new SimpleGridForce();
		s.f.add(force);

		createGrid();
	}

	//a method to change the dimensions of the cells, i.e. the width and the height
	@Override
	public void changeDimension(double width, double height, int xbox, int ybox)
	{
		setNumCellsX(xbox);
		setNumCellsY(ybox);

		createGrid();

		setGrid(width, height);
	}


	@Override
	public void updateGrid(ArrayList<Particle> particles) {
		getInterp().interpolateToGrid(particles);
		storeFields();
		getFsolver().step(this);
		getInterp().interpolateToParticle(particles);
	}

}
