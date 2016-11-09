package ichthyop.util;

import java.util.*;

public class Resources {

  /****************************************************************
   * Captions of buttons & menu items
   ****************************************************************/

  // Standard buttons
  final public static String BTN_OK = "Ok";
  final public static String BTN_CANCEL = "Cancel";
  final public static String BTN_SAVE = "Save";
  final public static String BTN_SAVEAS = "Save as";

  // Program names
  final public static String NAME_SHORT = "Ichthyop - ";
  final public static String NAME_LONG = "Ichthyop - Lagrangian tool for modelling ichthyoplankton dynamics";

  //File extensions
  final public static String EXTENSION_CONFIG = "cfg";
  final public static String EXTENSION_DRIFTER = "drf";

  // FrameIBM > menu
  final public static String MENU_FILE = "Configuration file";
  final public static String MENU_FILE_NEW = "New...";
  final public static String MENU_FILE_OPEN = "Open...";
  final public static String MENU_FILE_EDIT = "Edit...";
  final public static String MENU_FILE_EXIT = "Exit";
  final public static String MENU_DISPLAY = "Display options";
  final public static String MENU_DISPLAY_BG_S = "Display release zones";
  final public static String MENU_DISPLAY_BG_R = "Display recruitment zones";
  final public static String MENU_CHART_DEPTH = "Chart - Depth distribution";
  final public static String MENU_CHART_EDGE = "Chart - Out of domain";
  final public static String MENU_CHART_RECRUIT = "Chart - Recruitment";
  final public static String MENU_CHART_LENGTH = "Chart - Length distribution";
  final public static String MENU_CHART_STAGE = "Chart - Stage distribution";
  final public static String MENU_CHART_MORTALITY = "Chart - Mortality";
  final public static String MENU_DISPLAY_NONE = "Particle colour - None";
  final public static String MENU_DISPLAY_BATHY = "Particle colour - Depth";
  final public static String MENU_DISPLAY_ZONE = "Particle colour - Release zone";
  final public static String MENU_DISPLAY_TP = "Particle colour - Temperature";
  final public static String MENU_DISPLAY_PREFERENCE = "Preferences";

  // FrameIBM > buttons
  final public static String BTN_START = "Start";
  final public static String BTN_STOP = "Stop";
  final public static String BTN_CAPTURE = "Capture pictures";
  final public static String BTN_EXIT = "Exit";
  final public static String PRM_REFRESH = "Refresh frequency";

  // FrameIBM > labels
  final public static String LBL_TIME = "Time ";
  final public static String LBL_STEP = "Step ";
  final public static String LBL_BATHY = "Bathymetry [meter]";
  final public static String LBL_DEPTH = "Particle depth [meter]";
  final public static String LBL_TP = "Water temperature [Celsius]";

  // Status bar
  final public static String MSG_PREFIX = "Status : ";
  final public static String MSG_WAITING = "waiting for config file.";
  final public static String MSG_READY = "config file loaded. Ready to start.";
  final public static String MSG_LOADING = "loading file ";
  final public static String MSG_GET_FIELD = "getting fields...";
  final public static String MSG_REFRESH_SCREEN = "refresh trajectories";
  final public static String MSG_REPLAY = "replay mode. Move the slider.";

  // ZoneEditor
  final public static String TITLE_ZONE_EDITOR = "Zone editor";
  final public static String STR_NEW = "New";
  final public static String STR_DUPLICATE = "Duplicate";
  final public static String STR_EDIT = "Edit";
  final public static String STR_DELETE = "Delete";

  // JFrameZone
  final public static String TITLE_FRAME_ZONE = "Zone editor";
  final public static String STR_RELEASE_ZONE = "Release zone";
  final public static String STR_RECRUITMENT_ZONE = "Recruitment zone";
  final public static String LBL_GEOG_AREA = "Geographical area";
  final public static String LBL_TYPE_ZONE = "Type of zone";
  final public static String LBL_LON = "Lon °E";
  final public static String LBL_LAT = "Lat °N";
  final public static String LBL_P1 = "P1";
  final public static String LBL_P2 = "P2";
  final public static String LBL_P3 = "P3";
  final public static String LBL_P4 = "P4";
  final public static String LBL_BATHYMETRIC_MASK = "Bathymetric mask";
  final public static String LBL_BATHY_LINE_MIN = "Low bathymetry line";
  final public static String LBL_BATHY_LINE_MAX = "High bathymetry line";
  final public static String LBL_COLOR_ZONE = "Colour of zone (RGB)";
  final public static String LBL_RED = "Red";
  final public static String LBL_GREEN = "Green";
  final public static String LBL_BLUE = "Blue";

