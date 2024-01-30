# Ontogenetic vertical migration

The ontogenetic migration module controls the migration to different habitats as a function of age. Its implementation in Ichthyop follows the CMS one. It uses the CMS configuration file (`cms_ovm_config_file` parameter), which is formatted as follows:

```bash
nTime
nDepth
z1 z2 z3 z4 z5 z6 ... znDepth
t1 t2 t3 t4 t5 t6 ... tnTime

P1,1 P1,2 ... P1,nTime
P2,1 P2,2 ... P2,nTime
P3,1 P3,2 ... P3,nTime
          ...
PnDepth,1 PnDepth,2 ... PnDepth,nTime
```

The first line provides the number of time steps in the CMS file, the second line provides the number of vertical levels in the CMS file. The third line provides the depth values and the fourth lines provides the time step values.

The remaining lines provide the probability matrix $P_{z, t}$.

:::{note}
The sum of the probability matrix along the depth dimension should equal 100.
:::

At each time step, the index of the CMS time step, $k$, is determined by comparing the simulation and CMS times as follows:

$$
t_{CMS}(k) < t \leq t_{CMS}(k + 1)
$$

When the CMS time index changes, all the particles are randomly distributed on the vertical, following the probability distribution of the given CMS time. If the CMS time index remains unchanged, nothing is done.
