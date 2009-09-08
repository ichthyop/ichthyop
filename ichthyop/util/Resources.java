package ichthyop.util;

/**
 * This interface defined all the strings displayed in the application.
 * The file has been structured according to the application architecture.
 * For a french version of ichthyop the programmer will just have to create a
 * copy of this file, translating all the strings.
 *
 * <p>Note this is not the proper way to internationalize an application.
 * Java already offers some tools for internationalization
 * (it means to separate text, labels, messages, and other locale-sensitive
 * objects from the core source code), for instance the class
 * java.util.ResourceBundle.
 * Future development of the application should integrate it.</p>
 *
 * @see java.util.ResourceBundle for details about the apropriate way to
 * internationalizes the application.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */

public class Resources {

    ////////////////////
    // Application title
    ////////////////////

    final public static String TITLE_SHORT = "Ichthyop - ";

    final public static String TITLE_LARGE =
            "Ichthyop - Lagrangian tool for modelling ichthyoplankton dynamics";

    //////////////////////////
    // Main frame - Prefrences
    //////////////////////////

    final public static String BTN_OK = "Ok";

    final public static String BTN_CANCEL = "Cancel";

    final public static String TITLE_PREFERENCES =
            "Ichthyop - Display preferences";

    final public static String LBL_COLORBAR = "Colorbar extreme values";

    final public static String LBL_MIN_TP = "Tp min";

    final public static String LBL_MAX_TP = "Tp max";

    final public static String LBL_MIN_DEPTH = "Depth min";

    final public static String LBL_MAX_DEPTH = "Depth max";

    ///////////////////////
    // Main frame - Toolbar
    ///////////////////////

    final public static String BTN_START = "Start";

    final public static String BTN_STOP = "Stop";

    final public static String BTN_CAPTURE = "Capture pictures";
    
    final public static String CK_BOX_REPLAY = "Enable replay";

    final public static String BTN_EXIT = "Exit";

    final public static String TIP_BTN_EXIT =
            "Apply the changes and close the configuration editor";

    final public static String PRM_REFRESH = "Refresh";

    ////////////////////////
    // Main frame - Menu bar
    ////////////////////////

    final public static String MENU_FILE = "Configuration file";

    final public static String MENU_FILE_NEW = "New...";

    final public static String MENU_FILE_OPEN = "Open...";

    final public static String MENU_FILE_EDIT = "Edit...";

    final public static String MENU_FILE_PATH = "Change path";

    final public static String MENU_FILE_EXIT = "Exit";

    final public static String MENU_DISPLAY = "Display options";

    final public static String MENU_DISPLAY_BG_S = "Display release zones";

    final public static String MENU_DISPLAY_BG_R = "Display recruitment zones";

    final public static String MENU_CHART_DEPTH = "Chart - Depth distribution";

    final public static String MENU_CHART_EDGE = "Chart - Out of domain";

    final public static String MENU_CHART_RECRUIT = "Chart - Recruitment";

    final public static String MENU_CHART_LENGTH =
            "Chart - Length distribution";

    final public static String MENU_CHART_STAGE = "Chart - Stage distribution";

    final public static String MENU_CHART_MORTALITY = "Chart - Mortality";

    final public static String MENU_DISPLAY_NONE = "Particle colour - None";

    final public static String MENU_DISPLAY_BATHY = "Particle colour - Depth";

    final public static String MENU_DISPLAY_ZONE =
            "Particle colour - Release zone";

    final public static String MENU_DISPLAY_TP =
            "Particle colour - Temperature";

    final public static String MENU_DISPLAY_PREFERENCE = "Preferences";

    ///////////
    // Colorbar
    ///////////

    final public static String LBL_BATHY = "Bathymetry";

    final public static String LBL_DEPTH = "Particle depth";

    final public static String LBL_TP = "Water temperature";

    //////////////////////
    // Main frame - Slider
    //////////////////////

    final public static String LBL_TIME = "Time ";

    final public static String LBL_STEP = "Step ";

    /////////////
    // Status bar
    /////////////

    final public static String MSG_PREFIX = "Status: ";

    final public static String MSG_WAITING = "waiting for config file.";

    final public static String MSG_READY =
            "configuration file loaded. Ready to start.";

    final public static String MSG_OPEN = "Open file ";

