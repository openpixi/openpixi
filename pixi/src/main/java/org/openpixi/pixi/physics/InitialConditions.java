/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.force.ConstantForce;
import org.openpixi.pixi.physics.force.SpringForce;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.particles.ParticleFull;
import org.openpixi.pixi.physics.solver.EulerRichardson;
import org.openpixi.pixi.physics.solver.relativistic.BorisRelativistic;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.grid.ChargeConservingCIC;

import java.util.ArrayList;

public class InitialConditions {

	/**Random angle*/
	private static double phi;

	public static Simulation initRandomParticles(int count, double radius) {
		Settings stt = new Settings();

		stt.setTimeStep(1);

		//stt.setSpeedOfLight(3);
		// Use maximum speed available by grid
		stt.setSpeedOfLight(stt.getCellWidth() / stt.getTimeStep());

		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		stt.addForce(new ConstantForce());
		stt.setParticleList(
				createRandomParticles(stt.getSimulationWidth(), stt.getSimulationHeight(),
				stt.getSpeedOfLight() / 3, count, radius));
		stt.setBoundary(GeneralBoundaryType.Hardwall);
		//stt.setParticleSolver(new EulerRichardson());
		stt.setParticleSolver(new BorisRelativistic(stt.getSpeedOfLight()));
		

		Simulation simulation = new Simulation(stt);
		return simulation;
	}

	public static Simulation initGravity(int count, double radius) {
		Settings stt = new Settings();

		stt.setTimeStep(1);
		stt.setSpeedOfLight(3);
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		ConstantForce cf = new ConstantForce();
		cf.gy = -1;
		stt.addForce(cf);
		stt.setParticleList(
				createRandomParticles(stt.getSimulationWidth(), stt.getSimulationHeight(),
				stt.getSpeedOfLight(), count, radius));

		stt.setBoundary(GeneralBoundaryType.Hardwall);
		stt.setParticleSolver(new EulerRichardson());

		Simulation simulation = new Simulation(stt);
		return simulation;
	}

	public static Simulation initElectric(int count, double radius) {
		Settings stt = new Settings();

		stt.setTimeStep(1);
		stt.setSpeedOfLight(3);
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		ConstantForce cf = new ConstantForce();
		cf.ey = -1;
		stt.addForce(cf);
		stt.setParticleList(
				createRandomParticles(stt.getSimulationWidth(), stt.getSimulationHeight(),
				stt.getSpeedOfLight(), count, radius));

		stt.setBoundary(GeneralBoundaryType.Hardwall);
		stt.setParticleSolver(new EulerRichardson());

		Simulation simulation = new Simulation(stt);
		return simulation;
	}

	public static Simulation initMagnetic(int count, double radius) {
		Settings stt = new Settings();

		stt.setTimeStep(1);
		stt.setSpeedOfLight(3);
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		ConstantForce cf = new ConstantForce();
		cf.bz = -1;
		stt.addForce(cf);
		stt.setParticleList(
				createRandomParticles(stt.getSimulationWidth(), stt.getSimulationHeight(),
				stt.getSpeedOfLight(), count, radius));

		stt.setBoundary(GeneralBoundaryType.Hardwall);
		stt.setParticleSolver(new EulerRichardson());

		Simulation simulation = new Simulation(stt);
		return simulation;
	}

	public static Simulation initSpring(int count, double radius) {
		Settings stt = new Settings();

		stt.setTimeStep(1);
		stt.setSpeedOfLight(3);
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		stt.addForce(new ConstantForce());
		stt.addForce(new SpringForce());

		stt.setParticleList(new ArrayList<Particle>());

		stt.setBoundary(GeneralBoundaryType.Periodic);
		stt.setParticleSolver(new EulerRichardson());

		for (int k = 0; k < count; k++) {
			Particle par = new ParticleFull();
			par.setX(stt.getSimulationWidth() * Math.random());
			par.setY(stt.getSimulationHeight() * Math.random());
			par.setRadius(15);
			par.setVx(10 * Math.random());
			par.setVy(0);
			par.setMass(1);
			par.setCharge(.001);
			stt.addParticle(par);
		}

		Simulation simulation = new Simulation(stt);
		return simulation;
	}


	/**Creates particles on random positions with random speeds
	 * @param width		maximal x-coodrinate
	 * @param height	maximal y-coordinate
	 * @param maxspeed	maximal particle speed
	 * @param count		number of particles to be created
	 * @param radius	particle radius
	 * @return ArrayList of Particle2D
	 */
	public static ArrayList<Particle> createRandomParticles(double width, double height, double maxspeed, int count, double radius) {

		ArrayList<Particle> particlelist = new ArrayList<Particle>(count);

		for (int k = 0; k < count; k++) {
			Particle p = new ParticleFull();
			p.setX(width * Math.random());
			p.setY(height * Math.random());
			p.setRadius(radius);
			phi = 2 * Math.PI * Math.random();
			p.setVx(maxspeed * Math.cos(phi));
			p.setVy(maxspeed * Math.sin(phi));
			p.setMass(1);
            //overall charge is 0:
			if (k<count/2) {
				p.setCharge(.01);
			} else {
				p.setCharge(-.01);
			}
			particlelist.add(p);
		}

		return particlelist;
	}
	   
    public static Simulation initPair(double charge, double radius) {
	Settings stt = new Settings();

	stt.setTimeStep(1);
	stt.setSpeedOfLight(1);
	stt.setRelativistic(true);
	/*stt.setSimulationWidth(100);
	stt.setSimulationHeight(100);*/
	stt.setGridStep(10);
	stt.setGridCellsX(10);
	stt.setGridCellsY(10);
            stt.setNumOfParticles(2);

	stt.setBoundary(GeneralBoundaryType.Periodic);
            stt.setGridSolver(new SimpleSolver());

	for (int k = 0; k < 2; k++) {
		Particle par = new ParticleFull();
		par.setX(stt.getSimulationWidth() * 1/9.0*(k+4));
		par.setY(stt.getSimulationHeight() * 1/2);
		par.setRadius(radius);
		par.setVx(0);
		par.setVy(0);
		par.setMass(1);
		par.setCharge(charge*(1-2*k));
		stt.addParticle(par);
	}
            
            stt.setPoissonSolver(new PoissonSolverFFTPeriodic());
            stt.useGrid(true);
            stt.setInterpolator(new ChargeConservingCIC());
            //set to charge conserving CIC; already preset in settings
	Simulation simulation = new Simulation(stt);
	return simulation;
}

}
