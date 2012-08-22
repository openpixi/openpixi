LICENSE
=======

OpenPixi - Open Particle-in-Cell (PIC) simulator.
Copyright (C) 2012  OpenPixi.org

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

OpenPixi is an open Particle-In-Cell (PIC) simulator. 

Please try out the live version at

  http://www.openpixi.org


BUILD INSTRUCTIONS
==================

OpenPixi uses Maven for building ( http://maven.apache.org/ ).

To build Pixi:
    cd pixi
    mvn package

To launch Pixi:
    java -jar target/pixi-x.x-SNAPSHOT.jar


DEVELOP IN ECLIPSE
==================

To launch Pixi in Eclipse ( http://www.eclipse.org/ ) do the following:

1) Clone the GitHub repository to a local folder with
	cd "path to local folder"
	git clone git://github.com/openpixi/openpixi.git
2) Open Eclipse and go to Help > Install New Software
3) Choose "All Available Sites" from the dropdown menu
4) Search for "maven", choose the desired result and press finish
5) File > Import > Maven > Existing Maven Projects
6) Select the local folder that you have chosen previously


DISTRIBUTED VERSION
===================

To get started with the distributed version see our wiki
https://github.com/openpixi/openpixi/wiki/Getting-Started-with-Distributed-Version


VERSION HISTORY
===============

---
Version 0.5 - ?

* Ditributed version of OpenPixi which uses IBIS framework for communication
* Framework for throurough testing of the distributed version
* Parallel version of OpenPixi which uses threads
* Support for debugging and profiling through AspectJ
* Introduced Settings class to hold all the simulation settings

---
Version 0.4 - June 28, 2012

* Relativistic versions of particle solvers
* Field interpolator with charge conserving area weighting
* Poissonsolver
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

