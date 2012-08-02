package org.openpixi.pixi.aspectj;

import org.openpixi.pixi.distributed.Worker;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.util.IntBox;

public privileged aspect ParticleMovementDebug {

	pointcut particleChecked(Particle p):
		call(* *..ParticleBoundaries.apply(..)) && args(p)
		&& withincode(* *..ParticleMover.push(..)) ;

	pointcut underWorkerStep(Worker w): cflow(call(* *..step(..)) && target(w));


	after(Particle p, Worker w): particleChecked(p) && underWorkerStep(w) {
		IntBox partition = w.communicator.getPartitions()[w.workerID];
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


	after(Particle p): particleChecked(p) && !underWorkerStep(Worker) {
		System.out.println(particleMovementToStr(p));
	}


	private String particleMovementToStr(Particle p) {
		return String.format("ID %d [%.3f, %.3f] -> [%.3f, %.3f]",
				p.id,
				p.getPrevX(), p.getPrevY(),
				p.getX(), p.getY());
	}

}