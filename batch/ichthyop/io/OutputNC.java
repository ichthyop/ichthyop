package ichthyop.io;

/** import java.io */
import java.io.IOException;

/** import java.util */
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/** local import */
import ichthyop.core.Population;
import ichthyop.core.Simulation;
import ichthyop.util.IParticle;

/** import netcdf */
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ichthyop.util.Constant;

/**
 * The output manager; creates NetCDF output files, one per simulation.
 * The structure of the file follows the NetCDF Climate and Forecast (CF)
 * Metadata Conventions1. In the file are stored for each particle at every
 * time step information about location, status, temperature and salinity of
 * the surrounding water, recruitment, etc.
 * <br>
 * By default, the program records the time, longitude, latitude, depth,
 * death (alive, beached, out of the domain, dead cold), temperature and
 * salinity. It also records the length, the current zone and the state of
 * recruitment when the growth and recruitment (see sections 4.1.9 and 4.3)
 * processes are activated. The parameters used in the simulation are also recorded in the file, as global attributes.
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * <p>Company: </p>
 *
 * @author P.Verley
 */
public class OutputNC {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Object for creating/writing netCDF files.
     */
    private static NetcdfFileWriteable ncOut;
    /**
     * The {@code Population} object for scanning the collection of particles
     */
    private static Population population;
    /**
     * The number of recruitment zones
     */
    private static int nbRecruitmentZones;
    /**
     * <code>true</code> for a 3D simulation,
     * <code>false</code>otherwise
     */
    private static boolean FLAG_3D;
    /**
     * <code>true</code> if growth is simulated,
     * <code>false</code>otherwise
     */
    private static boolean FLAG_GROWTH;
    /**
     * <code>true</code> if recrtuiment is enabled,
     * <code>false</code>otherwise
     */
    private static boolean FLAG_RECRUITMENT;
    /**
     * <code>true</code> if the mode "Release from zone" is enable,
     * <code>false</code>otherwise
     */
    private static boolean FLAG_RELEASED_ZONE;
    /**
     *  <code>true</code> when food limitation for growth model is simulated,
     * <code>false</code>otherwise
     */
    private static boolean FLAG_PLANKTON;
    /**
     * NetCDF time dimension (unlimited)
     */
    private static Dimension time;
    /**
     * NetCDF drifter dimension: the number of particles.
     */
    private static Dimension drifter;
    /**
     * NetCDF zone dimension: number of release zones.
     */
    private static Dimension zone;
    /**
     * Array of rank 2, specialized for floats. Stores longitude.
     */
    private static ArrayFloat.D2 arrLon;
    /**
     * Array of rank 2, specialized for floats. Stores latitude.
     */
    private static ArrayFloat.D2 arrLat;
    /**
     * Array of rank 2, specialized for floats. Stores depth.
     */
    private static ArrayFloat.D2 arrDepth;
    /**
     * Array of rank 2, specialized for floats. Stores temperature.
     */
    private static ArrayFloat.D2 arrTp;
    /**
     * Array of rank 2, specialized for floats. Stores salinity.
     */
    private static ArrayFloat.D2 arrSal;
    /**
     * Array of rank 2, specialized for floats. Stores length.
     */
    private static ArrayFloat.D2 arrLength;
    /**
     * Array of rank 2, specialized for floats. Stores large phytoplankton
     * concentration.
     */
    private static ArrayFloat.D2 arrLargePhyto;
    /**
     * Array of rank 2, specialized for floats. Stores large zooplankton
     * concentration.
     */
    private static ArrayFloat.D2 arrLargeZoo;
    /**
     * Array of rank 2, specialized for floats. Stores small zooplankton
     * concentration.
     */
    private static ArrayFloat.D2 arrSmallZoo;
    /**
     * Array of rank 2, specialized for floats. Stores time.
     */
    private static ArrayDouble.D1 arrTime;
    /**
     * Array of rank 1, specialized for doubles. Stores zone number.
     */
    private static ArrayInt.D2 arrZone;
    /**
     * Array of rank 2, specialized for integers. Stores death status.
     */
    private static ArrayInt.D2 arrDeath;
    /**
     * Array of rank 3, specialized for integers. Stores recruitment status.
     */
    private static ArrayInt.D3 arrRecruit;
    /**
     * Current time rank for writing
     */
    private static int i_record;


////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates the NetCDF file after having added all of the Dimensions,
     * Variables, and Attributes
     *
     * @param i_simu an int, the index of the current simulation
     * @param nb_simu an int, the total number of simulations.
     * @param typeSimu an int, characterizing the mode, SINGLE or SERIAL
     * @throws an IOException if any output exception occurs while creating
     * the file.
     */
    public static void create(int i_simu, int nb_simu, int typeSimu) throws
            IOException {

        String strNcOut;

        /** Ensures ouput path not null. Throws an exception otherwise */
        if (Configuration.getDirectorOut() == null) {
            throw new IOException("Ouput path incorrect " +
                                  Configuration.getDirectorOut());
        }

        // Determines output file name
        if (typeSimu == Constant.SERIAL) {
            int n = 10;
            int nf_simu = 1;
            int nf_isimu = 1;
            while ((nb_simu % n) != nb_simu) {
                n *= 10;
                nf_simu++;
            }
            n = 10;
            while ((i_simu % n) != i_simu) {
                n *= 10;
                nf_isimu++;
            }

            strNcOut = Configuration.getDirectorOut()
                       + Configuration.getOutputFilename() + "_";
            for (int i = 0; i < (nf_simu - nf_isimu); i++) {
                strNcOut += String.valueOf(0);
            }
            strNcOut += String.valueOf(i_simu) + ".nc";

        } else {
            strNcOut = Configuration.getDirectorOut()
                       + Configuration.getOutputFilename() + ".nc";
        }

        // Create new netcdf file and add dimensions.
        ncOut = NetcdfFileWriteable.createNew(strNcOut, false);
        drifter = ncOut.addDimension("drifter", Configuration.getNbParticles());
        time = ncOut.addUnlimitedDimension("time");
        if (Configuration.getTypeRecruitment() != Constant.NONE) {
            zone = ncOut.addDimension("recruit_zone",
                                      Configuration.getRecruitmentZones().size());
        }

        // Add variables
        addVar2NcOut(Field.TIME);
        addVar2NcOut(Field.LONGITUDE);
        addVar2NcOut(Field.LATITUDE);
        addVar2NcOut(Field.DEPTH);
        addVar2NcOut(Field.DEATH);
        /*if (Configuration.is3D()) {
            addVar2NcOut(Field.TEMPERATURE);
            addVar2NcOut(Field.SALINITY);
        }
        if (Configuration.isGrowth()) {
            addVar2NcOut(Field.LENGTH);
            if (Configuration.isPlankton()) {
                addVar2NcOut(Field.LARGE_PHYTO);
                addVar2NcOut(Field.LARGE_ZOO);
                addVar2NcOut(Field.SMALL_ZOO);
            }
        }
        if (Configuration.getTypeRelease() == Constant.RELEASE_ZONE) {
            addVar2NcOut(Field.ZONE);
        }*/
        if (Configuration.getTypeRecruitment() != Constant.NONE) {
            addVar2NcOut(Field.RECRUITED);
        }

        // Add global attributes
        addGlobalAttribute2NcOut();

        ncOut.create();
        System.out.println("Creating output file : " + ncOut.getLocation());
    }

