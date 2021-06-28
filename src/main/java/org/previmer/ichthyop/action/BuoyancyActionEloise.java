/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.action;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.StageParticleLayer;

/**
 *
 * @author pverley
 */
public class BuoyancyActionEloise extends AbstractAction {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    final private static double KINEMATIC_VISCOSITY = 1.1*Math.pow(10, -2);   //[cm2/s]
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
    public static double maximumAge;
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
    private static double diameter;

    @Override
    public void loadParameters() throws Exception {
        
        particleDensity = Float.valueOf(getParameter("particle_density"));
        salinity_field = getParameter("salinity_field");
        temperature_field = getParameter("temperature_field");
        isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
        if (!isGrowth) {
            try {
                maximumAge = Double.valueOf(getParameter("age_max")) * 24.d * 3600.d;
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
        if (!isNull("density_file")) {
            String pathname = IOTools.resolveFile(getParameter("density_file"));
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

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    private void loadDensities(String csvFile) throws CsvException {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
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
            // Egg stage only
            int stage = ((StageParticleLayer) particle.getLayer(StageParticleLayer.class)).getStage();
            canApplyBuoyancy = (stage == 0);
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
           
            double time = getSimulationManager().getTimeManager().getTime();
            double dt = getSimulationManager().getTimeManager().get_dt();
            diameter = Double.valueOf(getParameter("diameter"));
            double sal = getSimulationManager().getDataset().get(salinity_field, particle.getGridCoordinates(), time).doubleValue();
            double tp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), time).doubleValue();
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), particle.getDepth() + move(diameter, sal, tp, dt)) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz});
        }
    }

    /**
     * Given the density of the water, the salinity and the temperature, the
     * function returns the vertical movement due to the bouyancy, in meter.
     * <pre>
     * We use the fall velocity from Ahrens formula to compute the vertical velocity due to buoyancy for spherical particles. 
     * John P. Ahrens, A fall velocity equation, JOURNAL OF WATERWAY, PORT, COASTAL, AND OCEAN ENGINEERING, 2000. 
     * 
     * w : vertical velocity [cm/second] due to buoyancy
     * g = acceleration of gravity [cm.s-2]
     * rho_p : egg density [g.cm-3]
     * rho_water : sea water density 
     * Delta : (rho_p-rho_water)/rho_water
     * nu : water kinematic viscosity [cm2/s]
     * 
     * A : Archimedes buoyancy index          A = (Delta * g * diameter^2)/(nu^2)
     * C1 : Coefficient for laminar flow regime     C1 = 0.055 * tanh(12 * A^(-0.59) * exp(-0.0004 * A))
     * Ct : Coefficient for turbulent flow regime   Ct = 1.06 * tanh(0.016 * A^(0.5) * exp(-120/A))
     * 
     * w = C1 * Delta * g * diameter^2 * (1/nu) + Ct * (Delta * g * diameter)^0.5
     * </pre>
     *
     * @param diameter a double, the diameter of the spherical particle [cm] 
     * @param salt a double, the sea water salinity [psu] at particle location
     * @param tp a double, the sea water temperature [celcius] at particle
     * location
     * @return a double, the vertical move of the particle [meter] dw * dt / 100
     */
    private double move(double diameter, double sal, double tp, double dt) {

        waterDensity = waterDensity(sal, tp); 
        double delta = Math.abs((particleDensity-waterDensity)/waterDensity);
        double A = delta*g*Math.pow(diameter, 3)/Math.pow(KINEMATIC_VISCOSITY,2);
        double C1 = 0.055f * Math.tanh(12*Math.pow(A, -0.59)*Math.exp(-0.0004*A));
        double C2 = 1.06f * Math.tanh(0.016*Math.pow(A, 0.5)*Math.exp(-120/A));
        double wpart =  C1 * delta * g * diameter * diameter * (1/KINEMATIC_VISCOSITY) + C2 * Math.pow(delta*g*diameter,0.5);
        double u = wpart ;
        
        if((float) particleDensity<(float) waterDensity){
            u = -u ; 
        }
        return u * dt / 100.0f;
        
    }

    /**
     * Calculates the water density according with the Unesco equation.
     *
     * @param sal a double, the sea water salinity [psu] at particle location.
     * @param tp a double, the sea water temperature [celsius] at particle
     * location
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

        R1
                = ((((C2 * tp - C3) * tp + C4) * tp - C5) * tp + C6) * tp - C7;
        R2
                = (((C8 * tp - C9) * tp + C10) * tp - C11) * tp + C12;
        R3
                = (-C13 * tp + C14) * tp - C15;

        return ((1000.d + (C1 * sal + R3 * Math.sqrt(Math.abs(sal)) + R2) * sal + R1 + DR350) / 1000.d);
    }

    public enum BuoyancyModel {

        CONSTANT_DENSITY,
        DENSITY_AS_AGE_FUNCTION;
    }
    //---------- End of class
}

