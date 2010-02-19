package ichthyop.io;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ichthyop.core.Zone;
import ichthyop.util.INIFile;
import ichthyop.util.Constant;
import ichthyop.util.Structure;
import ichthyop.util.NCField;
import ichthyop.util.Resources;
import ichthyop.util.calendar.Calendar1900;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import ucar.nc2.dataset.NetcdfDataset;

public class Configuration {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /////////////////////
    // Constants
    /////////////////////
    public static int VERSION = 220;

    /////////////////////
    // Section SIMULATION
    /////////////////////
    /**
     *  SINGLE (one simulation and UI) or SERIAL (several simulations no UI)
     */
    private static int TYPE_RUN;
    private static int NB_REPLICA;

    ////////////////
    // Section MODEL
    ////////////////
    private static int TYPE_MODEL;
    private static int TYPE_SCHEME;
    private static boolean BLN_RANGE;
    private static float lon1,  lat1,  lon2,  lat2;

    /////////////
    // Section IO
    /////////////
    private static String DIRECTORY_IN;
    private static String DIRECTORY_OUT;
    private static String FILE_MASK;
    private static String PATH_FILE_DRIFTERS;
    private static boolean BLN_RECORD;
    private static String OUTPUT_FILENAME;
    //private static int RECORD_DT;
    private static int RECORD_FREQUENCY;

    ////////////////////
    // Section TRANSPORT
    ////////////////////
    private static int DIM_SIMU;
    private static boolean BLN_VDISP;
    private static boolean BLN_HDISP;
    private static boolean BLN_BUOYANCY;
    private static float[] EGG_DENSITY;
    private static float BUOYANCY_AGE_LIMIT;
    private static boolean BLN_MIGRATION;
    private static float MIGRATION_AGE_LIMIT;
    private static float[] DEPTH_DAY;
    private static float[] DEPTH_NIGHT;

    //////////////////
    // Section RELEASE
    //////////////////
    private static int TYPE_RELEASE;
    private static int NB_PARTICLES;
    private static int[] DEPTH_RELEASE_MIN;
    private static int[] DEPTH_RELEASE_MAX;
    private static boolean BLN_PULSATION;
    private static int[] RELEASE_DT;
    private static int[] NB_RELEASE_EVENTS;
    private static boolean BLN_PATCHINESS;
    private static int NB_PATCHES;
    private static int[] PATCH_RADIUS;
    private static int[] PATCH_THICKNESS;
    private static ArrayList<Zone> listReleaseZone;

    //////////////////////
    // Section RECRUITMENT
    //////////////////////
    private static int TYPE_RECRUITMENT;
    private static float[] AGE_RECRUITMENT;
    private static float[] LENGTH_RECRUITMENT;
    private static float DURATION_IN_RECRUIT_AREA;
    private static boolean BLN_DEPTH_RECRUITMENT;
    private static float[] MIN_DEPTH_RECRUITMENT;
    private static float[] MAX_DEPTH_RECRUITMENT;
    private static boolean BLN_STOP_MOVING;
    private static ArrayList<Zone> listRecruitmentZone;

    //////////////////
    // Section BIOLOGY
    //////////////////
    private static boolean BLN_GROWTH;
    private static boolean BLN_PLANKTON;
    private static boolean BLN_LETHAL_TP;
    private static float[] LETHAL_TP_EGG;
    private static float[] LETHAL_TP_LARVA;

    ///////////////
    // Section TIME
    ///////////////
    private static int TIME_ARROW;
    private static int TYPE_CALENDAR;
    private static String TIME_ORIGIN;
    private static long[] TIME_T0;
    private static long TRANSPORT_DURATION;
    private static int DT;

//////////////
// Constructor
//////////////
    /**
     * Constructs a new {@code Configuration} object and reads the specified
     * configuration file.
     *
     * @param file the pathname of the configuration file
     * @throws IOException if an input error occurs when reading the
     * configuration file.
     */
    public Configuration(File file) throws IOException {

        INIFile inifile = new INIFile(file.toString());
        int fileVersion = getVersion(inifile);
        if (fileVersion != VERSION) {
            String title = Resources.TITLE_SHORT + "Version control";
            StringBuffer message = new StringBuffer("Version of your configuration file: ");
            message.append(versionToString(fileVersion));
            message.append('\n');
            message.append("Ichthyop current version: ");
            message.append(versionToString(VERSION));
            message.append('\n');
            message.append("Would you like Ichthyop to upgrade your configuration file ?");
            int reply = JOptionPane.showConfirmDialog(null, message.toString(), title, JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.NO_OPTION) {
                throw new IOException("version inconsistency.");
            } else {
                inifile = upgrade(inifile, fileVersion);
            }
        }
        read(inifile);

    }

////////////////////////////
// Definition of the methods
////////////////////////////
    private String versionToString(int version) {

        StringBuffer sversion = new StringBuffer(5);
        int major = version / 100;
        int minor = version / 10 - major * 10;
        int tag = version - 100 * major - 10 * minor;
        sversion.append(major);
        sversion.append('.');
        sversion.append(minor);
        sversion.append('.');
        sversion.append(tag);

        return sversion.toString();
    }

