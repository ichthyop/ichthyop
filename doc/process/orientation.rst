Orientation
#######################################

Active swimming has been implemented in Ichthyop following the work of Romain Chaput.
Three active swimming behaviours have been implemented: the rheotaxis orientation (i.e. against the current), the
cardinal orientation (i.e toward a given direction) and the reef orientation, i.e. orientation toward points of interests.

These three implementations will be described, but first the commmon features are presented

Common features
---------------------

The orientation processes all share common features. They both depend on a swimming velocity and random directions. The methods described
below differ on the way the random directions are drafted.

Swimming velocity is computed following :cite:`staatermanOrientationBehaviorFish2012`:

.. math::

    V = V_{hatch} + (V_{settle} - V_{hatch}) ^ {\log(age) / \log(PLD)}

with :math:`V_{hatch}` and :math:`V_{settle}` the larval velocity at hatching and settle, :math:`A` the age of the larva and
:math:`PLD` the transport duraction.

Random directions are, in the three methods, drafted following a Von Mises distribution:

.. math::

    f(\theta, \mu, \kappa) = \dfrac
    {\exp(\kappa \cos(\theta - \mu))}
    {2 \pi I_{0}(\kappa)}

where :math:`I_{0}(\kappa)` is the modified Bessel function of the first kind of order 0,
:math:`\mu` is angle where the distribution is centerred and :math:`\kappa` is the concentration
parameter. The distribution is as follows:

.. plot:: process/_static/plot_von_misses.py
    :align: center

For computation purposes, all the Von Mises drafts performed in Ichthyop are done by using :math:`mu = 0`. The
angles are thus centerred around 0. Then, the :math:`mu` value is added.


.. note::

    The methods differ in the values of the :math:`\mu` and :math:`\kappa` parameters.

.. note::

    :math:`\theta = 0` is eastward, :math:`\theta = \frac{\pi}{2}` is northward, etc.


When the angle is drafted, the larva displacement (in :math:`m`) is computed as follows:

.. math::

    \Delta X = V \times \cos(\theta) \times \Delta t

.. math::

    \Delta Y = V \times \sin(\theta) \times \Delta t

with :math:`\Delta t` the time step. Next, the corresponding change in longitude (:math:`\lambda`) and latitude (:math:`\varphi`) is computed as follows:

.. math::

    \Delta \lambda = \dfrac{\Delta X}{111138 \times \cos{\varphi}}

.. math::

    \Delta \varphi = \dfrac{\Delta Y}{111138 }


Cardinal orientation
-------------------------

In cardinal orientation, the user provides a fixed heading :math:`\theta_{card}` and a fixed :math:`\kappa` parameter.
Then, at each time step, a new angle is randomly drafted following a Von Misses distribution :math:`f(\theta, \theta_{card}, \mu)`.
