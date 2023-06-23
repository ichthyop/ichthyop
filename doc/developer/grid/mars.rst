MARS grid
############################

.. ipython:: python
    :suppress:

    import os
    import subprocess
    cwd = os.getcwd()

    fpath = "grid/_static/mars.py"
    subprocess.call(["python", fpath], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

In this section, the main features of the MARS grid and the implications in Ichthyop are summarized.

Horizontal
========================

In MARS, the 3D structure of the grid is the same as the one in NEMO:

.. figure:: _static/mars_schema_discretisation.*
   :width: 600 px
   :align: center

   MARS staggered grid structure

Vertical
===========================

The vertical coordinate of the Mars model is called :math:`\sigma`, which varies from -1 at seabed to 0 at the surface. In MARS, if the number of `T` points on the vertical is `kmax`, the number of `W` points is `kmax + 1`. For a given `T` cell located at the `k` index, the corresponding `W` point is located below. `k=0` corresponds to the bottom, while `k=kmax` corresponds to the surface.

.. figure:: _static/mars_schema_discretisation2.*
   :width: 600 px
   :align: center

   MARS grid structure.

The conversion from :math:`\sigma` to :math:`z`, using generalized :math:`\sigma` levels, is given in `Dumas (2009) <https://mars3d.ifremer.fr/docs/_static/2009_11_22_DocMARS_GB.pdf>`_ (equation 1.29):

.. math::

    z = \xi (1 + \sigma) + H_c \times [\sigma - C(\sigma)]  + H C(\sigma)

where :math:`\xi` is the free surface, :math:`H` is the bottom depth and :math:`H_c` is either the minimum depth or a shallow water depth above
which we wish to have more resolution. :math:`C` is defined as (equation 1.30):

.. math::

    C(\sigma) = (1 - \beta) \dfrac{\sinh(\theta \sigma)}{\sinh(\theta)} + \beta \dfrac{\tanh[\theta(\sigma + \frac{1}{2})]-\tanh(\frac{\theta}{2})}{2 \tanh(\frac{\theta}{2})}

However, if the :math:`H_c` variable is not found, the following formulation will be used:

.. math::

    z = \xi (1 + \sigma) + \sigma H

Note that the :math:`C(\sigma)` variable is read from the input file.
