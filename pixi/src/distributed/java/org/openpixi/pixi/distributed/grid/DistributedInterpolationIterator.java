package org.openpixi.pixi.distributed.grid;

import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.InterpolationIterator;
import org.openpixi.pixi.physics.grid.InterpolatorAlgorithm;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;
import java.util.List;


/**
 * When iterating through particles takes also into account the arriving particles
 * and the border particles.
 */
public class DistributedInterpolationIterator extends InterpolationIterator {

	private SharedDataManager sharedDataManager;
	/**
	 * Determines the zone of particles where we can safely interpolate to the particles
	 * without any need of cells from neighbors.
	 */
	private DoubleBox zoneOfLocalInfluence;


	public DistributedInterpolationIterator(
			InterpolatorAlgorithm interpolator,
			SharedDataManager sharedDataManager,
			DoubleBox zoneOfLocalInfluence) {

		super(interpolator);

		this.sharedDataManager = sharedDataManager;
		this.zoneOfLocalInfluence = zoneOfLocalInfluence;
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
	public void interpolateToGrid(List<Particle> localParticles, Grid grid, double tstep) {

		sharedDataManager.startExchangeOfParticles();
		grid.resetCurrent();

		// Remove leaving particles
		List<Particle> leavingParticles = sharedDataManager.getLeavingParticles();
		for (Particle leavingParticle: leavingParticles) {
			localParticles.remove(leavingParticle);
		}

		// Interpolate local particles
		for (Particle localParticle: localParticles) {
			interpolator.interpolateToGrid(localParticle, grid, tstep);
		}

		// Interpolate arriving particles
		List<Particle> arrivingParticles = sharedDataManager.getArrivingParticles();
		for (Particle arrivingParticle: arrivingParticles) {
			interpolator.interpolateToGrid(arrivingParticle, grid, tstep);
		}

		// Interpolate ghost particles
		List<Particle> ghostParticles = sharedDataManager.getGhostParticles();
		for (Particle ghostParticle: ghostParticles) {
			interpolator.interpolateToGrid(ghostParticle, grid, tstep);
		}

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

		// TODO do not create a new array list in each time step, just reuse one
		List<Particle> particlesWithOutsideInfluence = new ArrayList<Particle>();
		for (Particle particle: particles) {
			if (zoneOfLocalInfluence.contains(particle.getX(), particle.getY())) {
				interpolator.interpolateToParticle(particle, grid);
			}
			else {
				particlesWithOutsideInfluence.add(particle);
			}
		}

		sharedDataManager.waitForGhostCells();
		for (Particle particle: particlesWithOutsideInfluence) {
			interpolator.interpolateToParticle(particle, grid);
		}

		sharedDataManager.cleanUpCellCommunication();
	}


	@Override
	public void interpolateChargedensity(List<Particle> particles, Grid grid) {
		throw new UnsupportedOperationException(
				"Interpolation of charge density is done only once when the grid is initialized " +
				"before the Poisson solver is called. " +
				"Thus, it is expected to be done by non-distributed interpolation.");
	}


}
