(time-config)=

# Time configuration

## Beginning of the simulation

In Ichthyop, the user provides the time at which the simulation should start. This `initial_time` parameter, which must be
defined in the `app.time` option block, must be formatted as `year YYYY month MM day DD at HH:MM`, with `YYYY` the year, `MM` the
month, `DD` the day, `HH` the hour and `MM` the minutes where the simulation should start.

## Reading NetCDF times

When reading a NetCDF file (ocean currents, temperature, wind, wave, etc.), Ichthyop will determine the units in which NetCDF time is stored.
These units must meet the [CF Metadata Conventions](https://cfconventions.org/Data/cf-conventions/cf-conventions-1.7/build/ch04s04.html)
and therefore be provided as follows:

```bash
UNITS since YYYY-MM-DD HH:MM:SS
```

with `UNITS` the units in which the time is stored (usually `seconds`, `days` or `hours`), `YYYY` the year, `MM` the month, `DD` the day, `HH` the hour, `MM` the minutes  and `SS` the seconds of the reference date.

If a NetCDF `time::units` attribute is defined, Ichthyop will try to infer the NetCDF reference date and time units using this convention.

If it fails (i.e. the `time::units` attribute does not follow the convention) or if no `time::units` attribute is found,
Ichthyop will read the `time_origin` parameter from the `app.time` option block, which must be defined following the CF conventions.

:::{danger}
When reading two datasets (an ocean currents dataset and a wind dataset for instance), if none meets the CF convention, Ichthyop
will apply the units defined in the `time_origin` parameter to both datasets, even though they may have different time units.
**Therefeore, in this case, it is strongly recommended to manually include CF-like  time units attributes to each dataset (cf. below).**
:::

Manually updating the `units` attribute can be done by using
the [ncatted](https://linux.die.net/man/1/ncatted)  command (**Linux users**):

```bash
#!/bin/bash
for file in *nc
do
    ncatted -O -a units,time,o,s,"seconds since 1900-01-01 00:00:00" $file
done
```

This can also be done by using Python as follows:

```python
from glob import glob
import xarray as xr

units = 'seconds since 1900-01-01 00:00:00'
time = 'time'

filelist = glob('*nc')

for f in filelist:

    data = xr.open_dataset(f)
    data[time].attrs['units'] = units
    data[time]

    data.to_netcdf(f)
```

:::{note}
The user must replace `time` by the name of the time variable which is used by Ichthyop.
Common values are `time`, `scrum_time`, `time_counter`, `ocean_time`.

The units value must also be chosen consistently with the dataset
:::
