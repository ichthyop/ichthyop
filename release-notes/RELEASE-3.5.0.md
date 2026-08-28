# Release Notes — Java Changes (3.4.2 → 3.5.0)

## New Features

### New biological/behavioral processes

-   **Exponential growth model** (`ExponentialGrowthAction.java`): new growth process where length grows exponentially as a function of temperature (`L(t) = L₀ · exp(bTᶜt)`), configurable with `b`, `c`, `max_length`, `initial_length` parameters, and kills the particle with a new `LARGE` mortality cause if it exceeds `max_length`.
-   **Rafting process** (`RaftingAction.java`): moves a particle to the surface once a given age/size threshold is reached, so it can "raft" at the surface.
-   **Generic process activation/deactivation framework** (`AbstractAction.java`): every action now supports `activation_variable` (`age` or `length`), `activation_minimum_class_value`, and `activation_maximum_class_value`, letting any action be turned on/off based on particle age or length via a new `isActive(particle)` helper. This replaces several bespoke `age.min`/`age.max` implementations previously scattered across orientation actions (`CardinalOrientationAction`, `RheotaxisOrientationAction`, `ReefOrientationAction`) and is now applied consistently across `AdvectionAction`, `BitAction`, `BuoyancyAction`, `DebGrowthAction`, `DebGrowthAction_LP`, `GradientMoveAction`, `HDispAction`, `LethalSaltAction`, `LethalTempAction`, `LinearGrowthAction`, `MigrationAction`, `OntogeneticMigrationAction`, `RecruitmentStainAction`, `RecruitmentZoneAction`, `SnoozeAction`, `SoleGrowthAction`, `SwimmingAction`, `VDispAction`, `WaveDriftFileAction`, `WindAction`, `WindDriftAction`, and `WindDriftFileAction`.

### Swimming/orientation velocity now supports age, length, or CSV-based calculation

-   `SwimmingAction.java` and `OrientationVelocity.java` (used by `CardinalOrientationAction`, `RheotaxisOrientationAction`, `ReefOrientationAction`) now share a common mechanism to compute swimming speed either from a CSV file (age- or length-indexed) or from a formula based on body length (`swimming.body.length.speed`), controlled by new `swimming.speed.mode` and `swimming.speed.csv.enabled` parameters.
-   `RheotaxisOrientationAction.java`: new `can.swim.against.current` parameter to let particles swim against the current instead of being capped at current speed.
-   `ReefOrientationAction.java`: new `attraction.mode` parameter (`edges` or `barycenter`) to choose whether particles are attracted to the closest edge/vertex of a target polygon or its barycenter; internal distance/closest-point computations refactored into pluggable strategies.

### New/expanded output trackers

-   `AgeTracker.java`: new tracker recording particle age (days) in the NetCDF output.
-   `AbstractInitialStateTracker.java`, `InitialFloatTracker.java`, `InitialLatTracker.java`, `InitialLonTracker.java`: new infrastructure to record "initial state" variables (`lon_init`, `lat_init`) once per particle at release time rather than at every time step.
-   `ReleaseZoneTracker.java` now extends `AbstractInitialStateTracker` instead of `AbstractTracker`, and `ZoneRelease.java` registers it via the new `addPredefinedInitialStateTracker`.
-   `OutputManager.java`: adds support for initial-state trackers (`writeInitialStateToNetCDF`), invoked by `ReleaseManager.java` on every release event.
-   `IParticle` / `Particle.java`: new `getLength()` accessor exposed at the particle level.

### Dataset improvements

-   New `getBottomDepth(double[] pGrid)` method added to `IDataset` and implemented across dataset classes (`NemoDataset`, `Mars3dCommon`, `Roms3dCommon`, `Mercator_3D`, `NoveltisDataset`); used by `MigrationAction` to replace the old `getBathy` bounding logic.
-   2D datasets (`GlobCurrent`, `Mars2dCommon`, `Mercator2dDataset`, `NemoDataset_2D`, `OscarDataset`, `Regular2D`, `Regular2DProjected`, `Roms2dDataset`) now return `1` from `get_nz()` instead of throwing `UnsupportedOperationException`, improving compatibility of generic code paths with 2D runs.
-   `MigrationAction.java`: new optional `method=linear` mode (`getDepthLinear`) that linearly interpolates particle depth between day/night targets around sunrise/sunset instead of a step function.
-   `WaveDriftFileAction.java`: refactored variable naming (Stokes components separated from wave velocity/period), new `wave_constant_over_depth` and `depth_max` parameters, and NaN-safe handling of Stokes drift components.
-   `WindDriftFileAction.java`: added a latitude-flip patch (`shiftLat`) to correctly handle wind files whose latitude dimension is decreasing.

