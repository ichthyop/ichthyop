Visualize results
####################################

.. figure:: _static/step3-mapping.png
    :align: center

    Step 3, mapping

When the simulation is completed, the application automatically opens the current Ichthyop output file for visualizing the results. If your computer is connected to Internet, you should see the map being centered above the simulated area. Otherwise, it only displays a Grey background.

You may want to skip that step or keep it for later. In that case, just click on "Close NetCDF" and go to any other steps or exit the application. Any time, you can go back to this step: click on "Open NetCDF" and select the Ichthyop output file you wish to visualize. When the NetCDF file is opened, the application brings you back to the exact point where it was when the simulation just completed.

The application offers two ways for visualizing the results : draw the particle trajectories with a Web Map Service or export the particle trajectories in a KMZ file that can be opened with Google Earth. Both functions are completely independent one from another.

Results
@@@@@@@@@@@@@@@@@@@

Ichthyop archives the particle trajectories in NetCDF format, a machine-independent data formats for sharing array-oriented scientific data. The NetDCF file is recorded in the output folder (set in the Output section of the configuration file) and the file name contains the date and time of creation of the file.

Default contents of the NetCDF output file: time of the simulation, longitude, latitude, and depth at particle position, and mortality status.


Set particle color
@@@@@@@@@@@@@@@@@@@@@@@@

The :guilabel:`Default color` button determines the particle color for visualizing the trajectories.

Particles are plotted as small circles. :guilabel:`Particle size` determines the diameter of the circle in pixel.

To use a colorbar, select in the Combo box a variable archived in the Ichthyop output file you wish to visualize as a tri-color range. The :guilabel`Auto range` button will scan the values of the variable and suggest the following range : [mean - 2 * standard deviation; mean + 2 * standard deviation]. Do not forget to click on :guilabel:`Apply settings` for validating the changes of the color bar.

For taking off the color bar, select the :guilabel`None` item in the Combo box and click on :guilabel`Apply settings`. A click on :guilabel`Default color` button should also deactivate the color bar.

Make maps using Web Map Service
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

According to Wikipedia, a Web Map Service (WMS) is a standard protocol for serving georeferenced map images over the Internet.

Ichthyop provides three different WMS for displaying the ocean bathymetry and the cost line as a background of the particle trajectories.

Maps can be intuitively zoomed in and out with the mouse wheel and re-centered doing a mere drag and drop.

Depending on the quality of the Internet connection and how busy is the Web Map Server, the display of the background tiles might take a while or even not work at all. In that case, try again with a distinct WMS and change the zoom scale.

When the settings of the map looks satisfactory, click on "Make maps" button. Ichthyop will create a folder that has exactly the same name than the simulation NetCDF output file (without the .nc extension) in the output directory. Then maps are recorded in this folder as PNG pictures.

Again, depending on the computer capabilities and the number of particles, the map creation might require a large amount of the available dynamic memory and the application looks like it is frozen. Wait for the application to complete this step. Refer to section "Java Heap Space" if the application crashes because of memory problem.

Export trajectories to KMZ format
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

By default, Ichthyop records the particle trajectories in NetCDF format. It is perfectly adapted for archiving and sharing scientific data since it is a machine independent and array-oriented format. But it is not much handy for visualizing results.

Click on "Export to KMZ" button for recording the particle trajectories into a KMZ file. The file is recorded in the same directory than the Ichthyop output file, with the same name and the ".kmz" extension. KMZ format is the standard file format for visualizing georeferenced information with GoogleEarth.

Color settings (default color or color bar) and particle size will also be stored in the KMZ file.

When the export has performed, browse to the output folder and click on the KMZ file for launching GoogleEarth (assuming the program is installed on your computer).
