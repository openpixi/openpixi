LICENSE
=======

OpenPixi - Open Particle-in-Cell (PIC) simulator.
Copyright (C) 2012-2016  OpenPixi.org

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


INTRODUCTION
============

OpenPixi is an open Colored Particle-In-Cell (CPIC) simulator for the 
simulation of heavy-ion  collisions based on the Color Glass Condensate framework. 

  http://www.openpixi.org


BUILD INSTRUCTIONS
==================

OpenPixi uses Maven for building ( http://maven.apache.org/ ).

To build Pixi:
    cd pixi
    mvn package

To launch Pixi:
    java -jar target/pixi-x.x.jar
    
Note: x.x should be replaced by the appropriate version number, e.g. 1.0.

(In case there is a problem with an OpenGL panel, execute
    scripts/jogl-bug-workaround
    java -jar target/pixi-x.x.jar
)

To launch Pixi with a certain YAML file without GUI:
    java -cp target/pixi-x.x.jar org.openpixi.pixi.ui.MainBatch path/to/input.yaml
    
To launch Pixi in batch mode with a folder full of YAML files:
    java -cp target/pixi-x.x.jar org.openpixi.pixi.ui.MainBatch /path/to/input/

If you need to allocate more memory (e.g. 32gb) for the JVM, add the -Xmx flag.
    java -Xmx32g -cp target/pixi-x.x.jar org.openpixi.pixi.ui.MainBatch /path/to/input/

DEVELOP IN ECLIPSE
==================

To launch Pixi in Eclipse ( http://www.eclipse.org/ ) do the following:

1) Clone the GitHub repository to a local folder with
	cd "path to local folder"
	git clone git://github.com/openpixi/openpixi.git
2) Open Eclipse and go to Help > Install New Software
3) Choose "All Available Sites" from the dropdown menu
4) Search for "m2e", choose the desired result
   ("m2e - Extensions Development Support") and press finish
5) File > Import > Maven > Existing Maven Projects
6) Select the local folder that you have chosen previously


VERSION HISTORY
===============

---
Version 1.0 - June 1, 2016

* OpenPixi is now a Colored Particle-In-Cell simulator.
* Switched from Abelian fields to Yang-Mills (YM) fields based on real-time
  lattice gauge theory.
* Color glass condensate (CGC) simulations possible with non-Abelian currents
  based on colored particles and NGP interpolation.
* CGC initial conditions: McLerran-Venugopalan model, protons and finite
  nuclei (work-in-progress)
* Pure YM initial conditions: plane wave, Gaussian pulses, focused pulses
* Various observables calculateable using diagnostics: energy density, 
  pressure components, occupation numbers (work-in-progress)
* Use YAML files for configuring simulation initial conditions and diagnostics.
* GUI available with various different Panels displaying energy density,
  fields, etc.
* Python script to run a number of simulations on a cluster distributed
  across nodes (see pixi/scripts/vsc batch/).
* Threaded version still available, distributed version not yet implemented.

---
Version 0.5 - May 29, 2013

* Distributed version of OpenPixi which uses IBIS framework for communication
* Framework for thorough testing of the distributed version
* Parallel version of OpenPixi which uses threads
* Support for debugging and profiling through AspectJ
* Introduced Settings class to hold all the simulation settings

---
Version 0.4 - June 28, 2012

* Relativistic versions of particle solvers
* Field interpolator with charge conserving area weighting
* Poisson solver
* Decoupling of display size and simulation size
* Labels for slider values (issue 25)

---
Version 0.3 - April 4, 2012

* Interface organized in tabs: Fields, Settings, Collisions, Cell
* Collision: simple collision, with vectors, with matrices
* Collision detection: all particles, sweep and prune
* Calculate and display fields
* Field force on particles (work in progress)

---
Version 0.2 - March 14, 2012

* Additional solvers: Boris, BorisDamped, Euler, LeapFrog, LeapFrogDamped,
  SemiImplicitEuler
* New spring force
* Periodic org.openpixi.pixi.distributed.movement.boundary
* Basic accuracy test at 1/100 of the step size
* Display current at variable grid size

---
Version 0.1 - March 5, 2012

* Initial release.
* Simulate up to 10000 particles in constant electric, magnetic, and gravitational fields.
* Quick settings with particles of two different charges.
* Set frame rate and time step.
* Show frame rate and memory information.
