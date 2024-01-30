(zone-release)=

# Zone release

The zone release method (`ZoneRelease.java`) allows to release particles in different areas, which are defined in an XML zone file (`zone_file` parameter).

By default, the number of particles released in each zone ($N_k$) is equal to

$$
N_k = N_{tot} \times \dfrac{S_{k}}{\sum_{i=1}^{N}S_i}
$$

with $N_{tot}$ the total number of released particles, $S_k$ the surface of the $k^{th}$ release area and $N$ the number of release areas.

However, if the `user_defined_nparticles` parameter is set equal to `true`, then the proportion of the particles to release in each zone is defined by the user.

An example of zone release is provided below.

(fig-zone-release)=

:::{figure} _static/release_zones.*
:align: center
:width: 600

Example of a zone release.
:::