## Bug Fixes

-   **`DebGrowthAction_LP.java`**: fixed an incorrect unit conversion for energy conductance (`V_dot`) and corrected the structural growth flux computation (`flow_p_G`), which was previously divided by `E_G` twice.
-   **`GradientMoveAction.java`**: fixed a sign/direction bug in `Cell.direction()` and corrected the velocity unit conversion (cm/s → m/s).
-   **`LethalTempAction.java` / `LethalSaltAction.java`**: consolidated and corrected the logic for constant vs. CSV-file-based lethal temperature/salinity thresholds; previously the growth-stage-based branching could apply mismatched thresholds.
-   **`LengthStage.java`**: fixed the stage-lookup logic (`getStage`), which could return incorrect indices near stage boundaries.
-   **`AbstractStage.java`**: corrected the validation check comparing the number of stage tags vs. thresholds (now expects `tags.length == thresholds.length + 1`).
-   **`RequiredVariable.java`**: interpolation now skips `NaN` values instead of propagating them into the interpolated result.
-   **`Mercator_3D.java`**: particle grid index lookups changed from `floor` to `round` for depth/coordinate calculations.
-   **`DatasetUtil.java`**: fixed date-parsing patterns (`yyyy-MM-dd` → `yyyy-M-d`) to correctly parse single-digit month/day values in NetCDF time units.
-   **`RecruitmentStainAction.java`**: particle is now explicitly `lock()`ed upon recruitment in a stain.
-   **`VonMisesRandom.java`**: added support for a fixed random seed (via `ParameterManager` / `app.seed` block) to make simulations reproducible.

## Refactors / Internal Changes

-   `BuoyancyAction.java`: density computation refactored into pluggable functional interfaces (`getDensity`), with a new `density.method` (`constant`/`file`) and `density.class` (`age`/`length`) parameter pair replacing the old implicit CSV-file detection.
-   `LinearGrowthAction.java`: growth computation split into `growWithFood`/`growWithoutFood` strategies, gated by a new `growth.food.enabled` flag, and now takes an explicit `initial_length` parameter instead of using the first stage threshold.
-   `SoleGrowthAction.java`: also switched to an explicit `initial_length` parameter and made length/stage trackers independently toggleable.
-   `CheckGrowthParam.java`: now also recognizes `action.growth.exponential` as a valid growth module (alongside `action.growth` and `action.growthDeb`), enforcing that only one growth module is active at a time.
-   `ParticleMortality.java`: added new `LARGE` mortality cause (for the exponential growth cap).
-   Minor UI reorganizations in `IchthyopView.java` (WMS server list) and `NewConfigView.java` (generic template promoted to top of the config tree).

## Bug fix

-   Release zone output was computed at record frequency, which led sometimes to -1 values. Now it is computed just after release event (Eliot bug)
-   In the `MigrationAction.java`, positive depth values were not converted to negative ones, contrary to what is stated on the console
-   Bug in bouncing when 2D datasets are used (bug introduced in 3.4)

## New features

-   Particle age is provided as a standard output
-   Particle initial longitudes and latitudes are provided as a standard output
-   In `MigrationAction`, the depth of the particle can be linearly interpolated between sunrise and sunset. And the daytime depth is reached at the maximum of the day, and conversely for the night time depth
-   For `RheotaxisOrientationAction`, possibility to control whether particles can swim against the current or not with the `can.swim.against.current` parameter. **Default if false.**
-   Adding possibility to use speed in bodylength/seconds for orientation velocity calculation (#123).
-   In `ReefOrientationAction`, possibility to use either polygon edges or barycenters to computed distances to reefs (Celine's request)
-   Rafting process has been implemented (#114): the particle moves at the surface when reaching a certain age.
-   Exponential growth has been implemented (#115)
-   For each processes, user now can control whether it is active or not, depending on either age or length (if growth is activated).