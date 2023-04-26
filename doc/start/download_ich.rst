.. _osm_inst:

Downloading Ichthyop
-------------------------

The Ichthyop model is available on `GitHub <https://github.com/ichthyop/ichthyop>`_. There is two ways to recover Ichthyop:

- Using executable files (``.jar`` files).
- From source files.

Using executables
@@@@@@@@@@@@@@@@@@@@@@@@

Ichthyop users can download Ichthyop executables `here <https://github.com/ichthyop/ichthyop/tags>`_. Choose a version, and download the :samp:`ichthyop-X.Y.Z-jar-with-dependencies.jar` file (replacing :samp:`X.Y.Z` by the version number).

From source
@@@@@@@@@@@@@@@@@@@@@@@@@@@ 

To get the source code, type in a Terminal (Unix/MacOs) or Git Bash prompt (Windows):

.. code-block:: bash

    git clone https://github.com/ichthyop/ichthyop.git

The code can then be compiled either using IDE (NetBeans, VSCode) or using the following command line:

.. code-block:: bash
    
    mvn package
    
The executable will be generated in the ``target`` folder.
    
.. warning::
    
    To use the command line, Maven needs to be installed (see instructions on https://maven.apache.org/install.html)