    final public static String MSG_GET_FIELD = "get fields";

    final public static String MSG_REFRESH = "refresh trajectories";

    final public static String MSG_REPLAY = "replay mode. Move the slider.";

    final public static String MSG_INIT = "Initialize...";

    final public static String MSG_SETUP = "Setup...";

    final public static String MSG_COMPUTE = "compute ";


    /////////////////////////////////
    // Description of file extensions
    /////////////////////////////////

    final public static String EXTENSION_CONFIG = "Configuration file";

    final public static String EXTENSION_DRIFTER = "Drifter file";
    
    final public static String EXTENSION_NETCDF = "NetCDF file";

    /////////////
    // ZoneEditor
    /////////////

    final public static String TITLE_RECRUIT_ZONE_EDITOR = "Zones";

    final public static String TITLE_RELEASE_ZONE_EDITOR = "Zones";

    final public static String STR_NEW = "New";
    final public static String TIP_NEW_ZONE = "Create a new zone";

    final public static String STR_DUPLICATE = "Duplicate";
    final public static String TIP_DUPLICATE = "Duplicate the selected zone";

    final public static String STR_DELETE = "Delete";
    final public static String TIP_DELETE = "Delete the selected zone";

    final public static String STR_ZONE = "Zone";

    final public static String LBL_GEOG_AREA = "Geographical area";

    final public static String LBL_LON = "Lon (east)"; // also used in tab model

    final public static String LBL_LAT = "Lat (north)"; // also used in tab model

    final public static String LBL_P1 = "NW Corner";

    final public static String LBL_P2 = "NE Corner";

    final public static String LBL_P3 = "SE Corner";

    final public static String LBL_P4 = "SW Corner";

    final public static String LBL_BATHYMETRIC_MASK = "Bathymetric mask";

    final public static String LBL_BATHY_LINE_MIN = "Lower line";

    final public static String LBL_BATHY_LINE_MAX = "Higher line";

    final public static String LBL_COLOR_ZONE = "Color";

    final public static String TITLE_COLOR_CHOOSER = "Choose zone colour";

    /////////////////////////////////
    // Configuration editor - Toolbar
    /////////////////////////////////

    final public static String BTN_SAVE = "Save";
    final public static String TIP_BTN_SAVE = "Save the configuration file";

    final public static String BTN_SAVEAS = "Save as";
    final public static String TIP_BTN_SAVEAS =
            "Save the file under a new name";

    final public static String BTN_APPLY_EXIT = "Apply & Exit";

    ////////////////
    //Control charts
    ////////////////

    final public static String CHART_TITLE_DEPTH = "Depth distribution";

    final public static String CHART_TITLE_OUT = "Out of the domain";

    final public static String CHART_TITLE_LENGTH = "Length distribution";

    final public static String CHART_TITLE_DEAD_COLD = "Mortality";

    final public static String CHART_TITLE_RECRUITMENT = "Recruitment";

    final public static String CHART_TITLE_STAGE = "Stage distribution";

    final public static String CHART_LEGEND_TIME = "Time step";

    final public static String CHART_LEGEND_NB_PARTICLES =
            "Number of particles";

    final public static String CHART_LEGEND_LENGTH = "Length";

    final public static String CHART_LEGEND_DEPTH = "Depth";

    //////////////////////////////////
    //Configuration editor - Tab names
    //////////////////////////////////

    final public static String TITLE_CONFIG_EDITOR = "Configuration editor";

    final public static String TAB_MODEL = "Model";

    final public static String TAB_RELEASE = "Release";

    final public static String TAB_RECRUITMENT = "Recruitment";

    final public static String TAB_TRANSPORT = "Transport";

    final public static String TAB_TIME = "time";

    final public static String TAB_IO = "IO";

    final public static String TAB_BIOLOGY = "Biology";

    final public static String TAB_VARIABLE = "Variable";


    /////////
    // Tab IO
    /////////

    final public static String LBL_INPUT_PATH = "Input path";
    final public static String TIP_BTN_INPUT_PATH =
            "Choose an input path (path or OPeNDAP)";

    final public static String LBL_OUTPUT_PATH = "Output path";
    final public static String TIP_BTN_OUTPUT_PATH = "Choose an output path";

