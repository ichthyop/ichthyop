package ichthyop;

import java.io.*;
import java.text.*;
import java.util.*;
import java.awt.*;
import ichthyop.bio.*;
import ichthyop.datanc.*;
import ichthyop.util.*;

/**
 *
 * <p>Title: Population</p>
 *
 * <p>Description: Controls the properties of the Population of Individuals</p>
 *
 */

// -----------------------------------------------------------------------------
public class Population
    extends HashSet {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  /**
   * Number of particle "out" : beached and out of the simulated domain.
   */
  public static int outCounting;
  /**
   * Depth distribution of the particles.
   */
  public static double[] depthDistribution;
  /**
   * Census of the particles by stage when GROWTH is simulated.
   */
  public static double[] stageDistribution;
  /**
   * Recruitment counting
   */
  public static double[] recruitCounting;
  /**
   * Length distribution of the particle when GROWTH is simulated
   */
  public static double[] lengthDistribution;
  /**
   * Census of dead cold particules when LETHAL TEMPERATURE is active.
   */
  public static double[] mortalityCounting;

  private static long[] timeOfRelease;

  private int i_releaseEvent;
  private boolean isAllReleased;

  private static Iterator iter;
  private static int nb_alive;
  private static boolean blnRecord;
  private static long dt_record;
  private static long t0;
  private int i_record;

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructor
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public Population() {
    super(0);
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Initializes variables common to all particles. Formats the arrays containing the chart data.
   */
  void init() {

    t0 = Simulation.get_t0();
    i_releaseEvent = 0;
    isAllReleased = false;
    Particle.dt = GetConfig.get_dt();

    if (GetConfig.isRecord()) {
      i_record = 0;
      dt_record = GetConfig.getDtRecord();
    }

    if (!GetConfig.isSerial()) {
      outCounting = 0;
      recruitCounting = new double[GetConfig.getRecruitmentZones().size()
          * GetConfig.getReleaseZones().size()];
    }

    Particle.FLAG_3D = GetConfig.is3D();
    Particle.FLAG_RELEASED_ZONE = (GetConfig.getTypeRelease()
        == Resources.RELEASE_ZONE);

    Particle.FLAG_ISODEPTH = GetConfig.isIsoDepth();
    if (Particle.FLAG_ISODEPTH) {
      Particle.isoDepth = (double) GetConfig.getIsoDepth();
    }

    Particle.FLAG_LETHAL_TP = GetConfig.isLethalTp();
    if (Particle.FLAG_LETHAL_TP) {
      mortalityCounting = new double[3];
    }
    Particle.FLAG_GROWTH = GetConfig.isGrowth();
    if (Particle.FLAG_GROWTH) {
      if (!GetConfig.isSerial()) {
        stageDistribution = new double[3];
      }
      ToolsBio.initGrowth();
    }
    Particle.FLAG_BUOYANCY = GetConfig.isBuoyancy();
    if (Particle.FLAG_BUOYANCY) {
      ToolsBio.initBuoy();
    }
    Particle.FLAG_RECRUITMENT = (GetConfig.getTypeRecruitment()
        != Resources.RECRUIT_NONE);
    if (Particle.FLAG_RECRUITMENT) {
      Particle.NB_RZONES = GetConfig.getRecruitmentZones().size();
      Particle.valRecruit = new int[1][1][Particle.NB_RZONES];
    }

    computeReleasingTime();
  }

// -----------------------------------------------------------------------------
  /**
   * Launches the stepForward for each particle and the record of particle properties.
   *
   * @param year int
   * @param month int
   * @param day int
   */
  synchronized void stepForward(long time) {

    Simulation.dataNC.setAllFieldsAtTime(time);

    if (!isAllReleased) {
      if (Particle.FLAG_RELEASED_ZONE) {
        release(time);
      }
      else {
        release(GetConfig.getDrifterFile());
      }
    }

    iter = iterator();
    Particle.time = time;
    blnRecord = GetConfig.isRecordNc() && (time >= t0 + i_record * dt_record);
    nb_alive = 0; ;
    while (iter.hasNext()) {
      Particle particule = (Particle) iter.next();
      if (blnRecord) {
        particule.writeNc(i_record);
      }
      if (particule.isLiving()) {
        nb_alive++;
        particule.stepForward();
      }
    }
    if (blnRecord) {
      i_record++;
    }

  }

  //----------------------------------------------------------------------------
  /**
   * Records particle properties at the end of the simulation.
   */
  synchronized void writeLast() {
    iter = iterator();
    while (iter.hasNext()) {
      Particle particule = (Particle) iter.next();
      particule.writeNc(i_record);
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Collects all the information transmitted to the control charts.
   */
  void counting() {

    iter = iterator();
    outCounting = 0;
    stageDistribution = new double[3];
    mortalityCounting = new double[2];
    depthDistribution = new double[nb_alive];
    lengthDistribution = new double[nb_alive];
    int i_particle = 0, i_stage = 0;
    int stage;
    while (iter.hasNext()) {
      Particle particule = (Particle) iter.next();
      if (GetConfig.getTypeRecruitment() != Resources.RECRUIT_NONE
          && particule.isRecruited(GetConfig.getTypeRecruitment())) {
        recruitCounting[GetConfig.getRecruitmentZones().size() * particule.getNumZoneInit()
            + particule.getNumRecruitZone()]++;
      }
      if (particule.getDeath() == Resources.DEAD_OUT
          || particule.getDeath() == Resources.DEAD_BEACH) {
        outCounting++;
      }
      if (particule.FLAG_LETHAL_TP) {
        if (particule.isDeadCold()) {
          i_stage = (particule.FLAG_GROWTH)
              ? ( (ToolsBio.getStage(particule.getLength()) > 0) ? 1 : 0)
              : 0;
          mortalityCounting[i_stage]++;
        }
      }
      if (particule.isLiving() && !particule.isOld()) {
        depthDistribution[i_particle] = particule.getDepth();
        if (particule.FLAG_GROWTH) {
          lengthDistribution[i_particle] = particule.getLength();
          stage = ToolsBio.getStage(particule.getLength());
          stageDistribution[stage]++;
        }
        i_particle++;
      }
    }
  }

  //------------------------------------------------------------------------------
  /**
   * Determines the releasing calendar.
   * By default the time of releasing is limited to t0. If PULSATION is active
   * (multiple releasing event), times of releasing are determined functions of
   * the number of releasing events and the time step between two
   * releasing events.
   */
  private void computeReleasingTime() {

    timeOfRelease = new long[Particle.FLAG_RELEASED_ZONE
        ? Simulation.getNbReleaseEvents():1];
    for (int i = 0; i < Simulation.getNbReleaseEvents(); i++) {
      timeOfRelease[i] = Simulation.get_t0() +
          (long) (i * Simulation.getReleaseDt());
      //System.out.println(time_releasing);
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Releases particles in zones.
   * @param time long
   */
  private synchronized void release(long time) {
    int nbReleasedNow, nbReleased, nbReleaseZones,
        indexIndiv, nbAgregatedIndiv, nbInPatch = 0, i_part;
    double radius_patch = 0.d, thickness_patch = 0.d;
    double xmin, xmax, ymin, ymax;
    double depthMin = 0.d, depthMax = 0.d;
    Zone zone;
    Particle particle, particlePatch;
    boolean bln2D = (GetConfig.getDimSimu() == Resources.SIMU_2D);

    nbReleasedNow = GetConfig.isPulsation()
        ? GetConfig.getNbParticles() / Simulation.getNbReleaseEvents()
        : GetConfig.getNbParticles();
    int mod = GetConfig.getNbParticles() % Simulation.getNbReleaseEvents();
    nbReleasedNow += (i_releaseEvent < mod) ? 1 : 0;

    indexIndiv = this.size();
    nbReleaseZones = GetConfig.getReleaseZones().size();
    if (nbReleaseZones == 0) {
      System.out.println("!! Error - No release zone defined");
      isAllReleased = true;
      return;
    }
    nbReleased = nbReleasedNow;
    if (GetConfig.isPatchiness()) {
      nbInPatch = Math.max(0,
          nbReleasedNow / GetConfig.getNbPatches() - 1);
      nbReleased = GetConfig.getNbPatches();
      radius_patch = Simulation.getRadiusPatchi();
      thickness_patch = Simulation.getThickPatchi();
    }
    if (!bln2D) {
      depthMin = Simulation.getDepthReleaseMin();
      depthMax = Simulation.getDepthReleaseMax();
    }

    xmin = Double.MAX_VALUE;
    ymin = Double.MAX_VALUE;
    xmax = 0.d;
    ymax = 0.d;
    for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
      zone = (Zone) GetConfig.getReleaseZones().get(i_zone);
      xmin = Math.min(xmin, zone.getXmin());
      xmax = Math.max(xmax, zone.getXmax());
      ymin = Math.min(ymin, zone.getYmin());
      ymax = Math.max(ymax, zone.getYmax());
    }

    while (!isAllReleased && timeOfRelease[i_releaseEvent] >= time &&
        timeOfRelease[i_releaseEvent] <
        (time + Simulation.get_dt())) {
      i_part = 0;
      for (int p = 0; p < nbReleased; p++) {
        particle = bln2D
            ? new Particle2D(indexIndiv, xmin, xmax, ymin, ymax)
            : new Particle3D(indexIndiv, xmin, xmax, ymin, ymax, depthMin,
            depthMax);
        add(particle);
        //System.out.println(indexIndiv);
        indexIndiv++;
        if (GetConfig.isPatchiness()) {
          nbAgregatedIndiv = nbInPatch +
              (i_part < (nbReleasedNow % GetConfig.getNbPatches()) ? 1 : 0);
          double radius_grid = Simulation.dataNC.adimensionalize(
              radius_patch, particle.getX(), particle.getY());
          double r, teta;
          for (int f = 0; f < nbAgregatedIndiv; f++) {
            r = radius_grid * Math.random();
            teta = 2.0f * Math.PI * Math.random();
            particlePatch = bln2D
                ? new Particle2D(indexIndiv,
                particle.getNumZoneInit(),
                particle.getX() +
                r * Math.cos(teta),
                particle.getY() +
                r * Math.sin(teta))
                : new Particle3D(indexIndiv,
                particle.getNumZoneInit(),
                particle.getX() +
                r * Math.cos(teta),
                particle.getY() +
                r * Math.sin(teta),
                particle.getDepth() +
                thickness_patch * (Math.random() - 0.5f));
            add(particlePatch);
            indexIndiv++;
          }
        }
        i_part++;
      }
      i_releaseEvent++;
      System.out.println("Release event " + i_releaseEvent +
          " : done");
      isAllReleased = i_releaseEvent >= timeOfRelease.length;
    }

  }

  //----------------------------------------------------------------------------
  /**
   * Releases particle from initial coordinates stored in file *.drf.
   * @param strFile String
   */
  private synchronized void release(String strFile) {
    File fDrifter = new File(strFile);
    if (!fDrifter.exists() || !fDrifter.canRead()) {
      System.out.println("!! Error --> Drifter file " + fDrifter + " cannot be read");
      return;
    }
    String[] strCoord;
    double[] coord;
    NumberFormat nbFormat = NumberFormat.getInstance(Locale.getDefault());
    Particle individual;
    int indexIndiv = 0;
    boolean bln2D = (GetConfig.getDimSimu() == Resources.SIMU_2D);
    try {
      BufferedReader bfIn = new BufferedReader(new FileReader(fDrifter));
      String line;
      while ( (line = bfIn.readLine()) != null) {
        if (!line.startsWith("#") & ! (line.length() < 1)) {
          strCoord = line.split(" ");
          coord = new double[strCoord.length];
          for (int i = 0; i < strCoord.length; i++) {
            try {
              coord[i] = nbFormat.parse(strCoord[i].trim()).doubleValue();
            }
            catch (ParseException ex) {
            }
          }
          individual = bln2D
              ? new Particle2D(indexIndiv, coord[0], coord[1])
              : new Particle3D(indexIndiv, coord[0], coord[1],
              GetConfig.isIsoDepth() ? GetConfig.getIsoDepth() : -coord[2]);
          if (GetData.isInWater(individual)) {
            add(individual);
            indexIndiv++;
          }
        }
      }
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
      System.out.println("!! Error --> IOException - Problem reading drifter file " + fDrifter);
    }

    isAllReleased = true;
  }

  //----------------------------------------------------------------------------
  /**
   * Called by simulation.draw to display particles on screen
   */
  void draw(Graphics G, int w, int h) {

    Iterator iter = iterator();
    while (iter.hasNext()) {
      Particle indiv = (Particle) iter.next();
      indiv.drawStep(G, w, h);
    }
  }

//------- End of class Pouplation
}
