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

1) Menu > File > Import > Select root directory ..../pixi
2) Menu > Run > Run.


VERSION HISTORY
===============

---
Version 0.3 - ?

---
Version 0.2 - March 14, 2012

* Additional solvers: Boris, BorisDamped, Euler, LeapFrog, LeapFrogDamped,
  SemiImplicitEuler
* New spring force
* Periodic boundary
* Basic accuracy test at 1/100 of the step size
* Display current at variable grid size

---
Version 0.1 - March 5, 2012

* Initial release.
* Simulate up to 10000 particles in constant electric, magnetic, and gravitational fields.
* Quick settings with particles of two different charges.
* Set frame rate and time step.
* Show frame rate and memory information.

