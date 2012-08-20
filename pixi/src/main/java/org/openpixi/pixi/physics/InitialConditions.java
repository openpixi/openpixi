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
import org.openpixi.pixi.physics.solver.EulerRichardson;
import org.openpixi.pixi.physics.solver.relativistic.BorisRelativistic;

import java.util.ArrayList;

public class InitialConditions {

	/**Random angle*/
	private static double phi;

	public static Simulation initRandomParticles(int count, double radius) {
		Settings stt = new Settings();

		stt.setTimeStep(1);
		stt.setSpeedOfLight(3);
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);

		stt.addForce(new ConstantForce());
		stt.setNumOfParticles(count);
		stt.setParticleRadius(radius);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

		stt.setBoundary(GeneralBoundaryType.Hardwall);
		stt.setParticleSolver(new EulerRichardson());
		stt.setParticleSolver(new BorisRelativistic(
				stt.getCellWidth() / stt.getTimeStep()));

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
		stt.setNumOfParticles(count);
		stt.setParticleRadius(radius);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

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
		stt.setNumOfParticles(count);
		stt.setParticleRadius(radius);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

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
		stt.setNumOfParticles(count);
		stt.setParticleRadius(radius);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

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

		stt.setNumOfParticles(count);
		stt.setParticleRadius(radius);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

		stt.setBoundary(GeneralBoundaryType.Periodic);
		stt.setParticleSolver(new EulerRichardson());

		for (int k = 0; k < count; k++) {
			Particle par = new Particle();
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
			Particle p = new Particle();
			p.setX(width * Math.random());
			p.setY(height * Math.random());
			p.setRadius(radius);
			phi = 2 * Math.PI * Math.random();
			p.setVx(maxspeed * Math.cos(phi));
			p.setVy(maxspeed * Math.sin(phi));
			p.setMass(1);
			if (Math.random() > 0.5) {
				p.setCharge(.1);
			} else {
				p.setCharge(-.1);
			}
			particlelist.add(p);
		}

		return particlelist;
	}

}
