package org.openpixi.pixi.distributed.grid;

import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.parallel.particleaccess.ParticleAction;
import org.openpixi.pixi.parallel.particleaccess.ParticleIterator;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.grid.InterpolatorAlgorithm;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Does the necessary communication before interpolation.
 */
public class DistributedInterpolation extends Interpolation {

	private SharedDataManager sharedDataManager;
	/**
	 * Determines the zone of particles where we can safely interpolate to the particles
	 * without any need of cells from neighbors.
	 */
	private DoubleBox zoneOfLocalInfluence;
	private List<Particle> particlesWithOutsideInfluence =
			Collections.synchronizedList(new ArrayList<Particle>());

	/* These are passed to inner classes as a parameter to theirs methods. */
	private Grid grid;
	private double timeStep;

	private ParticleIterator particleIterator;

	private InterpolateToInsideParticle interpolateToInsideParticle = new InterpolateToInsideParticle();
	private InterpolateToOutsideParticle interpolateToOutsideParticle = new InterpolateToOutsideParticle();
	private InterpolateToGrid interpolateToGrid = new InterpolateToGrid();


	public DistributedInterpolation(
			InterpolatorAlgorithm interpolator,
			SharedDataManager sharedDataManager,
			DoubleBox zoneOfLocalInfluence,
			ParticleIterator particleIterator) {

		super(interpolator);

		this.sharedDataManager = sharedDataManager;
		this.zoneOfLocalInfluence = zoneOfLocalInfluence;
		this.particleIterator = particleIterator;
	}


	/**
	 * When interpolating to grid we need also the arriving particles and border particles
	 * from neighbors.
	 * In order not to wait too much for network communication we interleave the communication
	 * and computation.
	 * While the crossing and border particles are being exchanged,
	 * we interpolate our local particles.
	 */
	@Override
	public void interpolateToGrid(List<Particle> localParticles, Grid grid, double timeStep) {	   	
		
		sharedDataManager.startExchangeOfParticles();		
		grid.resetCurrent();

		// Remove leaving particles
		List<Particle> leavingParticles = sharedDataManager.getLeavingParticles();
		for (Particle leavingParticle: leavingParticles) {
			localParticles.remove(leavingParticle);
		}
		
		this.grid = grid;
		this.timeStep = timeStep;
		
		// Interpolate local particles
		particleIterator.execute(localParticles, interpolateToGrid);

		// Interpolate arriving particles
		List<Particle> arrivingParticles = sharedDataManager.getArrivingParticles();
		particleIterator.execute(arrivingParticles, interpolateToGrid);

		// Interpolate ghost particles
		List<Particle> ghostParticles = sharedDataManager.getGhostParticles();
		particleIterator.execute(ghostParticles, interpolateToGrid);

		// Add arriving particles to the list of local particles
		localParticles.addAll(arrivingParticles);

		sharedDataManager.cleanUpParticleCommunication();
	}


	/**
	 * When interpolating to particles we need also the cells from our neighbors.
	 * Similarly as with interpolateToGrid() we interleave the communication and computation.
	 * We first interpolate to particles which are not influenced by cells from neighbors and
	 * only when all the cells from neighbors arrived we interpolate to the rest of the particles.
	 */
	@Override
	public void interpolateToParticle(List<Particle> particles, Grid grid) {

		// Initiate the exchange of cells
		sharedDataManager.exchangeCells();

		// Here, due to an "if" statement (see the inner class),
		// we can not reuse the local interpolation and we have to do the iteration ourselves.
		this.grid = grid;
		particleIterator.execute(particles, interpolateToInsideParticle);

		sharedDataManager.waitForGhostCells();
		particleIterator.execute(particlesWithOutsideInfluence, interpolateToOutsideParticle);

		sharedDataManager.cleanUpCellCommunication();
		particlesWithOutsideInfluence.clear();
	}


	@Override
	public void interpolateChargedensity(List<Particle> particles, Grid grid) {
		throw new UnsupportedOperationException(
				"Interpolation of charge density is done only once when the grid is initialized " +
				"before the Poisson solver is called. " +
				"Thus, it is expected to be done by non-distributed interpolation.");
	}


	private class InterpolateToInsideParticle implements ParticleAction {
		public void execute(Particle particle) {
			if (zoneOfLocalInfluence.contains(particle.getX(), particle.getY())) {
				interpolator.interpolateToParticle(particle, grid);
			}
			else {
				particlesWithOutsideInfluence.add(particle);
			}
		}
	}


	private class InterpolateToOutsideParticle implements ParticleAction {
		public void execute(Particle particle) {
			interpolator.interpolateToParticle(particle, grid);
		}
	}


	private class InterpolateToGrid implements ParticleAction {
		public void execute(Particle particle) {
			interpolator.interpolateToGrid(particle, grid, timeStep);
		}
	}
}
