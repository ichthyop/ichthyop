package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.SalinityTracker;
import org.previmer.ichthyop.io.SpecificGravityTracker;
import org.previmer.ichthyop.io.TemperatureTracker;
import org.previmer.ichthyop.io.WaterDensityTracker;
import org.previmer.ichthyop.particle.BuoyantParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer.Stage;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class AndresBuoyancyAction extends AbstractAction {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    final private static double MEAN_MINOR_AXIS = 0.05f;
    final private static double MEAN_MAJOR_AXIS = 0.14f;
    final private static double LOGN = Math.log(2.f * MEAN_MAJOR_AXIS / MEAN_MINOR_AXIS);
    final private static double MOLECULAR_VISCOSITY = 0.01f; // [g/cm/s]
    final private static double g = 980.0f; // [cm/s2]
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Buoyuancy scheme only operates during the egg stage. But when the growth
     * of the particle is not simulated, it operates up to this age limit [day].
     */
    public static long maximumAge;
    /**
     * Sea water density at particle location.
     */
    private static double waterDensity;
    private String salinity_field;
    private String temperature_field;
    private boolean isGrowth;

    public void loadParameters() throws Exception {

        salinity_field = getSimulationManager().getParameterManager().getParameter(BlockType.OPTION, "option.biology_dataset", "salinity_field");
        temperature_field = getSimulationManager().getParameterManager().getParameter(BlockType.OPTION, "option.biology_dataset", "temperature_field");
        isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
        if (!isGrowth) {
            try {
                maximumAge = (long) (Float.valueOf(getParameter("age_max")) * 24.f * 3600.f);
            } catch (Exception ex) {
                maximumAge = getSimulationManager().getTimeManager().getTransportDuration();
                getLogger().warning("{Buoyancy} Could not find parameter buyancy maximum age in configuration file ==> application assumes maximum age = transport duration.");
            }
        }
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        getSimulationManager().getDataset().requireVariable(salinity_field, getClass());

        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("water_density_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(WaterDensityTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("specific_gravity_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SpecificGravityTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("temperature_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(TemperatureTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("salinity_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SalinityTracker.class);
        }


    }

    public void execute(IBasicParticle particle) {

        boolean canApplyBuoyancy = false;
        if (isGrowth) {
            canApplyBuoyancy = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getStage() == Stage.EGG;
        } else {
            canApplyBuoyancy = particle.getAge() < maximumAge;
        }

        if (canApplyBuoyancy) {
            double time = getSimulationManager().getTimeManager().getTime();
            double dt = getSimulationManager().getTimeManager().get_dt();
            double sal = getSimulationManager().getDataset().get(salinity_field, particle.getGridCoordinates(), time).doubleValue();
            double tp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), time).doubleValue();
            double move = move(sal, tp, dt, particle);
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), particle.getDepth() + move) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz});
        }
    }

    private double move(double sal, double tp, double dt, IBasicParticle particle) {

        waterDensity = BuoyancyAction.waterDensity(sal, tp);
        ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).updateRatioStage(tp, sal, waterDensity);
        int stage = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getEggStage();
        if (stage < 10) {
            double particleDensity = ((BuoyantParticleLayer) particle.getLayer(BuoyantParticleLayer.class)).computeSpecificGravity(tp, sal, waterDensity, particle);
            return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
                    * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f)
                    * (waterDensity - particleDensity)) / 100.0f) * dt);

        } else {
            return 0.d;
        }
    }
}
