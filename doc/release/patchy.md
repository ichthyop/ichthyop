# Patchy release

The patchy release method (`PatchyRelease.java`) can be viewed as an extension of the release stain method, where several stains are randomly created.

The number of stains is given by the `number_patches` parameter. The number of particles per stain is given by the `number_agregated` parameter. The radius and thickness (if 3D simulation) for each patch is given by the `radius_patch` and `thickness_patch` parameters, respectively.

Additionnally, there is the possibility to release patches in each release zone. This is achieved by setting the `per_zone` parameter to true. An example is provided in {numref}`fig-zone-patchy-release`.

If this parameter is set to false, the bounding box around the defined release zones (if any) or the full domain is used to release the patches, as shown
in {numref}`fig-uniform-patchy-release`.

(fig-zone-patchy-release)=

:::{figure} _static/release_patchy_zones.*
:align: center
:width: 600

Example of a patchy zone release.
:::

(fig-uniform-patchy-release)=

:::{figure} _static/release_patchy_uniform.*
:align: center
:width: 600

Example of a patchy uniform release.
:::
