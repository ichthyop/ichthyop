/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.SalinityTracker;
import org.previmer.ichthyop.io.SpecificGravityTracker;
import org.previmer.ichthyop.io.TemperatureTracker;
import org.previmer.ichthyop.particle.BuoyantParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer.Stage;
import org.previmer.ichthyop.particle.IniStageLayer;

/**
 *
 * @author pverley
 */
public class IgnacioBuoyancyAction extends AbstractAction {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    final private static double MEAN_MINOR_AXIS = 0.05f;
    final private static double MEAN_MAJOR_AXIS = 0.14f;
    final private static double LOGN = Math.log(2.f * MEAN_MAJOR_AXIS / MEAN_MINOR_AXIS);
    final private static double MOLECULAR_VISCOSITY = 0.01f; // [g/cm/s]
    final private static double g = 980.0f; // [cm/s2]
    final private static double DR350 = 28.106331f;
    final private static double C1 = 4.8314f * Math.pow(10, -4);
    final private static double C2 = 6.536332f * Math.pow(10, -9);
    final private static double C3 = 1.120083f * Math.pow(10, -6);
    final private static double C4 = 1.001685f * Math.pow(10, -4);
    final private static double C5 = 9.095290f * Math.pow(10, -3);
    final private static double C6 = 6.793952f * Math.pow(10, -2);
    final private static double C7 = 28.263737f;
    final private static double C8 = 5.3875f * Math.pow(10, -9);
    final private static double C9 = 8.2467f * Math.pow(10, -7);
    final private static double C10 = 7.6438f * Math.pow(10, -5);
    final private static double C11 = 4.0899f * Math.pow(10, -3);
    final private static double C12 = 8.24493f * Math.pow(10, -1);
    final private static double C13 = 1.6546f * Math.pow(10, -6);
    final private static double C14 = 1.0227f * Math.pow(10, -4);
    final private static double C15 = 5.72466f * Math.pow(10, -3);
    ///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Buoyuancy scheme only operates during the egg stage. But when the growth
     * of the particle is not simulated, it operates up to this age limit [day].
     */
    public static long maximumAge;
    /**
     * Egg density [g/cm3], a key parameter to calculate the egg buoyancy.
     */
    private static float particleDensity;
    /**
     * Sea water density at particle location.
     */
    private static double waterDensity;
    private String salinity_field;
    private String temperature_field;
    private boolean isGrowth;

    public void loadParameters() throws Exception {
        particleDensity = Float.valueOf(getParameter("particle_density"));
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
            double age = particle.getAge();
            double spawningTp = ((IniStageLayer) particle.getLayer(IniStageLayer.class)).getSpawingTemperature();
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), particle.getDepth() + move(age, spawningTp, sal, tp, dt)) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz});
        }
    }

    /**
     * Given the density of the water, the salinity and the temperature,
     * the function returns the vertical movement due to the bouyancy,
     * in meter.
     * <pre>
     * dw = vertical velocity [cm/second] due to buoyancy
     * g = acceleration of gravity [cm.s-2]
     * d = minor axis of the ovoid [cm]
     * l = major axis od the ovoid [cm]
     * rhoParticle = egg density [g.cm-3]
     * rhoW = sea water density
     * deltaRho = rhoParticle - rhoW
     * mu = water molecular viscosity [g.cm-1.s-1]
     * dw = 1/24 * g * d * d * deltaRho/rhoW * (1/mu) * ln(2 * l / d + ½)
     * </pre>
     *
     * @param salt a double, the sea water salinity [psu] at particle location
     * @param tp a double, the sea water temperature [celcius] at particle
     * location
     * @return a double, the vertical move of the particle [meter] dw * dt / 100
     */
    private double move(double age, double temp_ini, double sal, double tp, double dt) {

        double time_relative;
        /** tiempo relativo al D */
        double edad;
        /** para pasar el age a dias*/
        double correc_densidad;
        /**factor de correccion para la densidad del huevo)*/
        double Regner;
        /* Methodology:
        waterDensity = waterDensity(salt, temperature);
        deltaDensity = (waterDensity - eggDensity);
        quotient = (2 * MEAN_MAJOR_AXIS / MEAN_MINOR_AXIS);
        logn = Math.log(quotient);
        buoyancyEgg = (g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
        MOLECULAR_VISCOSITY * waterDensity) * (logn + 0.5f) * deltaDensity); //cms-1
        buoyancyMeters = (buoyancyEgg / 100.0f); //m.s-1
        return (buoyancyMeters * dt_sec); //meter
         */
        /**
         * Antes vamos a calcular como varia la densidad del huevo en funcion de su estado de desarrollo
         * para eso hay que calcular cual es el D segun Regner y hacer una regla de tres en funcion del estado
         * de desarrollo del huevo
         */
        Regner = (1 / 1.012896) * (1 + Math.exp((4.914322) - (0.257451 * temp_ini)));
        edad = age / 86400;
        time_relative = Math.min(100, (100 * edad) / Regner);
        correc_densidad = 1.002d + (0.000002872d * time_relative) + (0.0000021896d * time_relative * time_relative) - (0.000000060103d * time_relative * time_relative * time_relative) + (0.00000000040052d * time_relative * time_relative * time_relative * time_relative);

        double densityCorrected = (correc_densidad * particleDensity);

        waterDensity = waterDensity(sal, tp);

        return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
                * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f)
                * (waterDensity
                - densityCorrected)) / 100.0f) * dt);
    }

    /**
     * Calculates the water density according with the Unesco equation.
     *
     * @param waterSalinity a double, the sea water salinity [psu] at particle
     * location.
     * @param waterTemperature a double, the sea water temperature [celsius] at
     * particle location
     * @return double waterDensity, the sea water density [g.cm-3] at the
     * particle location.
     */
    public static double waterDensity(double sal, double tp) {

        /* Methodology
        1.Estimating water density according with Unesco equation
        S = waterSalinity;
        T = waterTemperature;
        SR = Math.sqrt(Math.abs(S));
        2. Pure water density at atmospheric pressure
        R1 = ( ( ( (C2 * T - C3) * T + C4) * T - C5) * T + C6) * T - C7;
        R2 = ( ( (C8 * T - C9) * T + C10) * T - C11) * T + C12;
        R3 = ( -C13 * T + C14) * T - C15;
        3. International one-atmosphere equation of state of water
        SIG = (C1 * S + R3 * SR + R2) * S + R1;
        4. Estimating SIGMA
        SIGMA = SIG + DR350;
        RHO1 = 1000.0f + SIGMA;
        waterDensity = (RHO1 / 1000.f); in [gr.cm-3]
         */

        double R1, R2, R3;

        R1 =
                ((((C2 * tp - C3) * tp + C4) * tp - C5) * tp + C6) * tp - C7;
        R2 =
                (((C8 * tp - C9) * tp + C10) * tp - C11) * tp + C12;
        R3 =
                (-C13 * tp + C14) * tp - C15;

        return ((1000.d + (C1 * sal + R3 * Math.sqrt(Math.abs(sal)) + R2) * sal + R1 + DR350) / 1000.d);
    }    //---------- End of class
}
