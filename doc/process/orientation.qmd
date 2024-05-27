# Orientation

Active swimming has been implemented in Ichthyop following the work of Romain Chaput.
Three active swimming behaviours have been implemented: the rheotaxis orientation (i.e. against the current), the
cardinal orientation (i.e toward a given direction) and the reef orientation, i.e. orientation toward points of interests.

These three implementations involve the computation of a swimming velocity and direction. The former is common to all
three methods, but the directions depend on the method considered.

(ref-control)=

```{eval-rst}
.. plot:: process/_static/draw_control_trajectories.py
    :align: center
    :caption: Trajectory without orientation
```

## Swimming velocity

The orientation processes all share common features. They both depend on a swimming velocity and random directions. The methods described
below differ on the way the random directions are drafted.

Swimming velocity is computed following {cite}`staatermanOrientationBehaviorFish2012`:

$$
V = V_{hatch} + (V_{settle} - V_{hatch}) ^ {\log(age) / \log(PLD)}
$$

with $V_{hatch}$ and $V_{settle}$ the larval velocity at hatching and settle, $A$ the age of the larva and
$PLD$ the transport duraction.

```{eval-rst}
.. plot:: process/_static/plot_vel_age.py
    :align: center
```

## Von Mises distributions

Von Mises distribution is used in all three methods.The Von Mises distribution is given by:

$$
f(\theta, \mu, \kappa) = \dfrac
{\exp(\kappa \cos(\theta - \mu))}
{2 \pi I_{0}(\kappa)}
$$

where $I_{0}(\kappa)$ is the modified Bessel function of the first kind of order 0,
$\mu$ is angle where the distribution is centerred and $\kappa$ is the concentration
parameter. The distribution is as follows:

```{eval-rst}
.. plot:: process/_static/plot_von_misses.py
    :align: center
```

For computation purposes, all the Von Mises drafts performed in Ichthyop are done by using $mu = 0$. The
angles are thus centerred around 0. Then, the $mu$ value is added.

:::{.callout-note}
$\theta = 0$ is eastward, $\theta = \frac{\pi}{2}$ is northward, etc.
:::

## Computation of displacement

Given a swimming velocity $V$ and a direction $\theta$,
the larva displacement (in $m$) is computed as follows:

$$
\Delta X = V \times \cos(\theta) \times \Delta t
$$

$$
\Delta Y = V \times \sin(\theta) \times \Delta t
$$

with $\Delta t$ the time step. Next, the corresponding change in longitude ($\lambda$) and latitude ($\varphi$) is computed as follows:

$$
\Delta \lambda = \dfrac{\Delta X}{111138 \times \cos{\varphi}}
$$

$$
\Delta \varphi = \dfrac{\Delta Y}{111138 }
$$

## Cardinal orientation

In cardinal orientation, the user provides a fixed heading $\theta_{card}$ and a fixed $\kappa$ parameter.
Then, at each time step, a new angle is randomly drafted following a Von Misses distribution $f(\theta, \theta_{card}, \kappa)$.

(ref-cardinal)=

```{eval-rst}
.. plot:: process/_static/draw_card_trajectories.py
    :align: center
    :caption: Trajectory with cardinal orientation
```

## Rheotaxis orientation

In the rheotaxis orientation method, the particles swim against the current. The user only provides a kappa parameter.

First, the angle of the current is computed as follows:

$$
\theta_{current} = \arctan2(V_{current}, U_{current})
$$

Then, the angle that the particle must follow is given by adding $\pi$:

$$
\theta_{direction} = \theta_{current} + \pi
$$

Finally, a random angle is picked up following a Von Mises distribution $f(\theta, \theta_{direction}, \kappa_{reef})$

(ref-rheo)=

```{eval-rst}
.. plot:: process/_static/draw_rheo_trajectories.py
    :align: center
    :caption: Trajectory using rheotaxis orientation
```

## Reef orientation

In the reef orientation method, the larva will target the closest target area (for instance coral reef).
These areas are defined in an `XML` zone file by a polygon and a zone-specific $\kappa$ parameter. The user also provides the sensory detection threshold of the larva (maximum detection distance $\beta$).

If the distance between the particle and the barycenter of the closest reef ($D$) is below
the detection thereshold $\beta$, the larva will swim in the direction of the reef.

(ref-orientation)=

```{eval-rst}
.. plot:: process/_static/draw_circle_reef.py
    :align: center
    :caption: Reef orientation process
```

First, the angle of the current trajectory, $\theta_{actual}$, is computed by using
the particle position at the previous time step (blue point) and the actual position (red point).

$$
\Delta_X = (X_{t - 1} - X_{t})
$$

$$
\Delta_Y = (Y_{t - 1} - Y_{t})
$$

$$
\theta_{actual} = \arctan2(\Delta Y, \Delta X) + \pi
$$

The direction toward the reef, $\theta_{reef}$ is also computed.

$$
\Delta_X = (X_{reef} - X_{t})
$$

$$
\Delta_Y = (Y_{reef} - Y_{t})
$$

$$
\theta_{reef} = \arctan2(\Delta Y, \Delta X)
$$

:::{.callout-warning}
The angles are computed in the $(X, Y)$ space. Therefore, longitude and latitude coordinates
are converted in $(X, Y)$ using the `latlon2xy` Dataset methods.
:::

The turning angle $\theta_{turning}$ is given by:

$$
\theta_{turning} = \theta_{reef} - \theta_{actual}
$$

The turning angle is then ponderated by the ratio of the distance from the reef to
the detection threshold as follows:

$$
\theta_{ponderated} = \left(1 - \dfrac{D}{\beta}\right) \theta_{turning}
$$

$$
\theta_{ponderated} = \left(1 - \dfrac{D}{\beta}\right) \left(\theta_{reef} - \theta_{actual}\right)
$$

Therefore, the closest to the reef, the strongest the turning angle.

Then, a random angle is picked up following a Von Mises distribution $f(\theta, \theta_{ponderated}, \kappa_{reef})$

An example of a trajectory is provided below. In this case, two
target destinations are provided (black boxes). The same $\kappa$ value was
used for both ares (1.2) and the $\beta$ parameter has been set equal to 3 km.

(ref-orientation-2)=

```{eval-rst}
.. plot:: process/_static/draw_reef_trajectories.py
    :align: center
    :caption: Trajectory using reef orientation
```
