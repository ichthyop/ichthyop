/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.arch.IParticle;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer.Stage;

/**
 *
 * @author pverley
 */
public class BuoyancyAction extends AbstractAction {

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
    private float particleDensity;
    /**
     * Sea water density at particle location.
     */
    private static double waterDensity;
    private String salinity_field;
    private String temperature_field;
    private boolean isGrowth;
    private float[] particleDensities;
    private float[] ages;
    private BuoyancyModel buoyancyModel;

    @Override
    public void loadParameters() throws Exception {
        particleDensity = Float.valueOf(getParameter("particle_density"));
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
        buoyancyModel = BuoyancyModel.CONSTANT_DENSITY;
        /*
         * Check whether there is a density CSV file
         */
        String density_file;
        try {
            density_file = getParameter("density_file");
        } catch (Exception ex) {
            density_file = null;
        }
        if (null != density_file && !density_file.isEmpty()) {
            String pathname = IOTools.resolveFile(density_file);
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("Density file " + pathname + " not found.");
            }
            if (!f.canRead()) {
                throw new IOException("Density file " + pathname + " cannot be read.");
            }
            loadDensities(pathname);
            buoyancyModel = BuoyancyModel.DENSITY_AS_AGE_FUNCTION;
        }
    }

    private void loadDensities(String csvFile) {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReader(new FileReader(csvFile), ';');
            List<String[]> lines = reader.readAll();

            // init arrays
            ages = new float[lines.size() - 1];
            particleDensities = new float[ages.length];

            // read ages (hours converted to seconds) and densities
            for (int i = 0; i < ages.length; i++) {
                String[] line = lines.get(i + 1);
                ages[i] = Float.valueOf(line[0]) * 3600.f;
                particleDensities[i] = Float.valueOf(line[1]);
            }
        } catch (IOException ex) {
            Logger.getLogger(BuoyancyAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute(IParticle particle) {

        boolean canApplyBuoyancy;
        if (isGrowth) {
            canApplyBuoyancy = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getStage() == Stage.EGG;
        } else {
            canApplyBuoyancy = particle.getAge() < maximumAge;
        }

        if (canApplyBuoyancy) {
            /*
             * For case of particle density varying with particle age, we
             * determine what is current density for the particle
             */
            if (buoyancyModel == BuoyancyModel.DENSITY_AS_AGE_FUNCTION) {
                particleDensity = particleDensities[ages.length - 1];
                float age = particle.getAge();
                for (int i = 0; i < ages.length - 1; i++) {
                    if (ages[i] <= age && age < ages[i + 1]) {
                        particleDensity = particleDensities[i];
                        break;
                    }
                }
            }
            //System.out.println("My age is " + (particle.getAge() / 3600.f) + " density: " + particleDensity);
            double time = getSimulationManager().getTimeManager().getTime();
            double dt = getSimulationManager().getTimeManager().get_dt();
            double sal = getSimulationManager().getDataset().get(salinity_field, particle.getGridCoordinates(), time).doubleValue();
            double tp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), time).doubleValue();
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), particle.getDepth() + move(sal, tp, dt)) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz});
        }
    }

    /**
     * Given the density of the water, the salinity and the temperature, the
     * function returns the vertical movement due to the bouyancy, in meter.
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
    private double move(double sal, double tp, double dt) {

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

        waterDensity = waterDensity(sal, tp);

        return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f)
                * (waterDensity - particleDensity)) / 100.0f) * dt);
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
    }

    public enum BuoyancyModel {

        CONSTANT_DENSITY,
        DENSITY_AS_AGE_FUNCTION;
    }
    //---------- End of class
}
