package ichthyop;

import java.io.*;
import java.util.*;
import java.awt.*;
import ichthyop.util.*;

/**
 *
 * <p>Title: Get configuration</p>
 *
 * <p>Description: Reads and loads in memory the parameters saved in the
 * file of configuration.</p>
 */
public class GetConfig {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private static INIFile cfgFile;

  private static ArrayList listReleaseZone, listRecruitmentZone;
  private static StringBuffer errMsg;
  private static boolean blnErr;

  //Section "CONFIG"
  private static int TYPE_CONFIG;

  //Section "MODEL"
  private static int TYPE_MODEL;

  //Section "SCHEME"
  private static int TYPE_SCHEME;

  //Section "2D3D"
  private static int TYPE_SIMU;

  //Section "IO"
  private static String DIRECTORY_IN;
  private static String DIRECTORY_OUT;

  //Section "TIME"
  private static int TYPE_CALENDAR;
  private static long[] TIME_T0;
  private static long TRANSPORT_DURATION;
  private static long DT;

  //Section "BIO"
  private static boolean BLN_BUOYANCY;
  private static float[] EGG_DENSITY;
  private static float AGE_LIMIT;
  private static boolean BLN_GROWTH;
  private static boolean BLN_LETHAL_TP;
  private static float[] LETHAL_TP_EGG;
  private static float[] LETHAL_TP_LARVAE;
  //private static float KS;

  //Section "RELEASE"
  private static int TYPE_RELEASE;
  private static String PATH_FILE_DRIFTERS;
  private static int NB_PARTICLES;
  private static int[] DEPTH_RELEASE_MIN;
  private static int[] DEPTH_RELEASE_MAX;
  private static boolean BLN_PULSATION;
  private static long[] RELEASE_DT;
  private static int[] NB_RELEASE_EVENTS;
  private static boolean BLN_PATCHINESS;
  private static int NB_PATCHES;
  private static int[] PATCH_RADIUS;
  private static int[] PATCH_THICKNESS;
  private static int NB_RELEASE_ZONES;

  //Section "RECRUITMENT"
  private static int TYPE_RECRUITMENT;
  private static float[] AGE_RECRUITMENT;
  private static float[] LENGTH_RECRUITMENT;
  private static long DURATION_IN_RECRUIT_AREA;
  private static int NB_RECRUITMENT_ZONES;

  //Section "OPTIONS OF SIMULATION"
  private static int TYPE_RECORD;
  private static long RECORD_DT;
  private static int RECORD_FREQUENCY;
  private static boolean BLN_ISO_DEPTH;
  private static float ISO_DEPTH;
  private static int NB_REPLICA;