    /**
     * Adds all the global attributes to the NetCDF file. Here the attributes
     * are the options of the current set of parameters.
     */
    private static void addGlobalAttribute2NcOut() {

        ncOut.addGlobalAttribute("title", "drifter monitoring");
        ncOut.addGlobalAttribute("model",
                                 Configuration.getTypeModel() == Constant.ROMS ?
                                 "roms" : "mars");
        ncOut.addGlobalAttribute("scheme",
                                 Configuration.getScheme() == Constant.EULER ?
                                 "euler" : "rk4");
        ncOut.addGlobalAttribute("transport_duration",
                                 String.valueOf(Simulation.getTransportDuration()));
        ncOut.addGlobalAttribute("transport_duration_units", "second");
        ncOut.addGlobalAttribute("dt", String.valueOf(Configuration.get_dt()));
        ncOut.addGlobalAttribute("dt_expl", "computational time step");
        ncOut.addGlobalAttribute("dt_units", "second");
        if (Configuration.getTypeRelease() == Constant.RELEASE_ZONE) {
            ncOut.addGlobalAttribute("release_depth_min",
                                     String.valueOf(Simulation.
                    getDepthReleaseMin()));
            ncOut.addGlobalAttribute("release_depth_max",
                                     String.valueOf(Simulation.
                    getDepthReleaseMax()));
            ncOut.addGlobalAttribute("release_depth_units", "meter");
            if (Configuration.isPulsation()) {
                ncOut.addGlobalAttribute("release_dt",
                                         String.valueOf(Simulation.getReleaseDt()));
                ncOut.addGlobalAttribute("release_dt_expl",
                                         "time between two release events");
                ncOut.addGlobalAttribute("release_dt_units", "second");
                ncOut.addGlobalAttribute("number_release_event",
                                         String.valueOf(Simulation.
                        getNbReleaseEvents()));
            }
            if (Configuration.isPatchiness()) {
                ncOut.addGlobalAttribute("number_patches",
                                         String.valueOf(Configuration.
                        getNbPatches()));
                ncOut.addGlobalAttribute("patch_radius",
                                         String.valueOf(Simulation.
                        getRadiusPatchi()));
                ncOut.addGlobalAttribute("patch_radius_units", "meter");
                ncOut.addGlobalAttribute("patch_thickness",
                                         String.valueOf(Simulation.
                        getThickPatchi()));
                ncOut.addGlobalAttribute("patch_thickness_units", "meter");
            }
        }
        if (Configuration.isLethalTp()) {
            ncOut.addGlobalAttribute("tp_egg",
                                     String.valueOf(Simulation.getLethalTpEgg()));
            ncOut.addGlobalAttribute("tp_egg_expl",
                                     "lower lethal temperature for egg");
            ncOut.addGlobalAttribute("tp_egg_units", "celsius");
            if (Configuration.isGrowth()) {
                ncOut.addGlobalAttribute("tp_larva",
                                         String.valueOf(Simulation.
                        getLethalTpLarvae()));
                ncOut.addGlobalAttribute("tp_larva_expl",
                                         "lower lethal temperature for larva");
                ncOut.addGlobalAttribute("tp_larva_units", "celsius");
            }
        }
        if (Configuration.isBuoyancy()) {
            ncOut.addGlobalAttribute("egg_dentity",
                                     String.valueOf(Simulation.getEggDensity()));
            ncOut.addGlobalAttribute("egg_dentity_units", "g.cm-3");
        }
        if (Configuration.getTypeRecruitment() == Constant.RECRUIT_AGE) {
            ncOut.addGlobalAttribute("age_recruit",
                                     String.valueOf(Simulation.
                    getAgeMinAtRecruitment()));
            ncOut.addGlobalAttribute("age_recruit_units", "day");
        }
        if (Configuration.getTypeRecruitment() == Constant.RECRUIT_LENGTH) {
            ncOut.addGlobalAttribute("length_recruit",
                                     String.valueOf(Simulation.
                    getLengthMinAtRecruitment()));
            ncOut.addGlobalAttribute("length_recruit_units", "millimeter");
        }
        if (Configuration.getTypeRecruitment() != Constant.NONE) {
            ncOut.addGlobalAttribute("duration_min",
                                     String.valueOf((float) Configuration.
                    getDurationInRecruitArea()
                    / (float) Constant.ONE_DAY));
            ncOut.addGlobalAttribute("duration_min_expl",
                                     "duration min in recruitment zone before being recruited");
            ncOut.addGlobalAttribute("duration_min_units", "second");
            if (Configuration.isDepthRecruitment()) {
                ncOut.addGlobalAttribute("depth_min_recruit",
                        String.valueOf(Simulation.getMinDepthRecruitment()));
                ncOut.addGlobalAttribute("depth_min_recruit_units", "meter");
                ncOut.addGlobalAttribute("depth_min_recruit_expl",
                        "particle minimum depth for being recruited");
                ncOut.addGlobalAttribute("depth_max_recruit",
                        String.valueOf(Simulation.getMaxDepthRecruitment()));
                ncOut.addGlobalAttribute("depth_max_recruit_units", "meter");
                ncOut.addGlobalAttribute("depth_max_recruit_expl",
                        "particle maximum depth for being recruited");
            }
        }
        if (Configuration.isSerial()) {
            ncOut.addGlobalAttribute("replica",
                                     String.valueOf(Simulation.getReplica()));
        }
        if (Configuration.isMigration()) {
            ncOut.addGlobalAttribute("depth_day",
                                     String.valueOf(Simulation.getDepthDay()));
            ncOut.addGlobalAttribute("depth_day_expl",
                                     "daytime depth of DVM scheme");
            ncOut.addGlobalAttribute("depth_day_unit", "meter");
            ncOut.addGlobalAttribute("depth_night",
                                     String.valueOf(Simulation.getDepthNight()));
            ncOut.addGlobalAttribute("depth_night_expl",
                                     "night-time depth of DVM scheme");
            ncOut.addGlobalAttribute("depth_night_unit", "meter");
        }

    }

