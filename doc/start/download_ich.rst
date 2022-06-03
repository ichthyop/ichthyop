.. _osm_inst:

Downloading Ichthyop
-------------------------

The Ichthyop model is available on GitHub: `https://github.com/ichthyop/ichthyop <https://github.com/ichthyop/ichthyop>`_

Ichthyop users
@@@@@@@@@@@@@@@@@@@@@@@@

Ichthyop users can download Ichthyop executables `here <https://github.com/ichthyop/ichthyop/tags>`_. Choose a version, and download the :samp:`ichthyop-X.Y.Z-jar-with-dependencies.jar` file (replacing :samp:`X.Y.Z` by the version number).

Ichthyop developers
@@@@@@@@@@@@@@@@@@@@@@@@@@@ 

Ichthyop developers will need to clone the source code. To do so, type in a Terminal (Unix/MacOs) or Git Bash prompt (Windows):

.. code-block:: bash

    git clone https://github.com/ichthyop/ichthyop.git

To compile the code, first install `Netbeans <https://netbeans.apache.org/>`_. 

Open Netbeans and click on :samp:`Open Project` (|open-proj| logo). Find the Ichthyop folder (you should see a |maven-logo| logo) and open it. Build the project by clicking on the :samp:`Clean and Build Project` (|build-proj| logo)

.. |maven-logo| image:: _static/Maven2Icon.png
.. |open-proj| image:: _static/openProject.png
.. |build-proj| image:: _static/rebuildProject.png

The :samp:`ichthyop-X.Y.Z-jar-with-dependencies.jar` file should be created in the `target` folder.