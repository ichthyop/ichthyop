package ichthyop;

import java.util.*;

import java.awt.*;

import ichthyop.bio.*;
import ichthyop.datanc.*;
import ichthyop.util.*;
import ucar.multiarray.*;

public abstract class Particle
    extends RhoPoint {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  /**
   * Age of particle [millisecond]
   */
  long age;
  /**
   * Number of releasing zone.
   */
  int numInitZone;
  /**
   * Number of current zone.
   */
  private int numCurrentZone;
  /**
   * Number of zone where the particle has been recruited.
   */
  private int numRecruitZone;
  /**
   * Recruitment status for each rectuiment zone.
   */
  private boolean[] isRecruited;
  /**
   * Minimum duration [millisecond] a particle has to spend within the same
   * before being recruited.
   */
  private static long durationMinInRecruitArea;
  /**
   * Duration [millisecond] presently spent by the particule within the
   * current zone.
   */
  private long timeInZone;
  /**
   * length of the particle [millimeter]
   */
  double length;
  /**
   * Particle is alive or not.
   */
  boolean living;
  /**
   * old = TRUE whenever particle has been advected more than the duration of
   * transport.
   */
  private boolean old;
  /**
   * Information about particle death.
   * <pre>
   * DEAD_NOT = 0 --> alive
   * DEAD_OUT = 1 --> out of the domain of simulation
   * DEAD_COLD = 2 --> found lethal sea water temperature
   * DEAD_BEACH = 3 --> beached particle
   * </pre>
   */
  int dead;
  /**
   * Sea water temperature at particle location.
   */
  private double temperature;
  /**
   * Sea water salinity at particle location.
   */
  private double salinity;

  private static Color color;
  int indexIndiv;
  final static int nx = GetData.get_nx();
  final static int ny = GetData.get_ny();
  static long time;
  static long dt;
  static double isoDepth;
  static int NB_RZONES;
  private static float lethal_tp;

  static boolean FLAG_ISODEPTH;
  static boolean FLAG_GROWTH;
  static boolean FLAG_BUOYANCY;
  static boolean FLAG_RECRUITMENT;
  static boolean FLAG_RELEASED_ZONE;
  static boolean FLAG_LETHAL_TP;
  static boolean FLAG_3D;
  static boolean FLAG_BIOMARKER_1;
  static boolean FLAG_BIOMARKER_2;

  static double[][] valLon = new double[1][1];
  static double[][] valLat = new double[1][1];
  static double[][] valDepth = new double[1][1];
  static double[] valTime = new double[1];
  static double[][] valxgrid = new double[1][1];
  static double[][] valygrid = new double[1][1];
  static double[][] valzgrid = new double[1][1];
  static double[][] valTp = new double[1][1];
  static double[][] valSal = new double[1][1];
  static double[][] valLength = new double[1][1];
  static int[][] valZone = new int[1][1];
  static int[][][] valRecruit = new int[1][1][2];
  static int[][] valDeath = new int[1][1];
  static double[][] valBiomarker1 = new double[1][1];
  static double[][] valBiomarker2 = new double[1][1];

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of abstract methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  abstract void moveEuler();

  abstract void moveRK4();

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Initializes the variables of the particle.
   */
  void init() {

    living = true;
    old = false;
    dead = Resources.DEAD_NOT;
    age = 0L;

    if (FLAG_RELEASED_ZONE) {
      numCurrentZone = -1;
    }

    if (FLAG_RECRUITMENT) {
      numRecruitZone = -1;
      timeInZone = 0L;
      durationMinInRecruitArea = GetConfig.getDurationInRecruitArea();
      isRecruited = new boolean[NB_RZONES];
      for (int i = 0; i < NB_RZONES; i++) {
        isRecruited[i] = false;
      }
    }

    if (FLAG_BUOYANCY) {
      if (GetConfig.isRecordNc()) {
        double[] saltntp = Simulation.dataNC.getSaltnTp(this, Simulation.get_t0());
        salinity = saltntp[0];
        temperature = saltntp[1];
      }
      else {
        salinity = 0.d;
        temperature = 0.d;
      }
    }

    if (FLAG_GROWTH) {
      if (GetConfig.isRecordNc() && !FLAG_BUOYANCY) {
        temperature = Simulation.dataNC.getTp(this, Simulation.get_t0());
      }
      else {
        temperature = 0.d;
      }
      length = ToolsBio.LENGTH_INIT;
    }

    if (FLAG_LETHAL_TP && !FLAG_GROWTH) {
      lethal_tp = (float) Simulation.getLethalTpEgg();
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Returns the number of the zone at current location.
   * @param typeZone : RELEASING or RECRUITMENT
   * @return -1 if the particle is not within any zone.
   */
  int getNumZone(int typeZone) {

    int nZone = -1;
    boolean foundZone = false;
    Iterator iter = typeZone == Resources.RELEASE
        ? GetConfig.getReleaseZones().iterator()
        : GetConfig.getRecruitmentZones().iterator();
    while (!foundZone && iter.hasNext()) {
      Zone znTmp = (Zone) iter.next();
      if (znTmp.isXYInZone(getX(), getY())) {
        nZone = znTmp.getIndexZone();
        foundZone = true;
      }
    }
    return nZone;

  }

  //----------------------------------------------------------------------------
  /**
   * Calls the advection method. Calls the growth methods if GROWTH is
   * simulated. Calls the buoyancy method if BUOYANCY is simulated.
   */
  synchronized void stepForward() {

    old = age > Simulation.getTransportDuration();

    if (!old) {
      if (GetConfig.getScheme() == Resources.EULER) {
        moveEuler();
      }
      else {
        moveRK4();
      }

      if (FLAG_BUOYANCY && living) {
        if (FLAG_GROWTH ? length < ToolsBio.HATCH_LENGTH
            : age < ToolsBio.age_lim_buoy) {
          ;
          double[] saltntp = Simulation.dataNC.getSaltnTp(this, time);
          salinity = saltntp[0];
          temperature = saltntp[1];
          setDepth(getDepth() + ToolsBio.addBuoyancy(salinity, temperature));
          geog2Grid();
          grid2Geog();
        }
      }

      if (FLAG_GROWTH && living) {
        length = ToolsBio.grow(length, temperature = Simulation.dataNC.getTp(this,
            time));
        if (FLAG_LETHAL_TP && ToolsBio.isDeadCold()) {
          dead = Resources.DEAD_COLD;
          living = false;
        }
      }
      else if (FLAG_LETHAL_TP && living) {
        temperature = Simulation.dataNC.getTp(this, time);
        if (temperature < lethal_tp) {
          dead = Resources.DEAD_COLD;
          living = false;
        }
      }

      age += dt;
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Determines whether the particle is recruited.
   * @param typeRecruit : RECRUIT_AGE or RECRUIT_LENGTH
   * @return TRUE for recruited.
   */
  public boolean isRecruited(int typeRecruit) {
    numCurrentZone = getNumZone(Resources.RECRUITMENT);
    if ( (numCurrentZone != -1) && !isRecruited[numCurrentZone]) {
      switch (typeRecruit) {
        case Resources.RECRUIT_AGE:
          if ( (float) (age / Resources.ONE_DAY) >=
              Simulation.getAgeMinAtRecruitment()) {
            timeInZone = (numRecruitZone == numCurrentZone)
                ? timeInZone + Simulation.get_dtDisplay() : 0L;
            numRecruitZone = numCurrentZone;
            return (isRecruited[numCurrentZone] = (timeInZone
                >= durationMinInRecruitArea));
          }
          break;
        case Resources.RECRUIT_LENGTH:
          if (length >= Simulation.getLengthMinAtRecruitment()) {
            timeInZone = (numRecruitZone == numCurrentZone)
                ? timeInZone + Simulation.get_dtDisplay() : 0L;
            numRecruitZone = numCurrentZone;
            return (isRecruited[numCurrentZone] = (timeInZone
                >= durationMinInRecruitArea));
          }
          break;
        case Resources.RECRUIT_NONE:
          return (isRecruited[numCurrentZone] = false);
      }
    }
    return false;
  }

  //----------------------------------------------------------------------------
  /**
   * Writes particle location and properties on netcdf file.
   */
  synchronized void writeNc(int itime) {

    valLon[0][0] = getLon();
    valLat[0][0] = getLat();
    valDepth[0][0] = getDepth();
    valTime[0] = (double) (time / Resources.ONE_SECOND);
    valDeath[0][0] = dead;

    if (indexIndiv == 0) {
      OutputNC.add2ncOut(OutputNC.TIME, new ArrayMultiArray(valTime), itime);
    }
    OutputNC.add2ncOut(OutputNC.LONGITUDE, new ArrayMultiArray(valLon), itime,
        indexIndiv);
    OutputNC.add2ncOut(OutputNC.LATITUDE, new ArrayMultiArray(valLat), itime,
        indexIndiv);
    OutputNC.add2ncOut(OutputNC.DEPTH, new ArrayMultiArray(valDepth), itime,
        indexIndiv);
    OutputNC.add2ncOut(OutputNC.DEATH, new ArrayMultiArray(valDeath), itime,
        indexIndiv);

    if (FLAG_3D) {
      double[] saltntp = Simulation.dataNC.getSaltnTp(this, time);
      valTp[0][0] = saltntp[1];
      OutputNC.add2ncOut(OutputNC.TEMPERATURE, new ArrayMultiArray(valTp),
          itime,
          indexIndiv);
      valSal[0][0] = saltntp[0];
      OutputNC.add2ncOut(OutputNC.SALINITY, new ArrayMultiArray(valSal), itime,
          indexIndiv);
    }
    if (FLAG_GROWTH) {
      valLength[0][0] = length;
      OutputNC.add2ncOut(OutputNC.LENGTH, new ArrayMultiArray(valLength), itime,
          indexIndiv);
    }
    if (FLAG_BIOMARKER_1) {
      valBiomarker1[0][0] = Simulation.dataNC.getBiomarker1(this, time);
      OutputNC.add2ncOut(OutputNC.BIOMARKER_1,
          new ArrayMultiArray(valBiomarker1), itime, indexIndiv);
    }
    if (FLAG_BIOMARKER_2) {
      valBiomarker2[0][0] = Simulation.dataNC.getBiomarker2(this, time);
      OutputNC.add2ncOut(OutputNC.BIOMARKER_2,
          new ArrayMultiArray(valBiomarker2), itime, indexIndiv);
    }


    if (FLAG_RECRUITMENT) {
      isRecruited(GetConfig.getTypeRecruitment());
      for (int i = 0; i < NB_RZONES; i++) {
        valRecruit[0][0][i] = isRecruited[i] ? 1 : 0;
      }
      OutputNC.add2ncOut(OutputNC.RECRUITED, new ArrayMultiArray(valRecruit),
          itime,
          indexIndiv);
    }

    if (FLAG_RELEASED_ZONE) {
      numCurrentZone = getNumZone(Resources.RECRUITMENT);
      valZone[0][0] = (numCurrentZone != -1)
          ? -1 * (numCurrentZone + 1)
          : getNumZone(Resources.RELEASE) + 1;
      OutputNC.add2ncOut(OutputNC.CURRENT_ZONE, new ArrayMultiArray(valZone),
          itime,
          indexIndiv);
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Draws particle location on screen.
   */
  void drawStep(Graphics G, int w, int h) {

    if (living) {
      int[] hv = new int[2];
      hv = SimuPanel.xy2hv(getX(), getY(), w, h);

      switch (MainFrame.getDisplayColor()) {
        case Resources.DISPLAY_DEPTH:
          color = SimuPanel.getColorIndividual(getDepth());
          break;
        case Resources.DISPLAY_TP:
          if (! (FLAG_GROWTH | FLAG_BUOYANCY)) {
            temperature = Simulation.dataNC.getTp(this, time);
          }

          //System.out.println(temperature);
          color = SimuPanel.getColorIndividual(temperature);
          break;
        case Resources.DISPLAY_ZONE:
          color = SimuPanel.getColorIndividual(numInitZone);
          break;
        default:
          color = Color.WHITE;
          break;
      }

      G.setColor(color);
      G.fillOval(hv[0] - 1, hv[1] - 1, 2, 2);
    }
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the getters
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public int getNumZoneInit() {
    return numInitZone;
  }

  //------------------------------------------------------------------------------
  public int getNumRecruitZone() {
    return numRecruitZone;
  }

  //----------------------------------------------------------------------------
  public boolean isLiving() {
    return living;
  }

  //----------------------------------------------------------------------------
  public boolean isOld() {
    return old;
  }

  //----------------------------------------------------------------------------
  public boolean isDeadCold() {
    return (dead == Resources.DEAD_COLD);
  }

  //----------------------------------------------------------------------------
  public int getDeath() {
    return dead;
  }

  //----------------------------------------------------------------------------
  public long getAge() {
    return age;
  }

  //----------------------------------------------------------------------------
  public double getLength() {
    return length;
  }

  //----------------------------------------------------------------------------
  public double getTp() {
    return temperature;
  }

//------------------------------------------------------------------------------
// End of class
}
