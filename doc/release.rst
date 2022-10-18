.. _release:

Particle release
####################################

.. ipython:: python
    :suppress:

    import os
    import subprocess
    cwd = os.getcwd()

    fpath = "release/_static/plot_release_types.py"
    subprocess.call(["python", fpath], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            

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
    release/schedule.rst
