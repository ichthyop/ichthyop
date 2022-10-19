Particles
############################

Ichthyop particles (``Particle.java`` class) contains the attributes described in :numref:`table-attr-part`.

.. _table-attr-part:

.. csv-table:: Particle state variables
   :file: _static/particle.csv
   :delim: ;
   :header-rows: 1
   :class: tight-table


.. /**
..  * Grid coordinate
..  */
.. private double x, y, z;
.. private double dx, dy, dz;
.. /**
..  * Geographical coordinate
..  */
.. private double lat, lon, depth;
.. /**
..  * <code>true</code> if 3 dimensions point false otherwise
..  */
.. private static boolean is3D;
.. private boolean latlonHaveChanged, depthHasChanged;
.. private boolean xyHaveChanged, zHasChanged;
.. private boolean exclusivityH, exclusivityV;
.. private static int nz;