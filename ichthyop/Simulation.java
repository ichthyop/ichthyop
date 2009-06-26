package ichthyop;

import java.util.*;
import java.awt.*;
import ichthyop.datanc.*;
import ichthyop.util.*;
import ichthyop.util.calendar.*;
import ucar.netcdf.*;

/**
 *
 * <p>Title: Simulation</p>
 *
 * <p>Description: Controls the Population of Individuals, the processes and the timing</p>
 *
 */

public class Simulation
    implements Runnable {
// -----------------------------------------------------------------------------

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  /**
   * Duration of simulation = duration of transport + duration of releasing
   */
  private static long simulationDuration;
  /**
   * Begining of the simulation [millisecond] since origin.
   */
  private static long t0;
  /**
   * Computational time step [millisecond]
   */
  private static long dt;
  /**
   * Time step [millisecond] calculated from the refresh frequency on screen
   */
  private static long dt_refresh;
  /**
   * Duration of transport of particle
   */
  private static long transportDuration;
  /**
   * Minimum duration [millisecond] a particle has to spend in a zone before
   * being recruited.
   */
  private static long durationInRecruitArea;
  /**
   * Criteria of recruitment
   */
  private static float ageMinAtRecruitment, lengthMinAtRecruitment;
  /**
   * Depth of releasing [meter]. Positive integers.
   */
  private static float depthReleaseMin, depthReleaseMax;
  /**
   * Lethal sea water temperature for particle [celsius]
   */
  private static float lethalTpEgg, lethalTpLarvae;
  /**
   * Egg density when BUOYANCY is simulated [g/cm3]
   */
  private static float eggDensity;
  /**
   * Dimension of a patch when PATCHINESS is on.
   */
  private static float patch_radius, patch_thickness;
  /**
   * Number of releasing events when MULTIPLE RELEASING EVENT is on.
   */
  private static int nbReleaseEvents;
  /**
   * Time step [millisecond] between two releasing events when MULTIPLE
   * RELEASING EVENT is on.
   */
  private static long dt_release;

  private MainFrame frame;
  private Population population;
  public static GetData dataNC;
  private static NetcdfFile ncOut;
  private static Calendar cld;
  private static int nbSteps;
  private static int i_step;
  private static boolean blnRepaint = true;
  private static int nb_replica, i_replica;

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructor
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public Simulation(MainFrame frame) {
    this.frame = frame;
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Sets the simulation up once a file of configuration has been loaded.
   * Runs some precomputation.
   */
  public void setUp() {

    System.out.println("Preliminary computation ...");

    switch (GetConfig.getTypeModel() + GetConfig.getDimSimu()) {
      case (Resources.ROMS + Resources.SIMU_2D):
        dataNC = new GetRData2D();
        break;
      case (Resources.ROMS + Resources.SIMU_3D):
        dataNC = new GetRData3D();
        break;
      case (Resources.MARS + Resources.SIMU_2D):
        dataNC = new GetMData2D();
        break;
      case (Resources.MARS + Resources.SIMU_3D):
        dataNC = new GetMData3D();
        break;
    }

    cld = GetConfig.getTypeCalendar() == Resources.CLIMATO
        ? new ClimatoCalendar()
        : new Calendar1900(Resources.YEAR_ORIGIN, Resources.MONTH_ORIGIN,
        Resources.DAY_ORIGIN, 0, 0, 0);

    transportDuration = GetConfig.getTransportDuration();
    dt = GetConfig.get_dt();
    nb_replica = GetConfig.getNbReplica();

    dataNC.setUp();

    System.out.println("Preliminary computation [OK]");
  }

  // -----------------------------------------------------------------------------
  /**
   * Reads the value of the parameters and initiates the SINGLE simulation.
   */
  void init() {

    t0 = GetConfig.get_t0(0);
    cld.setTimeInMillis(t0);

    dt_release = GetConfig.getReleaseDt(0);
    nbReleaseEvents = GetConfig.getNbReleaseEvents(0);
    long releaseDuration = dt_release * (nbReleaseEvents - 1);
    simulationDuration = transportDuration + releaseDuration;

    if (GetConfig.getTypeRelease() == Resources.RELEASE_ZONE) {
      depthReleaseMin = GetConfig.getDepthReleaseMin(0);
      depthReleaseMax = GetConfig.getDepthReleaseMax(0);
      if (GetConfig.isPatchiness()) {
        patch_radius = GetConfig.getRadiusPatchi(0);
        patch_thickness = GetConfig.getThickPatchi(0);
      }
    }

    if (GetConfig.isLethalTp()) {
      lethalTpEgg = GetConfig.getLethalTpEgg(0);
      lethalTpLarvae = GetConfig.getLethalTpLarvae(0);
    }

    if (GetConfig.isBuoyancy()) {
      eggDensity = GetConfig.getEggDensity(0);
    }

    if (GetConfig.getTypeRecruitment() == Resources.RECRUIT_LENGTH) {
      lengthMinAtRecruitment = GetConfig.getLengthRecruitment(0);
      durationInRecruitArea = GetConfig.getDurationInRecruitArea();
    }
    else if (GetConfig.getTypeRecruitment() == Resources.RECRUIT_AGE) {
      ageMinAtRecruitment = GetConfig.getAgeRecruitment(0);
      durationInRecruitArea = GetConfig.getDurationInRecruitArea();
    }

    dt_refresh = Math.max(frame.getDtDisplay(), GetConfig.get_dt());
    nbSteps = (int) (simulationDuration / dt_refresh);
    i_step = 0;
    initZone();
    population = new Population();
    dataNC.setFirstDay(true);
    population.init();

    if (GetConfig.isRecordNc()) {
      OutputNC.createFile(0, 1, Resources.SINGLE_SIMU);
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Steps forward on time. Increments time and orders screen refresh.
   * @param time : current time.
   */
  private void stepForward(long time) {

    population.stepForward(time);
    blnRepaint = (time >= t0 + i_step * dt_refresh);
    if (i_step < nbSteps && blnRepaint) {
      System.out.println("-----  t: Y" + cld.get(cld.YEAR)
          + "M" + (cld.get(cld.MONTH) + 1)
          + " day " + cld.get(cld.DAY_OF_MONTH)
          + " " + cld.get(cld.HOUR_OF_DAY) + ":"
          + cld.get(cld.MINUTE) +
          " ----------------");
      if (!GetConfig.isSerial()) {
        population.counting();
        frame.refresh();
      }
      i_step++;
    }
    cld.setTimeInMillis(time + dt);

  }

  //----------------------------------------------------------------------------
  /**
   * Runs the SINGLE simulation.
   */
  public void run() {

    while (!frame.isStoped()) {
      long now = cld.getTimeInMillis();
      if ( (now - t0) < simulationDuration) {
        stepForward(now);
      }
      else {
        if (GetConfig.isRecordNc()) {
          population.writeLast();
          OutputNC.close();
        }
        population.clear();
        System.out.println("----- End of the Simulation ----------");
        frame.createViewver();
        frame.setFlagStop(true);
        return;
      }
    }
    population.clear();
    System.out.println("Simulation interrupted");
    System.out.println("----------------------");
    frame.createViewver();
    return;
  }

  //----------------------------------------------------------------------------
  /**
   * Runs the SERIAL simulation.
   */
  void runSerial() {

    int i_simu = 0, nb_simu;
    long now, timeCPU0;
    StringBuffer strBfConsole = new StringBuffer();

    dt_refresh = GetConfig.getDtRecord();

    initZone();

    nb_simu = GetConfig.get_t0().length * GetConfig.getLethalTpEgg().length *
        GetConfig.getLethalTpLarvae().length * GetConfig.getEggDensity().length
        * GetConfig.getDepthReleaseMax().length * nb_replica
        * GetConfig.getNbReleaseEvents().length *
        GetConfig.getRadiusPatchi().length
        * GetConfig.getAgeRecruitment().length
        * GetConfig.getLengthRecruitment().length;

    timeCPU0 = System.currentTimeMillis();
    for (int i_t0 = 0; i_t0 < GetConfig.get_t0().length; i_t0++) {
      for (int i_depth = 0; i_depth < GetConfig.getDepthReleaseMax().length;
          i_depth++) {
        for (int i_TpEgg = 0; i_TpEgg < GetConfig.getLethalTpEgg().length;
            i_TpEgg++) {
          for (int i_TpLarvae = 0;
              i_TpLarvae < GetConfig.getLethalTpLarvae().length; i_TpLarvae++) {
            for (int i_density = 0;
                i_density < GetConfig.getEggDensity().length; i_density++) {
              for (int i_length = 0;
                  i_length < GetConfig.getLengthRecruitment().length; i_length++) {
                for (int i_age = 0;
                    i_age < GetConfig.getAgeRecruitment().length; i_age++) {
                  for (int i_puls = 0;
                      i_puls < GetConfig.getNbReleaseEvents().length; i_puls++) {
                    for (int i_patch = 0;
                        i_patch < GetConfig.getRadiusPatchi().length; i_patch++) {
                      for (int i = 0; i < nb_replica; i++) {

                        strBfConsole.delete(0, strBfConsole.length());
                        strBfConsole.append("\n############## Simulation ");
                        strBfConsole.append( (i_simu + 1));
                        strBfConsole.append(" / ");
                        strBfConsole.append(nb_simu);
                        strBfConsole.append(" ############# \n");
                        strBfConsole.append("----- Current parameters ----- \n");

                        t0 = GetConfig.get_t0(i_t0);
                        cld.setTimeInMillis(t0);

                        strBfConsole.append("t0 : Y");
                        strBfConsole.append(cld.get(cld.YEAR));
                        strBfConsole.append('M');
                        strBfConsole.append(cld.get(cld.MONTH) + 1);
                        strBfConsole.append(" day ");
                        strBfConsole.append(cld.get(cld.DAY_OF_MONTH));
                        strBfConsole.append(' ');
                        strBfConsole.append(cld.get(cld.HOUR_OF_DAY));
                        strBfConsole.append(':');
                        strBfConsole.append(cld.get(cld.MINUTE));
                        strBfConsole.append('\n');

                        if (GetConfig.getTypeRelease()
                            == Resources.RELEASE_ZONE) {
                          strBfConsole.append("Release : ZONE\n");
                          depthReleaseMin = GetConfig.getDepthReleaseMin(
                              i_depth);
                          depthReleaseMax = GetConfig.getDepthReleaseMax(
                              i_depth);
                          nbReleaseEvents = GetConfig.getNbReleaseEvents(
                              i_puls);

                          strBfConsole.append("Release depth [meter]: [");
                          strBfConsole.append(depthReleaseMin);
                          strBfConsole.append(" , ");
                          strBfConsole.append(depthReleaseMax);
                          strBfConsole.append("]\n");

                          if (GetConfig.isPulsation()) {
                            dt_release = GetConfig.getReleaseDt(i_puls);
                            /*nbReleasingEvents is initialized above because
                             even if no pulsation, nbReleasingEvent must be
                             defined and takes default value = 1;
                             */
                            strBfConsole.append("Pulsation : DEFINED\n");
                            strBfConsole.append("--> Number release events : ");
                            strBfConsole.append(nbReleaseEvents);
                            strBfConsole.append(
                                "\n--> Release dt [second] : ");
                            strBfConsole.append(dt_release
                                / Resources.ONE_SECOND);
                            strBfConsole.append('\n');
                          }
                          else {
                            strBfConsole.append("Pulsation : UNDEF\n");
                          }
                          if (GetConfig.isPatchiness()) {
                            patch_radius = GetConfig.getRadiusPatchi(i_patch);
                            patch_thickness = GetConfig.getThickPatchi(i_patch);

                            strBfConsole.append("Patchiness : DEFINED\n");
                            strBfConsole.append("--> Radius patch : ");
                            strBfConsole.append(patch_radius);
                            strBfConsole.append("\n--> Thickness patch : ");
                            strBfConsole.append(patch_thickness);
                            strBfConsole.append('\n');
                          }
                          else {
                            strBfConsole.append("Patchiness : UNDEF\n");
                          }
                        }
                        else {
                          strBfConsole.append("Release : FILE\n");
                        }

                        if (GetConfig.isLethalTp()) {
                          lethalTpEgg = GetConfig.getLethalTpEgg(i_TpEgg);
                          if (GetConfig.isGrowth()) {
                            lethalTpLarvae = GetConfig.getLethalTpLarvae(
                                i_TpLarvae);
                          }
                          strBfConsole.append("Lethal temperature : DEFINED\n");
                          if (GetConfig.isGrowth()) {
                            strBfConsole.append(
                                "--> Tp for egg [celsius] : ");
                            strBfConsole.append(lethalTpEgg);
                            strBfConsole.append(
                                "\n--> Tp for larva [celsius] : ");
                            strBfConsole.append(lethalTpLarvae);
                          }
                          else {
                            strBfConsole.append("--> Lethal tp [celsius] : ");
                            strBfConsole.append(lethalTpEgg);
                          }
                          strBfConsole.append('\n');
                        }
                        else {
                          strBfConsole.append("Lethal temperature : UNDEF\n");
                        }
                        if (GetConfig.isBuoyancy()) {
                          eggDensity = GetConfig.getEggDensity(i_density);

                          strBfConsole.append("Buoyancy : DEFINED\n");
                          strBfConsole.append("Egg density [g.cm-3] : ");
                          strBfConsole.append(eggDensity);
                          strBfConsole.append('\n');
                        }
                        else {
                          strBfConsole.append("Buoyancy : UNDEF\n");
                        }
                        if (GetConfig.getTypeRecruitment()
                            != Resources.RECRUIT_NONE) {
                          strBfConsole.append("Recruitment : DEFINED\n");
                          switch (GetConfig.getTypeRecruitment()) {
                            case Resources.RECRUIT_AGE:
                              ageMinAtRecruitment = GetConfig.getAgeRecruitment(
                                  i_age);
                              strBfConsole.append(
                                  "Age min at recruitment [day] : ");
                              strBfConsole.append(ageMinAtRecruitment);
                              break;
                            case Resources.RECRUIT_LENGTH:
                              lengthMinAtRecruitment = GetConfig.
                                  getLengthRecruitment(i_length);
                              strBfConsole.append(
                                  "Length min at recruitment [millimeter] : ");
                              strBfConsole.append(lengthMinAtRecruitment);
                              break;
                          }
                          strBfConsole.append('\n');
                          durationInRecruitArea = GetConfig.
                              getDurationInRecruitArea();
                        }
                        else {
                          strBfConsole.append("Recruitment : UNDEF\n");
                        }
                        i_replica = i;
                        strBfConsole.append("Replica : ");
                        strBfConsole.append(i_replica);
                        strBfConsole.append('\n');
                        strBfConsole.append("------------------------------");

                        System.out.println(strBfConsole.toString());

                        long releasingDuration = 0L;
                        simulationDuration = transportDuration
                            + releasingDuration;
                        nbSteps = GetConfig.isRecord()
                            ? (int) (simulationDuration / dt_refresh)
                            : (int) (simulationDuration / dt);
                        i_step = 0;
                        dataNC.setFirstDay(true);
                        population = new Population();
                        population.init();
                        if (GetConfig.isRecordNc()) {
                          OutputNC.createFile(i_simu, nb_simu,
                              Resources.SERIAL_SIMU);
                        }
                        //-----------------------------------------
                        while ( (now = cld.getTimeInMillis())
                            - t0 < simulationDuration) {
                          stepForward(now);
                        }
                        if (GetConfig.isRecordNc()) {
                          population.writeLast();
                        }
                        //-----------------------------------------
                        OutputNC.close();
                        i_simu++;

                        //----- Estimation of time left
                        long nbMilliSec = System.currentTimeMillis() - timeCPU0;
                        long nbMilliSecLeft = (nbMilliSec * (long) (nb_simu
                            - i_simu)) / (long) i_simu;
                        int nbDayLeft = (int) (nbMilliSecLeft
                            / Resources.ONE_DAY);
                        int nbHourLeft = (int) ( (nbMilliSecLeft
                            - Resources.ONE_DAY * (long) nbDayLeft)
                            / Resources.ONE_HOUR);
                        int nbMinLeft = (int) ( (nbMilliSecLeft
                            - Resources.ONE_DAY * (long) nbDayLeft
                            - Resources.ONE_HOUR * (long) nbHourLeft)
                            / Resources.ONE_MINUTE);
                        System.out.println("\n" + "Time Left = " + nbDayLeft
                            + "d "
                            + nbHourLeft + "h "
                            + nbMinLeft + "min");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    System.out.println("----- End of the Simulation ----------");
  }

  //----------------------------------------------------------------------------
  /**
   * Initializes the zone of releasing and recruitment.
   */
  private void initZone() {

    Zone znTmp;
    Iterator iter = GetConfig.getReleaseZones().iterator();
    while (iter.hasNext()) {
      znTmp = (Zone) iter.next();
      znTmp.geo2Grid();
    }
    iter = GetConfig.getRecruitmentZones().iterator();
    while (iter.hasNext()) {
      znTmp = (Zone) iter.next();
      znTmp.geo2Grid();
    }

  }

  //----------------------------------------------------------------------------
  /**
   * Called to display simulation on screen
   *
   * @param g Graphics
   * @param w int
   * @param h int
   */
  public void draw(Graphics g, int w, int h) {

    if (population != null) {
      population.draw(g, w, h);
    }
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the getters
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

//----------------------------------------------------------------------------
  public boolean getBlnRepaint() {
    return blnRepaint;
  }

  //----------------------------------------------------------------------------
  public void setBlnRepaint(boolean bln) {
    blnRepaint = bln;
  }

  //----------------------------------------------------------------------------
  public static long getTransportDuration() {
    return transportDuration;
  }

  //----------------------------------------------------------------------------
  public static long get_time() {
    return cld.getTimeInMillis();
  }

  //----------------------------------------------------------------------------
  public static Calendar getCalendar() {
    return cld;
  }

  //----------------------------------------------------------------------------
  public static float getLengthMinAtRecruitment() {
    return lengthMinAtRecruitment;
  }

  //----------------------------------------------------------------------------
  public static float getAgeMinAtRecruitment() {
    return ageMinAtRecruitment;
  }

  //----------------------------------------------------------------------------
  public static long getSimulationDuration() {
    return simulationDuration;
  }

  //----------------------------------------------------------------------------
  public static long get_t0() {
    return t0;
  }

  //----------------------------------------------------------------------------
  public static int getNbSteps() {
    return nbSteps;
  }

  //----------------------------------------------------------------------------
  public static int get_iStep() {
    return i_step;
  }

  //----------------------------------------------------------------------------
  public static long get_dt() {
    return dt;
  }

  //----------------------------------------------------------------------------
  public static long get_dtDisplay() {
    return dt_refresh;
  }

  //----------------------------------------------------------------------------
  public static long getDurationInRecruitArea() {
    return durationInRecruitArea;
  }

  //----------------------------------------------------------------------------
  public static float getEggDensity() {
    return eggDensity;
  }

  //----------------------------------------------------------------------------
  public static double getLethalTpEgg() {
    return lethalTpEgg; }

  public static double getLethalTpLarvae() {
    return lethalTpLarvae;
  }

  //----------------------------------------------------------------------------
  public static double getDepthReleaseMin() {
    return depthReleaseMin; }

  //----------------------------------------------------------------------------
  public static double getDepthReleaseMax() {
    return depthReleaseMax; }

  //----------------------------------------------------------------------------
  public static int getNbReleaseEvents() {
    return nbReleaseEvents;
  }

  //----------------------------------------------------------------------------
  public static long getReleaseDt() {
    return dt_release;
  }

  //----------------------------------------------------------------------------
  public static int getReplica() {
    return i_replica;
  }

  //----------------------------------------------------------------------------
  public static float getThickPatchi() {
    return patch_thickness;
  }

  //----------------------------------------------------------------------------
  public static float getRadiusPatchi() {
    return patch_radius;
  }

  //----------------------------------------------------------------------------
  public static NetcdfFile getNcOut() {
    return ncOut;
  }

//------------------------------------------------------------------------------
// End of class
}