    /**
     * 
     * @param file
     * @return
     */
    private int getVersion(INIFile file) throws IOException {

        try {
            int version = readInteger(file,
                    Structure.SECTION_SIMULATION,
                    Structure.VERSION);
            return version;
        } catch (NullPointerException e) {
        // version < 2.1.1
        }

        if (isV210(file)) {
            return 210;
        }
        if (isV200(file)) {
            return 200;
        }
        StringBuffer errMsg = new StringBuffer("Unable to evaluate version of the configuration file");
        errMsg.append('\n');
        errMsg.append("Version might be prior to 2.0.0 either the file is corrupted.");
        throw new IOException(errMsg.toString());
    }

    /**
     * 
     * @param file
     * @return
     */
    private boolean isV210(INIFile file) {

        String field = file.getStringProperty(Structure.SECTION_IO,
                Structure.OUTPUT_FILENAME);

        return field != null;
    }

    /**
     * 
     * @param file
     * @return
     */
    private boolean isV200(INIFile file) {

        String field = file.getStringProperty(Structure.SECTION_IO,
                Structure.FILTER);

        return field != null;
    }

    /**
     * 
     * @param file
     * @param version
     * @return
     */
    private INIFile upgrade(INIFile file, int version) throws IOException {

        File current = new File(file.getFileName());
        File backcup = new File(file.getFileName() + ".bak");
        copyFile(current, backcup);

        INIFile ufile = file;

        if (version < 210) {
            ufile = u200To210(file);
        }
        if (version < 211) {
            ufile = u210To211(ufile);
        }
        if (version < 220) {
            ufile = u211To220(ufile);
        }

        try {
            if (getVersion(ufile) == VERSION) {
                ufile.save();
                System.out.println("File " + new File(ufile.getFileName()).getName()
                        + " upgraded to " + versionToString(VERSION));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IOException("Problem occured while upgrading the configuration file.");
        }

        return ufile;
    }

    /**
     * 
     * @param file
     * @return
     */
    private INIFile u200To210(INIFile file) {

        INIFile ufile = file;

        TYPE_RUN = readInteger(file,
                Structure.SECTION_SIMULATION,
                Structure.RUN);

        // Check whether it is SERIAL or SINGLE run.
        String outputfilename = (TYPE_RUN == Constant.SERIAL)
                ? "serial_simu"
                : "single_simu";


        // Add property [IO] <ouput filename>
        ufile.setStringProperty(Structure.SECTION_IO,
                Structure.OUTPUT_FILENAME,
                outputfilename,
                null);

        // Add property [TIME] <arrow>
        ufile.setIntegerProperty(Structure.SECTION_TIME,
                Structure.ARROW,
                1,
                Structure.MAN_ARROW);

        // Add property [RECRUITMENT] <depth criterion>
        ufile.setBooleanProperty(Structure.SECTION_RECRUIT,
                Structure.DEPTH_RECRUIT,
                false,
                null);

        // Add property [RECRUITMENT] <depth min>
        ufile.setStringProperty(Structure.SECTION_RECRUIT,
                Structure.DEPTH_MIN,
                "null",
                null);

        // Add property [RECRUITMENT] <depth max>
        ufile.setStringProperty(Structure.SECTION_RECRUIT,
                Structure.DEPTH_MAX,
                "null",
                null);

        return ufile;
    }

    /**
     * 
     * @param file
     * @param version
     * @return
     */
    private INIFile u210To211(INIFile file) {

        INIFile ufile = file;

        // Add property [SIMULATION] <version>
        ufile.setIntegerProperty(Structure.SECTION_SIMULATION,
                Structure.VERSION,
                VERSION,
                null);

        // Add property [RECRUITMENT] <stop moving>
        ufile.setBooleanProperty(Structure.SECTION_RECRUIT,
                Structure.STOP,
                false,
                null);

        return ufile;
    }

    private INIFile u211To220(INIFile file) {
        INIFile ufile = file;

        // Add property [SIMULATION] <version>
        ufile.setIntegerProperty(Structure.SECTION_SIMULATION,
                Structure.VERSION,
                VERSION,
                null);

        return ufile;
    }

    /**
     * 
     * @param src
     * @param dest
     * @throws java.io.IOException
     */
    public static void copyFile(File src, File dest) throws IOException {

        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);

        java.nio.channels.FileChannel channelSrc = fis.getChannel();
        java.nio.channels.FileChannel channelDest = fos.getChannel();

        channelSrc.transferTo(0, channelSrc.size(), channelDest);

        fis.close();
        fos.close();

        System.out.println("The original configuration file has been copied as " + dest.getName());
    }

