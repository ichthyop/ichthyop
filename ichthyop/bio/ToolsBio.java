package ichthyop.bio;

import ichthyop.util.Resources;
import ichthyop.GetConfig;
import ichthyop.*;

public class ToolsBio {

  /*
   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
     %  Growth
   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
   */
  final public static int EGG = 0;
  final public static int YOLK_SAC_LARVA = 1;
  final public static int FEEDING_LARVA = 2;

  final private static double TP_THRESHOLD = 10.d; //°C
  final public static double LENGTH_INIT = 0.025d; // mm
  /**
   * Threshold to distinguish eggs from larvae
   */
  final public static double HATCH_LENGTH = 2.8d; //mm
  /**
   * Threshold between Yolk-Sac Larvae and Feeding Larvae
   */
  final private static double YOLK_TO_FEEDING_LENGTH = 4.5d; //mm

  private static boolean isDeadCold;
  private static int stage;
  private static double length;
  static double lethalTpEgg, lethalTpLarvae;
  private static double dt_day;
  private static double dt_sec;
  private static boolean FLAG_LETHAL_TP;

  //----------------------------------------------------------------------------
  public static void initGrowth() {
    FLAG_LETHAL_TP = GetConfig.isLethalTp();
    if (FLAG_LETHAL_TP) {
      lethalTpEgg = Simulation.getLethalTpEgg();
      lethalTpLarvae = Simulation.getLethalTpLarvae();
    }
    isDeadCold = false;
    dt_day = (double) GetConfig.get_dt() / (double) Resources.ONE_DAY;

  }

  //----------------------------------------------------------------------------
  public static double grow(double length, double temperature) {

    stage = getStage(length);
    isDeadCold = FLAG_LETHAL_TP
        ? ( (stage == EGG) && (temperature < lethalTpEgg))
        || ( (stage > EGG) && (temperature < lethalTpLarvae))
        : false;
    if (!isDeadCold) {
      length += (.02d + .03d * Math.max(temperature, TP_THRESHOLD)) * dt_day;
      return length;
    }
    return length;
  }

  //----------------------------------------------------------------------------
  public static int getStage(double length) {

    // Yolk-Sac Larvae
    if (length >= HATCH_LENGTH & length < YOLK_TO_FEEDING_LENGTH) {
      return YOLK_SAC_LARVA;
    }
    // Feeding Larvae
    else if (length >= YOLK_TO_FEEDING_LENGTH) {
      return FEEDING_LARVA;
    }
    //eggs
    return EGG;
  }

  //----------------------------------------------------------------------------
  public static boolean isDeadCold() {
    return isDeadCold;
  }

  //----------------------------------------------------------------------------

  /*
   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %  Buoyancy
   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
   */
  final private static double MEAN_MINOR_AXIS = 0.05f;
  final private static double MEAN_MAJOR_AXIS = 0.14f;
  final private static double LOGN = Math.log(2.f * MEAN_MAJOR_AXIS
      / MEAN_MINOR_AXIS);
  ;
  final private static double MOLECULAR_VISCOSITY = 0.01f; //gcm-1s-1
  final private static double g = 980.0f; //units (cms-2)
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

  public static long age_lim_buoy;
  private static float eggDensity;
  private static double R1, R2, R3;
  private static double waterDensity;

  //----------------------------------------------------------------------------
  public static void initBuoy() {
    if (!GetConfig.isGrowth()) {
      age_lim_buoy = GetConfig.getAgeLimit();
    }
    eggDensity = Simulation.getEggDensity();
    dt_sec = GetConfig.get_dt() / Resources.ONE_SECOND;
  }

  // -----------------------------------------------------------------------------
  /**
   * Given the density of the water, the salinity and the temperature,
   * the routine returns the vertical movement due to the bouyancy, in meter per second.
   *
   * @param density double
   * @param salt double
   * @param temp double
   * @return double
   */

  public static double addBuoyancy(double sal, double tp) {

    /* Methodology
         waterDensity = waterDensity(salt, temperature);
         deltaDensity = (waterDensity - eggDensity);
         quotient = (2 * MEAN_MAJOR_AXIS / MEAN_MINOR_AXIS);
         logn = Math.log(quotient);
           buoyancyEgg = (g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
     * MOLECULAR_VISCOSITY * waterDensity) * (logn + 0.5f) * deltaDensity); //cms-1
         buoyancyMeters = (buoyancyEgg / 100.0f); //m.s-1
         return (buoyancyMeters * dt_sec); //meter
     */

    waterDensity = waterDensity(sal, tp);
    return ( ( (g * MEAN_MINOR_AXIS * MEAN_MINOR_AXIS / (24.0f
        * MOLECULAR_VISCOSITY * waterDensity) * (LOGN + 0.5f) * (waterDensity
        - eggDensity)) / 100.0f) * dt_sec); //meter
  }

//------------------------------------------------------------------------------
  /**
   * Calculate the water density according with the Unesco equation.
   *
   * @param waterSalinity double
   * @param waterTemperature double
   * @return double waterDensity
   */

  //----------------------------------------------------------------------------
  private static double waterDensity(double sal, double tp) {

    /* Methodology
         1. Estimating water density according with Unesco equation
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
    R1 = ( ( ( (C2 * tp - C3) * tp + C4) * tp - C5) * tp + C6) * tp - C7;
    R2 = ( ( (C8 * tp - C9) * tp + C10) * tp - C11) * tp + C12;
    R3 = ( -C13 * tp + C14) * tp - C15;
    return ( (1000.d + (C1 * sal + R3 * Math.sqrt(Math.abs(sal)) + R2) * sal
        + R1 + DR350) / 1000.d);
  }

}
