package ichthyop.util;

/**
 * This interface gathers all the strings used to structure the file of
 * configuration. It basically provides three types of strings:
 * <ul>
 * <li> name of section. Declared as
 * <code>SECTION_the_name_of_the_section</code>
 * <li> name of the property
 * <li> a short legend for some properties or sections.
 * Declared as <code>MAN_the_legend</code>
 * </ul>
 *
 * Note: no String are defined for the variable name properties.
 * The application uses instead the name of the elements listed in enum
 * ichthyop.util.NCField. Example for the longitude field. The property name is
 * set to NCField.lon.name();
 *
 * @see ichthyop.util.INIFile for details about the file of configuration
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *  @author P.Verley
 */
public interface Structure {

    /////////////
    // SIMULATION
    /////////////

    final public static String SECTION_SIMULATION = "SIMULATION";
    final public static String MAN_SECTION_SIMULATION = "Options for the run";

    final public static String NB_REPLICA = "number replica";

    final public static String RUN = "run";
    final public static String MAN_RUN = "SINGLE = " + Constant.SINGLE
                                         + ", SERIAL = " + Constant.SERIAL;
    
    final public static String VERSION = "version";
    final public static String MAN_VERSION = "Corresponding Ichthyop version";

    /////
    // IO
    /////

    final public static String SECTION_IO = "IO";
    final public static String MAN_SECTION_IO = "Input & output options";

    final public static String INPUT_PATH = "input path";
    final public static String MAN_INPUT_PATH =
            "Path of the hydrodynamic model files";

    final public static String FILTER = "file filter";
    final public static String MAN_FILTER =
            "File filter allowing ? and * metacharacters just like shell filters";
    
    final public static String GRIDFILE = "gridfile";
    final public static String MAN_GRIDFILE =
            "If true, the grid file is read from and independant file";
    
    final public static String PATH_GRIDFILE = "gridfile path";

    final public static String DRIFTER = "drifter file";

    final public static String OUTPUT_PATH = "output path";
    final public static String MAN_OUTPUT_PATH = "Program output path";

    final public static String OUTPUT_FILENAME = "output filename";

    final public static String RECORD = "record";
    final public static String MAN_RECORD =
            "If TRUE, generates a NetCDF output file";

    final public static String RECORD_DT = "record frequency";

    ////////
    // MODEL
    ////////

    final public static String SECTION_MODEL = "MODEL";
    final public static String MAN_SECTION_MODEL = "Options of the model";

    final public static String MODEL = "model";
    final public static String MAN_MODEL = "ROMS = " + Constant.ROMS +
                                           ", MARS = " + Constant.MARS;

    final public static String SCHEME = "scheme";
    final public static String MAN_SCHEME = "EULER = " + Constant.EULER +
                                            ", RK4 = " + Constant.RK4;

    final public static String RANGE = "range";

    final public static String LAT = "lat";

    final public static String LON = "lon";

    ////////////
    // TRANSPORT
    ////////////
    final public static String SECTION_TRANSPORT = "TRANSPORT";
    final public static String MAN_SECTION_TRANSPORT =
            "Options for transport of the particles";

    final public static String DIMENSION = "dimension";
    final public static String MAN_DIMENSION = "2D = " + Constant.SIMU_2D +
                                               ", 3D = " + Constant.SIMU_3D;

    final public static String BUOYANCY = "buoyancy";

    final public static String EGG_DENSITY = "egg density";

    final public static String BUOYANCY_AGE_LIMIT = "buoyancy age limit";

    final public static String MIGRATION = "vertical migration";

    final public static String MIGRATION_AGE_LIMIT = "migration age limit";

    final public static String MIGRATION_DEPTH_DAY = "depth day";

    final public static String MIGRATION_DEPTH_NIGHT = "depth night";

    final public static String HDISP = "horizontal dispersion";

    final public static String VDISP = "vertical dispersion";


    ///////
    // TIME
    ///////

    final public static String SECTION_TIME = "TIME";
    final public static String MAN_SECTION_TIME = "Time options";

