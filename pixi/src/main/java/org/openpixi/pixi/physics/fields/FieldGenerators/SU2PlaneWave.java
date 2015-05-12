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
        this.amplitudeSpatialDirection = amplitudeSpatialDirection;
        this.amplitudeColorDirection = amplitudeColorDirection;
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
            int[] cellPosition = getCellPosition(c);
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
            omega = Math.sqrt(omega);

            //Phase of the plane wave at t = - dt/2 (for electric fields)
            double phase0 = Math.sin(omega * (this.timeStep / 2.0) + kx);
            //Phase of the plane wave at t = 0 (for links)
            double phase1 = Math.cos(kx);



            Cell currentCell = g.getCell(cellPosition);

            for(int i = 0; i < this.numberOfDimensions; i++)
            {
                //Setup the gauge links
                SU2Matrix U = (SU2Matrix) amplitudeYMField[i].mult(phase1).getLinkExact();
                currentCell.getU(i).mult(U);

                //Setup the electric fields
                YMField electricFieldAmplitude = new SU2Field();
                electricFieldAmplitude.set(amplitudeYMField[i]);
                electricFieldAmplitude.mult(phase0 * omega);

                currentCell.addE(i, amplitudeYMField[i].mult(phase0 * omega));
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
