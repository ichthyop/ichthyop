Running Ichthyop
-----------------------

Clicking on file (Windows)
#########################################

Open the Ichthyop folder and double click on the
``ichthyop-X.Y.Z-jar-with-dependencies.jar`` file, where ``X.Y.Z`` is the Ichthyop version. You should see the
Ichthyop console.

From command line (Unix/Mac Os X)
#########################################

Open a command line prompt (Terminal or CMD prompt) and navigate to the Ichthyop folder using ``cd``.

Then, type:

.. code:: bash

    java -jar ichthyop-X.Y.Z-jar-with-dependencies.jar

with ``X.Y.Z`` the Ichthyop version.

This will prompt the Java console. In order to run Ichthyop without the console, you need to specify a supplementary argument, which
is the XML configuration file.

.. code:: bash

    java -jar ichthyop-X.Y.Z-jar-with-dependencies.jar cfg-roms3d.xml