  //Frame display preferences
  final public static String TITLE_FRAME_PREF = "Ichthyop - Display preferences";
  final public static String LBL_COLORBAR = "Colorbar extreme values";
  final public static String LBL_MIN_TP = "Tp min";
  final public static String LBL_MAX_TP = "Tp max";
  final public static String LBL_MIN_DEPTH = "Depth min";
  final public static String LBL_MAX_DEPTH = "Depth max";
  final public static String LBL_UNIT_TP = "[Celsius]";
  final public static String LBL_UNIT_DEPTH = "[meter]";

  //Control charts
  final public static String CHART_TITLE_DEPTH = "Depth distribution";
  final public static String CHART_TITLE_OUT = "Out of the domain";
  final public static String CHART_TITLE_LENGTH = "Length distribution";
  final public static String CHART_TITLE_DEAD_COLD = "Mortality";
  final public static String CHART_TITLE_RECRUITMENT = "Recruitment";
  final public static String CHART_TITLE_STAGE = "Stage distribution";
  final public static String CHART_LEGEND_TIME = "Time step";
  final public static String CHART_LEGEND_NB_PARTICLES = "Number particles";
  final public static String CHART_LEGEND_LENGTH = "Length [millimeter]";
  final public static String CHART_LEGEND_DEPTH = "Depth [meter]";


  //FrameConfig Tabs
  final public static String TITLE_FRAME_CONFIG = "Editor of configuration";
  final public static String TAB_SIMULATION = "Simulation";
  final public static String TAB_RELEASING = "Release";
  final public static String TAB_RECRUITMENT = "Recruitment";

  //FrameConfig > tab simulation
  final public static String RD_BTN_ROMS = "ROMS";
  final public static String RD_BTN_MARS = "MARS";
  final public static String BTN_CONFIGURE = "Configure";
  final public static String RD_BTN_EULER = "Euler";
  final public static String RD_BTN_RK4 = "Runge Kutta 4";
  final public static String RD_BTN_2D = "2D";
  final public static String RD_BTN_3D = "3D";
  final public static String LBL_INPUT_PATH = "Input path";
  final public static String LBL_OUTPUT_PATH = "Output path";
  final public static String BTN_GET_INFO = "Get info";
  final public static String RD_BTN_TIME_REAL = "Gregorian calendar";
  final public static String RD_BTN_TIME_CLIMATO = "Climato calendar";
  final public static String BTN_RESET_TIME = "Reset";
  final public static String CK_BOX_BUOYANCY = "Buoyancy";
  final public static String CK_BOX_GROWTH = "Growth";
  final public static String CK_BOX_LETHAL_TP = "Lower lethal temperature";
  final public static String CKBOX_ISO_DEPTH = "Isodepth transport";
  final public static String RD_BTN_RECORD_NONE = "No output";
  final public static String RD_BTN_RECORD_NC = "Record tracks";
  final public static String RD_BTN_RECORD_TXT = "Record balance";

  //FrameConfig > tab simulation > info panel
  final public static String LBL_LON_MIN = "Lon min";
  final public static String LBL_LON_MAX = "Lon max";
  final public static String LBL_LAT_MIN = "Lat min";
  final public static String LBL_LAT_MAX = "Lat max";
  final public static String LBL_DEPTH_MAX = "Depth max";
  final public static String LBL_DT_MODEL = "Model dt";
  final public static String LBL_DT_RECORDS = "Records dt";

