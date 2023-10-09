---
substitutions:
  color: |-
    ```{image} _static/ico22/fill-color.png
    ```
  down: |-
    ```{image} _static/ico22/down.png
    ```
  list-add: |-
    ```{image} _static/ico22/list-add.png
    ```
  list-remove: |-
    ```{image} _static/ico22/list-remove.png
    ```
  up: |-
    ```{image} _static/ico22/up.png
    ```
---

# Zone definition

In Ichthyop, the user can define zones, either release zones or recruitment zones.

The zones can be edited using the GUI, as shown in {numref}`figure-gui-zone`

(figure-gui-zone)=

:::{figure} _static/zone.png
:align: center
:width: 600px

Ichthyop Zone editor
:::

## Adding, removing and renaming zones

The number of zones is managed on the left part of the panel.

New zones can be added by clicking the {{ list-add }} button. When a zone is selected, it can be removed by
clicking on the {{ list-remove }} button.

The reordering of the zones is achieved by clicking on the {{ up }} and {{ down }} buttons.

When double-clicking on the name of the zone on the left panel, the user can edit the zone name.

## Editing a zone

When a zone is selected, the user can edit different parameters associated with the zone.

First, the user can enable or disable a zone by clicking on the {guilabel}`Enabled` tick box.

The zones are defined by providing the points coordinates. Points can be added, removed and reordered by using the
{{ list-add }}, {{ list-remove }}, {{ up }} and {{ down }} buttons, respectively. The user can also change the format of the points coordinates by clicking on the radio buttons in the {guilabel}`Options` bottom panel.

Ichthyop defines two types of zones: one for release (see {numref}`zone-release`) problèmeand one for recruitment purposes.
This type is chosen by using the {guilabel}`Type of zone` combo box. For release zones, the user can specify the number of
particles that will be released in the zone (only if the `user_defined_nparticles` parameter is
set equal to true, cf {numref}`zone-release`). It is done by filling the {guilabel}`Number of released particles` textbox
and pressing {guilabel}`ENTER`

Each zone is associated with a color, that will be used to its representation in the graphical interface during the
preview and the display of the simulation results.
This color can be edited by using the {{ color }} button.

In the case of 3D simulations, you can specify the depth range to use in the zone. To activate this feature,
click on the {guilabel}`Activated` tick box of the {guilabel}`Thickness` panel.
You can provide the lower and upper depth that must be considered in the given zone (negative values).

In 3D simulations, you can also specify the bathymetric range that you want to include, for
instance if you want to release particles only
on the ocean shelf (i.e depth less than 200m). This can be done by activating the feature by clicking on the
{samp}`Activated` tick box of the {guilabel}`Bathymetric mask` panel.