    final public static String LBL_FILE_FILTER = "Filename filter";
    final public static String TIP_TXT_FILTER = "Type a filename filter";
    
    final public static String CK_BOX_GRIDFILE = "Specify a gridfile";
    final public static String TIP_CK_BOX_GRIDFILE =
            "Specify that the grid variables must be read from an independant file";
    
    final public static String LBL_GRIDFILE = "Gridfile path";
    final public static String TIP_BTN_GRIDFILE = "Pathname of the gridfile";

    final public static String LBL_DRIFTER_PATH = "Drifter pathname";
    final public static String TIP_BTN_DRIFTER_FILE =
            "Select the particle initial coordinates from an input file";

    final public static String CK_BOX_RECORD_NC = "Record tracks";
    final public static String TIP_CK_BOX_RECORD_NC =
            "Generate a NetCDF output file for particle tracking";

    final public static String LBL_OUTPUT_FILENAME = "Filename";
    final public static String TIP_TXT_OUTPUT_FILENAME =
            "Type an output filename";

    final public static String PRM_RECORD_FREQUENCY = "Record frequency";
    final public static String TIP_PRM_RECORD_FREQUENCY =
            "Set the record frequency as a number of computational time-steps";

    ////////////
    // Tab Model
    ////////////

    final public static String RD_BTN_ROMS = "ROMS";
    final public static String TIP_BTN_ROMS = "Select the ROMS simulation";

    final public static String RD_BTN_MARS = "MARS";
    final public static String TIP_BTN_MARS = "Select the MARS simulation";

    final public static String RD_BTN_EULER = "Euler";
    final public static String TIP_BTN_EULER = "Use a forward Euler scheme";

    final public static String RD_BTN_RK4 = "Runge Kutta 4";
    final public static String TIP_BTN_RK4 =
            "Use a Runge-Kutta 4th order scheme";

    final public static String CK_BOX_RANGE = "Resize domain of simulation";
    final public static String TIP_CK_BOX_RANGE =
            "Resize the simulated domain into a smaller area";

    final public static String LBL_BOUNDARY = "Dataset boundaries";

    final public static String LBL_MINIMA = "Minima";

    final public static String LBL_MAXIMA = "Maxima";

    ///////////
    // Tab Time
    ///////////

    final public static String RD_BTN_FORWARD = "Forward";
    final public static String TIP_RD_BTN_FORWARD =
            "Run the simulation forward in time";

    final public static String RD_BTN_BACKWARD = "Backward";
    final public static String TIP_RD_BTN_BACKWARD =
            "Run the simulation backward in time";

    final public static String RD_BTN_TIME_REAL = "Gregorian calendar";
    final public static String TIP_BTN_GREGORIAN_CALENDAR =
            "Use a Gregorian calendar";

    final public static String RD_BTN_TIME_CLIMATO = "Climato calendar";
    final public static String TIP_RD_BTN_CLIMATO_CALENDAR =
            "Use a 360 days per year calendar";

    final public static String PRM_BEGIN_SIMU = "Beginning of simulation";
    final public static String TIP_PRM_BEGIN_SIMU =
            "Set the date of beginning of simulation";

    final public static String PRM_DURATION_TRANSPORT = "Duration of transport";
    final public static String TIP_PRM_DURATION_TRANSPORT =
            "Set the duration of particle transport";

    final public static String PRM_INTERNAL_DT = "Computational time-step (dt)";
    final public static String TIP_PRM_DT = "Set the computational time-step";

    final public static String BTN_FILL_T0 = "Set to first time record";
    final public static String TIP_BTN_FILL_T0 =
            "Set the parameter to the first time record available";

    final public static String BTN_FILL_DURATION = "Set to max duration";
    final public static String TIP_BTN_FILL_DURATION =
            "Set the parameter to the maximum possible duration";

    final public static String BTN_FILL_DT = "Set to suitable dt";
    final public static String TIP_BTN_FILL_DT =
            "Set the parameter to the suitable dt calculated for Vmax = 1m/s";

    final public static String LBL_ORIGIN = "Time origin";
    final public static String TIP_TXT_ORIGIN =
            "Set the time origin of the gregorian calendar";

    final public static String LBL_FIRST_TIME = "First time record available";

    final public static String LBL_MAX_DURATION = "Maximum possible duration";