  //FrameConfig > tab releasing
  final public static String RD_BTN_RELEASE_ZONE = "From zone";
  final public static String RD_BTN_RELEASE_FILE = "From file";
  final public static String TXT_FIELD_DRIFTER_FILE = "Drifters initial coordinates file";
  final public static String CK_BOX_PULSATION = "Multi-release events";
  final public static String CK_BOX_PATCHINESS = "Patchiness";
  final public static String TITLE_RELEASE_ZONE_EDITOR = "Release zones";

  //FrameConfig > tab recruitment
  final public static String RD_BTN_RECRUIT_NONE = "None";
  final public static String RD_BTN_RECRUIT_AGE = "Age criteria";
  final public static String RD_BTN_RECRUIT_LENGTH = "Length criteria";
  final public static String TITLE_RECRUIT_ZONE_EDITOR = "Recruitment zones";

  //FrameConfig > tab simulation > parameters
  final public static String PRM_BEGIN_SIMU = "Beginning of simulation";
  final public static String PRM_DURATION_TRANSPORT = "Duration of transport";
  final public static String PRM_INTERNAL_DT = "Computational time-step (dt)";
  final public static String PRM_EGG_DENSITY = "Egg density";
  final public static String PRM_AGE_LIMIT_BUOY = "Age limit";
  final public static String PRM_LETHAL_TP_EGG = "For eggs";
  final public static String PRM_LETHAL_TP_LARVAE = "For larvae";
  final public static String PRM_ISODEPTH = "Isodepth line";
  final public static String PRM_RECORD_FREQUENCY = "Record frequency";

  //FrameConfig > tab releasing > parameters
  final public static String PRM_NB_RELEASED = "Number of particles";
  final public static String PRM_DEPTH_RELEASING_MIN = "Release depth min";
  final public static String PRM_DEPTH_RELEASING_MAX = "Release depth max";
  final public static String PRM_NB_RELEASING_VENTS = "Number of release events";
  final public static String PRM_RELEASING_DT = "Time between two release events";
  final public static String PRM_NB_PATCHES = "Number of patches";
  final public static String PRM_RADIUS_PATCH = "Patch radius";
  final public static String PRM_THICKNESS_PATCH = "Patch thickness";

  //FrameConfig > tab recruitment > parameters
  final public static String PRM_RECRUIT_AGE = "Age min for recruitment";
  final public static String PRM_RECRUIT_LENGTH = "Length min for recruitment";
  final public static String PRM_RECRUIT_DURATION_MIN = "Duration min to spend in recruitment area";

  //FrameConfig > parameter units
  final public static String UNIT_DURATION = "dddd hh:mm";
  final public static String UNIT_SECOND = "[second]";
  final public static String UNIT_MINUTE = "[minute]";
  final public static String UNIT_HOUR = "[hour]";
  final public static String UNIT_DAY = "[day]";
  final public static String UNIT_DENSITY = "[g/cm3]";
  final public static String UNIT_CELSIUS = "[celsius]";
  final public static String UNIT_METER = "[meter]";
  final public static String UNIT_MILLIMETER = "[millimeter]";
  final public static String UNIT_FACTOR_DT = "* dt";
  final public static String UNIT_NONE = "";

  // Frame field names
  final public static String TITLE_FIELD = "Field names - ";
  final public static String LBL_DIM = "DIMENSION";
  final public static String LBL_CONSTANT_FIELD = "CONSTANT FIELD";
  final public static String LBL_TIME_FIELD = "TIME DEPENDENT FIELD";
  final public static String LBL_ATTRIBUTE = "ATTRIBUTE";
  final public static String LBL_XI_DIM = "Dim in the XI-direction";
  final public static String LBL_ETA_DIM = "Dim in the ETA-direction";
  final public static String LBL_Z_DIM = "Dim in the Z-direction";
  final public static String LBL_TIME_DIM = "Dim in time";
  final public static String LBL_LONGITUDE = "Longitude of RHO point";
  final public static String LBL_LATITUDE = "Latitude of RHO point";
  final public static String LBL_MASK = "Mask on RHO point";
  final public static String LBL_BATHYMETRY = "Bathymetry at RHO point";
  final public static String LBL_PN = "Curvilinear coordinate metric in ETA";
  final public static String LBL_PM = "Curvilinear coordinate metric in XI";
  final public static String LBL_SIGMA = "Sigma levels";
  final public static String LBL_ZETA = "Free-surface elevation";
  final public static String LBL_U = "U-momentum component";
  final public static String LBL_V = "V-momentum component";
  // LBL_TIME is already defined above
  final public static String LBL_TEMPERATURE = "Temperature";
  final public static String LBL_SAL = "Salinity";
  final public static String LBL_THETAS = "S-coord surface control parameter";
  final public static String LBL_THETAB = "S-coord bottom control parameter";
  final public static String LBL_HC = "S-coord surface/bottom layer width";