    /**
     * Adds the specified variable to the NetCDF file.
     *
     * @param field a Field, the variable to be added in the file.
     */
    private static void addVar2NcOut(Field field) {

        ncOut.addVariable(field.short_name(), field.type(), field.dimensions());
        ncOut.addVariableAttribute(field.short_name(), "long_name",
                                   field.long_name());
        ncOut.addVariableAttribute(field.short_name(), "unit", field.unit());
        if (field.attribute1() != null) {
            ncOut.addVariableAttribute(field.short_name(), "origin",
                                       field.attribute1());
        }
        if (field.attribute2() != null) {
            ncOut.addVariableAttribute(field.short_name(), "calendar",
                                       field.attribute2());
        }
    }

    /**
     * Writes data to the specified variable.
     *
     * @param field a Field, the variable to be written
     * @param origin an int[], the offset within the variable to start writing.
     * @param array the Array that will be written; must be same type and
     * rank as Field
     */
    public static void write2NcOut(Field field, int[] origin, Array array) {

        try {
            ncOut.write(field.short_name(), origin, array);
        } catch (InvalidRangeException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Closes the NetCDF file.
     */
    public static void close() {
        try {
            ncOut.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {}
    }

    /**
     * Initializes the output manager.
     * @param pop the Population that will be scanned by the class to get the
     * particle's properties.
     */
    public static void init(Population pop) {

        FLAG_3D = Configuration.is3D();
        FLAG_GROWTH = Configuration.isGrowth();
        FLAG_PLANKTON = Configuration.isPlankton();
        FLAG_RECRUITMENT = (Configuration.getTypeRecruitment()
                            != Constant.NONE);
        FLAG_RELEASED_ZONE = (Configuration.getTypeRelease() ==
                              Constant.RELEASE_ZONE);
        nbRecruitmentZones = Configuration.getRecruitmentZones().size();

        i_record = 0;
        arrLon = new ArrayFloat.D2(1, Configuration.getNbParticles());
        arrLat = new ArrayFloat.D2(1, Configuration.getNbParticles());
        arrDepth = new ArrayFloat.D2(1, Configuration.getNbParticles());
        arrTime = new ArrayDouble.D1(1);
        arrDeath = new ArrayInt.D2(1, Configuration.getNbParticles());
        /*if (FLAG_3D) {
            arrTp = new ArrayFloat.D2(1, Configuration.getNbParticles());
            arrSal = new ArrayFloat.D2(1, Configuration.getNbParticles());
        }
        if (FLAG_RELEASED_ZONE) {
            arrZone = new ArrayInt.D2(1, Configuration.getNbParticles());
        }*/
        if (FLAG_RECRUITMENT) {
            arrRecruit = new ArrayInt.D3(1, Configuration.getNbParticles(),
                                         nbRecruitmentZones);
        }
        /*if (FLAG_GROWTH) {
            arrLength = new ArrayFloat.D2(1, Configuration.getNbParticles());
            if (FLAG_PLANKTON) {
                arrLargePhyto = new ArrayFloat.D2(1,
                                                  Configuration.getNbParticles());
                arrLargeZoo = new ArrayFloat.D2(1, Configuration.getNbParticles());
                arrSmallZoo = new ArrayFloat.D2(1, Configuration.getNbParticles());
            }
        }*/
        population = pop;

    }

    /**
     * Writes the results of the simulation at the specified time.
     * @param time a double, the current time [second] of the simulation
     */
    public static void write(double time) {

        //System.out.println("  --> record " + i_record + " - time " + (long) time);
        IParticle particle;
        Iterator<IParticle> iter = population.iterator();
        int[] origin = new int[] {i_record, 0};
        int[] origin_time = new int[] {i_record};
        int[] origin_recruited = new int[] {i_record, 0, 0};
        int i;
        while (iter.hasNext()) {
            particle = iter.next();
            i = particle.index();
            arrLon.set(0, i, (float) particle.getLon());
            arrLat.set(0, i, (float) particle.getLat());
            arrDepth.set(0, i, (float) particle.getDepth());
            arrDeath.set(0, i, particle.getDeath());
            /*if (FLAG_3D) {
                arrTp.set(0, i, (float) particle.getTemperature(time));
                arrSal.set(0, i, (float) particle.getSalinity(time));
            }
            if (FLAG_GROWTH) {
                arrLength.set(0, i, (float) particle.getLength());
                if (FLAG_PLANKTON) {
                    arrLargePhyto.set(0, i, (float) particle.getLargePhyto());
                    arrLargeZoo.set(0, i, (float) particle.getLargeZoo());
                    arrSmallZoo.set(0, i, (float) particle.getSmallZoo());
                }
            }*/
            if (FLAG_RECRUITMENT) {
                //particle.checkRecruitment(Configuration.getTypeRecruitment());
                for (int i_zone = 0; i_zone < nbRecruitmentZones; i_zone++) {
                    arrRecruit.set(0, i, i_zone,
                                   particle.isRecruited(i_zone) ? 1 : 0);
                }
            }
            /*if (FLAG_RELEASED_ZONE) {
                arrZone.set(0, i, particle.getNumZoneNC());
            }*/
        }
        arrTime.set(0, time);

        write2NcOut(Field.LONGITUDE, origin, arrLon);
        write2NcOut(Field.LATITUDE, origin, arrLat);
        write2NcOut(Field.DEPTH, origin, arrDepth);
        write2NcOut(Field.TIME, origin_time, arrTime);
        write2NcOut(Field.DEATH, origin, arrDeath);
        /*if (FLAG_3D) {
            write2NcOut(Field.TEMPERATURE, origin, arrTp);
            write2NcOut(Field.SALINITY, origin, arrSal);
        }
        if (FLAG_GROWTH) {
            write2NcOut(Field.LENGTH, origin, arrLength);
        }*/
        if (FLAG_RECRUITMENT) {
            write2NcOut(Field.RECRUITED, origin_recruited, arrRecruit);
        }
        /*if (FLAG_RELEASED_ZONE) {
            write2NcOut(Field.ZONE, origin, arrZone);
        }*/

        i_record++;
    }

    //////////////////////////////////////////////////////////////////////////////
    /**
     * This enum lists all the variables that could be added in the NetCDF
     * output file. Each {@code Field} holds the following information:
     * <ul>
     * <li> a short name: the name of the variable
     * <li> a long name: description of the variable
     * <li> the variable unit
     * <li> a first additional attribute
     * <li> a second additional attribute
     * <li> the Data type stored by the variable
     * <li> the number of dimensions
     * </ul>
     *
     * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
     * @author P.Verley
     */
    public enum Field {
        LONGITUDE("lon", "particle longitude", "degree east", DataType.FLOAT),
                LATITUDE("lat", "particle latitude", "degree north",
                         DataType.FLOAT),
                DEPTH("depth", "particle depth", "meter", DataType.FLOAT),
                XGRID("xgrid", "coordinate in x", "scalar", DataType.DOUBLE),
                YGRID("ygrid", "coordinate in y", "scalar", DataType.DOUBLE),
                ZGRID("zgrid", "coordinate in z", "scalar", DataType.DOUBLE),
                TIME("time", "time in second since origin", "second",
                     Configuration.getTimeOrigin(), Configuration.getCalendar(),
                     DataType.DOUBLE, 1),
                TEMPERATURE("temp", "water temperature at particle location",
                            "celsius",
                            DataType.FLOAT),
                SALINITY("salt", "water salinity at particle location", "psu",
                         DataType.FLOAT),
                LENGTH("length", "particle length", "millimeter",
                       DataType.FLOAT),
                ZONE("zone", "zone number at particle location",
                     "releasing zone > 0, recruitment zone < 0, out zone = 0",
                     DataType.INT),
                RECRUITED("recruited", "status of recruitment", "boolean",
                          DataType.INT, 3),
                DEATH("death", "cause of death",
                      "error = -1, alive = 0, out = 1, cold = 2, cold larve = 3, beached = 4, old = 5",
                      DataType.INT),
                LARGE_PHYTO("largePhyto",
                            "concentration in large phytoplankton at particule location ",
                            "mMol N m-3",
                            DataType.FLOAT),
                LARGE_ZOO("largeZoo",
                          "concentration in large zooplankton at particule location ",
                          "mMol N m-3",
                          DataType.FLOAT),
                SMALL_ZOO("smallZoo",
                          "concentration in small zooplankton at particule location ",
                          "mMol N m-3",
                          DataType.FLOAT);

        /**
         * Variable name
         */
        private final String short_name;
        /**
         * The description of the variable
         */
        private final String long_name;
        /**
         * Variable unit
         */
        private final String unit;
        /**
         * First additional attribute
         */
        private final String attribute1;
        /**
         * Second additional attribute
         */
        private final String attribute2;
        /**
         * Variable data type
         */
        private final DataType type;
        /**
         * Number of dimensions of the variable
         */
        private final int nb_dimensions;

        ///////////////
        // Constructors
        ///////////////

        /**
         * Constructs a new {@code Field} with two dimensions and no additional
         * attributes.
         *
         * @param name a String, the variable name
         * @param description the String that describes the variable
         * @param unit  a String, the variable unit
         * @param type a DataType, the variable data type
         */
        Field(String name, String description, String unit, DataType type) {
            this(name, description, unit, null, null, type, 2);
        }

        /**
         * Constructs a new {@code Field} with the specified dimensions
         * and no additional attributes.
         *
         * @param name a String, the variable name
         * @param description the String that describes the variable
         * @param unit  a String, the variable unit
         * @param type a DataType, the variable data type
         * @param nb_dimensions an int, the number of dimensions
         */
        Field(String name, String description, String unit, DataType type,
              int nb_dimensions) {
            this(name, description, unit, null, null, type, nb_dimensions);
        }

        /**
         * Constructs a new {@code Field} with the specified parameters
         *
         * @param name a String, the variable name
         * @param description the String that describes the variable
         * @param unit  a String, the variable unit
         * @param type a DataType, the variable data type
         * @param nb_dimensions an int, the number of dimensions
         * @param attribute1 a String for the first additional attribute
         * @param attribute2 a String for the second additional attribute
         */

        Field(String name, String description, String unit, String attribute1,
              String attribute2, DataType type, int nb_dimensions) {

            this.short_name = name;
            this.long_name = description;
            this.unit = unit;
            this.type = type;
            this.attribute1 = attribute1;
            this.attribute2 = attribute2;
            this.nb_dimensions = nb_dimensions;
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        /**
         * Gets the name of the variable.
         * @return a String, the variable name
         */
        public String short_name() {
            return short_name;
        }

        /**
         * Gets the description of the variable.
         * @return a String, the variable description
         */
        public String long_name() {
            return long_name;
        }

        /**
         * Gets the variable unit
         * @return a String, the variable unit
         */
        public String unit() {
            return unit;
        }

        /**
         * Gets the first addtionnal attribute
         * @return a String, variable attribute
         */
        public String attribute1() {
            return attribute1;
        }

        /**
         * Gets the second addtionnal attribute
         * @return a String, variable attribute
         */
        public String attribute2() {
            return attribute2;
        }

        /**
         * Gets the variable data type
         * @return the DataType of the variable
         */
        public DataType type() {
            return type;
        }

        /**
         * Gets the list of the variable dimensions.
         * @return the List of the variable {@code Dimension}s
         */
        public List<Dimension> dimensions() {

            ArrayList<Dimension>
                    dimensions = new ArrayList<Dimension>(nb_dimensions);
            dimensions.add(time);
            if (nb_dimensions >= 2) {
                dimensions.add(drifter);
            }
            if (nb_dimensions >= 3) {
                dimensions.add(zone);
            }
            return dimensions;
        }

        //----------- End of enum
    }

    //---------- End of class
}
