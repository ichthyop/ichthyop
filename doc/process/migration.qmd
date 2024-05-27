# Daily vertical migration

The `MigrationAction.java` manages the daily migration of the particles. It is done by providing daytime and nighttime depths, and the timing of the sunset and sunrise.

If the `daytime_depth_file` parameter is defined, it provides the depth of the particle during daytime. It is formatted as follows:

```bash
Age (day);Depth (m)
0.0;-20
3.0;-25
5.0;-30
8.0;-35
```

If this parameter is not found, a constant daytime depth, provided by the `daytime_depth` parameter, is assumed.

Same thing for the nighttime depths, which can be set by either the `nighttime_depth_file` or the `nighttime_depth` parameters.

The sunset and sunrise hours are set by the `sunset` and `sunrise` parameters, which must have a `HH::mm` format.

If the growth module is deactivated, the user must provide the minimum age (in days) at which the particle starts to migrate (`age_min` parameter). If the growth module is activated, it manages the activation or deactivation of the daily migration.

:::{.callout-warning}
When the target depth is greater than the total depth, the particle does not move.
:::