  /*************************************************
   *  Constant definition
   *************************************************/

  // Option flags
  final public static int SINGLE_SIMU = 0;
  final public static int SERIAL_SIMU = 1;

  final public static int ROMS = 0;
  final public static int MARS = 1;

  final public static int INTERANNUAL = 0;
  final public static int CLIMATO = 1;

  final public static int EULER = 0;
  final public static int RK4 = 1;

  final public static int RECRUIT_NONE = 0;
  final public static int RECRUIT_AGE = 1;
  final public static int RECRUIT_LENGTH = 2;

  final public static int RELEASE = 0;
  final public static int RECRUITMENT = 1;

  final public static int SIMU_2D = 0;
  final public static int SIMU_3D = 2;

  final public static int HORIZONTAL = 0;
  final public static int VERTICAL = 1;

  final public static int RELEASE_ZONE = 0;
  final public static int RELEASE_FILE = 1;

  final public static int DISPLAY_DEPTH = 0;
  final public static int DISPLAY_ZONE = 1;
  final public static int DISPLAY_TP = 2;
  final public static int DISPLAY_NONE = 3;

  final public static int RECORD_NONE = 0;
  final public static int RECORD_TXT = 1;
  final public static int RECORD_NC = 2;

  final public static int DEAD_NOT = 0;
  final public static int DEAD_OUT = 1;
  final public static int DEAD_COLD = 2;
  final public static int DEAD_BEACH = 3;

  // Time constants
  public static final int ONE_SECOND = 1000;
  public static final int ONE_MINUTE = 60 * ONE_SECOND;
  public static final int ONE_HOUR = 60 * ONE_MINUTE;
  public static final long ONE_DAY = 24 * ONE_HOUR;
  public static final long ONE_WEEK = 7 * ONE_DAY;

  // Default values for particle colorbar range.
  public static final float TP_MIN = 10.0f;
  public static final float TP_MAX = 20.0f;
  public static final float BATHY_MIN = 0.0f;
  public static final float BATHY_MAX = 100.0f;

  // Gregorian calendar origin
  public static final int YEAR_ORIGIN = 1900;
  public static final int MONTH_ORIGIN = Calendar.JANUARY;
  public static final int DAY_ORIGIN = 1;

  // Threshold for CFL error message
  public static final float THRESHOLD_CFL = 1.0f;

  /**********************************************************
   * Default field names for the ROMS & MARS files
   **********************************************************/
  //ROMS
  final public static String STR_XI_DIM_R = "xi_rho";
  final public static String STR_ETA_DIM_R = "eta_rho";
  final public static String STR_Z_DIM_R = "s_rho";
  final public static String STR_TIME_DIM_R = "time";
  final public static String STR_LON_R = "lon_rho";
  final public static String STR_LAT_R = "lat_rho";
  final public static String STR_MASK_R = "mask_rho";
  final public static String STR_BATHY_R = "h";
  final public static String STR_U3D_R = "u";
  final public static String STR_V3D_R = "v";
  final public static String STR_U2D_R = "ubar";
  final public static String STR_V2D_R = "vbar";
  final public static String STR_ZETA_R = "zeta";
  final public static String STR_TP_R = "temp";
  final public static String STR_SAL_R = "salt";
  final public static String STR_TIME_R = "scrum_time";
  final public static String STR_PN = "pn";
  final public static String STR_PM = "pm";
  final public static String STR_THETA_S = "theta_s";
  final public static String STR_THETA_B = "theta_b";
  final public static String STR_HC = "hc";

