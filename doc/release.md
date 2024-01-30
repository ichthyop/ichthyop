(release)=

# Particle release

```{eval-rst}
.. ipython:: python
    :suppress:

    import os
    import subprocess
    cwd = os.getcwd()

    fpath = "release/_static/plot_release_types.py"
    subprocess.call(["python", fpath], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

```

In the present section, the different release processes that are implemented within Ichthyop are described.
The parameters that are associated with the release processes must be included within {samp}`release` blocks
(cf. {numref}`xml-config`).

```{toctree}
:caption: 'Contents:'
:maxdepth: 1

release/stain.md
release/zone.md
release/text.md
release/patchy.md
release/bottom_surface.md
release/netcdf.md
release/schedule.md
```
