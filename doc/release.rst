.. _release:

Particle release
####################################

.. ipython:: python
    :suppress:

    import os
    import subprocess
    cwd = os.getcwd()

    fpath = "release/_static/plot_release_types.py"
    with open(fpath) as f:
        with open(os.devnull, "w") as DEVNULL:
            subprocess.call(["python", fpath], stdout=DEVNULL, stderr=subprocess.STDOUT)
            

In the present section, the different release processes that are implemented within Ichthyop are described. 
The parameters that are associated with the release processes must be included within :samp:`release` blocks
(cf. :numref:`xml_config`).


.. toctree::
   :maxdepth: 1
   :caption: Contents:

   release/stain.rst
   release/zone.rst
   release/text.rst
   release/patchy.rst
   release/bottom_surface
   release/netcdf.rst