  //MARS
  final public static String STR_XI_DIM_M = "lon";
  final public static String STR_ETA_DIM_M = "lat";
  final public static String STR_Z_DIM_M = "z";
  final public static String STR_TIME_DIM_M = "time";
  final public static String STR_LON_M = "lon";
  final public static String STR_LAT_M = "lat";
  final public static String STR_BATHY_M = "h0";
  final public static String STR_U3D_M = "uz";
  final public static String STR_V3D_M = "vz";
  final public static String STR_U2D_M = "u";
  final public static String STR_V2D_M = "v";
  final public static String STR_ZETA_M = "xe";
  final public static String STR_TP_M = "temp";
  final public static String STR_SAL_M = "sal";
  final public static String STR_TIME_M = "time";
  final public static String STR_SIGMA = "z";

  /*********************************************
   * Names of variables in config file
   *********************************************/
  //Section names & commentaries
  final public static String STR_SECTION_CONFIG = "CONFIG";

  final public static String STR_SECTION_MODEL = "MODEL";

  final public static String STR_SECTION_SCHEME = "SCHEME";

  final public static String STR_SECTION_IO = "IO";
  final public static String STR_MAN_IO = "Input/output directories";

  final public static String STR_SECTION_TIME = "TIME";
  final public static String STR_MAN_TIME = "Time options";

  final public static String STR_SECTION_BIO = "BIO";
  final public static String STR_MAN_BIO = "Biology options";

  final public static String STR_SECTION_RELEASE = "RELEASE";
  final public static String STR_MAN_RELEASING = "Release options";

  final public static String STR_SECTION_RECRUIT = "RECRUITMENT";
  final public static String STR_MAN_RECRUIT = "Recruitment options";

  final public static String STR_SECTION_OPTION_SIMU = "SIMULATION";
  final public static String STR_MAN_OPTION_SIMU = "General options";

  final public static String STR_SECTION_SZONE = "RELEASE ZONE ";
  final public static String STR_MAN_SZONE = "Definition of the release zone";

  final public static String STR_SECTION_RZONE = "RECRUITMENT ZONE ";
  final public static String STR_MAN_RZONE =
      "Definition of the recruitment zone";

  final public static String STR_SECTION_NAMES = "FIELD NAMES";
  final public static String STR_MAN_NAMES =
      "Variable names in netcdf input file";

  //Variable names & commentaries
  final public static String STR_CFG_CONFIG = "config";
  final public static String STR_MAN_CONFIG = "SINGLE = " + SINGLE_SIMU
      + ", SERIAL = " + SERIAL_SIMU;

  final public static String STR_CFG_MODEL = "model";
  final public static String STR_MAN_MODEL = "ROMS = " + ROMS + ", MARS = "
      + MARS;

  final public static String STR_CFG_SCHEME = "scheme";
  final public static String STR_MAN_SCHEME = "EULER = " + EULER + ", RK4 = "
      + RK4;

  final public static String STR_CFG_2D3D = "dimension";
  final public static String STR_MAN_2D3D = "2D = " + SIMU_2D + ", 3D = "
      + SIMU_3D;

  final public static String STR_CFG_DIRECTORY_IN = "input path";
  final public static String STR_MAN_DIRECTORY_IN =
      "Path of the hydrodynamic model files";

  final public static String STR_CFG_DIRECTORY_OUT = "output path";
  final public static String STR_MAN_DIRECTORY_OUT = "Program output path";

  final public static String STR_CFG_CALENDAR = "calendar";
  final public static String STR_MAN_CALENDAR = "INTERANNUAL = " + INTERANNUAL
      + ", CLIMATO = " + CLIMATO;

