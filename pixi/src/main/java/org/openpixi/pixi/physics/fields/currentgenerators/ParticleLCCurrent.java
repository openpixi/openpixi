package org.openpixi.pixi.physics.fields.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

public class ParticleLCCurrent implements ICurrentGenerator {

	private int direction;
	private int orientation;
	private double location;
	private double longitudinalWidth;

	private ArrayList<PointCharge> charges;
	private int[] transversalNumCells;
	private AlgebraElement[] transversalChargeDensity;
	private int totalTransversalCells;

	private int numberOfColors;
	private int numberOfComponents;
	private double as;
	private double at;
	private double g;

	private int[] numCells;

	private ArrayList<Particle> particles;

	NewLCPoissonSolver poissonSolver;

	public ParticleLCCurrent(int direction, int orientation, double location, double longitudinalWidth){
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;

		this.charges = new ArrayList<PointCharge>();
	}

	public void addCharge(double[] location, double[] colorDirection, double magnitude) {
		// This method should be called from the YAML object to add the charges for the current generator.
		this.charges.add(new PointCharge(location, colorDirection, magnitude));
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		// 0) Define some variables.
		numberOfColors = s.getNumberOfColors();
		numberOfComponents = s.grid.getElementFactory().numberOfComponents;
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();

		// 1) Initialize transversal charge density grid using the charges array.
		numCells = s.grid.getNumCells();
		transversalNumCells = GridFunctions.reduceGridPos(numCells, direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		transversalChargeDensity = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
		}

		// Iterate over (point) charges, round them to the nearest grid point and add them to the transversal charge density.
		for (int i = 0; i < charges.size(); i++) {
			PointCharge c = charges.get(i);
			AlgebraElement chargeAmplitude = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
			for (int j = 0; j < numberOfComponents; j++) {
				chargeAmplitude.set(j, c.colorDirection[j] * c.magnitude / Math.pow(as, s.getNumberOfDimensions() - 1));
			}
			transversalChargeDensity[GridFunctions.getCellIndex(GridFunctions.nearestGridPoint(c.location, as), transversalNumCells)].addAssign(chargeAmplitude);
		}
		// 2) Initialize the NewLightConePoissonSolver with the transversal charge density and solve for the fields U and E.
		poissonSolver = new NewLCPoissonSolver(direction, orientation, location, longitudinalWidth,
				transversalChargeDensity, transversalNumCells);
		poissonSolver.initialize(s);
		poissonSolver.solve(s);


		// 3) Interpolate grid charge and current density.
		initializeParticles(s, 1);
		applyCurrent(s);

		// You're done: charge density, current density and the fields are set up correctly.
	}

	public void applyCurrent(Simulation s) {
		evolveCharges(s);
		interpolateChargesAndCurrents(s);
		//computeCurrents(s);

		/*
		int maxDirection = numCells[direction];
		double t = s.totalSimulationTime;

		AlgebraElement[] lastCurrents = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			lastCurrents[i] =s.grid.getElementFactory().algebraZero();
		}
		for (int i = 0; i < maxDirection; i++) {
			double z = i * as - location;


			//double s0 = g * as * shapeFunction(z, t - at, orientation, longitudinalWidth);  // shape at t-dt times g*as
			double s1 = g * as * shapeFunction(z, t, orientation, longitudinalWidth);  // shape at t times g*as
			double s2 = g * as * shapeFunction(z + 0.5 * as, t - at/2, orientation, longitudinalWidth);  // shape at t-dt/2 times g*as
			//double ds = (s1 - s0)/at; // time derivative of the shape function

			for (int j = 0; j < totalTransversalCells; j++) {
				int[] transversalGridPos = GridFunctions.getCellPos(j, transversalNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transversalGridPos, direction, i);
				int cellIndex = s.grid.getCellIndex(gridPos);

				// a) Interpolate transversal charge density to grid charge density with a Gauss profile (at t).
				GroupElement V = poissonSolver.getV(i, j, t);
				s.grid.addRho(cellIndex, transversalChargeDensity[j].act(V).mult(s1));

				// b) Compute gird current density in a charge conserving manner at (t-dt/2).
				// Method: Sampling the analytical result on the grid (not charge conserving)
				GroupElement V2 = poissonSolver.getV(as * (i + 0.5), j, (t - at/2));
				s.grid.addJ(cellIndex, direction, transversalChargeDensity[j].act(V2).mult(s2*orientation));

			}
		}
		*/
	}

