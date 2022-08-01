Zone release
================

The zone release method (`ZoneRelease.java`) allows to release particles in different areas, which are defined in an XML zone file (`zone_file` parameter).

By default, the number of particles released in each zone (:math:`N_k`) is equal to 

.. math:: 

    N_k = N_{tot} \times \dfrac{S_{k}}{\sum_{i=1}^{N}S_i}

with :math:`N_{tot}` the total number of released particles, :math:`S_k` the surface of the :math:`k^{th}` release area and :math:`N` the number of release areas. 

However, if the `release_type` parameter is set equal to `USER`, then the number of particles released in each zone is defined by the user.

An example of zone release is provided below.


.. _fig-zone-release:

.. figure:: _static/release_zones.*
   :width: 600
   :align: center

   Example of a zone release.
