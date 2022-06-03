ROMS grid
############################

.. ipython:: python
    :suppress:

    import os
    import subprocess
    cwd = os.getcwd()

    fpath = "grid/_static/roms.py"
    with open(fpath) as f:
        with open(os.devnull, "w") as DEVNULL:
            subprocess.call(["python", fpath], stdout=DEVNULL, stderr=subprocess.STDOUT)

In this section, the main features of the ROMS grid and the implications in Ichthyop are summarized.

Horizontal
========================

The horizontal grid layout of the ROMS model is shown below:

.. figure:: _static/roms_grid_1.*
   :width: 600 px
   :align: center

   ROMS staggered grid structure

Contrary to NEMO and MARS, the ``U`` points are located on the *western* face, while velocities are 
located on the *southern* faces. However, However, as indicated on the `Wikiroms page <https://www.myroms.org/wiki/Numerical_Solution_Technique>`_, while the `T` interior domain has :math:`(N_y, N_x)` dimensions, 
the `U` domain is :math:`(N_y, N_x - 1)` points while and `V` domain is :math:`(N_y - 1, N_x)` points. 
Indeed, the first row for V and the first column for U are discarded. Therefore, the structure of the input ROMS grid is
as follows:

.. figure:: _static/ichthyop_grid_roms.*
   :width: 600 px
   :align: center
    
   ROMS grid as interpreted by Ichthyop

Land-sea mask
@@@@@@@@@@@@@@@@

.. figure:: _static/land_sea_mask_roms.*
   :width: 600 px
   :align: center

   Land-sea masking for ROMS grid

Interpolation
@@@@@@@@@@@@@@@@@@@

T interpolation
+++++++++++++++++

Given a given position index of a particle with the `T` grid, the determination of the interpolation is done as follows:

- First, the `i` index of the `T` grid column left of the particle is found. This is done by using `floor` on the `x - 0.5` value. The removing of 0.5 is to convert the `x` value from the computational grid to the `T` grid.
- Then, the `j` index of the `T` grid line below the particle is found. This is done by using `floor` on the `y - 0.5` value. The removing of 0.5 is to convert the `y` value from the computational grid to the `T` grid.
- The area to consider is defined by the `[i, i + 1]` and `[j, j + 1]` squares.

An illustration is given below

.. figure:: _static/interpolation_t_roms.*
   :width: 600 px
   :align: center
    
   Interpolation of T points from ROMS grid

U interpolation
+++++++++++++++++

Interpolation of `U` variables is done as follows:

- First, the `i` index of the `U` point left of the particle is found by using `floor(x - 1)`. The `-1` is to move from the computation grid to the `U` grid system.
- Then, the `j` index of the `U` grid line below the particle is found. This is done by using `floor` on the `y - 0.5` value. The `-0.5` is to move from the computation grid to the `U` grid system.
- The box used to average the variable is therefore defined by the `[i, i + 1]` and `[j, j + 1]` squares.

.. figure:: _static/interpolation_u_roms.*
   :width: 600 px
   :align: center
    
   Interpolation of U points from ROMS grid

V interpolation
+++++++++++++++++

Interpolation of `V` variables is done as follows:

- First, the `i` index of the `V` point left of the particle is found by using `floor(x - 0.5)`. The `-0.5` is to move from the computation grid to the `U` grid system.
- Then, the `j` index of the `V` grid line below the particle is found. This is done by using `floor` on the `y - 1` value. The `-1` is to move from the computation grid to the `U` grid system.
- The box used to average the variable is therefore defined by the `[i, i + 1]` and `[j, j + 1]` squares.

.. figure:: _static/interpolation_v_roms.*
   :width: 600 px
   :align: center
    
   Interpolation of V points from ROMS grid

Is on edge
@@@@@@@@@@@@@@@@@@@@@@@@@@@

A particle is considered to be out of domain when :math:`x \leq 1` (no possible interpolation of U on the western face), when :math:`y \geq N_x - 1` 
(no possible interpolation of U on the eastern face), when :math:`y \leq 1` (no possible interpolation of V on the southern domain) or when :math:`y \geq N_y - 1` (no possible interpolation of V on the northern part of the domain).

The excluded domain is represented below:

.. figure:: _static/in_on_edge_roms.*
   :width: 600 px
   :align: center
    
   Excluded domain in the Ichthyop ROMS simulations.


Vertical
==================

Sigma coordinate
@@@@@@@@@@@@@@@@@@@@@@@@

The vertical coordinate system of ROMS is discussed on `WikiRoms <https://www.myroms.org/wiki/Vertical_S-coordinate>`_ and shown below.

.. figure:: _static/roms_vertical_grid.*
   :width: 400 px
   :align: center

   Vertical grid in the ROMS model


The vertical coordinate in ROMS is :math:`\sigma`, which varies between :math:`-1` (ocean bottom) and :math:`0` (ocean surface). There are two implementations of the :math:`\sigma` to :math:`z` conversion, both using sea-level anomalies (:math:`\zeta`) and bathymetry (:math:`h`).

The first one is available in ROMS since 1999 and is given by:

.. math::

    z(x,y,\sigma,t) = S(x,y,\sigma) + \zeta(x,y,t) \left[1 + \dfrac{S(x,y,\sigma)}{h(x,y)}\right]

with

.. math::

    S(x,y,\sigma) = h_c \, \sigma + \left[h(x,y) - h_c\right] \, C(\sigma)

and :math:`h_c` and :math:`C(\sigma)` parameters provided in the grid file.

The second transform, called UCLA-ROMS, is given by:

.. math::

    z(x,y,\sigma,t) = \zeta(x,y,t) + \left[\zeta(x,y,t) + h(x,y)\right] \, S(x,y,\sigma)

with

.. math::

    S(x,y,\sigma) = \dfrac{h_c \, \sigma + h(x,y)\, C(\sigma)}{h_c + h(x,y)}

and :math:`h_c` and :math:`C(\sigma)` parameters provided in the grid file.

It can be rewritten in the same form as the original one.

.. math::

    z(x,y,\sigma,t) = h(x,y) S(x,y,\sigma) + \zeta(x,y,t) + \zeta(x,y,t) S(x,y,\sigma)

.. math::

    z(x,y,\sigma,t) = h(x,y) S(x,y,\sigma) + \zeta(x,y,t) \left[1 + S(x,y,\sigma)\right]

.. math::

    z(x,y,\sigma,t) = h(x,y) S(x,y,\sigma) + \zeta(x,y,t) \left[1 + \dfrac{h(x, y)S(x,y,\sigma)}{h(x, y)}\right]

In this form, both formulations can be expressed as: 

.. math:: 

    z(x,y,\sigma,t) = H_0(x, y, \sigma) + \zeta(x,y,t) \left[1 + \dfrac{H_0(x, y, \sigma)}{h(x, y)}\right]

with :math:`H_0` which is constant overt time, and which varies between the classical and the UCLA formulations. For the classical formulation:

.. math::

    H_0(x, y, \sigma) = S(x, y, \sigma)

For the UCLA formulation:

.. math::

    H_0(x, y, \sigma) = h(x, y) S(x, y, \sigma)
