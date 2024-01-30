# Lethal temperature and salinity

In Ichthyop, there is the possibility to define a range of temperature and salinity beyond which the particle is killed.

## Lethal temperature

The functionning og the lethal temperature module depends on whether the growth module is enabled or not.

### Growth disabled

lethal temperature can either be provided in a CSV file (`lethal_temp_file` parameter), which provides the lower and upper temperature values that can be supported by the particle as a function of age (in hours). The file must have the following format:

```bash
Time(hour);Cold temperature (C);Warm temperature (C)
0;14;22
48;13;22
96;12;22
```

In this case, three age classes will be considered: $[0, 48[$, $[48, 96[$ and $[96, \infty[$

If the `lethal_temp_file` parameter is not defined, single values will be used independently of the age. These values are provided in the `cold_lethal_salinity_egg` and `warm_lethal_salinity_egg` parameters.

Note that the `temperature_field` parameter, providing the name of the temperature variable, must also be provided.

```{index} lethal_temp_file, temperature_field, lethal_temp_file, cold_lethal_salinity_egg, warm_lethal_salinity_egg
```

### Growth enabled

If the growth module is enabled, two cold and warm lethal temperatures must be provided. One for eggs (`cold_lethal_temperature_egg` and `hot_lethal_temperature_egg`), one for larva (`cold_lethal_temperature_larva` and `hot_lethal_temperature_larva`).

The stage (`egg` or `larva`) is determined by the growth module, and the right temperature range is applied to the particle.

```{index} cold_lethal_temperature_egg, hot_lethal_temperature_egg, cold_lethal_temperature_larva, hot_lethal_temperature_larva
```

## Lethal salinity

Lethal salinity can either be provided in a CSV file (`lethal_salt_file` parameter), which provides the lower and upper salinity values that can be supported by the particle as a function of age (in hours). The file must have the following format:

```bash
Time(hour);Fresh salinity (PSU);Salty salinity (PSU)
0;35;40
48;30;40
96;30;45
```

In this case, three age classes will be considered: $[0, 48[$, $[48, 96[$ and $[96, \infty[$

If the `lethal_salt_file` parameter is not defined, single values will be used independently of the age. These values are provided in the `fresh_lethal_salinity_egg` and `saline_lethal_salinity_egg` parameters.

Note that the `salinity_field_` parameter, providing the name of the salinity variable, must also be provided.

```{index} lethal_salt_file, salinity_field, fresh_lethal_salinity_egg, saline_lethal_salinity_egg
```