    final public static String ARROW = "arrow";
    final public static String MAN_ARROW = "Forward = "
                                           + Constant.FORWARD
                                           + ", Backward = "
                                           + Constant.BACKWARD;

    final public static String CALENDAR = "calendar";
    final public static String MAN_CALENDAR = "Gregorian = " +
                                              Constant.GREGORIAN
                                              + ", Climato = " +
                                              Constant.CLIMATO;

    final public static String TIME_ORIGIN = "time origin";

    final public static String T0 = "t0";

    final public static String TRANSPORT_DURATION = "transport duration";

    final public static String DT = "dt";


    ///////////
    // VARIABLE
    ///////////
    final public static String SECTION_VARIABLE = "VARIABLE";

    final public static String MAN_SECTION_VARIABLE =
            "Variable names in NetCDF input file";

    /*
         The properties of this section are the names of the elements
     listed in enum util/NCField.java
     Example for the longitude field. The corresponding name of the
     property is NCField.lon.name()
     */

    //////////
    // RELEASE
    //////////

    final public static String SECTION_RELEASE = "RELEASE";
    final public static String MAN_SECTION_RELEASE = "Release options";

    final public static String TYPE_RELEASE = "release";
    final public static String MAN_TYPE_RELEASE = "Zone = "
                                                  + Constant.RELEASE_ZONE +
                                                  ", Text file = " +
                                                  Constant.RELEASE_TXTFILE +
                                                  ", Netcdf file = "
                                                  + Constant.RELEASE_NCFILE;

    final public static String NB_PARTICLES = "number particles";

    final public static String DEPTH_MIN = "depth min";

    final public static String DEPTH_MAX = "depth max";

    final public static String PULSATION = "pulsation";

    final public static String RELEASE_DT = "release dt";

    final public static String NB_RELEASE_EVENTS =
            "number release events";

    final public static String PATCHINESS = "patchiness";

    final public static String NB_PATCHES = "number patches";

    final public static String RADIUS_PATCH = "radius patch";

    final public static String THICK_PATCH = "thickness patch";

    //////////////
    // RECRUITMENT
    //////////////

    final public static String SECTION_RECRUIT = "RECRUITMENT";
    final public static String MAN_RECRUIT = "Recruitment options";

    final public static String RECRUIT = "recruitment";
    final public static String MAN_TYPE_RECRUIT = "none = " +
                                                  Constant.NONE
                                                  + "; age = " +
                                                  Constant.RECRUIT_AGE +
                                                  "; length = " +
                                                  Constant.RECRUIT_LENGTH;

    final public static String AGE_RECRUIT = "age criterion";

    final public static String LENGTH_RECRUIT = "length criterion";

    final public static String DURATION_RECRUIT = "duration min";
    
    final public static String DEPTH_RECRUIT = "depth criterion";
    
    // DEPTH_MIN & DEPTH MAX already declared for RELEASE section
    //final public static String DEPTH_MIN = "depth min";

    //final public static String DEPTH_MAX = "depth max";
    
    final public static String STOP = "stop moving";

    //////////
    // BIOLOGY
    //////////

    final public static String SECTION_BIO = "BIOLOGY";
    final public static String MAN_BIO = "Biology options";

    final public static String GROWTH = "growth";

    final public static String PLANKTON = "plankton";

    final public static String LETHAL_TP = "lethal temperature";

    final public static String LETHAL_TP_EGG = "temperature egg";

    final public static String LETHAL_TP_LARVA = "temperature larva";

    ///////
    // ZONE
    ///////

    final public static String SECTION_RELEASE_ZONE = "RELEASE ZONE ";
    final public static String MAN_RELEASE_ZONE = "Definition of the release zone";

    final public static String SECTION_RECRUITMENT_ZONE = "RECRUITMENT ZONE ";
    final public static String MAN_RECRUITMENT_ZONE =
            "Definition of the recruitment zone";

    final public static String LON_ZONE = "lonP";

    final public static String LAT_ZONE = "latP";

    final public static String BATHY_MIN = "bathy min";

    final public static String BATHY_MAX = "bathy max";

    final public static String RED = "red";

    final public static String BLUE = "blue";

    final public static String GREEN = "green";

    //---------- End of interface
}