    /**
     * Reads the data stored in the configuration file
     *
     * @param file an INIFile, the configuration file.
     * @throws IOException
     */
    private void read(INIFile file) throws IOException {

        TYPE_RUN = readInteger(file,
                Structure.SECTION_SIMULATION,
                Structure.RUN);

        boolean isSerial = (TYPE_RUN == Constant.SERIAL);

        NB_REPLICA = isSerial
                ? readInteger(file,
                Structure.SECTION_SIMULATION,
                Structure.NB_REPLICA)
                : 0;

        readIO(file, Structure.SECTION_IO);
        readModel(file, Structure.SECTION_MODEL);
        readTime(file, Structure.SECTION_TIME, isSerial);
        readBiology(file, Structure.SECTION_BIO, isSerial);
        readTransport(file, Structure.SECTION_TRANSPORT, isSerial);
        readVariable(file, Structure.SECTION_VARIABLE);
        readRelease(file, Structure.SECTION_RELEASE, isSerial);
        readRecruitment(file, Structure.SECTION_RECRUIT, isSerial);
    }

    /**
     * Reads all properties under the IO section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     */
    private void readIO(INIFile file, String section) {

        DIRECTORY_IN = readString(file, section, Structure.INPUT_PATH);
        FILE_MASK = readString(file, section, Structure.FILTER);
        PATH_FILE_DRIFTERS = readString(file, section, Structure.DRIFTER);
        DIRECTORY_OUT = readString(file, section, Structure.OUTPUT_PATH);
        BLN_RECORD = file.getBooleanProperty(section, Structure.RECORD);
        if (BLN_RECORD) {
            OUTPUT_FILENAME = readString(file, section, Structure.OUTPUT_FILENAME);
            RECORD_FREQUENCY = readInteger(file, section, Structure.RECORD_DT);
        }
    }

    /**
     * Reads all properties under the MODEL section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     */
    private void readModel(INIFile file, String section) {

        TYPE_MODEL = readInteger(file, section, Structure.MODEL);
        TYPE_SCHEME = readInteger(file, section, Structure.SCHEME);
        BLN_RANGE = file.getBooleanProperty(section, Structure.RANGE);
        if (BLN_RANGE) {
            lat1 = readFloat(file, section, Structure.LAT + String.valueOf(1));
            lon1 = readFloat(file, section, Structure.LON + String.valueOf(1));
            lat2 = readFloat(file, section, Structure.LAT + String.valueOf(2));
            lon2 = readFloat(file, section, Structure.LON + String.valueOf(2));
        }
    }

    /**
     * Reads all properties under the TIME section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     */
    private void readTime(INIFile file, String section, boolean isSerial) {

        TIME_ARROW = readInteger(file, section, Structure.ARROW);
        TYPE_CALENDAR = readInteger(file, section, Structure.CALENDAR);
        if (TYPE_CALENDAR == Constant.GREGORIAN) {
            TIME_ORIGIN = readString(file, section, Structure.TIME_ORIGIN);
        }
        TIME_T0 = readLong(file, section, Structure.T0, isSerial);
        TRANSPORT_DURATION = readLong(file, section,
                Structure.TRANSPORT_DURATION);
        DT = readInteger(file, section, Structure.DT);
    }

    /**
     * Reads all properties under the TRANSPORT section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     */
    private void readTransport(INIFile file, String section, boolean isSerial) {

        DIM_SIMU = readInteger(file, section, Structure.DIMENSION);

        BLN_HDISP = file.getBooleanProperty(section, Structure.HDISP);
        BLN_VDISP = file.getBooleanProperty(section, Structure.VDISP);
        BLN_BUOYANCY = file.getBooleanProperty(section, Structure.BUOYANCY);
        if (BLN_BUOYANCY) {
            EGG_DENSITY = readFloat(file, section, Structure.EGG_DENSITY,
                    isSerial);
            BUOYANCY_AGE_LIMIT = readFloat(file, section,
                    Structure.BUOYANCY_AGE_LIMIT);
        }
        BLN_MIGRATION = file.getBooleanProperty(section, Structure.MIGRATION);
        if (BLN_MIGRATION) {
            DEPTH_DAY = readFloat(file,
                    section,
                    Structure.MIGRATION_DEPTH_DAY,
                    isSerial);
            DEPTH_NIGHT = readFloat(file,
                    section,
                    Structure.MIGRATION_DEPTH_NIGHT,
                    isSerial);
            if (!BLN_GROWTH) {
                MIGRATION_AGE_LIMIT = readFloat(file, section,
                        Structure.MIGRATION_AGE_LIMIT);
            }
        }

    }

