# Buoyancy

The buoyancy module allow to vertically displace a particle, depending on it's density and on the sea water density, following {cite}`parada2003modelling`.

The buoyancy-induced vertical velocity of the particle is given by:

$$
W_{buoy} =  \dfrac{1}{24} \times g \times  a \times  b  \times \dfrac{\rho_{water} - \rho_{part}}{\rho_{water}} \mu^{-1} \log\left(2 \dfrac{a}{b} + 0.5\right);
$$

with $a$ and $b$ the semi-major axis and semi-minor axis of an ellipse (`mean_major_axis` and `mean_minor_axis` parameters), $\mu$ the molecular viscosity (`molecular_viscosity` parameter), $\rho_{water}$ the water density, $\rho_{part}$ the particle density and $g$ the gravitational acceleration ($cm.s^{-2}$).

If the particle density varies with age, it can be provided in a CSV file (`density_file` parameter), formatted as follows:

```bash
Age(hour);Density (g/cm3)
0;1.0235625
24;1.02374
48;1.023335
60;1.0239
66;1.025672
```

If the `density_file` parameter is not found, a constant density will be assumed (`particle_density` parameter).

The buoyancy process is only applied for the early life stages. If the growth process is disabled, it is controlled by the `age_max` parameter (provided in days). If this parameter is not provided, the buoyancy process will always be applied.

If the growth process is enabled, the application of the buoyancy module will be automatically managed.
