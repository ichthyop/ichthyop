Random swimming
#######################################

Ichthyop allows particles to randomly swim. This is achieved by using the 
``SwimmingAction.java``. The velocity may vary with the age of the particle.

The user provides a CSV absolute velocity file (``velocity_file`` parameter), which must me formatted as follows:

.. code:: 
    
    Age (days);Speed (m/s)
    0;0.1
    5;0.2
    15;0.3

The user also provides a boolean parameter (``constant_velocity``) that specifies whether the velocity should remain as defined in the CSV file, 
or if the absolute velocity should be randomly selected as follows:

.. math:: 
    
   \|U\| = \|U\|_{file} \times \kappa

with :math:`\kappa` a random value in the :math:`[0, 2]` interval and :math:`\|U\|_{file}` the velocity defined in the CSV file.

At each time-step, the zonal and meridonal velocities are defined as follows:

.. math::
    
    U = \kappa' \varepsilon  \|U\| 
    
.. math::
    
    V = \varepsilon' \sqrt{\|U\|^2 - U^2}
    
with :math:`\kappa'` a random value between 0 and 1, and 
:math:`\varepsilon` and :math:`\varepsilon'` random values either equal
to 1 or -1.