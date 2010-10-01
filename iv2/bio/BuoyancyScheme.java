package ichthyop.bio;

import ichthyop.core.Simulation;
import ichthyop.io.Configuration;

/**
 * <p>This class simulates a buoyancy scheme. The buoyancy only operates during
 * the egg stage. If the growth of the particles is not simulated, it
 * operates up to  and "age limit" defined by the user in the file of
 * configuration. The egg  density is a key parameter in the calculation of
 * the buoyancy. It must be  defined by the user in the file of configuration
 * when buoyancy is simulated.</p>
 *
 * In the buoyancy scheme, the particle is represented by an ovoid. The minor
 * axis is set to 0.1 centimeter and the major axis takes the value 0.28
 * centimeter. Theses values are predefined in the code of the class,
 * in the section "Declaration of constants". It is possible to modify
 * these values but it then requires a recompilation of the file.
 * The vertical velocity is calculated using the Denny (1993) equations. These
 * equations integrate the force that is required to move the ovoid in the
 * direction of the major axis. The vertical velocity is therefore function of
 * the acceleration of gravity, the sea water density, the sea water viscosity,
 * the length of both minor and major axis ans the particle density.
 *
 * <p>For details see Parada C, van der Lingen CD, Mullon C, Penven P (2003)
 * Modelling the effect of buoyancy on the transport of anchovy
 * (Engraulis capensis) eggs from spawning to nursery grounds in the
 * southern Benguela: an IBM approach. Fish Oceanogr 12:170-184.</p>
 *
 * @author P.Verley
 */
public class BuoyancyScheme {

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
    //Include for A. Ospina
    final private static double alfaK = 42922.0767d;
    final private static double betaK = 2.290236d;

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Model time step [second]
     */
    private static double dt;
    /**
     * Buoyuancy scheme only operates during the egg stage. But when the growth
     * of the particle is not simulated, it operates up to this age limit [day].
     */
    public static long age_lim_buoy;
    /**
     * Egg density [g/cm3], a key parameter to calculate the egg buoyancy.
     */
    private static double eggDensity;
    /**
     * Sea water density at particle location.
     */
    private static double waterDensity;
    ////////////////////////////
// for Andres Ospina-Alvarez
////////////////////////////
    /**
     * Age limit for Buoyancy scheme (A. Ospina)
     */
    public static double age_lim_buoy_mov;
    /**
     * Egg stage at particle location not corrected.
     */
    private static double stage;
    /**
     * Egg stage at particle location corrected.
     */
    private static double stageC;
    /**
     * DevK constant.
     */
    private static double DevK;
    /**
     * Time of egg hatch
     */
    private static double timeHatch;
    /**
     * Day of egg hatch
     */
    private static double dayHatch;
    /**
     * Hatching density minus spwaning density
     */
    private static double rangeDens;
    /**
     * Hatching depth for egg, value choiced for users
     */
    public final static double HATCHING_DEPTH = -25;
    
////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Reads some parameters in the configuration file:
     * <ul>
     * <li>time step dt
     * <li>egg density
     * <li>age limit for buoyancy in case the growth is not simulated
     * </ul>
     */
    public static void init() {

        if (!Configuration.isGrowth()) {
            age_lim_buoy = Configuration.getBuoyAgeLimit();
        }
        eggDensity = Simulation.getEggDensity();
        dt = Configuration.get_dt();
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
    public static double move(double sal, double tp) {

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

        return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f) *
                (waterDensity - eggDensity)) / 100.0f) * dt);
    }

    /**
     * For Andres OSPINA
     * Buoyancy as a function a salinity, temperature and particle age.
     * 
     */
    public static double move(double sal, double tp, double age) {

        waterDensity = waterDensity(sal, tp);

        return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f) *
                (waterDensity - getEggDensity(age))) / 100.0f) * dt);
    }

    public static double move(double sal, double spawning_density, double hatching_density, double age, double tp) {

        waterDensity = waterDensity(sal, tp);
        age_lim_buoy_mov = 3600 * alfaK * Math.pow(tp, -betaK);
        if (age <= age_lim_buoy_mov) {
            return (g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
                    * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f)
                    * (waterDensity - getEggDensity(spawning_density,
                    hatching_density, age, tp) / 100.0f) * dt);
        } else {
            return 0;
        }

    }

    private static double getEggDensity(double age) {

        double spec_gravity = 0.d;
        double age_in_day = age / (24 * 3600);
        //System.out.println("age day: " + age_in_day);

        spec_gravity = age_in_day > age_lim_buoy
                ? eggDensity
                : 1.0539d * Math.exp(-0.0018d * age_in_day);
        
        //System.out.println(spec_gravity);

        return spec_gravity;
    }

    private static double getEggDensity(double spawning_density, double hatching_density, double age, double tp) {

        double age_in_hour = age / 3600;

        DevK = age_in_hour * Math.pow(tp, betaK);

        timeHatch = alfaK * Math.pow(tp, -betaK);
        dayHatch = timeHatch / 24;

        rangeDens = hatching_density - spawning_density;

        stage = 0.000000000000235 * Math.pow(DevK, 3) - 0.0000000132 * Math.pow(DevK, 2)
                + 0.0003605 * DevK + 0.6278;
        stageC = stage + 1.4;

        eggDensity = (1.3977 * Math.pow(10, -25) * (2.3738 * Math.pow(10, 20)
                * Math.pow(stageC, 6) - 9.3745 * Math.pow(10, 21) * Math.pow(stageC, 5)
                + 1.3323 * Math.pow(10, 23) * Math.pow(stageC, 4) - 7.9280
                * Math.pow(10, 23) * Math.pow(stageC, 3) + 1.5734 * Math.pow(10, 24)
                * Math.pow(stageC, 2) + 9.8733 * Math.pow(10, 23) * stageC - 5.6919
                * Math.pow(10, 22))) * (rangeDens) + spawning_density;

        System.out.println("spawning density     " + (float) spawning_density);
        System.out.println("hatching density     " + (float) hatching_density);
        System.out.println("egg density:         " + (float) eggDensity);
        System.out.println("days for Hatch:      " + (float) dayHatch * 24.0f);
        //System.out.println("Stage: " + (float) stage);
        System.out.println("Age in hours:        " + (float) age_in_hour);
        //System.out.println("Temperature: " + (float) tp);
        System.out.println("-------------------------------------------");
        return (eggDensity);
    }
    
    /**
     * For Andres OSPINA
     * Compute Egg denstity as function of the spawning density
     */
    /*private static double getEggDensity(double spawning_density) {

        double egg_density = 0.d;
        egg_density = ax + bx + spawning_density; 
        return egg_density;
    }*/
    
    /**
     * For Andres OSPINA
     * Buoyancy as a function a salinity, temperature and particle spawning density.
     * 
     */
    /*public static double move(double sal, double tp, double spawning_density) {

        waterDensity = waterDensity(sal, tp);

        return (((g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f) *
                (waterDensity - getEggDensity(spawning_density))) / 100.0f) * dt);
    }*/

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
         
        double  R1, R2, R3;

        R1 = ((((C2 * tp - C3) * tp + C4) * tp - C5) * tp + C6) * tp - C7;
        R2 = (((C8 * tp - C9) * tp + C10) * tp - C11) * tp + C12;
        R3 = ( -C13 * tp + C14) * tp - C15;

        return ((1000.d + (C1 * sal + R3 * Math.sqrt(Math.abs(sal)) + R2) * sal + R1 + DR350) / 1000.d);
    }    //---------- End of class
}
