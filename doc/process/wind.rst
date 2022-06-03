Wind drift
#############################

The wind-drift is implemented in the `WindDriftFileAction.java` class. This class requires that 2D (time, latitude, longitude) wind fields are provided. This is done by providing a `input_path` and a `file_filter` parameter, which specify the location and names of the wind files.

The user also provides  `field_time`, `wind_u`, `wind_v`, `longitude`, `latitude` parameters, which specify the names of the time, zonal wind, meridional wind, longitude and latitude variables in the NetCDF files.

The user also provides a `depth_application` parameter, which specifies the depth at which the wind will impact the trajectories (only valid for 3D simulations), a `wind_factor` (:math:`F`, multiplication factor), an `angle` (:math:`\theta`) and a `wind_convention` parameter (:math:`\varepsilon`, +1 if convention is ocean-based, i.e. wind-to, -1 if convention is atmospheric based, i.e. wind-from).

The changes in particle longitude (:math:`\lambda`) and latitude (:math:`\phi`) is provided as follows:


.. math::

    \Delta \lambda_0 = \frac{\Delta  t \times U_W }{\frac{R \pi}{180} \times \cos(\frac{\pi \phi}{ 180})}
  
.. math:: 

    \Delta \phi_0 =  \frac{\Delta  t \times V_W}{ \frac{R \pi}{180}}


.. math::

    \Delta \lambda = \varepsilon \times F \times \left(\Delta \lambda_0 \times \cos\left(\frac{\theta \pi}{180}\right) - \Delta \phi_0 \times \sin\left(\frac{\theta \pi}{180}\right)\right)

.. math::

    \Delta \phi = \varepsilon \times F \times \left(\Delta \lambda_0 \times \sin\left(\frac{\theta \pi}{180}\right) + \Delta \phi_0 \times \sin\left(\frac{\theta \pi}{180}\right)\right)

.. note::

    :math:`R` is the Earth Radius and equals 6367.74 km. In the code, :math:`\dfrac{R \pi}{180}`, which is the distance in m of a 1 degree cell,  is  approximated to 111138 m