    final public static String LBL_DT = "Dataset dt";

    final public static String LBL_DT_RECORD = "Dataset record dt";

    final public static String LBL_DT_CFL =
            "Suitable dt calculated for Vmax = 1m/s";

    ////////////
    // Transport
    ////////////

    final public static String RD_BTN_2D = "2D";
    final public static String TIP_BTN_2D = "Simulate in two dimensions";

    final public static String RD_BTN_3D = "3D";
    final public static String TIP_BTN_3D = "Simulate in three dimensions";

    final public static String CK_BOX_ADVECTH = "Horizontal advection";

    final public static String CK_BOX_ADVECTV = "Vertical advection";

    final public static String CK_BOX_VDISP = "Vertical dispersion";
    final public static String TIP_CK_BOX_VDISP =
            "Enable/disable vertical dispersion";

    final public static String CK_BOX_HDISP = "Horizontal dispersion";
    final public static String TIP_CK_BOX_HDISP =
            "Enable/disable horizontal dispersion";

    final public static String CK_BOX_MIGRATION = "Diel vertical migration";
    final public static String TIP_CK_BOX_MIGRATION =
            "Enable/disable diel vertical migration";

    final public static String PRM_AGE_LIMIT_MIGRATION = "Minimum age";
    public static String TIP_PRM_AGE_LIMIT_MIGRATION =
            "Set the age from which vertical migration is applied";

    final public static String PRM_DEPTH_DAY = "Depth at daytime";
    final public static String TIP_PRM_DEPTH_DAY =
            "Set the particle depth during the day";

    final public static String PRM_DEPTH_NIGHT = "Depth at nighttime";
    final public static String TIP_PRM_DEPTH_NIGHT =
            "Set the particle depth during the night";

    final public static String CK_BOX_BUOYANCY = "Buoyancy";
    final public static String TIP_CK_BOX_BUOYANCY = "Enable/disable buoyancy";

    final public static String PRM_EGG_DENSITY = "Egg density";
    final public static String TIP_PRM_EGG_DENSITY = "Set the egg density";

    final public static String PRM_AGE_LIMIT_BUOYANCY = "Maximum age";
    final public static String TIP_PRM_AGE_LIMIT_BUOY =
            "Set the age until which buoyancy is applied";

    //////////
    // Biology
    //////////

    final public static String CK_BOX_GROWTH = "Growth";
    final public static String TIP_CK_BOX_GROWTH =
            "Simulates the growth in length of particles";
    final public static String TEXT_GROWTH =
            "Default growth model. Please refer to source file Growth.java";

    final public static String CK_BOX_PLAKTON = "Plankton limitation";
    final public static String TIP_CK_BOX_PLANKTON =
            "Set a growth limiting factor as a function of plankton availability";
    final public static String TEXT_PLANKTON =
            "Default plankton availability model. Please refer to source file Growth.java";

    final public static String CK_BOX_LETHAL_TP = "Lower lethal temperature";
    final public static String TIP_CK_BOX_LETHAL_TP =
            "Set the lower lethal water temperature";

    final public static String PRM_LETHAL_TP_EGG = "For eggs";
    final public static String TIP_PRM_LETHAL_TP_EGG =
            "Set the lower lethal water temperature for eggs";

    final public static String PRM_LETHAL_TP_LARVAE = "For larvae";
    final public static String TIP_PRM_LETHAL_TP_LARVAE =
            "Set the lower lethal water temperature for larvae";

    //////////
    // Release
    //////////

    final public static String RD_BTN_RELEASE_ZONE = "From zone";
    final public static String TIP_RD_BTN_RELEASE_ZONE =
            "Randomly release the particles within pre-defined zones";

    final public static String RD_BTN_RELEASE_FILE = "From file";
    final public static String TIP_RD_BTN_RELEASE_FILE =
            "Read particle initial coordinates from a text or NetCDF input file";

    final public static String CK_BOX_PULSATION = "Multi-release events";
    final public static String TIP_CK_BOX_PULSATION =
            "Simulate multi-release events";

    final public static String CK_BOX_PATCHINESS = "Patchiness";
    final public static String TIP_CK_BOX_PATCHINESS =
            "Simulate a patchy initial spatial distribution of particles";

