====================
ICHTHYOP readme file
================================
http://www.previmer.org/ichthyop

Ichthtyop is an individual based model that simulates Lagrangian transport of particles.

Copyright (c) Philippe VERLEY 2006-2012

Release: 3.1 (2012/10/30)

Feedback & Bug reports: info@previmer.org

===========
Requirement

Get latest Java installed, at least JRE 1.6 Update 35, recommended JRE 1.7

For most users, Java is already installed and up-to-date. Skip this
step and go to the start-up instructions.

Latest version of JRE (at least 1.6 Update 35) can be download at http://www.java.com/en/download/manual.jsp

=======
Startup

Double click on ichthyop-3.#.jar

Or from a command line, go to the ichthyop/ folder and type the following:

java -jar ichthyop-3.#.jar

To avoid any heap memory problem, you'd rather type:

java -Xms512m -Xmx1024m -jar ichthyop-3.#.jar

=========
First Run

Step 1 - Configuration > New
Select one of the templates and save the configuration file.
Step 2 - Simulation. Click "Preview" then "Run simulation".

===================
License information

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

For details about the GNU General Public License, please see http://www.gnu.org/licenses/

================================
Description of files and folders

% Files
ichthyop-3.#.jar --> Ichthyop executable file
readme.txt --> this document

% Directories
lib --> Necessary libraries to run the program
input --> Basic NetCDF input files used for the examples

===========
Changes log

% From 3.0b to 3.1
Bug fixes:
Configuration panel - Crashed at displaying a block with no visible parameter.
Backtracking - Used to crash for particles reaching the edge of the domain.
Backtracking - Did not work in batch neither SERIAL mode.
Backtracking - Time spans were incorrectly defined for KMZ export.
Backtracking - Multiple release events was not supported.
Zone editor - Ensured all floating numbers are dot-separated. Bathymetric mask was always enabled even though the checkbox was not selected.
Release particles - Application did not warn the user when attempting to release particles under the bottom or above the surface.
Dataset - Application used to throw an 'IOException, two many files opened' when working with many NetCDF input files.
OPA NEMO - computation of the vertical velocity was not correct. Now requires fields e3u_ps & e3v_ps.
OPA NEMO - Handles different formats for the gdept, gdeptw, e3t, etc. grid variables since it can vary from one configuration to an other.
Configuration menu - Save as button did not work.
Gregorian calendar - The time converter generated a 24h offset on leap years only.
ROMS Dataset - Wrong calculation of the NEW type of vertical coordinate.

New features:
Coastline behavior - Now there is a parameter to control coastal behavior. Particle might beach, bounce on the coastline as a billiard ball or just standstill.
Mars - Handles generalized sigma level (Mars V8).
Mars - Handles rotated domains (lon & lat as two dimensional fields).
WMS - Broaden the zoom range.
Recruitment stain - Look for recruited particles within an area defined by a central point and a radius.
NetCDF library updated to last version (4.2 April 2011). It handles NetCDF4.
Dataset - Added an option to deactivate a thorough and expensive sorting of the NetCDF input files when the files are already chronologically sorted.
GETM - Added a new module for integrating GETM hydrodynamics model outputs in Ichthyop. Not validated yet. To be considered as Beta development.
ROMS - Accepts separated grid file.
Buoyancy - Set particle density as an age function provided in an external CSV file.

==================
end of readme file