    /**
     * Reads all properties under the VARIABLE section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     */
    private void readVariable(INIFile file, String section) {

        NCField[] variable = NCField.values();
        for (int i = 0; i < variable.length; i++) {
            if (variable[i].isRequired(TYPE_MODEL + DIM_SIMU)) {
                variable[i].setName(TYPE_MODEL,
                        readString(file, section, variable[i].name()));
            }
        }
    }

    /**
     * Reads all properties under the RELEASE section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     */
    private void readRelease(INIFile file, String section, boolean isSerial) throws
            IOException {

        TYPE_RELEASE = readInteger(file, section, Structure.TYPE_RELEASE);
        switch (TYPE_RELEASE) {
            case Constant.RELEASE_ZONE:
                readReleaseZone(file, section, isSerial);
                break;
            case Constant.RELEASE_TXTFILE:
                readReleaseTxt(file, section);
                break;
            case Constant.RELEASE_NCFILE:
                readReleaseNc(file, Structure.SECTION_IO);
                break;
        }

        listReleaseZone = readZone(file, Constant.RELEASE);
    }

    /**
     * Reads the properties under the RELEASE section, when the release mode from
     * zone is selected.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     */
    private void readReleaseZone(INIFile file, String section, boolean isSerial) {

        NB_PARTICLES = readInteger(file, section, Structure.NB_PARTICLES);
        if (DIM_SIMU == Constant.SIMU_3D) {
            DEPTH_RELEASE_MIN = readInteger(file, section,
                    Structure.DEPTH_MIN,
                    isSerial);
            DEPTH_RELEASE_MAX = readInteger(file, section,
                    Structure.DEPTH_MAX,
                    isSerial);
            BLN_PULSATION = file.getBooleanProperty(section,
                    Structure.PULSATION);
            if (BLN_PULSATION) {
                NB_RELEASE_EVENTS = readInteger(file, section,
                        Structure.NB_RELEASE_EVENTS,
                        isSerial);
                RELEASE_DT = readInteger(file, section,
                        Structure.RELEASE_DT,
                        isSerial);
            } else {
                NB_RELEASE_EVENTS = new int[]{1};
                RELEASE_DT = new int[]{0};
            }

            BLN_PATCHINESS = file.getBooleanProperty(section,
                    Structure.PATCHINESS);
            if (BLN_PATCHINESS) {
                NB_PATCHES = readInteger(file, section,
                        Structure.NB_PATCHES);
                PATCH_RADIUS = readInteger(file, section,
                        Structure.RADIUS_PATCH,
                        isSerial);
                if (DIM_SIMU == Constant.SIMU_3D) {
                    PATCH_THICKNESS = readInteger(file, section,
                            Structure.THICK_PATCH,
                            isSerial);
                }
            }
        } else {
            NB_RELEASE_EVENTS = new int[]{1};
            RELEASE_DT = new int[]{0};
        }

    }

