Configuration
####################################

Here you will find the usual "File menu" functions : create a new configuration file, open an existing one, close it or save and save as the configuration file.

.. figure:: _static/step1-configure.png 
    :align: center

    Step 1, configure

New configuration file
@@@@@@@@@@@@@@@@@@@@@@@@@@@@

.. figure:: _static/new.png
    :align: center

    Step 1, create a new configuration file

The application comes with some preset examples of configuration files (the templates). Select one of the templates, change the name of the configuration if the suggested name does not suit you and click on the Create button.

Content of the configuration file
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

.. figure:: _static/edit.png
    :align: center

    Step 1, edit the configuration file

The configuration file is organized in several categories and each category contains several blocks of parameters.

When the configuration file is created out of a template, it is ready to use and you do not need to change any parameter for running the simulation. Little by little you can explore the configuration file, starting with the "Main" blocks and change some parameters to see what is happening. On a second time you can start playing with the advanced parameters that activate and control the behaviors of the particles.

Main blocks:

- Time: Set the simulation time options, such as the beginning of the simulation, the duration of transport, the time step, etc.
- Output: Options that control the record of the particle tracks in a NetCDF output file.
- Dataset: Management of the hydrodynamic dataset.
- Release: Determine how and where the particles should be released.

Advanced blocks:

- Transport: Parameters for controlling the advection process, the dispersion, the vertical migration, the wind drift, etc.
- Release: Additional ways for releasing particles, from zones, with position recorded in a text file or in a NetDCF file, etc.
- Biology: Control the biological processes such as growth or cold water sensitivity, etc.

Each block is fully described and commented in the block information area. As well, you will find a description and the necessary explanations for each parameter in the parameter information area.

.. warning:: 

    Do not forget to save the configuration file before going to the next step.
    
.. danger::
    
    **Parameters cannot be added from within the Ichthyop console. Only paramters that are already defined
    on the XML file can be edited using the console**