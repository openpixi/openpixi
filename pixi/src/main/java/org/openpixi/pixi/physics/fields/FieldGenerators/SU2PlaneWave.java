package org.openpixi.pixi.physics.fields.FieldGenerators;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.*;

public class SU2PlaneWave implements IFieldGenerator {

    private int numberOfDimensions;
    private int numberOfComponents;
    private double[] k;
    private double[] amplitudeSpatialDirection;
    private double[] amplitudeColorDirection;
    private double amplitudeMagnitude;
    private Simulation s;
    private Grid g;
    private double timeStep;

    public SU2PlaneWave(double[] k, double[] amplitudeSpatialDirection, double[] amplitudeColorDirection, double amplitudeMagnitude)
    {
        this.numberOfDimensions = k.length;
        this.numberOfComponents = amplitudeColorDirection.length;

        this.k = k;

		/*
			Amplitude directions should be normalized.
		 */
        this.amplitudeSpatialDirection = this.normalizeVector(amplitudeSpatialDirection);
        this.amplitudeColorDirection = this.normalizeVector(amplitudeColorDirection);

        this.amplitudeMagnitude = amplitudeMagnitude;
    }

    public void applyFieldConfiguration(Simulation s) {
        this.s = s;
        this.g = s.grid;
        this.timeStep = s.getTimeStep();

		/*
			Setup the field amplitude for the plane wave.
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
			Cycle through each cell and apply the plane wave configuration to the links and electric fields.
		 */
        for (int c = 0; c < numberOfCells; c++)
        {
            int[] cellPosition = g.getCellPos(c);
            double[] position =  getPosition(cellPosition);

			/*
			 *  Compute the phase at t = 0  and t = -dt/2
			 */
            double kx = 0.0;
            double omega = 0.0;
            for(int i = 0; i < this.numberOfDimensions; i++)
            {
                kx += this.k[i] * position[i];
                omega += this.k[i] * this.k[i];
            }
            omega = s.getSpeedOfLight() * Math.sqrt(omega);

            //Factor of the plane wave at t = - dt/2 (for electric fields)
            double factorForE = omega * Math.sin(omega * (this.timeStep / 2.0) + kx);
            //Phase of the plane wave at t = 0 (for links)
            double factorForU = Math.cos(kx);



            Cell currentCell = g.getCell(cellPosition);

            for(int i = 0; i < this.numberOfDimensions; i++)
            {
                //Setup the gauge links
                SU2Matrix U = (SU2Matrix) currentCell.getU(i).mult(amplitudeYMField[i].mult(factorForU).getLinkExact());
				currentCell.setU(i, U);
				//System.out.println(currentCell.getU(i).get(0));

                //Setup the electric fields
                currentCell.addE(i, amplitudeYMField[i].mult(factorForE));
            }
        }

    }

	private double[] normalizeVector(double[] vector)
	{
		double norm = 0.0;
		double[] output = new double[vector.length];
		for(int i = 0; i < vector.length; i++)
		{
			norm += vector[i] * vector[i];
		}
		norm = Math.sqrt(norm);
		for(int i = 0; i < vector.length; i++)
		{
			output[i] = vector[i] / norm;
		}
		return output;
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
