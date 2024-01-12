Orientation
#######################################

Active swimming has been implemented in Ichthyop following the work of Romain Chaput.
Three active swimming behaviours have been implemented: the rheotaxis orientation (i.e. against the current), the
cardinal orientation (i.e toward a given direction) and the reef orientation, i.e. orientation toward points of interests.

These three implementations involve the computation of a swimming velocity and direction. The former is common to all
three methods, but the directions depend on the method considered.

Swimming velocity
---------------------

The orientation processes all share common features. They both depend on a swimming velocity and random directions. The methods described
below differ on the way the random directions are drafted.

Swimming velocity is computed following :cite:`staatermanOrientationBehaviorFish2012`:

.. math::

    V = V_{hatch} + (V_{settle} - V_{hatch}) ^ {\log(age) / \log(PLD)}

with :math:`V_{hatch}` and :math:`V_{settle}` the larval velocity at hatching and settle, :math:`A` the age of the larva and
:math:`PLD` the transport duraction.

.. plot:: process/_static/plot_vel_age.py
    :align: center

Von Mises distributions
-----------------------------

Von Mises distribution is used in all three methods.The Von Mises distribution is given by:

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

    :math:`\theta = 0` is eastward, :math:`\theta = \frac{\pi}{2}` is northward, etc.

Computation of displacement
--------------------------------

Given a swimming velocity :math:`V` and a direction :math:`\theta`,
the larva displacement (in :math:`m`) is computed as follows:

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
Then, at each time step, a new angle is randomly drafted following a Von Misses distribution :math:`f(\theta, \theta_{card}, \kappa)`.

Reef orientation
--------------------

In the reef orientation method, the larva will target the closest target area (for instance coral reef).
These areas are defined in an ``XML`` zone file by a polygon and a zone-specific :math:`\kappa` parameter. The user also provides the sensory detection threshold of the larva (maximum detection distance :math:`\beta`).

If the distance between the particle and the barycenter of the closest reef (:math:`D`) is below
the detection thereshold :math:`\beta`, the larva will swim in the direction of the reef.


.. _ref_orientation:

.. plot:: process/_static/draw_circle_reef.py
    :align: center
    :caption: Reef orientation process

First, the angle of the current trajectory, :math:`\theta_{current}`, is computed by using
the particle position at the previous time step (blue point) and the actual position (red point). This is done by using
the haversine formula:

.. math::

    Y = \sin(\lambda_{dest} - \lambda_{origin}) \cos(\phi_{dest})


.. math::

    X =
    \cos(\phi_{origin})  \sin(\phi_{dest}) - \sin(\phi_{origin})  \cos(\phi_{dest}) cos(\lambda_{dest} - \lambda_{origin})


.. math::

    \theta = \arctan2 (Y, X)

with :math:`\phi_{origin}` and :math:`\lambda_{origin}` the latitude and longitude of the initial position, and
with :math:`\phi_{dest}` and :math:`\lambda_{dest}` the latitude and longitude of the target destination.

The direction toward the reef, :math:`\theta_{reef}` is computed by using the same formula on the actual position of the particle and the reef
position and the reef location (orange point).

The turning angle :math:`\theta_{turning}` is given by:

.. math::

    \theta_{turning} = \theta_{reef} - \theta_{current}

The turning angle is then ponderated by the ratio of the distance from the reef to
the detection threshold as follows:

.. math::

    \theta_{ponderated} = \left(1 - \dfrac{D}{\beta}\right) \theta_{turning}

.. math::

    \theta_{ponderated} = \left(1 - \dfrac{D}{\beta}\right) \left(\theta_{reef} - \theta_{current}\right)

Therefore, the closest to the reef, the strongest the turning angle.

Then, a random angle is picked up following a Von Mises distribution :math:`f(\theta, \theta_{ponderated}, \kappa_{reef})`