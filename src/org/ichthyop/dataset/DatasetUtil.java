/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.util.MetaFilenameFilter;
import org.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class DatasetUtil extends IchthyopLinker {

    private static final double TO_RAD = Math.PI / 180.d;
    private static final double DEARTH = 2 * 6367000.d;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    /**
     * List directory content with file pattern accepting * and ?
     * metacharacters. Not recursive.
     *
     * @param filepath, a String the directory
     * @param pattern, the file pattern with either * or ? metacharacters.
     * @return the list of files matching the pattern, alphabetically sorted.
     */
    public static List<String> list(String filepath, String pattern, boolean recursive) {
        List<String> list = new ArrayList();

        File path = new File(filepath);

        if (path.isDirectory()) {
            File[] listFile = path.listFiles(new MetaFilenameFilter(pattern));
            if (listFile.length > 0) {
                list = new ArrayList(listFile.length);
                for (File file : listFile) {
                    list.add(file.toString());
                }
            }
            if (recursive) {
                File[] subdirectories = path.listFiles((file) -> {
                    return file.isDirectory();
                });
                for (File subdirectory : subdirectories) {
                    list.addAll(list(subdirectory.getAbsolutePath(), pattern, recursive));
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public static boolean isValidDataset(String location) {
        try (NetcdfFile nc = NetcdfDataset.openDataset(location, true, null)) {
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /**
     * Sort of the NetCDF files from the list, comparing the first value of the
     * time variable.
     *
     * @param list
     * @param strTime, the name of the time variable in the NetCDF file
     * @param timeArrow, 1 forward, -1 backward
     */
    public static void sort(List list, String strTime, int timeArrow) {
        if (list.isEmpty() || list.size() == 1) {
            return;
        }
        list.sort(new NCComparator(strTime, timeArrow));
    }

    /**
     * Returns the first time value of the NetCDF file in seconds
     *
     * @param file, path of the NetCDF file
     * @param strTime, name of the time variable
     * @return the first time value of the NetCDF file in seconds
     * @throws java.io.IOException if the file does not exist
     */
    public static double timeFirst(String file, String strTime) throws IOException {
        NetcdfFile nc = NetcdfDataset.openDataset(file);
        Array timeArr = nc.findVariable(strTime).read();
        double convert = guessTimeConversion(nc.findVariable(strTime));
        nc.close();
        return convert == 1.d
                ? skipSeconds(timeArr.getDouble(timeArr.getIndex().set(0)))
                : convert * timeArr.getDouble(timeArr.getIndex().set(0));
    }

    /**
     * Returns the last time value of the NetCDF file in seconds
     *
     * @param file, path of the NetCDF file
     * @param strTime, name of the time variable
     * @return the last time value of the NetCDF file in seconds
     * @throws java.io.IOException if the file does not exist
     */
    public static double timeLast(String file, String strTime) throws IOException {
        NetcdfFile nc = NetcdfDataset.openDataset(file);
        Array timeArr = nc.findVariable(strTime).read();
        double convert = guessTimeConversion(nc.findVariable(strTime));
        nc.close();
        return convert == 1.d
                ? skipSeconds(timeArr.getDouble(timeArr.getIndex().set(timeArr.getShape()[0] - 1)))
                : convert * timeArr.getDouble(timeArr.getIndex().set(timeArr.getShape()[0] - 1));
    }

    public static double timeAtRank(NetcdfFile nc, String strTime, int rank) throws IOException {
        Array timeArr = nc.findVariable(strTime).read();
        double convert = guessTimeConversion(nc.findVariable(strTime));
        return (convert == 1.d)
                ? skipSeconds(timeArr.getDouble(timeArr.getIndex().set(rank)))
                : convert * timeArr.getDouble(timeArr.getIndex().set(rank));
    }

    /**
     * Checks whether time comprises between first (inclusive) and last
     * (exclusive) values of the NetCDF time variable.
     *
     * @param time, time expressed in seconds
     * @param file, path of the NetCDF file
     * @param strTime, name of the NetCDF time variable
     * @param time_arrow, 1 forward, -1 backward. In backward the whole time
     * logic is reverted
     * @return TRUE if the time value comprises between [time0, timeN[ in
     * forward and ]time0, timeN] in backward mode, with time0 and timeN the
     * first and last time value of the NetCDF file.
     * @throws IOException if the NetCDF file does not exist
     */
    public static boolean isTimeIntoFile(double time, String file, String strTime, int time_arrow) throws IOException {
        double tf = timeFirst(file, strTime);
        double tl = timeLast(file, strTime);
        //System.out.println("IF "+ file + " "+ time + " " + tf + " "+ tl);
        // Special case one time step per file
        if (tf == tl) {
            return time == tf;
        }
        // Standard case tf < tl
        return (time_arrow > 0)
                ? time >= tf && time < tl
                : time > tf && time <= tl;
    }

    /**
     * Checks whether the time value comprises between the last (inclusive) time
     * value of the 1st NetCDF file and the first (exclusive) time value of the
     * 2nd NetCDF file.
     *
     * @param time, time value in seconds
     * @param file1, first NetCDF file
     * @param file2, second NetCDF file
     * @param strTime, name of the NetCDF time variable
     * @param timeArrow, 1 forward, -1 backward.
     * @return TRUE if time comprises in [f1TimeN, f2Time0[ in forward or
     * [f1Time0, f2TimeN[ in backward mode, with f1Time0, f2Time0, f1TimeN and
     * f2TimeN the first and last time values of the NetCDF file one and two.
     * @throws IOException
     */
    public static boolean isTimeBetweenFile(double time, String file1, String file2, String strTime, int timeArrow) throws IOException {
        return (timeArrow > 0)
                ? time >= timeLast(file1, strTime) && time < timeFirst(file2, strTime)
                : time <= timeFirst(file1, strTime) && time > timeLast(file2, strTime);
    }

    /**
     *
     * @param list
     * @param index
     * @param timeArrow
     * @return
     * @throws IOException
     */
    public static int next(List<String> list, int index, int timeArrow) throws IOException {
        if ((list.size() == 1) || ((index + 1) >= list.size())) {
            throw new IOException("[dataset] Unable to find any file following " + list.get(index));
        }
        return index + 1;
    }

    public static boolean hasNext(List<String> list, int index) {
        return (index + 1) < list.size();
    }

    public static int index(String dataset_prefix, List<String> list, double time, int timeArrow, String strTime) throws IOException {

        // Check whether the time is within a file
        for (int index = 0; index < list.size(); index++) {
            if (isTimeIntoFile(time, list.get(index), strTime, timeArrow)) {
                return index;
            }
        }

        // Check whether the time is between two files
        for (int index = 0; index < list.size() - 1; index++) {
            if (isTimeBetweenFile(time, list.get(index), list.get(index + 1), strTime, timeArrow)) {
                return index;
            }
        }

        // Time value not found among NetCDF files        
        StringBuilder msg = new StringBuilder();
        msg.append("[dataset] ").append(dataset_prefix).append(" Time value ");
        msg.append(time);
        msg.append(" (in seconds) not contained among NetCDF files.");
        throw new IndexOutOfBoundsException(msg.toString());
    }

    /*
     * Finds the index of the dataset time variable such as      <code>time(rank) <= time < time(rank + 1)
     *
     * @param time a double, the current time [second] of the simulation
     * @return an int, the current rank of the NetCDF dataset for time dimension
     * @throws an IOException if an error occurs while reading the input file
     *
     */
    public static int rank(double time, NetcdfFile nc, String strTime, int timeArrow) throws ArrayIndexOutOfBoundsException, IOException {

        int lrank = 0;
        double nctime;
        Array timeArr = null;
        try {
            Variable vtime = nc.findVariable(strTime);
            double convert = guessTimeConversion(vtime);
            timeArr = vtime.read();
            nctime = (convert == 1)
                    ? skipSeconds(timeArr.getDouble(timeArr.getIndex().set(lrank)))
                    : timeArr.getDouble(timeArr.getIndex().set(lrank)) * convert;
            while (time >= nctime) {
                if (timeArrow < 0 && time == nctime) {
                    break;
                }
                lrank++;
                nctime = (convert == 1)
                        ? skipSeconds(timeArr.getDouble(timeArr.getIndex().set(lrank)))
                        : timeArr.getDouble(timeArr.getIndex().set(lrank)) * convert;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = timeArr.getShape()[0];
        }
        lrank = lrank - (timeArrow + 1) / 2;

        return lrank;
    }

    public static String findTimeVariable(NetcdfFile nc) {

        String variable_time = null;
        if (null != nc.getUnlimitedDimension()) {
            String time_dim = nc.getUnlimitedDimension().getFullName();
            variable_time = DatasetUtil.findVariable(nc, time_dim);
        }
        if (null == variable_time) {
            String[] names = new String[]{"time", "ocean_time", "time_counter", "scrum_time"};
            for (String name : names) {
                variable_time = DatasetUtil.findVariable(nc, name);
                if (null != variable_time) {
                    break;
                }
            }
        }
        return variable_time;
    }

    public static String findVariable(NetcdfFile nc, String name) {

        for (Variable variable : nc.getVariables()) {
            String vname = variable.getFullName();
            if (vname.equalsIgnoreCase(name)) {
                return vname;
            }
            Attribute sname = variable.findAttributeIgnoreCase("standard_name");
            if (null != sname) {
                if (sname.getStringValue().equalsIgnoreCase(name)) {
                    return vname;
                }
            }
            Attribute lname = variable.findAttributeIgnoreCase("long_name");
            if (null != lname) {
                if (lname.getStringValue().equalsIgnoreCase(name)) {
                    return vname;
                }
            }
        }
        return null;
    }

    /** For a given dataset, loops over all the files and all the variables and extracts a HashMap 
     * <Variable Names, List of files containing variable>.
     * 
     * @param dataset_prefix Prefix of the dataset (used only for log message)
     * @param location Location of the files (path or file)
     * @param coordinateV True if coordinates variables only should be extracted.
     * @return
     * @throws IOException 
     */
    public static HashMap<String, List<String>> mapVariables(String dataset_prefix, String location, boolean coordinateV) throws IOException {

        if (!new File(location).exists()) {
            throw new IOException("[dataset] " + dataset_prefix + " location does not exist " + location);
        }

        HashMap<String, List<String>> map = new HashMap();

        if (new File(location).isDirectory()) {
            for (File file : new File(location).listFiles()) {
                HashMap<String, List<String>> submap = mapVariables(dataset_prefix, file.getAbsolutePath(), coordinateV);
                for (String name : submap.keySet()) {
                    if (map.containsKey(name)) {
                        map.get(name).addAll(submap.get(name));
                    } else {
                        map.put(name, submap.get(name));
                    }
                }
                submap.clear();
            }
        } else if (DatasetUtil.isValidDataset(location)) {
            try (NetcdfFile nc = NetcdfDataset.openDataset(location, true, null)) {
                for (Variable variable : nc.getVariables()) {
                    if ((coordinateV & variable.isCoordinateVariable())
                            || (!coordinateV & !variable.isCoordinateVariable())) {
                        List<String> names = new ArrayList();
                        names.add(variable.getFullName().toLowerCase());
                        Attribute sname = variable.findAttributeIgnoreCase("standard_name");
                        if (null != sname) {
                            names.add(sname.getStringValue().toLowerCase());
                        }
                        Attribute lname = variable.findAttributeIgnoreCase("long_name");
                        if (null != lname) {
                            names.add(lname.getStringValue().toLowerCase());
                        }
                        for (String name : names) {
                            if (map.containsKey(name)) {
                                map.get(name).add(location);
                            } else {
                                List<String> list = new ArrayList();
                                list.add(location);
                                map.put(name, list);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(DatasetUtil.class.getName()).log(Level.WARNING, "[dataset] " + dataset_prefix + " Error listing variables from dataset " + location, ex);
            }
        }

        if (map.isEmpty()) {
            throw new IOException("[dataset] " + dataset_prefix + " Could not list any" + (coordinateV ? " coordinate " : " ") + "variable. Check dataset location " + location);
        }

        return map;
    }

    /**
     * Guess whether time is expressed in seconds in the NetCDF file and if not
     * return a conversion value to adjust it to seconds. So far it detects
     * seconds, hours or days. The test rely on the netCDF variable attribute
     * "units" (case insensitive).
     *
     * @param time, the NetCDF time variable
     * @return a conversion factor for time in seconds. Return one if the time
     * is already expressed in seconds.
     */
    public static double guessTimeConversion(Variable time) {
        // Try to read the units attribute to confirm it is seconds
        Attribute attrUnits = time.findAttributeIgnoreCase("units");
        if (null != attrUnits) {
            String units = attrUnits.getStringValue().toLowerCase();
            if (units.contains("second")) {
                // Seconds confirmed
                return 1.d;
            }
            if (units.contains("hour")) {
                // Hours confirmed
                return 3600.d;
            }
            if (units.contains("day")) {
                // Days confirmed
                return 3600.d * 24.d;
            }
//            if (units.contains("year")) {
//                // Years confirmed
//                return 3600.d * 24.d * 365.d;
//            }
        }

        // By default time is assumed to be expressed in seconds.
        // Should we throw an error instead of making such assumption ?..
        return 1.d;
    }

    public static NetcdfFile open(String filename, boolean enhanced) throws IOException {
        NetcdfFile nc;
        nc = NetcdfDataset.openDataset(filename, enhanced, null);
        getLogger().log(Level.FINE, "[dataset] Opened {0}", filename);
        return nc;
    }

    /*
     * Loads the NetCDF dataset from the specified filename.
     *
     * @param opendapURL a String that can be a local pathname or an OPeNDAP
     * URL.
     * @throws IOException
     */
    public static NetcdfFile openURL(String opendapURL, boolean enhanced) throws IOException {

        NetcdfFile ncIn;
        getLogger().log(Level.INFO, "[dataset] Opening remote URL {0} Please wait...", opendapURL);
        ncIn = NetcdfDataset.openDataset(opendapURL, enhanced, null);
        getLogger().log(Level.INFO, "[dataset] Remote URL opened {0}", opendapURL);
        return ncIn;
    }

    private static double skipSeconds(double time) {
        return 100.d * Math.floor(time / 100.d);
    }

    /**
     * Computes the geodesic distance between the two points (lat1, lon1) and
     * (lat2, lon2)
     *
     * @param lat1 a double, the latitude of the first point
     * @param lon1 a double, the longitude of the first point
     * @param lat2 double, the latitude of the second point
     * @param lon2 double, the longitude of the second point
     * @return a double, the curvilinear absciss s(A[lat1, lon1]B[lat2, lon2])
     */
    public static double geodesicDistance(double lat1, double lon1, double lat2, double lon2) {

        double lat1_rad = TO_RAD * lat1;
        double lat2_rad = TO_RAD * lat2;
        double lon1_rad = TO_RAD * lon1;
        double lon2_rad = TO_RAD * lon2;

        double sindlat = Math.sin(0.5d * (lat2_rad - lat1_rad));
        double sindlon = Math.sin(0.5d * (lon2_rad - lon1_rad));
        double d = DEARTH * Math.asin(
                Math.sqrt(sindlat * sindlat + Math.cos(lat1_rad) * Math.cos(lat2_rad) * sindlon * sindlon));

        return d;
    }
}
