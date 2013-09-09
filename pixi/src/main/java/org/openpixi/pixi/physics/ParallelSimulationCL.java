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

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.util.*;
import com.nativelibs4java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import org.bridj.Pointer;
import static org.bridj.Pointer.*;
import static java.lang.Math.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.util.ArrayList;
import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.grid.LocalInterpolation;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.SimpleParticleBoundaries;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.util.DoubleBox;


public class ParallelSimulationCL{
        /**Timestep*/
         public double tstep;
         /**Width of simulated area*/
         private double width;
         /**Height of simulated area*/
         private double  height;
         private double speedOfLight;

         /** Number of iterations in the non-interactive simulation. */
         private int iterations;

         /**Contains all Particle2D objects*/
         public ArrayList<Particle> particles;
         public CombinedForce f;
         private ParticleMover mover;
         
         /**Grid for dynamic field calculation*/
         public Grid grid;
         public Detector detector;
         public CollisionAlgorithm collisionalgorithm;

         /**Particle list to be passed to the kernel */
         private Pointer<Double> inParticles;

         /**Force passed to the kernel*/
         private Pointer<Double> inForce;
         
         /**Grid cells passed to the kernel*/
         private Pointer<Double> inCells;
       
         /**Boundaries passed to the kernel*/
         private Pointer<Integer> inBoundaries;
         
         /**Number of Particle attributes */
         private int P_SIZE = 22;
         
         /**Number of Force attributes*/
         private int F_SIZE = 10;
         
         /**Number of Cell attributes*/
         private int C_SIZE = 8;
         
         /**
         * We can turn on or off the effect of the grid on particles by
         * adding or removing this force from the total force.
         */
         private SimpleGridForce gridForce = new SimpleGridForce();
         private boolean usingGridForce = false;

         private ParticleGridInitializer particleGridInitializer = new ParticleGridInitializer();

         private Interpolation interpolation;

         /**solver for the electrostatic poisson equation*/
         private PoissonSolver poisolver;


         public Interpolation getInterpolation() {
                return interpolation;
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
        public ParallelSimulationCL(Settings settings) {
                tstep = settings.getTimeStep();
                width = settings.getSimulationWidth();
                height = settings.getSimulationHeight();
                speedOfLight = settings.getSpeedOfLight();
                iterations = settings.getIterations();

                // TODO make particles a generic list
                particles = (ArrayList<Particle>)settings.getParticles();
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
                }
                else {
                        turnGridForceOff();
                }

                poisolver = settings.getPoissonSolver();
                interpolation = new LocalInterpolation(
                                settings.getInterpolator(), settings.getParticleIterator());
                particleGridInitializer.initialize(interpolation, poisolver, particles, grid);

                detector = settings.getCollisionDetector();
                collisionalgorithm = settings.getCollisionAlgorithm();

                prepareAllParticles();
        }
        public void turnGridForceOn() {
                     if (!usingGridForce) {
                             f.add(gridForce);
                             usingGridForce = true;
                     }
             }


