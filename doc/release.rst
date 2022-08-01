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


.. toctree::
   :maxdepth: 1
   :caption: Contents:

   release/stain.rst
   release/zone.rst
   release/patchy.rst
   release/netcdf.rst
