Patchy release
================

The patchy release method (`PatchyRelease.java`) can be viewed as an extension of the release stain method, where several stains can be defined.

The number of stains is given by the `number_patches` parameter. The number of particles per stain is given by the `number_agregated` parameter. The radius and thickness (if 3D simulation) for each patch is given by the `radius_patch` and `thickness_patch` parameters, respectively. 

Additionnally, there is the possibility to release patches in each release zone. This is achieved by setting the `per_zone` parameter to true.
