Surface and bottom releases
#################################

The surface and bottom release methods (`SurfaceRelease.java` and `BottonRelease.java`) allow to randomly release 
particles over the entire domain, either on the surface or at the bottom (for 3D simulations only).

The only parameter required by these methods is the number of particles (`number_particles` parameter). 
There is no control over the area where the particles are released.

An example of a surface release is shown in :numref:`plot-surface-release`.

.. _plot-surface-release:
    
.. figure:: _static/release_surface.*
   :width: 600
   :align: center
   
   Example of a surface release.