	private double shapeFunction(double z, double t, int o, double width) {
		Gaussian gauss = new Gaussian(0.0, width);
		return gauss.value(z - o * t);
	}

	private void initializeParticles(Simulation s, int particlesPerLink) {
		particles = new ArrayList<Particle>();
		// Traverse through charge density and add particles by sampling the charge distribution

		int maxDirection = numCells[direction];
		double t0 = 0.0;

		//double cellVolume = Math.pow(as, s.getNumberOfDimensions());
		double cellVolume = 1.0;
		double prefactor = g * as;
		for (int i = 0; i < maxDirection; i++) {
			double z = i * as - location;
			double shape = shapeFunction(z, t0, orientation, longitudinalWidth);  // shape at t times g*as
			for (int j = 0; j < totalTransversalCells; j++) {
				// Particle charge
				int[] transversalGridPos = GridFunctions.getCellPos(j, transversalNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transversalGridPos, direction, i);
				GroupElement V = poissonSolver.getV(i, j, t0);
				AlgebraElement charge = transversalChargeDensity[j].act(V).mult(shape * cellVolume * prefactor);


				// Particle position
				double[] particlePosition = new double[gridPos.length];
				for (int k = 0; k < gridPos.length; k++) {
					particlePosition[k] = gridPos[k] * as;
				}

				// Particle velocity
				double[] particleVelocity = new double[gridPos.length];
				for (int k = 0; k < gridPos.length; k++) {
					if(k == direction) {
						particleVelocity[k] = 1.0 * orientation;
					} else {
						particleVelocity[k] = 0.0;
					}
				}

				// Create particle instance and add to particle array.
				if(charge.square() > 10E-20 * prefactor) {
					Particle p = new Particle();
					p.pos0 = particlePosition;
					p.pos1 = particlePosition.clone();
					p.vel = particleVelocity;
					p.Q0 = charge;
					p.Q1 = charge.copy();

					particles.add(p);
				}
			}
		}

	}

	private void evolveCharges(Simulation s) {
		for(Particle p : particles) {
			// swap variables for charge and position
			p.swap();
			// move particle position according to velocity
			p.move(at);

			// Evolve particle charges
			// check if one cell or two cell move
			int longitudinalIndex0 = (int) (p.pos0[direction] / as);
			int longitudinalIndex1 = (int) (p.pos1[direction] / as);
			if(longitudinalIndex0 == longitudinalIndex1) {
				// one cell move
				int cellIndex = s.grid.getCellIndex(GridFunctions.flooredGridPoint(p.pos0, as));
				double delta = p.vel[direction] * at / as;
				GroupElement U = s.grid.getU(cellIndex, direction).getAlgebraElement().mult(delta).getLink();
				if(orientation > 0) {
					p.evolve(U);
				} else {
					p.evolve(U.adj());
				}
			} else {
				// two cell move
				int cellIndex0 = s.grid.getCellIndex(GridFunctions.flooredGridPoint(p.pos0, as));
				int cellIndex1 = s.grid.getCellIndex(GridFunctions.flooredGridPoint(p.pos1, as));
				if(longitudinalIndex0 < longitudinalIndex1) {
					// path is split into two parts
					double delta0 = Math.abs(longitudinalIndex1 - p.pos0[direction] / as);	// from x(t) to x_{n+1}
					double delta1 = Math.abs(p.pos1[direction] / as - longitudinalIndex1);	// from x_{n+1} to x(t+dt)

					GroupElement U0 = s.grid.getU(cellIndex0, direction).getAlgebraElement().mult(delta0).getLink();
					GroupElement U1 = s.grid.getU(cellIndex1, direction).getAlgebraElement().mult(delta1).getLink();

					p.evolve(U0.mult(U1));
				} else {
					// path is split into two parts
					double delta0 = Math.abs(p.pos0[direction] / as - longitudinalIndex1);	// from x(t) to x_{n+1}
					double delta1 = Math.abs(longitudinalIndex1 - p.pos1[direction] / as);	// from x_{n+1} to x(t+dt)

					GroupElement U0 = s.grid.getU(cellIndex0, direction).getAlgebraElement().mult(delta0).getLink();
					GroupElement U1 = s.grid.getU(cellIndex1, direction).getAlgebraElement().mult(delta1).getLink();

					p.evolve(U0.mult(U1).adj());
				}
			}
		}
	}

