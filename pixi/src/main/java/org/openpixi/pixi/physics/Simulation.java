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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.grid.LocalInterpolation;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.SimpleParticleBoundaries;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

	/**
	 * Timestep
	 */
	public double tstep;
	/**
	 * Width of simulated area
	 */
	private double width;
	/**
	 * Height of simulated area
	 */
	private double height;
	private double speedOfLight;
	/**
	 * Number of iterations in the non-interactive simulation.
	 */
	private int iterations;
	/**
	 * Total number of steps simulated so far.
	 */
	public int tottime;
	/**
	 * Total number of steps between spectral measurements.
	 */
	public int specstep;
	/**
	 * File path to output files.
	 */
	private String filePath;
	/**
	 * Contains all Particle2D objects
	 */
	public ArrayList<Particle> particles;
	public CombinedForce f;
	private ParticleMover mover;
	/**
	 * Grid for dynamic field calculation
	 */
	public Grid grid;
	public Detector detector;
	public CollisionAlgorithm collisionalgorithm;
	/**
	 * We can turn on or off the effect of the grid on particles by adding or
	 * removing this force from the total force.
	 */
	private SimpleGridForce gridForce = new SimpleGridForce();
	private boolean usingGridForce = false;
	public boolean relativistic = false;
	private ParticleGridInitializer particleGridInitializer = new ParticleGridInitializer();
	private Interpolation interpolation;
	/**
	 * solver for the electrostatic poisson equation
	 */
	private PoissonSolver poisolver;

	public Interpolation getInterpolation() {
		return interpolation;
	}
	
	public int getIterations() {
		return iterations;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getSpeedOfLight() {
		return speedOfLight;
	}

	public ParticleMover getParticleMover() {
		return mover;
	}

	/**
	 * Constructor for non distributed simulation.
	 */
	public Simulation(Settings settings) {
		tstep = settings.getTimeStep();
		width = settings.getSimulationWidth();
		height = settings.getSimulationHeight();
		speedOfLight = settings.getSpeedOfLight();
		iterations = settings.getIterations();
		tottime = 0;
		specstep = settings.getSpectrumStep();
		filePath = settings.getFilePath();
		relativistic = settings.getRelativistic();

		// TODO make particles a generic list
		particles = (ArrayList<Particle>) settings.getParticles();
		f = settings.getForce();

		ParticleBoundaries particleBoundaries = new SimpleParticleBoundaries(
				new DoubleBox(0, width, 0, height),
				settings.getParticleBoundary());
		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaries,
				settings.getParticleIterator());

		grid = new Grid(settings);
		if (settings.useGrid()) {
			turnGridForceOn();
		} else {
			turnGridForceOff();
		}

		poisolver = settings.getPoissonSolver();
		interpolation = new LocalInterpolation(
				settings.getInterpolator(), settings.getParticleIterator());
		particleGridInitializer.initialize(interpolation, poisolver, particles, grid);

		detector = settings.getCollisionDetector();
		collisionalgorithm = settings.getCollisionAlgorithm();

		prepareAllParticles();
		
		clearFile();
	}

	/**
	 * Constructor for distributed simulation. Expects settings specific to the
	 * local node => the simulation width and height as well as the number of
	 * cells in y and x direction must pertain to local simulation not to the
	 * global simulation. (No need to set poison solver and run
	 * ParticleGridInitializer as it was already run on the master node).
	 */
	public Simulation(Settings settings,
			Grid grid,
			List<Particle> particles,
			ParticleBoundaries particleBoundaries,
			Interpolation interpolation) {

		this.tstep = settings.getTimeStep();
		this.width = settings.getSimulationWidth();
		this.height = settings.getSimulationHeight();
		this.speedOfLight = settings.getSpeedOfLight();
		this.iterations = settings.getIterations();
		this.tottime = 0;
		this.specstep = settings.getSpectrumStep();
		this.filePath = settings.getFilePath();
		this.relativistic = settings.getRelativistic();

		this.particles = (ArrayList<Particle>) particles;
		f = settings.getForce();

		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaries,
				settings.getParticleIterator());

		this.grid = grid;
		if (settings.useGrid()) {
			turnGridForceOn();
		} else {
			turnGridForceOff();
		}

		this.interpolation = interpolation;

		detector = settings.getCollisionDetector();
		collisionalgorithm = settings.getCollisionAlgorithm();

		prepareAllParticles();
		
		clearFile();
	}

	public void turnGridForceOn() {
		if (!usingGridForce) {
			f.add(gridForce);
			usingGridForce = true;
		}
        if(!f.forces.contains(gridForce)){
            f.add(gridForce);
        }
	}

	public void turnGridForceOff() {
		if (usingGridForce) {
			f.remove(gridForce);
			usingGridForce = false;
		}
	}

	/**
	 * Runs the simulation in steps. (for interactive simulations)
	 */
	public void step() throws FileNotFoundException,IOException {

		if (continues()) {
			// Only write to file while simulation continues.
			writeToFile(tstep*tottime);
			if( (tottime % specstep) == 0) writeSpecFile(tottime);
		}
		particlePush();
		detector.run();
		collisionalgorithm.collide(detector.getOverlappedPairs(), f, mover.getSolver(), tstep);
		interpolation.interpolateToGrid(particles, grid, tstep);
		grid.updateGrid(tstep);
		interpolation.interpolateToParticle(particles, grid);

		tottime++;
	}

	/**
	 * Whether the simulation should continue.
	 * @return
	 */
	public boolean continues() {
		return tottime <= iterations;
	}

	/**
	 * Runs the entire simulation at once. (for non-interactive simulations)
	 */
	public void run() throws FileNotFoundException,IOException {
		while (continues()) {
			step();
		}
	}
	/**
	 * Checks if the files are already existent and deletes them.
	 */
	public void clearFile() {
		File particlesfile = new File("output/particles_seq.txt");
		boolean fileExists1 = particlesfile.exists();
		if(fileExists1 == true) {
			particlesfile.delete();
		}
		
		File gridfile = new File("output/cells_seq.txt");
		boolean fileExists2 = gridfile.exists();
		if(fileExists2 == true) {
			gridfile.delete();
		}
	}

	/**
	 * Get output file in correct subdirectory.
	 * Create subdirectories if necessary.
	 * @param filename
	 * @return file
	 */
	public File getOutputFile(String filename) {
		// Default output path is
		// 'output/' + filePath + '/' + filename
		File fullpath = new File("output");
		if(!fullpath.exists()) fullpath.mkdir();

		fullpath = new File(fullpath, filePath);
		if(!fullpath.exists()) fullpath.mkdir();

		return new File(fullpath, filename);
	}

	/**
	 * Write the results to a txt file
	 */
	public void writeToFile(double time) throws IOException {
		//PrintWriter pw = new PrintWriter(new File("particles_seq.txt"));
		
		File file = getOutputFile("particles_seq.txt");
		FileWriter pw = new FileWriter(file, true);
		double kinetic = 0;
		double kineticTotal = 0;
		
		if(time == 0) {
			pw.write("#time \t x \t y \t vx \t vy \t kinetic \t Ex \t Ey \t Bz");
			pw.write("\n");
		} else {}
		
		pw.write(time + "\t");
		
		for (int i = 0; i < particles.size(); i++) {
			pw.write(particles.get(i).getX() + "\t");
			pw.write(particles.get(i).getY() + "\t");
			//pw.write(particles.get(i).getRadius() + "\n");
			pw.write(particles.get(i).getVx() + "\t");
			pw.write(particles.get(i).getVy() + "\t");
			if(relativistic == false) {kinetic = particles.get(i).getMass()*(particles.get(i).getVx() * particles.get(i).getVx() + particles.get(i).getVy()*particles.get(i).getVy())/2;}
			else {kinetic = Math.sqrt(particles.get(i).getMass()*particles.get(i).getMass()*( particles.get(i).getVx() * particles.get(i).getVx() + particles.get(i).getVy()*particles.get(i).getVy() + 1) ); }
			pw.write(kinetic + "\t");
			kineticTotal += kinetic;
			pw.write(particles.get(i).getEx() + "\t");
			pw.write(particles.get(i).getEy() + "\t");
			pw.write(particles.get(i).getBz() + "\t");
			/*pw.write(particles.get(i).getAx() + "\n");
			pw.write(particles.get(i).getAy() + "\n");
			pw.write(particles.get(i).getMass() + "\n");
			pw.write(particles.get(i).getCharge() + "\n");
			pw.write(particles.get(i).getPrevX() + "\n");
			pw.write(particles.get(i).getPrevY() + "\n");
			pw.write(particles.get(i).getPrevPositionComponentForceX() + "\n");
			pw.write(particles.get(i).getPrevPositionComponentForceY() + "\n");
			pw.write(particles.get(i).getPrevTangentVelocityComponentOfForceX() + "\n");
			pw.write(particles.get(i).getPrevTangentVelocityComponentOfForceY() + "\n");
			pw.write(particles.get(i).getPrevNormalVelocityComponentOfForceX() + "\n");
			pw.write(particles.get(i).getPrevNormalVelocityComponentOfForceY() + "\n");
			pw.write(particles.get(i).getPrevBz() + "\n");
			pw.write(particles.get(i).getPrevLinearDragCoefficient() + "\n");*/
		}
		pw.write("\n");
		
		pw.close();

		file = getOutputFile("cells_seq.txt");
		//pw = new PrintWriter(file);
		pw = new FileWriter(file, true);
		
		pw.write(time + "\t");
		
		double SumRho = 0;
		double SumJx = 0;
		double SumJy = 0;
		double fieldEnergy = 0;
		double GaussLaw = 0;
		//int NumPoints = grid.getNumCellsX()*grid.getNumCellsY();
		
		for (int i = 0; i < grid.getNumCellsX(); i++) {
			for (int j = 0; j < grid.getNumCellsY(); j++) {
				
				SumRho += grid.getCells()[i][j].getRho();
				SumJx += grid.getCells()[i][j].getJx();
				SumJy += grid.getCells()[i][j].getJy();
				fieldEnergy += ( grid.getCells()[i][j].getBz()*grid.getCells()[i][j].getBz() + grid.getCells()[i][j].getEx()*grid.getCells()[i][j].getEx() + grid.getCells()[i][j].getEy()*grid.getCells()[i][j].getEy())/2;
				GaussLaw += (grid.getEx((i+1)%grid.getNumCellsX(), j) - grid.getEx(i, j)) / grid.getCellWidth() +
						(grid.getEy(i, (j+1)%grid.getNumCellsY()) - grid.getEy(i, j)) / grid.getCellHeight() - grid.getRho(i,j)*4*Math.PI;
				/*pw.write(grid.getCells()[i][j].getJx() + "\n");
				pw.write(grid.getCells()[i][j].getJy() + "\n");
				pw.write(grid.getCells()[i][j].getRho() + "\n");
				pw.write(grid.getCells()[i][j].getPhi() + "\n");
				pw.write(grid.getCells()[i][j].getEx() + "\n");
				pw.write(grid.getCells()[i][j].getEy() + "\n");
				pw.write(grid.getCells()[i][j].getBz() + "\n");
				pw.write(grid.getCells()[i][j].getBzo() + "\n");*/
			}
		}
		pw.write(kineticTotal + "\t");
		pw.write(fieldEnergy + "\t");
		pw.write(SumRho + "\t");
		pw.write(SumJx + "\t");
		pw.write(SumJy + "\t");
		pw.write(GaussLaw + "\t");
		
		pw.write("\n");
		
		pw.close();
		
	}

	public void writeSpecFile(int time) throws FileNotFoundException {
		File file = getOutputFile("spec" + time + ".txt");
		PrintWriter sw = new PrintWriter(file);
		
		for (int i = 0; i < particles.size(); i++) {
			sw.write(i + "\t");
			sw.write(particles.get(i).getX() + "\t");
			sw.write(particles.get(i).getY() + "\t");
			sw.write(particles.get(i).getVx() + "\t");
			sw.write(particles.get(i).getVy() + "\t");
			sw.write("\n");
		}
		
		sw.close();

		file = getOutputFile("snapshot" + time + ".txt");
		PrintWriter snap = new PrintWriter(file);

		for (int i = 0; i < grid.getNumCellsXTotal(); i++) {
			for (int j = 0; j < grid.getNumCellsYTotal(); j++) {
				
				snap.write(i + "\t");
				snap.write(j + "\t");
				snap.write(grid.getCells()[i][j].getJx() + "\t");
				//snap.write(grid.getCells()[i][j].getPhi() + "\t");
				snap.write("\n");
				
			}
		}
		
		snap.close();
		
	}
	
	public void particlePush() {
		mover.push(particles, f, tstep);
	}

	public void prepareAllParticles() {
		mover.prepare(particles, f, tstep);
	}

	public void completeAllParticles() {
		mover.complete(particles, f, tstep);
	}
}
