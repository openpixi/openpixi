package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.distributed.Worker;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs the following information:
 * - global position of particle
 * - arriving particles
 * - leaving particles
 * - border particles
 * - ghost particles
 */
public privileged aspect DistributedMovementDebug extends DistributedSimulationDebug {

	//----------------------------------------------------------------------------------------------
	// Log particle movement
	//----------------------------------------------------------------------------------------------

	pointcut particleChecked(Particle p):
		call(* *..ParticleBoundaries.applyOnParticleCenter(..)) && args(p)
		&& withincode(* *..ParticleMover.push(..));


	@AdviceName("logDistributedMovement")
	after(Particle p, Worker w): particleChecked(p) && underWorkerStep(w) {
		IntBox partition = w.partitions[w.workerID];
		double xoffset = partition.xmin() * w.globalSettings.getCellWidth();
		double yoffset = partition.ymin() * w.globalSettings.getCellHeight();
		double xPosGlobal = xoffset + p.getX();
		double yPosGlobal = yoffset + p.getY();

		System.out.println(String.format(
				"Particle %d at node %d moved to global position global [%.3f, %.3f]",
				p.id,
				w.workerID,
				xPosGlobal,
				yPosGlobal));
	}

	//----------------------------------------------------------------------------------------------
	// Log arriving / leaving / ghost / border particles
	//----------------------------------------------------------------------------------------------

	pointcut getArrivingParticles():
			call(* *..SharedDataManager.getArrivingParticles());

	@AdviceName("logArrivingParticles")
	after(Worker w) returning(List<Particle> particles):
			getArrivingParticles() && underWorkerStep(w) {
		logParticleList(w.workerID, "Arriving", particles);
	}


	pointcut getLeavingParticles():
			call(* *..SharedDataManager.getLeavingParticles());

	@AdviceName("logLeavingParticles")
	after(Worker w) returning(List<Particle> particles):
			getLeavingParticles() && underWorkerStep(w) {
		logParticleList(w.workerID, "Leaving", particles);
	}


	pointcut getGhostParticles():
			call(* *..SharedDataManager.getGhostParticles());

	@AdviceName("logGhostParticles")
	after(Worker w) returning(List<Particle> particles):
			getGhostParticles() && underWorkerStep(w) {
		logParticleList(w.workerID, "Ghost", particles);
	}


	pointcut sendBorderParticles(SharedDataManager sdm):
			call(* *..startExchangeOfParticles()) && target(sdm);

	@AdviceName("logBorderParticles")
	after(Worker w, SharedDataManager sdm): sendBorderParticles(sdm) && underWorkerStep(w) {
		List<Particle> allBorderParticles = new ArrayList<Particle>();
		for (SharedData sd: sdm.sharedData.values()) {
			allBorderParticles.addAll(sd.borderParticles);
		}
		logParticleList(w.workerID, "Border", allBorderParticles);
	}


	private void logParticleList(int workerID, String particlesName, List<Particle> particles) {
		if (particles.size() == 0) {
			return;
		}

		System.out.print(String.format("%s particles at node %d are", particlesName, workerID));
		for (Particle p: particles) {
			System.out.print(" " + p.id);
		}
		System.out.println();
	}

}