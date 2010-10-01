package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IAndresBuoyantParticle;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IGrowingParticle;
import org.previmer.ichthyop.arch.IGrowingParticle.Stage;
import org.previmer.ichthyop.particle.AndresBuoyantParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer;

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

        salinity_field = getParameter("salinity_field");
        temperature_field = getParameter("temperature_field");
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

    }

    public void execute(IBasicParticle particle) {

         boolean canApplyBuoyancy = false;
        if (isGrowth) {
            canApplyBuoyancy = ((IGrowingParticle) particle.getLayer(GrowingParticleLayer.class)).getStage() == Stage.EGG;
        } else {
            canApplyBuoyancy = particle.getAge() < maximumAge;
        }

        if (canApplyBuoyancy) {
            double time = getSimulationManager().getTimeManager().getTime();
            double dt = getSimulationManager().getTimeManager().get_dt();
            double sal = getSimulationManager().getDataset().get(salinity_field, particle.getGridCoordinates(), time).doubleValue();
            double tp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), time).doubleValue();
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), particle.getDepth() + move(sal, tp, dt, particle)) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz});
        }
    }

    private double move(double sal, double tp, double dt, IBasicParticle particle) {

        waterDensity = BuoyancyAction.waterDensity(sal, tp);
        int stage = ((IAndresBuoyantParticle) particle.getLayer(AndresBuoyantParticleLayer.class)).getStage();
        if (stage < 11) {
            double particleDensity = ((IAndresBuoyantParticle) particle.getLayer(AndresBuoyantParticleLayer.class)).getSpecificGravity(tp);
            return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
                    * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f)
                    * (waterDensity - particleDensity)) / 100.0f) * dt);

        } else {
            return 0.d;
        }
    }
}