  final public static String STR_CFG_T0 = "t0";
  final public static String STR_CFG_TRANSPORT_DURATION = "transport duration";
  final public static String STR_CFG_DT = "dt";
  final public static String STR_CFG_BUOY = "buoyancy";
  final public static String STR_CFG_EGG_DENSITY = "egg density";
  final public static String STR_CFG_AGE_LIMIT = "age limit";
  final public static String STR_CFG_GROWTH = "growth";
  final public static String STR_CFG_LETHAL_TP = "lethal temperature";
  final public static String STR_CFG_TP_EGG = "temperature egg";
  final public static String STR_CFG_TP_LARVAE = "temperature larva";
  final public static String STR_CFG_BIOMARKER_1 = "biomarker 1";
  final public static String STR_CFG_BIOMARKER_2 = "biomarker 2";

  final public static String STR_CFG_TYPE_RELEASE = "release";
  final public static String STR_MAN_TYPE_RELEASING = "ZONE = "
      + RELEASE_ZONE + ", FILE = " + RELEASE_FILE;

  final public static String STR_CFG_DRIFTERS = "drifter file";
  final public static String STR_CFG_NB_PARTICLES = "number particles";
  final public static String STR_CFG_DEPTH_MIN = "depth min";
  final public static String STR_CFG_DEPTH_MAX = "depth max";
  final public static String STR_CFG_PULSATION = "pulsation";
  final public static String STR_CFG_RELEASING_DT = "release dt";
  final public static String STR_CFG_NB_RELEASING_EVENTS =
      "number release events";
  final public static String STR_CFG_PATCHINESS = "patchiness";
  final public static String STR_CFG_NB_PATCHES = "number patches";
  final public static String STR_CFG_RADIUS_PATCH = "radius patch";
  final public static String STR_CFG_THICK_PATCH = "thickness patch";
  final public static String STR_CFG_NB_SZONES = "number zones";
  final public static String STR_CFG_RECRUIT = "recruitment";
  final public static String STR_MAN_TYPE_RECRUIT = "NONE = " + RECRUIT_NONE
      + ", AGE = " + RECRUIT_AGE + ", LENGTH = " + RECRUIT_LENGTH;
  final public static String STR_CFG_ISO_DEPTH_MVT = "isodepth transport";
  final public static String STR_CFG_ISO_DEPTH = "isodepth";

  final public static String STR_CFG_AGE_RECRUIT = "age criterion";
  final public static String STR_CFG_LENGTH_RECRUIT = "length criterion";
  final public static String STR_CFG_DURATION_RECRUIT = "duration min";
  final public static String STR_CFG_NB_RZONES = "number zones";
  final public static String STR_CFG_RECORD = "record";
  final public static String STR_MAN_RECORD = "0 = no record, 1 = statistics, 2 = tracks";
  final public static String STR_CFG_RECORD_DT = "record frequency";
  final public static String STR_CFG_NB_REPLICA = "number replica";

  final public static String STR_CFG_LON_ZONE = "lonP";
  final public static String STR_CFG_LAT_ZONE = "latP";
  final public static String STR_CFG_BATHY_MIN = "bathy min";
  final public static String STR_CFG_BATHY_MAX = "bathy max";
  final public static String STR_CFG_RED = "red";
  final public static String STR_CFG_BLUE = "blue";
  final public static String STR_CFG_GREEN = "green";
  final public static String STR_CFG_XI_DIM = "dim xi";
  final public static String STR_CFG_ETA_DIM = "dim eta";
  final public static String STR_CFG_Z_DIM = "dim z";
  final public static String STR_CFG_TIME_DIM = "dim time";
  final public static String STR_CFG_LON = "lon";
  final public static String STR_CFG_LAT = "lat";
  final public static String STR_CFG_MASK = "mask";
  final public static String STR_CFG_BATHY = "bathy";
  final public static String STR_CFG_U = "u";
  final public static String STR_CFG_V = "v";
  final public static String STR_CFG_ZETA = "zeta";
  final public static String STR_CFG_TP = "tp";
  final public static String STR_CFG_SAL = "sal";
  final public static String STR_CFG_TIME = "time";
  final public static String STR_CFG_PN = "pn";
  final public static String STR_CFG_PM = "pm";
  final public static String STR_CFG_THETA_S = "thetas";
  final public static String STR_CFG_THETA_B = "thetab";
  final public static String STR_CFG_HC = "hc";
  final public static String STR_CFG_SIGMA = "sigma";

}
