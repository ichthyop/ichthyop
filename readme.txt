====================
ICHTHYOP readme file
================================
http://www.previmer.org/ichthyop

Ichthtyop is an individual based model that simulates Lagrangian transport of particles.

Copyright (c) Philippe VERLEY 2006-2013

Release: 3.2 (2013/04/10)

Feedback & Bug reports: info@previmer.org

===========
Requirement

Get latest Java installed, at least JRE 1.6 update 45 (1.7 recommended).

For most users, Java is already installed and up-to-date. Skip this
step and go to the start-up instructions.

Latest version of JRE (1.7 recommended) can be download at http://www.java.com/en/download/manual.jsp

=======
Startup

Double click on ichthyop-3.2.jar

Or from a command line, go to the ichthyop/ folder and type the following:

java -jar ichthyop-3.2.jar

To avoid any heap memory problem, you'd rather type:

java -Xms512m -Xmx1024m -jar ichthyop-3.2.jar

If you work behind a proxy:

java -jar -Dhttp.proxyHost=your.proxy.com -Dhttp.proxyPort=8080 ichthyop-3.2b.jar

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
ichthyop-3.2.jar --> Ichthyop executable file
readme.txt --> this document

% Directories
lib --> Necessary libraries to run the program
input --> Basic NetCDF input files used for the examples

==================
end of readme file
