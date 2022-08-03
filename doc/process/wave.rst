Wave drift
##################

Ichthyop can take into account the effects of waves on the particles trajectories, following the Stokes drift equations of :cite:`stokes_2009`:

.. math:: 
    
    U_{S} = w \times k \times a^2 \times \exp\left(2 \times k \times z\right) 

In Ichthyop, the user provides zonal and meridional wave stokes drift and
the wave periods. The horizontal displacement of particles is computed as follows:

.. math:: 
    
    \|U_{wave}\| = U_{wave}^2 + V_{wave}^2
    
.. math:: 
    
    \lambda_{wave} =  \|U_{wave}\| \times T_{wave}
    
.. math:: 

    k_{wave} =  \dfrac{2 \pi}{\lambda_{wave}}
 
.. math:: 
    
    \Delta X = F \times U_{cur} \times \Delta t \times \exp\left(2 \times k_{wave} \times z\right)
    

.. math:: 
    
    \Delta Y = F \times V_{cur} \times \Delta t \times \exp\left(2 \times k_{wave} \times z\right)
    
with  :math:`U_{wave}` and :math:`V_{wave}` the zonal and meridional stokes components, :math:`T_{wave}` 
the wave period, :math:`U_{cur}` and :math:`V_{cur}` the zonal and meridional ocean current components, :math:`z`
the depth and  :math:`F` a multiplication factor provided by the user.

.. todo:: 
    
    Add references for Ichthyop use and implementation