	private void interpolateChargesAndCurrents(Simulation s) {
		// Interpolate particle charges to charge density on the grid
		for(Particle p : particles) {
			// indices of the lattice sites around the particle
			int[] gridPosOld = GridFunctions.flooredGridPoint(p.pos0, as);
			int cellIndex0Old = s.grid.getCellIndex(gridPosOld);
			int cellIndex1Old = s.grid.shift(cellIndex0Old, direction, 1);
			double delta0Old = Math.abs(gridPosOld[direction] - p.pos0[direction] / as);
			double delta1Old = Math.abs(p.pos0[direction] / as - (gridPosOld[direction] + 1));

			GroupElement U0Old = s.grid.getUnext(cellIndex0Old, direction).getAlgebraElement().mult(delta0Old).getLink().adj();
			GroupElement U1Old = s.grid.getUnext(cellIndex0Old, direction).getAlgebraElement().mult(delta1Old).getLink();

			int[] gridPosNew = GridFunctions.flooredGridPoint(p.pos1, as);
			int cellIndex0New = s.grid.getCellIndex(gridPosNew);
			int cellIndex1New = s.grid.shift(cellIndex0New, direction, 1);
			double delta0New = Math.abs(gridPosNew[direction] - p.pos1[direction] / as);
			double delta1New = Math.abs(p.pos1[direction] / as - (gridPosNew[direction]+1));

			GroupElement U0New = s.grid.getU(cellIndex0New, direction).getAlgebraElement().mult(delta0New).getLink().adj();
			GroupElement U1New = s.grid.getU(cellIndex0New, direction).getAlgebraElement().mult(delta1New).getLink();

			// Interpolate charges
			s.grid.addRho(cellIndex0New, p.Q1.act(U0New).mult(delta1New));
			s.grid.addRho(cellIndex1New, p.Q1.act(U1New).mult(delta0New));

			// Compute charge conserving currents
			int longitudinalIndex0 = (int) (p.pos0[direction] / as);
			int longitudinalIndex1 = (int) (p.pos1[direction] / as);
			if(longitudinalIndex0 == longitudinalIndex1) {
				// one cell move
				AlgebraElement rhoNew = p.Q1.act(U0New).mult(delta1New);
				AlgebraElement rhoOld = p.Q0.act(U0Old).mult(delta1Old);

				s.grid.addJ(cellIndex0New, direction, rhoNew.sub(rhoOld).mult(- as / at ));
			} else if(longitudinalIndex0 < longitudinalIndex1) {
				// two cell move to the right (TODO)
				/*
				AlgebraElement rho0New = p.Q1.act(U0New).mult(delta1New);
				AlgebraElement rho0Old = p.Q0.act(U0Old).mult(delta1Old);

				AlgebraElement leftCurrent = rho0Old.mult(as / at);

				GroupElement Up = s.grid.getU(cellIndex0Old, direction).adj();

				AlgebraElement rightCurrent = leftCurrent.act(Up);
				leftCurrent.addAssign();
				*/

			} else if(longitudinalIndex0 > longitudinalIndex1) {
				// two cell move to the left (TODO)

			}

		}
	}

	private void computeCurrents(Simulation s) {
	}

	class Particle {
		public double[] pos0;
		public double[] pos1;
		public double[] vel;

		public AlgebraElement Q0;
		public AlgebraElement Q1;

		public void swap() {
			AlgebraElement tQ = Q0;
			Q0 = Q1;
			Q1 = tQ;

			double[] tPos = pos0;
			pos0 = pos1;
			pos1 = tPos;
		}

		public void move(double dt) {
			for (int i = 0; i < pos0.length; i++) {
				pos1[i] = pos0[i] + vel[i] * dt;
			}
		}

		public void evolve(GroupElement U) {
			Q1 = Q0.act(U);
		}
	}

	class PointCharge {
		public double[] location;
		public double[] colorDirection;
		double magnitude;

		public PointCharge(double[] location, double[] colorDirection, double magnitude) {
			this.location = location;
			this.colorDirection = normalize(colorDirection);
			this.magnitude = magnitude;
		}

		private double[] normalize(double[] v) {
			double norm = 0.0;
			for (int i = 0; i < v.length; i++) {
				norm += v[i] * v[i];
			}
			norm = Math.sqrt(norm);
			double[] result = new double[v.length];
			for (int i = 0; i < v.length; i++) {
				result[i] = v[i] / norm;
			}
			return result;
		}
	}
}
