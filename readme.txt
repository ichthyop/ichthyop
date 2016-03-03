====================
ICHTHYOP readme file
================================
http://www.ichthyop.org

Ichthtyop is an individual based model that simulates Lagrangian transport of particles.

Copyright (c) Philippe VERLEY 2006-2015

Release: 3.3alpha (2015/12/16) 
https://forge.ifremer.fr/svn/ichthyop/branches/stable-3@r857

Feedback & Bug reports: www.ichthyop.org/forum

=============
Alpha version

This alpha version of Ichthyop gathers the latest developments and bug fixes since the previous release. It has not go through full testing yest and might be unstable. This alpha release will keep changing (new features and more bug fixing) until the developer consider it is feature complete and ready for a new release.
No JAR (Java executable file) is provided for the alpha version, the source code is distributed as a Netbeans project.

==========================
Changes since Ichthyop 3.2

Bug fixes:
* Horizontal dispersion now works in backward mode
* Multi release events works in backward mode
* NaN values for U and V velocities are handled with Mars2d
* Zone display in Step 3 Mapper has been improved. A zone is now drawed cell by cell, as it is done in the preview. The approach has a few inconvinients though (i) when zones overlap the last one drawn will cover the other ones; (ii) the zone defined in the output NetCDF files prior to this commit will not be drawable anymore; (iii) the WMSMapper.java needs the Dataset to be initialized to be able to draw the zones. Which is fine if the user draw the zones just after running the simulation. It will be an issue when the mapping is down at an other time. Nonetheless the user can still load the corresponding configuration file and click on Preview to initialize the dataset.
* Random generator number in the horizontal dispersion process was always initialised with the same seed. Set a unique seed for every run.

New features:
* New plugin for Mercator2D data (from local NetCDF files)
* New plugin for OSCAR data (from local NetCDF files and from OpenDAP)
* Vertical migration: user can define depth at day and depth at night as functions of age, provided in CSV files.
* Multithread option in Population.java can be set to TRUE. Experimental feature though, might not work satisfactorily yet.
* New growth function SoleGrowthAction.java dlength = c1[stage] * Math.pow(temperature, c2[stage]) * dt_day. c1 and c2 are user defined and depend on user defined length stages
* NemoDataset, new parameter read_var_w = true/false whether the W variable should be read or recalculated
* Replaced ClimatoCalendar by Day360Calendar which takes into account an Origin of time (same parameter used for the Gregorian Calendar).
* NemoDataset, added new function to reconstruct three-dimensional e3t field from e3t_0, e3t_ps and mbathy

===========
Requirement

JDK 1.7
Netbeans 8.02

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

% From 3.1 to 3.2
Bug fixes:
Multi release events - Some particles were overwritten at new release event.
Mapping - The JVM would often crash (architecture and version dependant) when trying to retrieve map background though the computer is offline or behind a proxy. Created and offline tile factory.
Bactracking - Multi release event did not work in backward mode.

New features:
Dataset - Ichthyop can read Symphonie (http://sirocco.omp.obs-mip.fr/outils/Symphonie/Accueil/SymphoAccueil.htm) NetCDF output files.
Lethal temperature - Added a hot lethal temperature. Lethal temperatures (both cold and hot) can be provided as a function of particle age, in a CSV file.
Buoyancy - Egg density can be provided as a function of age, in a CSV file. 

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
Configuration menu - Save as button did not work.
Gregorian calendar - The time converter generated a 24h offset on leap years only.
ROMS Dataset - Wrong calculation of the NEW type of vertical coordinate.
Coastal advection - 


New features:
Coastline behavior - Now there is a parameter to control coastal behavior. Particle might beach, bounce on the coastline as a billiard ball or just standstill.
Mars - Handles generalized sigma level (Mars V8).
Mars - Handles rotated domains (lon & lat as two dimensional fields).
WMS - Broaden the zoom range.
Recruitment stain - Look for recruited particles within an area defined by a central point and a radius.
NetCDF library updated to last version (4.2 April 2011)
Dataset - Added an option to deactivate a thorough and expensive sorting of the NetCDF input files when the files are already chronologically sorted.

==================
end of readme file
