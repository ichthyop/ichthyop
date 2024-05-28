# NEMO grid

```{eval-rst}
.. ipython:: python
    :suppress:

    import os
    import subprocess

    cwd = os.getcwd()
    fpath = "developer/grid/_static/nemo.py"
    subprocess.call(["python", fpath], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
```

In this section, the main features of the NEMO grid and the implications in Ichthyop are summarized.

## Horizontal

### Computation domain

In the NEMO grid, the computation domain extends from the western edge of the western T cell (`i=0`) to the eastern edge of the eastern T cells (`i=nx`), and from the
southern edge of the southern T cells (`j=0`) to the northern edge of the nothern T cells (`j=ny`).

### Indexing

The horizontal layout of the NEMO grid is as follows:

```{image} _static/nemo_hor_index.png
:align: center
:width: 250 px
```

Tracer points (`T` points) are stored at the center of the cell. Zonal velocities (`U` points) are stored on the eastern face, while meridional velocities (`V` points) are stored on the northern face. **U, V and T points have the same number of elements!**

### Scale factors

In NEMO, the zonal and meridional length of the cells are stored in the `e1x` and `e2x` variables, with `x` equals to `t`, `u` or `v` depending on the point considered.

### Land-sea mask

For a particle at a given location, we determine whether it is on land as follows:

- We extract the `i` index of the `T` land-sea mask to use by computing `round(x - 0.5)`.
- We extract the `j` index of the `T` land-sea mask to use by computing `round(y - 0.5)`.
- We extract the mask value at the `(i, j)` location.

```{image} _static/landsea_mask_nemo.*
:align: center
:width: 500 px
```

### Close to coast

To determine whether a particle is close to coast, we extract the three neighbouring T cells. If one of them is land, then it assumed to be close to coast.

```{image} _static/close_to_coast_nemo.*
:align: center
:width: 500 px
```

### Is On Edge

The particle is considered to be out of the domain if the `y` value is greater than `ny - 0.5` (no possible interpolation of `T` points) or less than `1` (no possible interpolation of `V` points).

If there is no zonal cyclicity, the particle is also considered to be out of the domain if the `x` value is greater than `nx - 0.5` (no possible interpolation of `T` points) or less than `1` (no possible interpolation of `U` points).

```{image} _static/is_on_edge_nemo.*
:align: center
:width: 500 px
```

### Zonal cyclicity

For regional simulations, there is no zonal cyclicty. On the other hand, for global NEMO simulations, which runs
on the ORCA grid, the zonal cyclicity is as follows (indexes are provided for T points):

```{image} _static/zonal_cyclicity_nemo.*
:align: center
:width: 500 px
```

Therefore:

- if $x \leq 1$, the particle is moved at $N_x - 2 + x$
- if $x \geq nx - 1$, the particle is moved at $x - N_x + 2)$

### Interpolation

#### Interpolation of T variables

Given a given position index of a particle with the `T` grid, the determination of the interpolation is done as follows:

- First, the `i` index of the `T` grid column left of the particle is found. This is done by using `floor` on the `x - 0.5` value. The removing of 0.5 is to convert the `x` value from the computational grid to the `T` grid.
- Then, the `j` index of the `T` grid line below the particle is found. This is done by using `floor` on the `y - 0.5` value. The removing of 0.5 is to convert the `y` value from the computational grid to the `T` grid.
- The area to consider is defined by the `[i, i + 1]` and `[j, j + 1]` squares.

An illustration is given below

```{image} _static/t_interpolation_nemo.*
:align: center
:width: 500px
```

#### Interpolation of U variables

Interpolation of `U` variables is done as follows:

- First, the `i` index of the `U` point left of the particle is found by using `floor(x - 1)`. The `-1` is to move from the computation grid to the `U` grid system.
- Then, the `j` index of the `U` grid line below the particle is found. This is done by using `floor` on the `y - 0.5` value. The `-0.5` is to move from the computation grid to the `U` grid system.
- The box used to average the variable is therefore defined by the `[i, i + 1]` and `[j, j + 1]` squares.

```{image} _static/u_interpolation_nemo.*
:align: center
:width: 500px
```

#### Interpolation of V variables

Interpolation of `V` variables is done as follows:

- First, the `i` index of the `V` point left of the particle is found by using `floor(x - 0.5)`. The `-0.5` is to move from the computation grid to the `U` grid system.
- Then, the `j` index of the `V` grid line below the particle is found. This is done by using `floor` on the `y - 1` value. The `-1` is to move from the computation grid to the `U` grid system.
- The box used to average the variable is therefore defined by the `[i, i + 1]` and `[j, j + 1]` squares.

```{image} _static/v_interpolation_nemo.*
:align: center
:width: 500px
```

## Vertical

### Indexing

The original NEMO vertical indexing system is shown below:

```{image} _static/nemo_ver_index.png
:align: center
:width: 250 px
```

Index starts at 0 (at the surface) and ends at $N_z - 1$ at depth. There are as many `W` levels as `T` levels. In NEMO, the `T` point situated at $N_z - 1$ **is always masked**. `W` levels are located *above* the corresponding `T` points.

Therefore, in Ichthyop, only the first $N_z - 1$ `T` points are read, while all the $N_z$ `W` points are read. This is show below:

:::{figure} _static/vertical_indexing_nemo.*
:align: center
:width: 500 px

Vertical indexing system as used in Ichthyop. Red dashed lines represent the `W` levels (cell edges), whose index is given in the gray box.
:::

There is now $N_z$ W levels but $N_z - 1$ T levels.

Furthermore, since in Ichthyop the first index corresponds to the seabed, the arrays are vertically flipped. Consequently, the final structure of the vertical grid is as follows:

:::{figure} _static/corrected_vertical_indexing_nemo.*
:align: center
:width: 250 px

Corrected vertical indexing, with `k=0` associated with the bottom depth.
:::

### Land-sea mask

:::{figure} _static/vertical_landsea_mask_nemo.*
:align: center
:width: 250 px

Vertical land-sea mask
:::

### Interpolation

#### T variables

:::{figure} _static/vertical_t_interpolation_nemo.*
:align: center
:width: 250 px

Vertical interpolation of T variables.
:::

#### W variables

:::{figure} _static/vertical_w_interpolation_nemo.*
:align: center
:width: 250 px

Vertical interpolation of T variables.
:::
