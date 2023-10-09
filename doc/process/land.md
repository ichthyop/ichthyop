# Coastal behaviour

## Bouncing

```{eval-rst}
.. ipython:: python
    :suppress:

    import os
    import subprocess

    cwd = os.getcwd()
    fpath = "process/_static/plot_bouncing.py"
    subprocess.call(["python", fpath], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

```

In the bouncing mode, the particle will bounce on the coast. First, whether the bouncing occurs on a meridional or a zonal coastline is determined.

In case of a meridional coastline, as shown in {numref}`fig_bouncing`, the calculation of the new position is performed as follows.

Let's assume that the particle is at the position $(x, y)$ and is moved at the postion $(x + \Delta x, y + \Delta y)$. We suppose that

$$
\Delta y = \Delta y_1  + \Delta y_2,
$$ (bounc_1)

where $\Delta y_1$ is the meridional distance between the particle and the coastline, and $\Delta y_2$ is the distance that the particle will spend on land.

In the bouncing mode, the position increment can be written as

$$
\Delta_{cor} y = \Delta y_1 - \Delta y_2
$$ (bounc_2)

By replacing $\Delta y_2$ using {eq}`bounc_1`, we can write:

$$
\Delta_{cor} y = \Delta y_1 - (\Delta y - \Delta y_1)
$$

$$
\Delta_{cor} y = 2 \Delta y_1 - \Delta y
$$

(fig-bouncing)=

:::{figure} _static/bouncing.*
:align: center
:width: 500 px

Coastal behaviour in bouncing mode.
:::
