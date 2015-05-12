package org.openpixi.pixi.physics.fields.FieldGenerators;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.*;

public class SU2PlanePulse implements IFieldGenerator {

	private int numberOfDimensions;
	private int numberOfComponents;
	private double[] direction;
	private double[] position;
	private double[] amplitudeSpatialDirection;
	private double[] amplitudeColorDirection;
	private double amplitudeMagnitude;
	private double sigma;

	private Simulation s;
	private Grid g;
	private double timeStep;

	public SU2PlanePulse(double[] direction,
						 double[] position,
						 double[] amplitudeSpatialDirection,
						 double[] amplitudeColorDirection,
						 double amplitudeMagnitude,
						 double sigma)
	{
		this.numberOfDimensions = direction.length;
		this.numberOfComponents = amplitudeColorDirection.length;

		this.direction = direction;
		this.position = position;
		this.amplitudeSpatialDirection = amplitudeSpatialDirection;
		this.amplitudeColorDirection = amplitudeColorDirection;
		this.amplitudeMagnitude = amplitudeMagnitude;
		this.sigma = sigma;
	}

	public void applyFieldConfiguration(Simulation s) {
		this.s = s;
		this.g = s.grid;
		this.timeStep = s.getTimeStep();

		/*
			Setup the field amplitude for the plane pulse.
		 */
		SU2Field[] amplitudeYMField = new SU2Field[this.numberOfDimensions];
		for (int i = 0; i < this.numberOfDimensions; i++) {
			amplitudeYMField[i] = new SU2Field(
					this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[0],
					this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[1],
					this.amplitudeMagnitude * this.amplitudeSpatialDirection[i] * this.amplitudeColorDirection[2]);
		}

		/*
			Calculate number of cells in the grid.
		 */
		int numberOfCells = 1;
		for (int i = 0; i < this.numberOfDimensions; i++) {
			numberOfCells *= g.getNumCells(i);
		}


		/*
			Cycle through each cell and apply the plane pulse configuration to the links and electric fields.
		 */
		for (int c = 0; c < numberOfCells; c++)
		{
			int[] cellPosition = getCellPosition(c);
			double[] currentPosition =  getPosition(cellPosition);

			/*
			 *  Compute the phase at t = 0  and t = -dt/2
			 */
			double scalarProduct = 0.0;
			for(int i = 0; i < this.numberOfDimensions; i++)
			{
				scalarProduct += this.direction[i] * (currentPosition[i] - this.position[i]);
			}

			//Multiplicative factor for the plane pulse at t = - dt/2 (for electric fields)
			double phaseE = scalarProduct + s.getTimeStep()/2.0;
			double factorForE = - Math.exp(-1.0 / (2.0 * sigma * sigma) * Math.pow(phaseE, 2.0)) *
								phaseE / (sigma * sigma);
			//Multiplicative factor for the plane pulse at t = 0 (for links)
			double factorForU = Math.exp(-1.0 / (2.0 * this.sigma * this.sigma) * Math.pow(scalarProduct, 2.0));



			Cell currentCell = g.getCell(cellPosition);

			for(int i = 0; i < this.numberOfDimensions; i++)
			{
				//Setup the gauge links
				SU2Matrix U = (SU2Matrix) amplitudeYMField[i].mult(factorForU).getLinkExact();
				currentCell.getU(i).mult(U);

				//Setup the electric fields
				currentCell.addE(i, amplitudeYMField[i].mult(factorForE));
			}
		}

	}

	private int getCellIndex(int[] pos)
	{

		for(int i = 0; i < this.numberOfDimensions; i++)
		{
			pos[i] = (pos[i] % this.g.getNumCells(i) + this.g.getNumCells(i)) % this.g.getNumCells(i);
		}

		int cellPos = pos[0];

		for(int i = 1; i < pos.length; i++)
		{
			cellPos *= this.g.getNumCells(i);
			cellPos += pos[i];
		}
		return  cellPos;
	}

	private int[] getCellPosition(int cellIndex)
	{
		int[] pos = new int[this.numberOfDimensions];

		for(int i = this.numberOfDimensions-1; i >= 0; i--)
		{
			pos[i] = cellIndex % this.g.getNumCells(i);
			cellIndex -= pos[i];
			cellIndex /= this.g.getNumCells(i);
		}

		return pos;
	}

	private double[] getPosition(int[] cellPosition)
	{
		double[] position =  new double[this.numberOfDimensions];
		for(int i = 0; i < this.numberOfDimensions; i++)
		{
			position[i] =  cellPosition[i] * g.getLatticeSpacing();
		}
		return position;
	}
}