    final public static String PRM_NB_RELEASED = "Number of particles";
    final public static String TIP_PRM_NB_RELEASED =
            "Set the number of particles used in the simulation";

    final public static String PRM_DEPTH_RELEASING_MIN = "Release depth min";
    final public static String TIP_PRM_DEPTH_MIN =
            "Define the minimum depth for particle release";

    final public static String PRM_DEPTH_RELEASING_MAX = "Release depth max";
    final public static String TIP_PRM_DEPTH_MAX =
            "Define the maximum depth for particle release";

    final public static String PRM_NB_RELEASE_VENTS =
            "Number of release events";
    final public static String TIP_PRM_NB_EVENTS =
            "Set the number of release events";

    final public static String PRM_RELEASE_DT =
            "Time between two release events";
    final public static String TIP_PRM_RELEASE_DT =
            "Setsthe time between two release events";

    final public static String PRM_NB_PATCHES = "Number of patches";
    final public static String TIP_PRM_NB_PACTHES = "Set the number of patches";

    final public static String PRM_RADIUS_PATCH = "Patch radius";
    final public static String TIP_PRM_RADIUS_PATCH = "Set patches radius";

    final public static String PRM_THICKNESS_PATCH = "Patch thickness";
    final public static String TIP_PRM_THICKNESS_PATCH =
            "Set patches thickness";

    ///////////
    // Variable
    ///////////

    final public static String BTN_APPLY = "Apply";
    final public static String TIP_BTN_APPLY = "Apply the changes";

    final public static String BTN_DEFAULT = "Default";
    final public static String TIP_BTN_DEFAULT = "Use default names";

    final public static String BTN_RESET = "Reset";
    final public static String TIP_BTN_RESET = "Undo all changes";

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

    // LBL_TIME is already defined above, for Main frame - Slider

    final public static String LBL_TEMPERATURE = "Temperature";

    final public static String LBL_SAL = "Salinity";

    final public static String LBL_THETAS = "S-coord surface control parameter";

    final public static String LBL_THETAB = "S-coord bottom control parameter";

    final public static String LBL_HC = "S-coord surface/bottom layer width";

    final public static String LBL_KV = "Vertical turbulent diffusion";

    //////////////
    // Recruitment
    //////////////

    final public static String RD_BTN_RECRUIT_NONE = "None";
    final public static String TIP_RD_BTN_RECRUIT_NONE = "No recruitment";

    final public static String RD_BTN_RECRUIT_AGE = "Age criteria";
    final public static String TIP_RD_BTN_RECRUIT_AGE =
            "Test recruitment using a criterion based on particle age";

    final public static String RD_BTN_RECRUIT_LENGTH = "Length criteria";
    final public static String TIP_RD_BTN_RECRUIT_LENGTH =
            "Test recruitment using a criterion based on particle length";

    final public static String PRM_RECRUIT_AGE = "Age min for recruitment";
    final public static String TIP_PRM_RECRUIT_AGE =
            "Set the minimal age for recruitment";

    final public static String PRM_RECRUIT_LENGTH =
            "Length min for recruitment";
    final public static String TIP_PRM_RECRUIT_LENGTH =
            "Set the minimal length for recruitment";

    final public static String PRM_RECRUIT_DURATION_MIN =
            "Duration min to spend in recruitment area";
    final public static String TIP_RECRUIT_DURATION_MIN = "Set the minimal duration a particle has to spend within a recruitment zone before recruitment";

    final public static String CK_BOX_RECRUIT_DEPTH = "Add depth criterion";
    final public static String TIP_CK_BOX_RECRUIT_DEPTH =
            "Add a recruitment criterion based on particle depth";
    
    final public static String PRM_RECRUIT_MIN_DEPTH = "Depth min";
    final public static String TIP_PRM_RECRUIT_MIN_DEPTH =
            "Set the top layer depth";
    
    final public static String PRM_RECRUIT_MAX_DEPTH = "Depth max";
    final public static String TIP_PRM_RECRUIT_MAX_DEPTH =
            "Set the bottom layer depth";
    
    final public static String CK_BOX_STOP_MOVING = "Stop moving once recruited";
    final public static String TIP_CK_BOX_STOP_MOVING =
            "Immobilizes the particle once it has been recruited";
    
    ////////
    // Units
    ////////

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

    //---------- End of interface
}