  //Section "FIELD NAMES"
  private static String STR_XI_DIM;
  private static String STR_ETA_DIM;
  private static String STR_Z_DIM;
  private static String STR_TIME_DIM;
  private static String STR_LON;
  private static String STR_LAT;
  private static String STR_MASK;
  private static String STR_BATHY;
  private static String STR_U;
  private static String STR_V;
  private static String STR_ZETA;
  private static String STR_TP;
  private static String STR_SAL;
  private static String STR_TIME;
  private static String STR_PN;
  private static String STR_PM;
  private static String STR_THETA_S;
  private static String STR_THETA_B;
  private static String STR_HC;
  private static String STR_SIGMA;

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructors
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public GetConfig(File cfgFile) {
    this.cfgFile = new INIFile(cfgFile.toString());
    errMsg = new StringBuffer();
    errMsg.append(
        "! Error --> NullException - Problem reading the following properties in file ");
    errMsg.append(cfgFile.getName());
    errMsg.append(" : \n");
    blnErr = false;
    try {
      //Section "MODEL"
      TYPE_CONFIG = this.cfgFile.getIntegerProperty(Resources.
          STR_SECTION_CONFIG,
          Resources.STR_CFG_CONFIG);
      switch (TYPE_CONFIG) {
        case Resources.SINGLE_SIMU:
          getInfoSingleSimu();
          break;
        case Resources.SERIAL_SIMU:
          getInfoSerialSimu();
          break;
      }
    }
    catch (Exception ex) {

    }
    getZones();
    getFieldNames();

    if (blnErr) {
      System.out.println(errMsg.toString());
    }
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Reads the name of the variables of the hydrodynamic model.
   */
  private void getFieldNames() {

    STR_XI_DIM = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_XI_DIM);
    STR_ETA_DIM = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_ETA_DIM);
    STR_Z_DIM = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_Z_DIM);
    STR_TIME_DIM = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_TIME_DIM);
    STR_LON = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_LON);
    STR_LAT = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_LAT);
    STR_BATHY = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_BATHY);
    STR_U = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_U);
    STR_V = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_V);
    STR_TP = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_TP);
    STR_SAL = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_SAL);
    STR_TIME = getStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_TIME);

    switch (TYPE_MODEL + TYPE_SIMU) {
      case (Resources.ROMS + Resources.SIMU_2D):
        STR_MASK = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_MASK);
        STR_PN = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_PN);
        STR_PM = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_PM);
        break;
      case (Resources.ROMS + Resources.SIMU_3D):
        STR_MASK = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_MASK);
        STR_ZETA = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ZETA);
        STR_PN = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_PN);
        STR_PM = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_PM);
        STR_THETA_S = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_THETA_S);
        STR_THETA_B = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_THETA_B);
        STR_HC = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_HC);

        break;
      case (Resources.MARS + Resources.SIMU_3D):
        STR_ZETA = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ZETA);
        STR_SIGMA = getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_SIGMA);
        break;
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Loads releasing and recruitment zones.
   */
  private void getZones() {

    String strZone;
    int typeZone;
    double[] lon = new double[4];
    double[] lat = new double[4];
    double bathyMin, bathyMax;
    Color color;
    int colorR, colorG, colorB;

    listReleaseZone = new ArrayList(NB_RELEASE_ZONES);
    listRecruitmentZone = new ArrayList(NB_RECRUITMENT_ZONES);

    typeZone = Resources.RELEASE;

    for (int i = 0; i < NB_RELEASE_ZONES; i++) {
      strZone = Resources.STR_SECTION_SZONE + String.valueOf(i + 1);
      for (int j = 0; j < 4; j++) {
        lon[j] = getFloatProperty(strZone,
            Resources.STR_CFG_LON_ZONE + String.valueOf(j + 1));
        lat[j] = getFloatProperty(strZone,
            Resources.STR_CFG_LAT_ZONE + String.valueOf(j + 1));
      }
      bathyMin = getFloatProperty(strZone,
          Resources.STR_CFG_BATHY_MIN);
      bathyMax = getFloatProperty(strZone,
          Resources.STR_CFG_BATHY_MAX);
      colorR = getIntegerProperty(strZone, Resources.STR_CFG_RED);
      colorG = getIntegerProperty(strZone, Resources.STR_CFG_GREEN);
      colorB = getIntegerProperty(strZone, Resources.STR_CFG_BLUE);
      color = new Color(colorR, colorG, colorB);
      listReleaseZone.add(new Zone(typeZone, i, lon[0],
          lat[0],
          lon[1], lat[1], lon[2],
          lat[2], lon[3], lat[3], bathyMin,
          bathyMax, color));
    }

    typeZone = Resources.RECRUITMENT;

    for (int i = 0; i < NB_RECRUITMENT_ZONES; i++) {
      strZone = Resources.STR_SECTION_RZONE + String.valueOf(i + 1);
      for (int j = 0; j < 4; j++) {
        lon[j] = getFloatProperty(strZone,
            Resources.STR_CFG_LON_ZONE + String.valueOf(j + 1));
        lat[j] = getFloatProperty(strZone,
            Resources.STR_CFG_LAT_ZONE + String.valueOf(j + 1));
      }
      bathyMin = getFloatProperty(strZone, Resources.STR_CFG_BATHY_MIN);
      bathyMax = getFloatProperty(strZone, Resources.STR_CFG_BATHY_MAX);
      colorR = getIntegerProperty(strZone, Resources.STR_CFG_RED);
      colorG = getIntegerProperty(strZone, Resources.STR_CFG_GREEN);
      colorB = getIntegerProperty(strZone, Resources.STR_CFG_BLUE);
      color = new Color(colorR, colorG, colorB);
      listRecruitmentZone.add(new Zone(typeZone, i, lon[0],
          lat[0],
          lon[1], lat[1], lon[2],
          lat[2], lon[3], lat[3], bathyMin,
          bathyMax, color));

    }

  }

  //----------------------------------------------------------------------------
  /**
   * Loads parameters for SINGLE simulation.
   */
  private void getInfoSingleSimu() throws Exception {

    TIME_T0 = new long[1];
    EGG_DENSITY = new float[1];
    LETHAL_TP_EGG = new float[1];
    LETHAL_TP_LARVAE = new float[1];
    DEPTH_RELEASE_MIN = new int[1];
    DEPTH_RELEASE_MAX = new int[1];
    RELEASE_DT = new long[1];
    NB_RELEASE_EVENTS = new int[1];
    PATCH_RADIUS = new int[1];
    PATCH_THICKNESS = new int[1];
    AGE_RECRUITMENT = new float[1];
    LENGTH_RECRUITMENT = new float[1];

    //Section "MODEL"
    TYPE_MODEL = getIntegerProperty(Resources.STR_SECTION_MODEL,
        Resources.STR_CFG_MODEL);
    TYPE_SIMU = getIntegerProperty(Resources.STR_SECTION_MODEL,
        Resources.STR_CFG_2D3D);

    //Section "SCHEME"
    TYPE_SCHEME = getIntegerProperty(Resources.STR_SECTION_SCHEME,
        Resources.STR_CFG_SCHEME);

    //Section "IO"
    DIRECTORY_IN = getStringProperty(Resources.STR_SECTION_IO,
        Resources.STR_CFG_DIRECTORY_IN) +
        File.separator;
    DIRECTORY_OUT = getStringProperty(Resources.STR_SECTION_IO,
        Resources.
        STR_CFG_DIRECTORY_OUT) +
        File.separator;

    //Section "TIME"
    TYPE_CALENDAR = getIntegerProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_CALENDAR);
    TIME_T0[0] = getLongProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_T0);
    TRANSPORT_DURATION = getLongProperty(Resources.STR_SECTION_TIME,
        Resources.
        STR_CFG_TRANSPORT_DURATION);
    DT = getLongProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_DT);

    //Section "BIO"
    BLN_BUOYANCY = cfgFile.getBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_BUOY);
    BLN_GROWTH = cfgFile.getBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_GROWTH);
    BLN_LETHAL_TP = cfgFile.getBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_LETHAL_TP);
    if (BLN_BUOYANCY) {
      EGG_DENSITY[0] = getFloatProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_EGG_DENSITY);
      if (!BLN_GROWTH) {
        AGE_LIMIT = getFloatProperty(Resources.STR_SECTION_BIO,
            Resources.STR_CFG_AGE_LIMIT);
      }
    }
    if (BLN_LETHAL_TP) {
      LETHAL_TP_EGG[0] = getFloatProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_EGG);
      LETHAL_TP_LARVAE[0] = getFloatProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_LARVAE);
    }

    //Section "RELEASEG"
    TYPE_RELEASE = getIntegerProperty(Resources.STR_SECTION_RELEASE,
        Resources.STR_CFG_TYPE_RELEASE);
    if (TYPE_RELEASE == Resources.RELEASE_ZONE) {
      NB_PARTICLES = getIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_PARTICLES);
      DEPTH_RELEASE_MIN[0] = getIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MIN);
      DEPTH_RELEASE_MAX[0] = getIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MAX);
      BLN_PULSATION = cfgFile.getBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PULSATION);
      if (BLN_PULSATION) {
        RELEASE_DT[0] = getLongProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RELEASING_DT);
        NB_RELEASE_EVENTS[0] = getIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_RELEASING_EVENTS);
      }
      else {
        RELEASE_DT[0] = 0L;
        NB_RELEASE_EVENTS[0] = 1;
      }
      BLN_PATCHINESS = cfgFile.getBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PATCHINESS);
      if (BLN_PATCHINESS) {
        NB_PATCHES = getIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_PATCHES);
        PATCH_RADIUS[0] = getIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RADIUS_PATCH);
        PATCH_THICKNESS[0] = getIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_THICK_PATCH);
      }
      NB_RELEASE_ZONES = getIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_SZONES);
    }
    else {
      PATH_FILE_DRIFTERS = getStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DRIFTERS);
      File fDrifter = new File(PATH_FILE_DRIFTERS);
      if (!fDrifter.exists() || !fDrifter.canRead()) {
        System.out.println(
            "! Error --> IOException - Unable to read drifter file " + fDrifter);
        blnErr = true;
        return;
      }
      NB_PARTICLES = 0;
      try {
        BufferedReader bfIn = new BufferedReader(new FileReader(fDrifter));
        String line;
        while ( (line = bfIn.readLine()) != null) {
          if (!line.startsWith("#") && ! (line.length() < 1)) {
            NB_PARTICLES++;
          }
        }
      }
      catch (java.io.IOException e) {
        System.out.println(
            "! Error --> IOException - Problem while reading drifter file "
            + fDrifter);
      }
      NB_RELEASE_ZONES = 0;
    }

    //Section "RECRUITMENT"
    TYPE_RECRUITMENT = getIntegerProperty(Resources.STR_SECTION_RECRUIT,
        Resources.STR_CFG_RECRUIT);
    if (TYPE_RECRUITMENT != Resources.RECRUIT_NONE) {
      switch (TYPE_RECRUITMENT) {
        case Resources.RECRUIT_AGE:
          AGE_RECRUITMENT[0] = getFloatProperty(Resources.
              STR_SECTION_RECRUIT, Resources.STR_CFG_AGE_RECRUIT);
          break;
        case Resources.RECRUIT_LENGTH:
          LENGTH_RECRUITMENT[0] = getFloatProperty(Resources.
              STR_SECTION_RECRUIT, Resources.STR_CFG_LENGTH_RECRUIT);
          break;
      }
      DURATION_IN_RECRUIT_AREA = (long) (getFloatProperty(
          Resources.STR_SECTION_RECRUIT,
          Resources.STR_CFG_DURATION_RECRUIT) * Resources.ONE_DAY);
      NB_RECRUITMENT_ZONES = getIntegerProperty(Resources.STR_SECTION_RECRUIT,
          Resources.STR_CFG_NB_RZONES);
    }
    else {
      NB_RECRUITMENT_ZONES = 0;
    }

    //Section "OPTIONS OF SIMULATION"
    TYPE_RECORD = getIntegerProperty(Resources.
        STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_RECORD);
    if (TYPE_RECORD != Resources.RECORD_NONE) {
      RECORD_FREQUENCY = getIntegerProperty(Resources.
          STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_RECORD_DT);
    }
    BLN_ISO_DEPTH = cfgFile.getBooleanProperty(Resources.
        STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_ISO_DEPTH_MVT);
    if (BLN_ISO_DEPTH) {
      ISO_DEPTH = getFloatProperty(Resources.
          STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_ISO_DEPTH);
    }
    NB_REPLICA = 1;
  }

  //----------------------------------------------------------------------------
  /**
   * Loads parameters for SERIAL simulation.
   */
  private void getInfoSerialSimu() {

    //Section "MODEL"
    TYPE_MODEL = cfgFile.getIntegerProperty(Resources.STR_SECTION_MODEL,
        Resources.STR_CFG_MODEL);
    TYPE_SIMU = cfgFile.getIntegerProperty(Resources.STR_SECTION_MODEL,
        Resources.STR_CFG_2D3D);

    //Section "SCHEME"
    TYPE_SCHEME = cfgFile.getIntegerProperty(Resources.STR_SECTION_SCHEME,
        Resources.STR_CFG_SCHEME);

    //Section "IO"
    DIRECTORY_IN = getStringProperty(Resources.STR_SECTION_IO,
        Resources.STR_CFG_DIRECTORY_IN) +
        File.separator;
    DIRECTORY_OUT = getStringProperty(Resources.STR_SECTION_IO,
        Resources.
        STR_CFG_DIRECTORY_OUT) +
        File.separator;

    //Section "TIME"
    TYPE_CALENDAR = cfgFile.getIntegerProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_CALENDAR);
    TIME_T0 = getLongProperties(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_T0);
    TRANSPORT_DURATION = getLongProperty(Resources.STR_SECTION_TIME,
        Resources.
        STR_CFG_TRANSPORT_DURATION);
    DT = getLongProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_DT);

    //Section "BIO"
    BLN_BUOYANCY = cfgFile.getBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_BUOY);
    BLN_GROWTH = cfgFile.getBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_GROWTH);
    BLN_LETHAL_TP = cfgFile.getBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_LETHAL_TP);
    if (BLN_BUOYANCY) {
      EGG_DENSITY[0] = getFloatProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_EGG_DENSITY);
      if (!BLN_GROWTH) {
        AGE_LIMIT = getFloatProperty(Resources.STR_SECTION_BIO,
            Resources.STR_CFG_AGE_LIMIT);
      }
    }
    if (BLN_LETHAL_TP) {
      LETHAL_TP_EGG = getFloatProperties(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_EGG);
      if (BLN_GROWTH) {
        LETHAL_TP_LARVAE = getFloatProperties(
            Resources.STR_SECTION_BIO, Resources.STR_CFG_TP_LARVAE);
      }
    }

    //Section "RELEASE"
    TYPE_RELEASE = cfgFile.getIntegerProperty(Resources.STR_SECTION_RELEASE,
        Resources.STR_CFG_TYPE_RELEASE);
    if (TYPE_RELEASE == Resources.RELEASE_ZONE) {
      NB_PARTICLES = cfgFile.getIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_PARTICLES);
      DEPTH_RELEASE_MIN = getIntegerProperties(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MIN);
      DEPTH_RELEASE_MAX = getIntegerProperties(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MAX);

      BLN_PULSATION = cfgFile.getBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PULSATION);
      if (BLN_PULSATION) {
        RELEASE_DT = getLongProperties(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RELEASING_DT);
        NB_RELEASE_EVENTS = getIntegerProperties(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_RELEASING_EVENTS);
      }
      else {
        RELEASE_DT = new long[] {0L};
        NB_RELEASE_EVENTS = new int[] {1};
      }
      BLN_PATCHINESS = cfgFile.getBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PATCHINESS);
      if (BLN_PATCHINESS) {
        NB_PATCHES = cfgFile.getIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_PATCHES);
        PATCH_RADIUS = getIntegerProperties(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RADIUS_PATCH);
        PATCH_THICKNESS = getIntegerProperties(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_THICK_PATCH);
      }
      NB_RELEASE_ZONES = cfgFile.getIntegerProperty(Resources.
          STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_SZONES);
    }
    else {
      PATH_FILE_DRIFTERS = getStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DRIFTERS);
      File fDrifter = new File(PATH_FILE_DRIFTERS);
      if (!fDrifter.exists() || !fDrifter.canRead()) {
        System.out.println("!! Error --> Drifter file " + fDrifter
            + " cannot be read");
        return;
      }
      NB_PARTICLES = 0;
      try {
        BufferedReader bfIn = new BufferedReader(new FileReader(fDrifter));
        String line;
        while ( (line = bfIn.readLine()) != null) {
          if (!line.startsWith("#") && ! (line.length() < 1)) {
            NB_PARTICLES++;
          }
        }
      }
      catch (java.io.IOException e) {
        e.printStackTrace();
        System.out.println(
            "!! Error --> IOException - Problem reading drifter file "
            + fDrifter);
      }
      NB_RELEASE_ZONES = 0;
    }

    //Section "RECRUITMENT"
    TYPE_RECRUITMENT = cfgFile.getIntegerProperty(Resources.
        STR_SECTION_RECRUIT,
        Resources.STR_CFG_RECRUIT);
    if (TYPE_RECRUITMENT != Resources.RECRUIT_NONE) {
      switch (TYPE_RECRUITMENT) {
        case Resources.RECRUIT_AGE:
          AGE_RECRUITMENT = getFloatProperties(Resources.
              STR_SECTION_RECRUIT,
              Resources.STR_CFG_AGE_RECRUIT);
          break;
        case Resources.RECRUIT_LENGTH:
          LENGTH_RECRUITMENT = getFloatProperties(Resources.
              STR_SECTION_RECRUIT,
              Resources.STR_CFG_LENGTH_RECRUIT);
          break;
      }
      DURATION_IN_RECRUIT_AREA = (long) (getFloatProperty(
          Resources.STR_SECTION_RECRUIT,
          Resources.STR_CFG_DURATION_RECRUIT) * Resources.ONE_DAY);
      NB_RECRUITMENT_ZONES = cfgFile.getIntegerProperty(Resources.
          STR_SECTION_RECRUIT,
          Resources.STR_CFG_NB_RZONES);
    }
    else {
      NB_RECRUITMENT_ZONES = 0;
    }

    //Section "OPTIONS OF SIMULATION"
    TYPE_RECORD = cfgFile.getIntegerProperty(Resources.
        STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_RECORD);
    if (TYPE_RECORD != Resources.RECORD_NONE) {
      RECORD_FREQUENCY = cfgFile.getIntegerProperty(Resources.
          STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_RECORD_DT).intValue();
    }
    BLN_ISO_DEPTH = cfgFile.getBooleanProperty(Resources.
        STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_ISO_DEPTH_MVT);
    if (BLN_ISO_DEPTH) {
      ISO_DEPTH = getFloatProperty(Resources.
          STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_ISO_DEPTH);
    }
    NB_REPLICA = cfgFile.getIntegerProperty(Resources.STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_NB_REPLICA);
  }

  //----------------------------------------------------------------------------
  /**
   * Reads and return serie of long properties.
   */
  private static long[] getLongProperties(String STR_SECTION, String STR_VAR) {
    ArrayList list = new ArrayList(0);
    long[] array;
    Long nb;
    int i = 0;
    while ( (nb = cfgFile.getLongProperty(STR_SECTION,
        STR_VAR + " " + String.valueOf(i))) != null) {
      list.add(nb);
      i++;
    }
    if (list.size() == 0) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
    }
    array = new long[list.size()];
    for (int n = 0; n < list.size(); n++) {
      nb = (Long) list.get(n);
      array[n] = nb;
    }
    return array;
  }

  //----------------------------------------------------------------------------
  /**
   * Reads and returns serie of float properties.
   */
  private static float[] getFloatProperties(String STR_SECTION, String STR_VAR) {
    ArrayList list = new ArrayList(0);
    float[] array;
    Double nb;
    int i = 0;
    while ( (nb = cfgFile.getDoubleProperty(STR_SECTION,
        STR_VAR + " " + String.valueOf(i))) != null) {
      list.add(nb);
      i++;
    }
    if (list.size() == 0) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
    }
    array = new float[list.size()];
    for (int n = 0; n < list.size(); n++) {
      nb = (Double) list.get(n);
      array[n] = nb.floatValue();
    }
    return array;

  }

  //----------------------------------------------------------------------------
  /**
   * Reads and returns serie of Integer properties.
   */
  private static int[] getIntegerProperties(String STR_SECTION, String STR_VAR) {
    ArrayList list = new ArrayList(0);
    int[] array;
    Integer nb;
    int i = 0;
    while ( (nb = cfgFile.getIntegerProperty(STR_SECTION,
        STR_VAR + " " + String.valueOf(i))) != null) {
      list.add(nb);
      i++;
    }
    if (list.size() == 0) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
    }
    array = new int[list.size()];
    for (int n = 0; n < list.size(); n++) {
      nb = (Integer) list.get(n);
      array[n] = nb.intValue();
    }
    return array;

  }

  //----------------------------------------------------------------------------
  /**
   * Returns an Integer property.
   */
  private static int getIntegerProperty(String STR_SECTION, String STR_VAR) {

    Number nb;
    nb = cfgFile.getIntegerProperty(STR_SECTION, STR_VAR);
    if (nb == null) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
      return 0;
    }
    return nb.intValue();
  }

  //----------------------------------------------------------------------------
  /**
   * Returns a FLoat property.
   */
  private static float getFloatProperty(String STR_SECTION, String STR_VAR) {

    Number nb;
    nb = cfgFile.getDoubleProperty(STR_SECTION, STR_VAR);
    if (nb == null) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
      return 0.f;
    }
    return nb.floatValue();
  }

  //----------------------------------------------------------------------------
  /**
   * Returns a Long property.
   */
  private static long getLongProperty(String STR_SECTION, String STR_VAR) {

    Number nb;
    nb = cfgFile.getLongProperty(STR_SECTION, STR_VAR);
    if (nb == null) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
      return 0L;
    }
    return nb.longValue();
  }

  //----------------------------------------------------------------------------
  /**
   * Returns a String property.
   */
  private static String getStringProperty(String STR_SECTION, String STR_VAR) {

    String str;
    str = cfgFile.getStringProperty(STR_SECTION, STR_VAR);
    if (str == null) {
      errMsg.append('[');
      errMsg.append(STR_SECTION);
      errMsg.append("] <");
      errMsg.append(STR_VAR);
      errMsg.append(">\n");
      blnErr = true;
      return "";
    }
    return str;
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the getters
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //---------------------------------------------------------
  public static boolean isErr() {
    return blnErr;
  }

  //---------------------------------------------------------
  public static long[] get_t0() {
    return TIME_T0;
  }

  public static long get_t0(int i) {
    return TIME_T0[i];
  }

  //---------------------------------------------------------
  public static long get_dt() {
    return DT;
  }

  //---------------------------------------------------------
  public static int getNbParticles() {
    return NB_PARTICLES;
  }

  //---------------------------------------------------------
  public static long getTransportDuration() {
    return TRANSPORT_DURATION;
  }

  //---------------------------------------------------------
  public static long[] getReleaseDt() {
    return RELEASE_DT == null ? new long[1] : RELEASE_DT;
  }

  public static long getReleaseDt(int i) {
    return RELEASE_DT[i];
  }

  //---------------------------------------------------------
  public static int[] getDepthReleaseMin() {
    return DEPTH_RELEASE_MIN == null ? new int[1] : DEPTH_RELEASE_MIN;
  }

  public static int getDepthReleaseMin(int i) {
    return DEPTH_RELEASE_MIN[i];
  }

  //---------------------------------------------------------
  public static boolean isPulsation() {
    return BLN_PULSATION;
  }

  //---------------------------------------------------------
  public static int getDepthReleaseMax(int i) {
    return DEPTH_RELEASE_MAX[i];
  }

  public static int[] getDepthReleaseMax() {
    return DEPTH_RELEASE_MAX == null ? new int[1] : DEPTH_RELEASE_MAX;
  }

  //---------------------------------------------------------
  public static float[] getLethalTpEgg() {
    return LETHAL_TP_EGG == null ? new float[1] : LETHAL_TP_EGG;
  }

  public static float getLethalTpEgg(int i) {
    return LETHAL_TP_EGG[i];
  }

  //---------------------------------------------------------
  public static float[] getLethalTpLarvae() {
    return LETHAL_TP_LARVAE == null ? new float[1] : LETHAL_TP_LARVAE;
  }

  public static float getLethalTpLarvae(int i) {
    return LETHAL_TP_LARVAE[i];
  }

  //---------------------------------------------------------
  public static float[] getAgeRecruitment() {
    return AGE_RECRUITMENT == null ? new float[1] : AGE_RECRUITMENT;
  }

  public static float getAgeRecruitment(int i) {
    return AGE_RECRUITMENT[i];
  }

  //---------------------------------------------------------
  public static float[] getLengthRecruitment() {
    return LENGTH_RECRUITMENT == null ? new float[1] : LENGTH_RECRUITMENT;
  }

  public static float getLengthRecruitment(int i) {
    return LENGTH_RECRUITMENT[i];
  }

  //---------------------------------------------------------
  public static boolean isBuoyancy() {
    return BLN_BUOYANCY;
  }

  //---------------------------------------------------------
  public static boolean isGrowth() {
    return BLN_GROWTH;
  }

  //---------------------------------------------------------
  public static boolean isLethalTp() {
    return BLN_LETHAL_TP;
  }

  //---------------------------------------------------------
  public static boolean isRecordNc() {
    return TYPE_RECORD == Resources.RECORD_NC;
  }

  //---------------------------------------------------------
  public static boolean isRecord() {
    return TYPE_RECORD != Resources.RECORD_NONE;
  }

  //---------------------------------------------------------
  public static int getTypeRecord() {
    return TYPE_RECORD;
  }

  //---------------------------------------------------------
  public static long getDtRecord() {
    return (long) RECORD_FREQUENCY * DT;
  }

  //--------------------------------------------------------
  public static int getRecordFrequency() {
    return RECORD_FREQUENCY;
  }

  //---------------------------------------------------------
  public static boolean isIsoDepth() {
    return BLN_ISO_DEPTH;
  }

  //---------------------------------------------------------
  public static float getIsoDepth() {
    return ( -ISO_DEPTH);
  }

  //---------------------------------------------------------
  public static int getDimSimu() {
    return TYPE_SIMU;
  }

  //---------------------------------------------------------
  public static boolean is3D() {
    return TYPE_SIMU == Resources.SIMU_3D;
  }

  //---------------------------------------------------------
  public static int getTypeCalendar() {
    return TYPE_CALENDAR;
  }

  //---------------------------------------------------------
  public static int getTypeModel() {
    return TYPE_MODEL;
  }

  //---------------------------------------------------------
  public static int getTypeRecruitment() {
    return TYPE_RECRUITMENT;
  }

  //---------------------------------------------------------
  public static int getTypeRelease() {
    return TYPE_RELEASE;
  }

  //---------------------------------------------------------
  public static int getNbPatches() {
    return NB_PATCHES;
  }

  //---------------------------------------------------------
  public static boolean isPatchiness() {
    return BLN_PATCHINESS;
  }

  //---------------------------------------------------------
  public static int[] getNbReleaseEvents() {
    return NB_RELEASE_EVENTS == null ? new int[1] : NB_RELEASE_EVENTS;
  }

  public static int getNbReleaseEvents(int i) {
    return NB_RELEASE_EVENTS[i];
  }

  //---------------------------------------------------------
  public static float[] getEggDensity() {
    return EGG_DENSITY == null ? new float[1] : EGG_DENSITY;
  }

  public static float getEggDensity(int i) {
    return EGG_DENSITY[i];
  }

  //---------------------------------------------------------
  public static long getAgeLimit() {
    return (long) (AGE_LIMIT * Resources.ONE_DAY);
  }

  //---------------------------------------------------------
  public static long getDurationInRecruitArea() {
    return DURATION_IN_RECRUIT_AREA;
  }

  //---------------------------------------------------------
  public static String getDirectorIn() {
    return DIRECTORY_IN;
  }

  //---------------------------------------------------------
  public static String getDirectorOut() {
    return DIRECTORY_OUT;
  }

  //---------------------------------------------------------
  public static int[] getRadiusPatchi() {
    return PATCH_RADIUS == null ? new int[1] : PATCH_RADIUS;
  }

  public static int getRadiusPatchi(int i) {
    return PATCH_RADIUS[i];
  }

  //---------------------------------------------------------
  public static int[] getThickPatchi() {
    return PATCH_THICKNESS;
  }

  public static int getThickPatchi(int i) {
    return PATCH_THICKNESS[i];
  }

  //---------------------------------------------------------
  public static int getNbReplica() {
    return NB_REPLICA;
  }

  //---------------------------------------------------------
  public static boolean isSerial() {
    return TYPE_CONFIG == Resources.SERIAL_SIMU;
  }

  //---------------------------------------------------------
  public static ArrayList getRecruitmentZones() {
    return listRecruitmentZone;
  }

  //---------------------------------------------------------
  public static ArrayList getReleaseZones() {
    return listReleaseZone;
  }

  //--------------------------------------------------------------------------
  public static String getStrXiDim() {
    return STR_XI_DIM;
  }

  //--------------------------------------------------------------------------
  public static String getStrEtaDim() {
    return STR_ETA_DIM;
  }

  //--------------------------------------------------------------------------
  public static String getStrZDim() {
    return STR_Z_DIM;
  }

  //--------------------------------------------------------------------------
  public static String getStrTimeDim() {
    return STR_TIME_DIM;
  }

  //--------------------------------------------------------------------------
  public static String getStrLon() {
    return STR_LON;
  }

  //--------------------------------------------------------------------------
  public static String getStrLat() {
    return STR_LAT;
  }

  //--------------------------------------------------------------------------
  public static String getStrBathy() {
    return STR_BATHY;
  }

  //--------------------------------------------------------------------------
  public static String getStrMask() {
    return STR_MASK;
  }

  //--------------------------------------------------------------------------
  public static String getStrU() {
    return STR_U;
  }

  //--------------------------------------------------------------------------
  public static String getStrV() {
    return STR_V;
  }

  //--------------------------------------------------------------------------
  public static String getStrTp() {
    return STR_TP;
  }

  //--------------------------------------------------------------------------
  public static String getStrSal() {
    return STR_SAL;
  }

  //--------------------------------------------------------------------------
  public static String getStrZeta() {
    return STR_ZETA;
  }

  //--------------------------------------------------------------------------
  public static String getStrSigma() {
    return STR_SIGMA;
  }

  //--------------------------------------------------------------------------
  public static String getStrPn() {
    return STR_PN;
  }

  //--------------------------------------------------------------------------
  public static String getStrPm() {
    return STR_PM;
  }

  //--------------------------------------------------------------------------
  public static String getStrThetaS() {
    return STR_THETA_S;
  }

  //--------------------------------------------------------------------------
  public static String getStrThetaB() {
    return STR_THETA_B;
  }

  //--------------------------------------------------------------------------
  public static String getStrHc() {
    return STR_HC;
  }

  //--------------------------------------------------------------------------
  public static String getStrTime() {
    return STR_TIME;
  }

  //--------------------------------------------------------------------------
  public static int getScheme() {
    return TYPE_SCHEME;
  }

  //----------------------------------------------------------------------------
  public static String getDrifterFile() {
    return PATH_FILE_DRIFTERS;
  }

  //----------------------------------------------------------------------------
  public static String getCfgName() {
    return cfgFile.toString();
  }

//------------------------------------------------------------------------------
// End of class
}
