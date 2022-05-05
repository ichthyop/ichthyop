# ICHTHYOP Release notes

## Changes in 3.3.12

### New features

- Adding the reading of noleap calendars.
- Possibility to select the NetCDF output format (usefull for Windows users).
- Adding some unit tests (NetCDF time)
- Adding the possibility to save density maps instead of trajectories.
- Adding saving of a `drifter` variable in order to use for mapping
- Adding possibility to use NetCDF file compression (new set of parameters)

### Bug fix

- Consideration of the case where HH:mm:ss is not provided in units (issues #22) 
- Consideration of the case where time is provided as HH:mm instead of HH:mm:ss (issue #32)
- If NetCDF time cannot be properly read in NetCDF (wrong units), use the string provided in the `time_origin` parameter.
- Adding the case when time units is in `minutes since ...`
- Correct a bug in the writting of time in the Ichthyop console.

## Changes in 3.3.11

### Bug fix

- Resolve a problem in the saving of XML Zone files on Windows. Encoding was automatically set to Cp1512, not handled by XML. Now UTF-8 should be saved. Bug was corrected for configuration files in 3.3.6 but not for zone files.

## Changes in 3.3.10

### Bug fix

- Add the `units` attribute (which replaces the `origin` attribute) to the time variable. It was lost with the new time management in Ichthyop.
- **Correct bug in template creation (templates were considered as resources by Ichthyop but were kept in `java` folder, which was not used as resource folder).**

## Changes in 3.3.9

### New features

- Resources have been moved in a new folder (`src/main/resources`). This allows the VSCOde debugger to work more  nicely (debugging in `java` files and not in `class` files). 

### Bug fix

- Template configuration and forcing files have been updated to match the new management of time (use of units attributes in the NetCDF)
- Closing of NetcdfFile in the `getDate()` method (`DatasetUtil.java`), which causes errors after multiple opening (pointed out by Amael Dupaix)

## Changes in Ichthyop 3.3.8

### Bug fix

- Correct output issue when running multiple simulations (pointed out by Amael Dupaix)

## Changes in Ichthyop 3.3.7

### New features

- Remove all warnings (VSCode)
- Remove deprecated NetCDF functions, use new NetCDF implementation
- Re-add the `WaveDriftFileAction.java` file.
- New constructor for the `InterAnnualCalendar` object
- Saving the stage values in the `DebGrowthAction.java`

### Bug fix

- Correction of the pC calculation in the classical DEB (forgot Tahr correction) + change starvation correction (`||` instead of `&&`)
- Correction in the saving of zones areas: `zoneX` is the dimension (number of points) while `coord_zoneX` is the coordinates of each point. Allows to read file using Python Xarray without problems.
- Correction in the time management in Ichthtyop. Use `LocalDateTime` extensively.
- Correction in the lock of particles when recruited. This insures that analysing recruitment from `zones` or `recruited_zones` variables gives the same result (spotted by Stephane Pous)
- Correction of a bug in the saving of ouput file with Zones + Gui (spotted by Stephane Pous)
- Correction of a bug in the display of zones in GUI (used the wrong variable to display variables on maps)
- Do not include a user-defined track variable if already included (for instance `temperature` in `LethalTempAction.java`)

## Changes in Ichthyop 3.3.6

### Bug fixes

- Resolve a problem in the saving of XML configuration files on Windows. Encoding was automatically set to Cp1512, not handled by XML. Now UTF-8 should be saved.

## Changes in Ichthyop 3.3.5

### New features

- Automatic compilation tests using GitHub actions
- Badges on README.md.
- New License to allow connections with Zenodo (GPL-3)
- Adding debugging stuff that saves W computed by Ichthyop and leaves program

### Bug fixes

- Deactivate the `compile on save` Netbeans feature (compilation errors not always detected)
- Correction of a bug in the `TxtFileRelease` class. In 3.3.4, changes were made to insure that the output drifter dimension has the same size as the effectively released particles, but in fact the file has a 0 drifter dimension. Moved back to previous state, with the display of a warning message.

## Changes in Ichthyop 3.3.4

### Bug fixes

- Correction in the reading of gdepT/gdepW in NemoDataset (use of `round` instead of `floor`)
- Possibility to use DEB length criterion for the RecruitmentAction (no more conflict with traditional growth actions like linear)
- Debugging the configuration update manager (work from resource stream rather than URL, which failed when running the jar file directly)

### New features

- Adding NEMO in 2D
- Possibility to read Roms W field instead of computing it (not fully satisfying though)
- Nyctemeral migration not active if the target depth is below the local sea floor
- Moving to Maven Java system (easier compilation using `mvn -B package`).
- Adding possibility to run Ichthyop in map coordinates rather than in lon/lat (developped for polar application, Dennis Jongsomjit).
- Adding functional response to linear growth action (implies reading food variable if activated)

## Changes in Ichthyop 3.3.3

### Bug fixes

- Correction in the calculation of W in NEMO dataset using divergence equation.

### New features

- Taking into account partial steps in NEMO dataset.

## Changes since Ichthyop 3.2

### Bug fixes:
* ROMS3D the VertCoordType global attribute was not read correctly and lead to incorrect calculation of the vertical levels.
* RungeKutta scheme now works in backward for 2D simulations
* The 'zone' output variable displays -99 for particles that have not been released yet (used to be zero, which is incorrect information)
* The 'release_zone' output variable now works with multiple release events (only the first release event would be written in the variable and all other particle release zones were set to zero)
* Horizontal dispersion now works in backward mode
* Multi release events works in backward mode
* NaN values for U and V velocities are handled with MARS2D and ROMS2D
* Zone display in Step 3 Mapper has been improved. A zone is now drawed cell by cell, as it is done in the preview. The approach has a few inconvinients though (i) when zones overlap the last one drawn will cover the other ones; (ii) the zone defined in the output NetCDF files prior to this commit will not be drawable anymore; (iii) the WMSMapper.java needs the Dataset to be initialized to be able to draw the zones. Which is fine if the user draw the zones just after running the simulation. It will be an issue when the mapping is down at an other time. Nonetheless the user can still load the corresponding configuration file and click on Preview to initialize the dataset.
* Random generator number in the horizontal dispersion process was always initialised with the same seed. Set a unique seed for every run.

### New features:
* Linear growth can define custom larval stages with length thresholds
* "Patches in zones" release mode accepts a new parameter "per_zone" to indicate whether the number of particles is global (as it used to be) or per release zones (new feature).
* New plugin for ROMS3D OpenDAP
* All the Dataset plugins detect automatically the unit of the time value by reading variable attribute "units". It can be either seconds or days, but can be easily extended to other units on demand.
* Added command line option -quiet in the batch mode for printing only error message
* New module 'Active swimming'. Swimming velocity is provided as an age function in a separated CSV file. The module accepts two modes : the input swimming velocity can either be constant (the particle always swims at the defined velocity) or random (the particle swims at random velocity among [0, 2 * defined velocity]). Swimming is isotropic.
* New plugin for NOVELTIS data (from local NetCDF files)
* New plugin for Mercator2D data (from local NetCDF files), regular grid
* New plugin for OSCAR data (from local NetCDF files and from OpenDAP) http://www.oscar.noaa.gov/
* Vertical migration: user can define depth at day and depth at night as functions of age, provided in CSV files.
* Multithread option in Population.java can be set to TRUE. Experimental feature though, might not work satisfactorily yet.
* New growth function SoleGrowthAction.java dlength = c1[stage] * Math.pow(temperature, c2[stage]) * dt_day. c1 and c2 are user defined and depend on user defined length stages
* NemoDataset, new parameter read_var_w = true/false whether the W variable should be read or recalculated
* Replaced ClimatoCalendar by Day360Calendar which takes into account an Origin of time (same parameter used for the Gregorian Calendar).
* NemoDataset, added new function to reconstruct three-dimensional e3t field from e3t_0, e3t_ps and mbathy

 
### Requirement

Java >= 1.8

### Run Ichthyop 

Double click on the JAR file or run it from commmand line 'java -jar ichthyop-3.3.jar'

### License information

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

For details about the GNU General Public License, please see http://www.gnu.org/licenses/

### Description of files and folders

% Files
ichthyop-3.3.jar, java exectuable
readme.txt --> this document

% Directories
cfg --> Ichthyop configuration folder
lib --> Necessary libraries to run the program
input --> Basic NetCDF input files used for the examples

##  From 3.1 to 3.2
### Bug fixes:
Multi release events - Some particles were overwritten at new release event.
Mapping - The JVM would often crash (architecture and version dependant) when trying to retrieve map background though the computer is offline or behind a proxy. Created and offline tile factory.
Bactracking - Multi release event did not work in backward mode.

### New features:
Dataset - Ichthyop can read Symphonie (http://sirocco.omp.obs-mip.fr/outils/Symphonie/Accueil/SymphoAccueil.htm) NetCDF output files.
Lethal temperature - Added a hot lethal temperature. Lethal temperatures (both cold and hot) can be provided as a function of particle age, in a CSV file.
Buoyancy - Egg density can be provided as a function of age, in a CSV file. 

## From 3.0b to 3.1
### Bug fixes:
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


### New features:
Coastline behavior - Now there is a parameter to control coastal behavior. Particle might beach, bounce on the coastline as a billiard ball or just standstill.
Mars - Handles generalized sigma level (Mars V8).
Mars - Handles rotated domains (lon & lat as two dimensional fields).
WMS - Broaden the zoom range.
Recruitment stain - Look for recruited particles within an area defined by a central point and a radius.
NetCDF library updated to last version (4.2 April 2011)
Dataset - Added an option to deactivate a thorough and expensive sorting of the NetCDF input files when the files are already chronologically sorted.