    /**
     * Reads the properties under the RELEASE section, when the release mode
     * from text file is selected.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     */
    private void readReleaseTxt(INIFile file, String section) throws
            IOException {

        File fDrifter = new File(PATH_FILE_DRIFTERS);
        if (!fDrifter.exists() || !fDrifter.canRead()) {
            throw new IOException("Drifter file " + PATH_FILE_DRIFTERS + " cannot be read");
        }
        NB_PARTICLES = 0;
        RELEASE_DT = new int[]{0};
        NB_RELEASE_EVENTS = new int[]{1};
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(fDrifter));
            String line;
            while ((line = bfIn.readLine()) != null) {
                if (!line.startsWith("#") && !(line.length() < 1)) {
                    NB_PARTICLES++;
                }
            }
        } catch (java.io.IOException e) {
            throw new IOException("Problem reading drifter file " + fDrifter);
        }
    }

    /**
     * Reads the properties under the RELEASE section, when the release mode
     * from NetCDF file is selected.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     */
    private void readReleaseNc(INIFile file, String section) throws IOException {

        NB_PARTICLES = 0;
        RELEASE_DT = new int[]{0};
        NB_RELEASE_EVENTS = new int[]{1};
        try {
            NB_PARTICLES = NetcdfDataset.openFile(PATH_FILE_DRIFTERS,
                    null).findDimension("drifter").
                    getLength();
        } catch (IOException e) {
            throw new IOException("Problem reading Ichthyop output file " + PATH_FILE_DRIFTERS);
        }

    }

    /**
     * Reads all properties under the RECRUITMENT section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     */
    private void readRecruitment(INIFile file, String section, boolean isSerial) {

        TYPE_RECRUITMENT = readInteger(file, section, Structure.RECRUIT);
        if (TYPE_RECRUITMENT != Constant.NONE) {
            switch (TYPE_RECRUITMENT) {
                case Constant.RECRUIT_LENGTH:
                    LENGTH_RECRUITMENT = readFloat(file, section,
                            Structure.LENGTH_RECRUIT,
                            isSerial);
                    break;
                case Constant.RECRUIT_AGE:
                    AGE_RECRUITMENT = readFloat(file, section,
                            Structure.AGE_RECRUIT, isSerial);
                    break;
            }
            DURATION_IN_RECRUIT_AREA = readFloat(file, section,
                    Structure.DURATION_RECRUIT);
            BLN_DEPTH_RECRUITMENT = file.getBooleanProperty(section,
                    Structure.DEPTH_RECRUIT);
            if (BLN_DEPTH_RECRUITMENT) {
                MIN_DEPTH_RECRUITMENT = readFloat(file, section,
                        Structure.DEPTH_MIN,
                        isSerial);
                MAX_DEPTH_RECRUITMENT = readFloat(file, section,
                        Structure.DEPTH_MAX,
                        isSerial);
            }
            BLN_STOP_MOVING = file.getBooleanProperty(section,
                    Structure.STOP);
        }
        listRecruitmentZone = readZone(file, Constant.RECRUITMENT);
    }

    /**
     * Reads all properties under the BIOLOGY section.
     *
     * @param file an INIFile, the configuration file.
     * @param section a String, the name of the section
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     */
    private void readBiology(INIFile file, String section, boolean isSerial) {

        BLN_GROWTH = file.getBooleanProperty(section, Structure.GROWTH);
        BLN_PLANKTON = file.getBooleanProperty(section, Structure.PLANKTON);
        BLN_LETHAL_TP = file.getBooleanProperty(section, Structure.LETHAL_TP);
        if (BLN_LETHAL_TP) {
            LETHAL_TP_EGG = readFloat(file, section, Structure.LETHAL_TP_EGG,
                    isSerial);
            if (BLN_GROWTH) {
                LETHAL_TP_LARVA = readFloat(file, section,
                        Structure.LETHAL_TP_LARVA, isSerial);
            }
        }
    }

    /**
     * Reads the value(s) of the specified property as {@code long}.
     * The method is specially designed for reading parameters that take
     * several values in SERIAL mode.
     * Nonetheless it can be used for SINGLE mode, and
     * the method returns a single-element array.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     * @return a long[] that holds the multiple values of the properties in
     * SERIAL mode, or single-element array in SINGLE mode.
     */
    private long[] readLong(INIFile file, String section,
            String property, boolean isSerial) {

        long[] array = null;
        Long nb;
        if (isSerial) {
            ArrayList list = new ArrayList();
            int i = 0;
            while ((nb = file.getLongProperty(section,
                    property + " " +
                    String.valueOf(i))) != null) {
                list.add(nb);
                i++;
            }
            if (list.size() == 0) {
                throw new NullPointerException("Problem reading property [" +
                        section + "] <" + property + ">");
            }
            array = new long[list.size()];
            for (int n = 0; n < list.size(); n++) {
                nb = (Long) list.get(n);
                array[n] = nb;
            }
        } else {
            nb = file.getLongProperty(section, property);
            if (nb == null) {
                throw new NullPointerException("Problem reading property [" +
                        section + "] <" + property + ">");
            }
            array = new long[]{nb.longValue()};
        }
        return array;
    }

    /**
     * Reads the value of the specified property as a {@code long}.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @return a long, the value of the specified property
     */
    private long readLong(INIFile file, String section, String property) {

        Long nb = file.getLongProperty(section, property);
        if (nb == null) {
            throw new NullPointerException("Problem reading property [" +
                    section + "] <" + property + ">");
        }
        return nb;
    }

    /**
     * Reads the value(s) of the specified property as {@code float}.
     * The method is specially designed for reading parameters that take
     * several values in SERIAL mode.
     * Nonetheless it can be used for SINGLE mode, and
     * the method returns a single-element array.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     * @return a float[] that holds the multiple values of the properties in
     * SERIAL mode, or single-element array in SINGLE mode.
     */
    private float[] readFloat(INIFile file, String section,
            String property, boolean isSerial) {

        float[] array = null;
        Double nb;
        if (isSerial) {
            ArrayList list = new ArrayList();
            int i = 0;
            while ((nb = file.getDoubleProperty(section,
                    property + " " +
                    String.valueOf(i))) != null) {
                list.add(nb);
                i++;
            }
            if (list.size() == 0) {
                throw new NullPointerException("Problem reading property [" +
                        section + "] <" + property + ">");
            }
            array = new float[list.size()];
            for (int n = 0; n < list.size(); n++) {
                nb = (Double) list.get(n);
                array[n] = nb.floatValue();
            }
        } else {
            nb = file.getDoubleProperty(section, property);
            if (nb == null) {
                throw new NullPointerException("Problem reading property [" +
                        section + "] <" + property + ">");
            }
            array = new float[]{nb.floatValue()};
        }
        return array;
    }

    /**
     * Reads the value of the specified property as a {@code float}.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @return a float, the value of the specified property
     */
    private float readFloat(INIFile file, String section, String property) {

        Double nb = file.getDoubleProperty(section, property);
        if (nb == null) {
            throw new NullPointerException("Problem reading property [" +
                    section + "] <" + property + ">");
        }
        return nb.floatValue();
    }

    /**
     * Reads the value(s) of the specified property as {@code integer}.
     * The method is specially designed for reading parameters that take
     * several values in SERIAL mode.
     * Nonetheless it can be used for SINGLE mode, and
     * the method returns a single-element array.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @param isSerial <code>true</code> for SERIAL mode,
     *                 <code>false</code> for SINGLE
     * @return an int[] that holds the multiple values of the properties in
     * SERIAL mode, or single-element array in SINGLE mode.
     */
    private int[] readInteger(INIFile file, String section,
            String property, boolean isSerial) {

        int[] array = null;
        Integer nb;
        if (isSerial) {
            ArrayList list = new ArrayList();
            int i = 0;
            while ((nb = file.getIntegerProperty(section,
                    property + " " +
                    String.valueOf(i))) != null) {
                list.add(nb);
                i++;
            }
            if (list.size() == 0) {
                throw new NullPointerException("Problem reading property [" +
                        section + "] <" + property + ">");
            }
            array = new int[list.size()];
            for (int n = 0; n < list.size(); n++) {
                nb = (Integer) list.get(n);
                array[n] = nb;
            }
        } else {
            nb = file.getIntegerProperty(section, property);
            if (nb == null) {
                throw new NullPointerException("Problem reading property [" +
                        section + "] <" + property + ">");
            }
            array = new int[]{nb};
        }
        return array;
    }

    /**
     * Reads the value of the specified property as an {@code integer}.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @return an integer, the value of the specified property
     */
    private int readInteger(INIFile file, String section, String property) {

        Integer nb = file.getIntegerProperty(section, property);
        if (nb == null) {
            throw new NullPointerException("Problem reading property [" +
                    section + "] <" + property + ">");
        }
        return nb;
    }

    /**
     * Reads the value of the specified property as a {@code String}.
     *
     * @param file an INIFile, the configuration file
     * @param section a String, the section name
     * @param property a String, the property name
     * @return a String, the value of the specified property
     */
    private String readString(INIFile file, String section,
            String property) {

        String str;
        str = file.getStringProperty(section, property).trim();
        if (str == null) {
            throw new NullPointerException("Problem reading property [" +
                    section + "] <" + property + ">");
        }

        if (str.matches("null")) {
            return null;
        }

        return str;
    }

    /**
     * Reads the zone definitions of the specified type (release or recruitment).
     *
     * @param file an INIFile, the configuration file
     * @param type an int, characterizing the type of zone.
     * @return an ArrayList, the list of the zones hold in the configuration
     * file.
     */
    private ArrayList<Zone> readZone(INIFile file, int type) {

        ArrayList<Zone> list;
        double[] lon = new double[4];
        double[] lat = new double[4];
        int bathyMin, bathyMax;
        Color color;
        int colorR, colorG, colorB;
        int numberZones = 0;

        String prefix = (type == Constant.RELEASE)
                ? Structure.SECTION_RELEASE_ZONE
                : Structure.SECTION_RECRUITMENT_ZONE;

        String[] sections = file.getAllSectionNames();
        for (String section : sections) {
            if (section.toLowerCase().startsWith(prefix.toLowerCase())) {
                numberZones++;
            }
        }

        list = new ArrayList(numberZones);

        for (int i = 0; i < numberZones; i++) {
            String section = prefix + String.valueOf(i + 1);
            for (int j = 0; j < 4; j++) {
                lon[j] = readFloat(file,
                        section,
                        Structure.LON_ZONE + String.valueOf(j + 1));
                lat[j] = readFloat(file,
                        section,
                        Structure.LAT_ZONE + String.valueOf(j + 1));
            }
            bathyMin = readInteger(file, section, Structure.BATHY_MIN);
            bathyMax = readInteger(file, section, Structure.BATHY_MAX);
            colorR = readInteger(file, section, Structure.RED);
            colorG = readInteger(file, section, Structure.GREEN);
            colorB = readInteger(file, section, Structure.BLUE);
            color = new Color(colorR, colorG, colorB);

            list.add(new Zone(type,
                    i,
                    lon[0], lat[0],
                    lon[1], lat[1],
                    lon[2], lat[2],
                    lon[3], lat[3],
                    bathyMin, bathyMax,
                    color));
        }
        return list;
    }