         public void turnGridForceOff() {
                 if (usingGridForce) {
                         f.remove(gridForce);
                         usingGridForce = false;
                 }
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

         /**
          * Converts arrays of Objects objects into arrays
          * of Doubles so they can be passed to the OpenCL kernel
          */
         public void hostToKernelConversion(int particlesSize, int forceSize, int cellsSize, int boundSize, ByteOrder byteOrder){
             int k = 0;

             inParticles = allocateDoubles(particlesSize).order(byteOrder);
             inForce     = allocateDoubles(forceSize).order(byteOrder);
             inCells     = allocateDoubles(cellsSize).order(byteOrder);
             inBoundaries= allocateInts(boundSize).order(byteOrder);
             
             for(int i = 0; i < particlesSize; i += P_SIZE){
                 inParticles.set(i + 0, particles.get(k).getX());
                 inParticles.set(i + 1, particles.get(k).getY());
                 inParticles.set(i + 2, particles.get(k).getRadius());
                 inParticles.set(i + 3, particles.get(k).getVx());
                 inParticles.set(i + 4, particles.get(k).getVy());
                 inParticles.set(i + 5, particles.get(k).getAx());
                 inParticles.set(i + 6, particles.get(k).getAy());

                 inParticles.set(i + 7, particles.get(k).getMass());
                 inParticles.set(i + 8, particles.get(k).getCharge());
                 inParticles.set(i + 9, particles.get(k).getPrevX());
                 inParticles.set(i + 10, particles.get(k).getPrevY());

                 inParticles.set(i + 11, particles.get(k).getEx());
                 inParticles.set(i + 12, particles.get(k).getEy());
                 inParticles.set(i + 13, particles.get(k).getBz());

                 inParticles.set(i + 14, particles.get(k).getPrevPositionComponentForceX());
                 inParticles.set(i + 15, particles.get(k).getPrevPositionComponentForceY());

                 inParticles.set(i + 16, particles.get(k).getPrevTangentVelocityComponentOfForceX());
                 inParticles.set(i + 17, particles.get(k).getPrevTangentVelocityComponentOfForceY());

                 inParticles.set(i + 18, particles.get(k).getPrevNormalVelocityComponentOfForceX());
                 inParticles.set(i + 19, particles.get(k).getPrevNormalVelocityComponentOfForceY());

                 inParticles.set(i + 20, particles.get(k).getPrevBz());
                 inParticles.set(i + 21, particles.get(k++).getPrevLinearDragCoefficient());

             }
             
             k = 0;
             for(int i = 0; i < forceSize; i += F_SIZE){
                 inForce.set(i + 0, f.getForceX(particles.get(k)));
                 inForce.set(i + 1, f.getForceY(particles.get(k)));
                 inForce.set(i + 2, f.getPositionComponentofForceX(particles.get(k)));
                 inForce.set(i + 3, f.getPositionComponentofForceY(particles.get(k)));
                 inForce.set(i + 4, f.getTangentVelocityComponentOfForceX(particles.get(k)));
                 inForce.set(i + 5, f.getTangentVelocityComponentOfForceY(particles.get(k)));    
                 inForce.set(i + 6, f.getNormalVelocityComponentofForceX(particles.get(k)));
                 inForce.set(i + 7, f.getNormalVelocityComponentofForceX(particles.get(k)));
                 inForce.set(i + 8, f.getBz(particles.get(k)));
                 inForce.set(i + 9, f.getLinearDragCoefficient(particles.get(k++)));
             }
             
             k = 0;
             int numCellsX = grid.getNumCellsXTotal();
             int numCellsY = grid.getNumCellsYTotal();
             int c = 0;
             
             for(int i = 0; i < cellsSize; i++)
                 inCells.set(i, 0.0);
             
             for(int i = 0; i < numCellsX; i++){
                 for(int j = 0; j < numCellsY; j++){
                     inCells.set(c + 0, grid.getCells()[i][j].getJx());
                     inCells.set(c + 1, grid.getCells()[i][j].getJy());
                     inCells.set(c + 2, grid.getCells()[i][j].getRho());
                     inCells.set(c + 3, grid.getCells()[i][j].getPhi());
                     inCells.set(c + 4, grid.getCells()[i][j].getEx());
                     inCells.set(c + 5, grid.getCells()[i][j].getEy());
                     inCells.set(c + 6, grid.getCells()[i][j].getBz());
                     inCells.set(c + 7, grid.getCells()[i][j].getBzo());                     
                     c += C_SIZE;
                 }
             }
             
             k = 0;
             for(int i = 0; i < numCellsX * numCellsY; i++){
                inBoundaries.set(k++, grid.parallelBoundariesArray[i]);
             }
         }
         
         
         /**
          * Runs the entire simulation in one kernel
          */
         public void runParallelSimulation() throws IOException, InterruptedException{
                int particlesSize = particles.size() * P_SIZE;
                int forceSize     = particles.size() * F_SIZE;
                int cellsSize     = grid.getNumCellsXTotal() * grid.getNumCellsYTotal() * C_SIZE;
                int boundSize     = grid.getNumCellsXTotal() * grid.getNumCellsYTotal();
                
                CLContext context = JavaCL.createBestContext();
                CLQueue queue = context.createDefaultQueue();
                ByteOrder byteOrder = context.getByteOrder();
               
                long workGroupSize = context.getDevices()[0].getMaxWorkGroupSize() - 1;
                hostToKernelConversion(particlesSize, forceSize, cellsSize, boundSize, byteOrder);
              
                // Create an OpenCL input buffer :
                CLBuffer<Double> inPar = context.createDoubleBuffer(Usage.InputOutput, inParticles);
                CLBuffer<Double> inFor = context.createDoubleBuffer(Usage.Input, inForce);
                CLBuffer<Double> inCel = context.createDoubleBuffer(Usage.InputOutput, inCells);
                CLBuffer<Integer> inBound = context.createIntBuffer(Usage.InputOutput, inBoundaries);
                
                //call the kernel
                SimulationKernel kernels = new SimulationKernel(context);
                int n = (int) workGroupSize;
                int[] globalSizes = new int[] { n };
                int[] localSizes = new int[] { n };

                CLEvent pushEvt = null;
                CLEvent resetEvt = null;
                CLEvent interpEvt = null;
                CLEvent storeEvt = null;
                CLEvent solveEEvt = null;
                CLEvent solveBEvt = null;
                CLEvent partInterpEvt = null;
             
                int processedParticles = 0;
                for (int i = 0; i < iterations; i++) {
                    while(processedParticles < particles.size()){
                        pushEvt = kernels.particle_push_boris(queue, inPar, tstep, n, particles.size(), processedParticles,
                                                              width, height, globalSizes, localSizes);
                        
//                        pushEvt = kernels.particle_push_boris_damped(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                                     width, height, globalSizes, localSizes);

//                        pushEvt = kernels.particle_push_euler(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                              width, height, globalSizes, localSizes);
                    
//                        pushEvt = kernels.particle_push_euler_richardson(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                                         width, height, globalSizes, localSizes);
                    
//                        pushEvt = kernels.particle_push_semiimplicit_euler(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                                           width, height, globalSizes, localSizes);
                    
//                        pushEvt = kernels.particle_push_leap_frog(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                                  width, height, globalSizes, localSizes);
                        
//                        pushEvt = kernels.particle_push_leap_frog_damped(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                                         width, height, globalSizes, localSizes);
                        
//                        pushEvt = kernels.particle_push_leap_frog_half_step(queue, inPar, tstep, n, particles.size(), processedParticles,
//                                                                            width, height, globalSizes, localSizes);
                        processedParticles += workGroupSize;
                    }

                    processedParticles = 0;
                    

                    
//                    t1 = System.currentTimeMillis();
                    resetEvt = kernels.reset_current(queue, inCel,  n, 
                                                     grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
                                                     globalSizes, localSizes, pushEvt);
                    
                    while(processedParticles < particles.size()){
                        interpEvt = kernels.charge_conserving_CIC(queue, inPar, inCel, inBound, tstep, n, particles.size(), processedParticles, 
                                                                  grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
                                                                  grid.getCellWidth(), grid.getCellHeight(),  globalSizes, localSizes, resetEvt);
//                        interpEvt = kernels.cloud_in_cell(queue, inPar, inCel, inBound, tstep, n, particles.size(), processedParticles, 
//                                                          grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
//                                                          grid.getCellWidth(), grid.getCellHeight(),  globalSizes, localSizes, resetEvt);
                        
                        processedParticles += workGroupSize;
                    }
                    
                    processedParticles = 0;
                    
//                    t1 = System.currentTimeMillis();
                    storeEvt = kernels.store_fields(queue, inCel, n, 
                                                    grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
                                                    globalSizes, localSizes, interpEvt);
                    solveEEvt = kernels.solve_for_e(queue, inCel, inBound, 0, n, grid.getNumCellsX(), grid.getNumCellsY(),
                                                    grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
                                                    grid.getCellWidth(), grid.getCellHeight(), globalSizes, localSizes, storeEvt);

                    solveBEvt = kernels.solve_for_b(queue, inCel, inBound, 0, n, grid.getNumCellsX(), grid.getNumCellsY(),
                                                    grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
                                                    grid.getCellWidth(), grid.getCellHeight(), globalSizes, localSizes, solveEEvt);
                  
                    
//                    t1 = System.currentTimeMillis();
                    while(processedParticles < particles.size()){
                        partInterpEvt = kernels.particle_interpolation(queue, inPar, inCel, tstep, n, particles.size(), processedParticles, 
                                                                       grid.getNumCellsXTotal(), grid.getNumCellsYTotal(),
                                                                       grid.getCellWidth(), grid.getCellHeight(), globalSizes, localSizes, solveBEvt);
                        processedParticles += workGroupSize;
                    }
                    
                    processedParticles = 0;
                }

                //get output
                Pointer<Double> outPar = inPar.read(queue, 0, particlesSize, partInterpEvt);// inPar.read(queue, partInterpEvt);
                
                //print results
                PrintWriter pw = new PrintWriter(new File("pcl.txt"));
                for (int i = 0; i < particlesSize; i+=P_SIZE) {
                    pw.write(outPar.get(i + 0) + "\n");
                    pw.write(outPar.get(i + 1) + "\n");
                    pw.write(outPar.get(i + 2) + "\n");
                    pw.write(outPar.get(i + 3) + "\n");
                    pw.write(outPar.get(i + 4) + "\n");
                    pw.write(outPar.get(i + 5) + "\n");
                    pw.write(outPar.get(i + 6) + "\n");
                    pw.write(outPar.get(i + 7) + "\n");
                    pw.write(outPar.get(i + 8) + "\n");
                    pw.write(outPar.get(i + 9) + "\n");
                    pw.write(outPar.get(i + 10) + "\n");
                    pw.write(outPar.get(i + 11) + "\n");
                    pw.write(outPar.get(i + 12) + "\n");
                    pw.write(outPar.get(i + 13) + "\n");
                    pw.write(outPar.get(i + 14) + "\n");
                    pw.write(outPar.get(i + 15) + "\n");
                    pw.write(outPar.get(i + 16) + "\n");
                    pw.write(outPar.get(i + 17) + "\n");
                    pw.write(outPar.get(i + 18) + "\n");
                    pw.write(outPar.get(i + 19) + "\n");
                    pw.write(outPar.get(i + 20) + "\n");
                    pw.write(outPar.get(i + 21) + "\n");
                }                    
                pw.close();
         }

     }
