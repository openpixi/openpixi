package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.distributed.SharedData;
import org.openpixi.pixi.distributed.SharedDataManager;
import org.openpixi.pixi.distributed.Worker;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.ArrayList;
import java.util.List;

public privileged aspect ParticleMovementDebug {

	pointcut underWorkerStep(Worker w): cflow(call(* *..step(..)) && target(w));

	//----------------------------------------------------------------------------------------------
	// Log particle movement
	//----------------------------------------------------------------------------------------------

	pointcut particleChecked(Particle p):
		call(* *..ParticleBoundaries.apply(..)) && args(p)
		&& withincode(* *..ParticleMover.push(..));


	@AdviceName("logDistributedMovement")
	after(Particle p, Worker w): particleChecked(p) && underWorkerStep(w) {
		IntBox partition = w.partitions[w.workerID];
		double xoffset = partition.xmin() * w.globalSettings.getCellWidth();
		double yoffset = partition.ymin() * w.globalSettings.getCellHeight();
		double xPosGlobal = xoffset + p.getX();
		double yPosGlobal = yoffset + p.getY();

		String msg = String.format(
				"Worker %d %s global [%.3f, %.3f]",
				w.workerID,
				particleMovementToStr(p),
				xPosGlobal,
				yPosGlobal);
		System.out.println(msg);
	}


	@AdviceName("logNonDistributedMovement")
	after(Particle p): particleChecked(p) && !underWorkerStep(Worker) {
		System.out.println("Non-distributed simulation " + particleMovementToStr(p));
	}


	private String particleMovementToStr(Particle p) {
		return String.format("ID %d [%.3f, %.3f] -> [%.3f, %.3f]",
				p.id,
				p.getPrevX(), p.getPrevY(),
				p.getX(), p.getY());
	}

	//----------------------------------------------------------------------------------------------
	// Log arriving / leaving / ghost / border particles
	//----------------------------------------------------------------------------------------------

	pointcut getArrivingParticles():
			call(* *..SharedDataManager.getArrivingParticles());

	@AdviceName("logArrivingParticles")
	after(Worker w) returning(List<Particle> particles):
			getArrivingParticles() && underWorkerStep(w) {
		logParticleList(w.workerID, "arriving", particles);
	}


	pointcut getLeavingParticles():
			call(* *..SharedDataManager.getLeavingParticles());

	@AdviceName("logLeavingParticles")
	after(Worker w) returning(List<Particle> particles):
			getLeavingParticles() && underWorkerStep(w) {
		logParticleList(w.workerID, "leaving", particles);
	}


	pointcut getGhostParticles():
			call(* *..SharedDataManager.getGhostParticles());

	@AdviceName("logGhostParticles")
	after(Worker w) returning(List<Particle> particles):
			getGhostParticles() && underWorkerStep(w) {
		logParticleList(w.workerID, "ghost", particles);
	}


	pointcut sendBorderParticles(SharedDataManager sdm):
			call(* *..startExchangeOfParticles()) && target(sdm);

	@AdviceName("logBorderParticles")
	after(Worker w, SharedDataManager sdm): sendBorderParticles(sdm) && underWorkerStep(w) {
		List<Particle> allBorderParticles = new ArrayList<Particle>();
		for (SharedData sd: sdm.sharedData.values()) {
			allBorderParticles.addAll(sd.borderParticles);
		}
		logParticleList(w.workerID, "border", allBorderParticles);
	}


	private void logParticleList(int workerID, String particlesName, List<Particle> particles) {
		if (particles.size() == 0) {
			return;
		}

		System.out.print("Worker " + workerID + " " + particlesName);
		for (Particle p: particles) {
			System.out.print(" " + p.id);
		}
		System.out.println();
	}

}