//////////
// Getters
//////////
    /**
     * Gets the values of the property "beginning of the simulation"
     * @return a long[], times [second] of the beginning of the simulation
     */
    public static long[] get_t0() {
        return TIME_T0;
    }

    /**
     * Gets the ith value of the property "beginning of the simulation".
     *
     * @param i the index of the value.
     * @return a long, the time [second] of the beginning of the simulation.
     */
    public static long get_t0(int i) {
        return TIME_T0[i];
    }

    //---------------------------------------------------------
    public static int getTimeOrigin(int field) {
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Calendar1900 cld = new Calendar1900();
        dtFormat.setCalendar(cld);
        cld.setTimeInMillis(0);
        try {
            cld.setTime(dtFormat.parse(TIME_ORIGIN));
        } catch (Exception ex) {
        }
        return cld.get(field);
    }

    //---------------------------------------------------------
    public static int getTimeArrow() {
        return TIME_ARROW;
    }

    //---------------------------------------------------------
    public static String getTimeOrigin() {
        return TIME_ORIGIN;
    }

    //---------------------------------------------------------
    public static int get_dt() {
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
    public static int[] getReleaseDt() {
        return RELEASE_DT == null ? new int[1] : RELEASE_DT;
    }

    public static int getReleaseDt(int i) {
        return RELEASE_DT[i];
    }

    //---------------------------------------------------------
    public static int[] getDepthReleaseMin() {
        return DEPTH_RELEASE_MIN == null ? new int[1] : DEPTH_RELEASE_MIN;
    }

    public static int getDepthReleaseMin(int i) {
        return DEPTH_RELEASE_MIN != null ? DEPTH_RELEASE_MIN[i] : 0;
    }

    //---------------------------------------------------------
    public static boolean isPulsation() {
        return BLN_PULSATION;
    }

    //---------------------------------------------------------
    public static int getDepthReleaseMax(int i) {
        return DEPTH_RELEASE_MIN != null ? DEPTH_RELEASE_MAX[i] : 0;
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
        return LETHAL_TP_LARVA == null ? new float[1] : LETHAL_TP_LARVA;
    }

    public static float getLethalTpLarvae(int i) {
        return LETHAL_TP_LARVA[i];
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
    public static float[] getMinDepthRecruitment() {
        return MIN_DEPTH_RECRUITMENT == null
                ? new float[1]
                : MIN_DEPTH_RECRUITMENT;
    }

    public static float getMinDepthRecruitment(int i) {
        return MIN_DEPTH_RECRUITMENT[i];
    }

    //---------------------------------------------------------
    public static float[] getMaxDepthRecruitment() {
        return MAX_DEPTH_RECRUITMENT == null
                ? new float[1]
                : MAX_DEPTH_RECRUITMENT;
    }

    public static float getMaxDepthRecruitment(int i) {
        return MAX_DEPTH_RECRUITMENT[i];
    }

    //---------------------------------------------------------
    public static boolean isDepthRecruitment() {
        return BLN_DEPTH_RECRUITMENT;
    }
    
    //---------------------------------------------------------
    public static boolean isStopMoving() {
        return BLN_STOP_MOVING;
    }

    //---------------------------------------------------------
    public static boolean isBuoyancy() {
        return BLN_BUOYANCY;
    }

    //---------------------------------------------------------
    public static boolean isGrowth() {
        return BLN_GROWTH;
    }

    public static boolean isPlankton() {
        return BLN_PLANKTON;
    }

    //---------------------------------------------------------
    public static boolean isLethalTp() {
        return BLN_LETHAL_TP;
    }

    //---------------------------------------------------------
    public static boolean isRecordNc() {
        return BLN_RECORD;
    }

    //---------------------------------------------------------
    public static boolean isRecord() {
        return BLN_RECORD;
    }

    //---------------------------------------------------------
    public static boolean isVDisp() {
        return BLN_VDISP;
    }

    //---------------------------------------------------------
    public static boolean isHDisp() {
        return BLN_HDISP;
    }


    //---------------------------------------------------------
    public static int getTypeRecord() {
        return Constant.RECORD_NC;
    }

    //---------------------------------------------------------
    public static int getDtRecord() {
        return RECORD_FREQUENCY * DT;
    }

    //--------------------------------------------------------
    public static int getRecordFrequency() {
        return RECORD_FREQUENCY;
    }

    public static boolean isMigration() {
        return BLN_MIGRATION;
    }

    //---------------------------------------------------------
    public static float[] getDepthDay() {
        return (DEPTH_DAY == null) ? new float[1] : DEPTH_DAY;
    }

    public static float getDepthDay(int i) {
        return DEPTH_DAY[i];
    }

    //---------------------------------------------------------
    public static float[] getDepthNight() {
        return (DEPTH_NIGHT == null) ? new float[1] : DEPTH_DAY;
    }

    public static float getDepthNight(int i) {
        return DEPTH_NIGHT[i];
    }

    public static int getScheme() {
        return TYPE_SCHEME;
    }

    //---------------------------------------------------------
    public static int getDimSimu() {
        return DIM_SIMU;
    }

    //---------------------------------------------------------
    public static boolean is3D() {
        return DIM_SIMU == Constant.SIMU_3D;
    }

    //---------------------------------------------------------
    public static int getTypeCalendar() {
        return TYPE_CALENDAR;
    }

    //---------------------------------------------------------
    public static String getCalendar() {
        return (TYPE_CALENDAR == Constant.CLIMATO) ? "360_days" : "gregorian";
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
    public static int getBuoyAgeLimit() {
        return (int) (BUOYANCY_AGE_LIMIT * Constant.ONE_DAY);
    }

    public static int getMigrationAgeLimit() {
        return (int) (MIGRATION_AGE_LIMIT * Constant.ONE_DAY);
    }

    //---------------------------------------------------------
    public static int getDurationInRecruitArea() {
        return (int) (DURATION_IN_RECRUIT_AREA * Constant.ONE_DAY);
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
    public static String getFileMask() {
        return FILE_MASK;
    }

    //---------------------------------------------------------
    public static String getOutputFilename() {
        return OUTPUT_FILENAME;
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
        return TYPE_RUN == Constant.SERIAL;
    }

    //---------------------------------------------------------
    public static ArrayList<Zone> getRecruitmentZones() {
        return listRecruitmentZone;
    }

    //---------------------------------------------------------
    public static ArrayList<Zone> getReleaseZones() {
        return listReleaseZone;
    }

    //----------------------------------------------------------
    public static boolean isRanged() {
        return BLN_RANGE;
    }

    //----------------------------------------------------------
    public static float[] getP1() {
        return new float[]{lon1, lat1};
    }

    //----------------------------------------------------------
    public static float[] getP2() {
        return new float[]{lon2, lat2};
    }

    public static String getDrifterFile() {
        return PATH_FILE_DRIFTERS;
    }

    //--------------------------------------------------------------------------
    public static String getStrXiDim() {
        return NCField.xiDim.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrEtaDim() {
        return NCField.etaDim.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrZDim() {
        return NCField.zDim.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrTimeDim() {
        return NCField.timeDim.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrLon() {
        return NCField.lon.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrLat() {
        return NCField.lat.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrBathy() {
        return NCField.bathy.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrMask() {
        return NCField.mask.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrU() {
        return is3D()
                ? NCField.u3d.getName(TYPE_MODEL)
                : NCField.u2d.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrV() {
        return is3D()
                ? NCField.v3d.getName(TYPE_MODEL)
                : NCField.v2d.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrTp() {
        return NCField.temp.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrSal() {
        return NCField.sal.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrZeta() {
        return NCField.zeta.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrSigma() {
        return NCField.sigma.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrPn() {
        return NCField.pn.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrPm() {
        return NCField.pm.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrThetaS() {
        return NCField.thetaS.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrThetaB() {
        return NCField.thetaB.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrHc() {
        return NCField.hc.getName(TYPE_MODEL);
    }

    //--------------------------------------------------------------------------
    public static String getStrTime() {
        return NCField.time.getName(TYPE_MODEL);
    }

    public static String getStrKv() {
        return NCField.kv.getName(TYPE_MODEL);
    }

    //---------- End of class
}
