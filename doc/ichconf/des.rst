Configuration files
########################

Ichthyop is configured using `XML <https://en.wikipedia.org/wiki/XML>`_ configuration files. It should always start as follows:

.. code:: XML
    
    <icstructure>
    <long_name>Generic Ichthyop configuration file</long_name>
    <description>The file has few pre-defined parameters.</description>

    </icstructure>
    
Configuration blocks
+++++++++++++++++++++++++

Ichthyop is configured by blocks, each block managing a specific aspect of the model. The blocks are as follows:

- :samp:`ACTION`: Block of parameters related to the :samp:`action` classes (cf. :numref:`process`).
- :samp:`RELEASE`: Block of parameters related to the :samp:`release` classes (cf. :numref:`release`)
- :samp:`DATASET`: Block of parameters related to the :samp:`datasets` classes.
- :samp:`OPTION`: Block of parameters related to the remaining parameters.

New blocks can be added in the XML file as follows:

.. code:: XML

    <block type="option">
            <key>app.transport</key>
            <tree_path>Transport/General</tree_path>
            <description>Set the general transport options of the simulation.</description>
    </block>
    
The :samp:`key` tag is used to identify the configuration block.  The :samp:`tree_path` tag is used in the Ichthyop console to 
create the parameter tree. The :samp:`description` field is used to display the block description in the 
Ichthyop console.

Configuration parameters
++++++++++++++++++++++++++++++

To each block is associated a list of parameters. This list of parameter is added in the XML as follows:

.. code:: XML

    <parameters>
    </parameters>
    
Inside the :samp:`parameters` tags, new parameters are defined as follows:

.. code:: XML
    
    <parameter>
        <key>output_path</key>
        <value>output</value>
        <long_name>Output path</long_name>
        <format>path</format>
        <default>output</default>
        <description>Select the folder where the simulation NetCDF output file should be saved.</description>
    </parameter>
    
The :samp:`key` tag allows to identify the parameter, while the :samp:`value` tag specifies the value of the parameter. The 
remaining tags are only used by the Ichthyop console.  The :samp:`long_name` and :samp:`description` tags are used by the console
to provide informations about the parameter. 

The :samp:`format` tag specifies the parameter format, which will be used by the console parameter editor. The accepted values are:

- :samp:`path`: For files and folders
- :samp:`date`: For dates (format must be :samp:`year YYYY month MM day at HH:MM`)
- :samp:`duration`: For duration (format must be :samp:`#### day(s) ## hour(s) ## minute(s)`)
- :samp:`float`: For real values
- :samp:`integer`: For integer values.
- :samp:`class`: For class parameters. It allow the user to choose an existing Ichthyop class in the configuration file.
- :samp:`list`: For a list of string parameters, separated by :samp:`,`
- :samp:`boolean`: For boolean parameters. It allows the user to select `true` or `false` using a simple combo box.
- :samp:`combo`: For parameters with a limited set of values, which can be selected in the console with a combo box.
- :samp:`lonlat`: For geographical coordinates.

In the case of :samp:`combo` parameters, the list of accepted parameters is specified by 
providing as many :samp:`accepted` tags as necessary. For instance:

.. code:: XML

    <parameter>
        <key>time_arrow</key>
        <long_name>Direction of the simulation</long_name>
        <value>forward</value>
        <format>combo</format>
        <accepted>backward</accepted>
        <accepted>forward</accepted>
        <default>forward</default>
        <description>Run the simulation backward or forward in time.</description>
    </parameter>