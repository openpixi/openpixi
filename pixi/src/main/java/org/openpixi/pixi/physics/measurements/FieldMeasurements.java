package org.openpixi.pixi.physics.measurements;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.parallel.cellaccess.CellAction;


public class FieldMeasurements {

	private EFieldSquared Esquared;
	private BFieldSquared Bsquared;
	private GaussLaw GaussConstraint;
	private TotalCharge totalCharge;

	private boolean useRestrictedRegion;
	private boolean[] restrictedRegion;

	/**
	 * Empty constructor for standard usage.
	 */
	public FieldMeasurements() {
		this.useRestrictedRegion = false;

		Esquared = new EFieldSquared();
		Bsquared = new BFieldSquared();
		GaussConstraint = new GaussLaw();
		totalCharge = new TotalCharge();
	}

	/**
	 * Alternative constructor for use with non-periodic boundaries.
	 * @param restrictedRegion
	 */
	public FieldMeasurements(boolean[] restrictedRegion) {
		this.useRestrictedRegion = true;

		Esquared = new EFieldSquared(restrictedRegion);
		Bsquared = new BFieldSquared(restrictedRegion);
		GaussConstraint = new GaussLaw(restrictedRegion);
		totalCharge = new TotalCharge(restrictedRegion);

		this.restrictedRegion = restrictedRegion;
	}

	public double calculateEsquared(Grid grid) {
		Esquared.reset();
		grid.getCellIterator().execute(grid, Esquared);
        return Esquared.getSum(grid);
	}
	
	public double calculateBsquared(Grid grid) {
		Bsquared.reset();
		grid.getCellIterator().execute(grid, Bsquared);
        return Bsquared.getSum(grid);
	}
	
	public double calculateEsquared(Grid grid, int dir) {
		Esquared.reset();
		grid.getCellIterator().execute(grid, Esquared);
        return Esquared.getSum(grid, dir);
	}
	
	public double calculateBsquared(Grid grid, int dir) {
		Bsquared.reset();
		grid.getCellIterator().execute(grid, Bsquared);
        return Bsquared.getSum(grid, dir);
	}
	
	public double calculateGaussConstraint(Grid grid) {
		GaussConstraint.reset();
		grid.getCellIterator().execute(grid, GaussConstraint);
        return GaussConstraint.getSum(grid);
	}

	public double calculateTotalCharge(Grid grid) {
		totalCharge.reset(grid);
		grid.getCellIterator().execute(grid, totalCharge);
		return totalCharge.getSum(grid);
	}

	private class FieldMeasurementAction implements CellAction {
		protected boolean useRestrictedRegion;
		protected boolean[] restrictedRegion;

		public FieldMeasurementAction() {
			this.useRestrictedRegion = false;
		}

		public FieldMeasurementAction(boolean[] restrictedRegion) {
			this.useRestrictedRegion = true;
			this.restrictedRegion = restrictedRegion;
		}

		public void execute(Grid grid, int index) {

		}
	}

	private class EFieldSquared extends FieldMeasurementAction {

		private double[] sum;

		public EFieldSquared() {
			super();
		}

		public EFieldSquared(boolean[] restrictedRegion) {
			super(restrictedRegion);
		}

        public void reset() {
        	sum = new double[3];//TODO Make this method d-dimensional!!
        }
        
        public double getSum(Grid grid) {
			double norm = Math.pow(grid.getLatticeSpacing()*grid.getGaugeCoupling(), 2) * grid.getTotalNumberOfCells();
			return (sum[0]+sum[1]+sum[2]) / norm;
        }
        
        public double getSum(Grid grid, int dir) {
			double norm = Math.pow(grid.getLatticeSpacing()*grid.getGaugeCoupling(), 2) * grid.getTotalNumberOfCells();
        	return sum[dir] / norm;
        }

        public void execute(Grid grid, int index) {
			if(!restrictedRegion[index]) {
				int numDir = grid.getNumberOfDimensions();
				double[] res = new double[numDir];
				for (int i = 0; i < numDir; i++) {
					res[i] += grid.getE(index, i).square();
					//res += grid.getEsquaredFromLinks(coor, i);
				}
				synchronized (this) {
					for (int i = 0; i < numDir; i++) {
						sum[i] += res[i];    // Synchronisierte Summenbildung
					}
				}
			}
		}
	}

	private class BFieldSquared extends FieldMeasurementAction {

		private double[] sum;

		public BFieldSquared() {
			super();
		}

		public BFieldSquared(boolean[] restrictedRegion) {
			super(restrictedRegion);
		}

        public void reset() {
        	sum = new double[3];
        }
        
        public double getSum(Grid grid)
		{
			double norm = Math.pow(grid.getLatticeSpacing()*grid.getGaugeCoupling(), 2) * grid.getTotalNumberOfCells();
			return (sum[0]+sum[1]+sum[2]) / norm;
        }

		public double getSum(Grid grid, int dir) {
			double norm = Math.pow(grid.getLatticeSpacing()*grid.getGaugeCoupling(), 2) * grid.getTotalNumberOfCells();
			return sum[dir] / norm;
		}
        
        public void execute(Grid grid, int index) {
			if(!restrictedRegion[index]) {
				int numDir = grid.getNumberOfDimensions();
				double[] res = new double[numDir];
				for (int i = 0; i < numDir; i++) {
					//res += grid.getB(coor, i).square();
					// Averaging B(-dt/2) and B(dt/2) to approximate B(0).
					res[i] += 0.5 * (grid.getBsquaredFromLinks(index, i, 0) + grid.getBsquaredFromLinks(index, i, 1));
				}

				synchronized (this) {
					for (int i = 0; i < numDir; i++) {
						sum[i] += res[i];    // Synchronisierte Summenbildung
					}
				}
			}
		}
	}
	
	private class GaussLaw extends FieldMeasurementAction {

		private double sum;

		public GaussLaw() {
			super();
		}

		public GaussLaw(boolean[] restrictedRegion) {
			super(restrictedRegion);
		}

        public void reset() {
        	sum = 0.0;
        }
        
        public double getSum(Grid grid) {
			double norm = Math.pow(grid.getLatticeSpacing()*grid.getGaugeCoupling(), 2) * grid.getTotalNumberOfCells();
			return sum / norm;
        }
        
        public void execute(Grid grid, int index) {
			if(!restrictedRegion[index]) {
				double result = grid.getGaussConstraintSquared(index);
				synchronized (this) {
					sum += result;   // Synchronisierte Summenbildung
				}
			}
		}
	}

	private class TotalCharge extends FieldMeasurementAction {

		private AlgebraElement charge;

		public TotalCharge() {
			super();
		}

		public TotalCharge(boolean[] restrictedRegion) {
			super(restrictedRegion);
		}

		public void reset(Grid grid) {
			charge = grid.getElementFactory().algebraZero();
		}

		public double getSum(Grid grid) {
			charge.multAssign(Math.pow(grid.getLatticeSpacing(), grid.getNumberOfDimensions()));
			double latticeUnitsNorm = grid.getGaugeCoupling() * grid.getLatticeSpacing();
			return Math.sqrt(charge.square()) / latticeUnitsNorm;
		}

		public void execute(Grid grid, int index) {
			if(!restrictedRegion[index]) {
				synchronized (this) {
					charge.addAssign(grid.getRho(index));   // Synchronisierte Summenbildung
				}
			}
		}
	}
	
}