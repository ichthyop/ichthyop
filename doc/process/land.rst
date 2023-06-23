Coastal behaviour
#############################

Bouncing
+++++++++++++++++++++++

.. ipython:: python
    :suppress:

    import os
    import subprocess

    cwd = os.getcwd()
    fpath = "process/_static/plot_bouncing.py"
    subprocess.call(["python", fpath], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


In the bouncing mode, the particle will bounce on the coast. First, whether the bouncing occurs on a meridional or a zonal coastline is determined.

In case of a meridional coastline, as shown in :numref:`fig_bouncing`, the calculation of the new position is performed as follows. 

Let's assume that the particle is at the position :math:`(x, y)` and is moved at the postion :math:`(x + \Delta x, y + \Delta y)`. We suppose that 


.. math:: 
    :label: bounc_1

    \Delta y = \Delta y_1  + \Delta y_2,

where :math:`\Delta y_1` is the meridional distance between the particle and the coastline, and :math:`\Delta y_2` is the distance that the particle will spend on land.

In the bouncing mode, the position increment can be written as

.. math::
    :label: bounc_2

    \Delta_{cor} y = \Delta y_1 - \Delta y_2

By replacing :math:`\Delta y_2` using :eq:`bounc_1`, we can write:

.. math::

    \Delta_{cor} y = \Delta y_1 - (\Delta y - \Delta y_1)
    
.. math::

    \Delta_{cor} y = 2 \Delta y_1 - \Delta y
    


.. _fig_bouncing:

.. figure:: _static/bouncing.*
   :width: 500 px
   :align: center

   Coastal behaviour in bouncing mode.


    
