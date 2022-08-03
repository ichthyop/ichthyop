Growth
######################################

There is 2 different implementations of the growth module. They can be selected by setting the 
``class_name`` parameter in the ``action.growth`` action configuration block.

Linear growth
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

To use the linear growth method described in :cite:`LETT20081210`, choose the ``LinearGrowthAction.java`` class. 

Length increment is provided as follows:

.. math:: 

    \Delta L = C_1 + C_2 \times \dfrac{F}{F + K_S} \times max(T, T_{thres}) \times \Delta t

where :math:`\Delta t` is the time-step (in days), :math:`C_1` and :math:`C_2` are parameters (``coeff1`` and ``coeff2``), :math:`T_{thres}` is a temperature threshold (``threshold_temp`` parameter), :math:`F` is the food quantity and :math:`K_S` is
a half-saturation constant (``half_saturation`` parameter). If the latter is not defined or equals 0, :math:`Q` is assumed to be :math:`1`. 

The name of the food and temperature variables are provided by the ``food_field`` and ``temperature_field`` parameters.

.. todo:: 

    Add the description of the DEB module

Sole growth
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

If the :samp:`SoleGrowthAction.java` class is selected, the growth model used in :cite:`TANNER201778` is 
used. It relies on the growth equation from :cite:`fonds1979laboratory` and given by:

.. math:: 

    \Delta L = C_1 \times T^{C_2} \times \Delta t

with :math:`L` the length, :math:`T` the temperature and :math:`\Delta t` the time step in days, and :math:`C_1` and :math:`C_2` are parameters (``c1`` and ``c2`` respectively).

The temperature field is provided by the ``temperature_field`` parameter.

.. index:: c1, c2