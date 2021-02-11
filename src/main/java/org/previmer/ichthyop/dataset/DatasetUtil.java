/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import static org.previmer.ichthyop.SimulationManagerAccessor.getLogger;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.TimeManager;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class DatasetUtil {

    /**
     * List directory content with file pattern accepting * and ?
     * metacharacters. Not recursive.
     *
     * @param path, a String the directory
     * @param pattern, the file pattern with either * or ? metacharacters.
     * @return the list of files matching the pattern, alphabetically sorted.
     * @throws IOException if no file match the pattern
     */
    public static List<String> list(String path, String pattern) throws IOException {
        List<String> list = new ArrayList<>();

        File inputPath = new File(IOTools.resolvePath(path));
        if (!inputPath.isDirectory()) {
            throw new IOException("[Dataset] " + inputPath + " is not a valid directory.");
        }
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(pattern));
        if (listFile.length > 0) {
            list = new ArrayList<>(listFile.length);
            for (File file : listFile) {
                list.add(file.toString());
            }
            Collections.sort(list);
        }
        return list;
    }

    /**
     * Sort of the NetCDF files from the list, comparing the first value of the
     * time variable.
     *
     * @param list
     * @param strTime, the name of the time variable in the NetCDF file
     * @param timeArrow, 1 forward, -1 backward
     */
    public static void sort(List<String> list, String strTime, int timeArrow) {
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
        if (!new File(file).isFile()) {
            throw new FileNotFoundException(file);
        }
        
        return getDate(file, strTime, 0);
        
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
        if (!new File(file).isFile()) {
            throw new FileNotFoundException(file);
        }
        NetcdfFile nc = NetcdfDataset.openDataset(file);
        Array timeArr = nc.findVariable(strTime).read();
        nc.close();
        
        int lastIndex = timeArr.getShape()[0] - 1;
        
        return getDate(file, strTime, lastIndex);
        
    }

    public static double timeAtRank(NetcdfFile nc, String strTime, int rank) throws IOException {
        return DatasetUtil.getDate(nc.getLocation(), strTime, rank);
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
            throw new IOException("[Dataset] Unable to find any file following " + list.get(index));
        }
        return index + 1;
    }

    public static int index(List<String> list, double time, int timeArrow, String strTime) throws IOException {

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
        msg.append("[Dataset] Time value ");
        msg.append(time);
        msg.append(" (in seconds) not contained among NetCDF files.");
        throw new IndexOutOfBoundsException(msg.toString());
    }

    /**
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
            timeArr = nc.findVariable(strTime).read();
            nctime = DatasetUtil.getDate(nc.getLocation(), strTime, lrank);
            while (time >= nctime) {
                if (timeArrow < 0 && time == nctime) {
                    break;
                }
                lrank++;
                nctime = DatasetUtil.getDate(nc.getLocation(), strTime, lrank);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = timeArr.getShape()[0];
        }
        lrank = lrank - (timeArrow + 1) / 2;

        return lrank;
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
        }

        // By default time is assumed to be expressed in seconds.
        // Should we throw an error instead of making such assumption ?..
        return 1.d;
    }

    static NetcdfFile openFile(String filename, boolean enhanced) throws IOException {
        NetcdfFile nc;
        nc = NetcdfDataset.openDataset(filename, enhanced, null);
        getLogger().log(Level.INFO, "'{'Dataset'}' Open {0}", filename);
        return nc;
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     *
     * @param opendapURL a String that can be a local pathname or an OPeNDAP
     * URL.
     * @throws IOException
     */
    static NetcdfFile openURL(String opendapURL, boolean enhanced) throws IOException {

        NetcdfFile ncIn;
        getLogger().log(Level.INFO, "Opening remote URL {0} Please wait...", opendapURL);
        ncIn = NetcdfDataset.openDataset(opendapURL, enhanced, null);
        getLogger().log(Level.INFO, "'{'Dataset'}' Open remote {0}", opendapURL);
        return ncIn;
    }

    /**
     * Computes the Hyperbolic Sinus of x
     *
     * @param x
     * @return
     */
    public static double sinh(double x) {
        return ((Math.exp(x) - Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Cosinus of x
     *
     * @param x
     * @return
     */
    public static double cosh(double x) {
        return ((Math.exp(x) + Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Tangent of x
     *
     * @param x
     * @return
     */
    public static double tanh(double x) {
        return (sinh(x) / cosh(x));
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

        double lat1_rad = Math.PI * lat1 / 180.d;
        double lat2_rad = Math.PI * lat2 / 180.d;
        double lon1_rad = Math.PI * lon1 / 180.d;
        double lon2_rad = Math.PI * lon2 / 180.d;

        double d = 2 * 6367000.d
                * Math.asin(Math.sqrt(Math.pow(Math.sin((lat2_rad - lat1_rad) / 2), 2)
                                + Math.cos(lat1_rad) * Math.cos(lat2_rad) * Math.pow(Math.sin((lon2_rad - lon1_rad) / 2), 2)));

        return d;
    }

    /**
     * <p>
     * The functions computes the 2nd order approximate derivative at index
     * i</p>
     * <code>diff2(X, i) == diff(diff(X), i) == diff(diff(X))[i]</code>
     *
     * @param X double[]
     * @param k
     * @return double
     */
    public static double diff2(double[] X, int k) {

        int length = X.length;
        /**
         * Returns NaN if size <= 2
         */
        if (length < 3) {
            return Double.NaN;
        }

        /**
         * This return statement traduces the natural spline hypothesis M(0) =
         * M(nz - 1) = 0
         */
        if ((k <= 0) || (k >= (length - 1))) {
            return 0.d;
        }

        return (X[k + 1] - 2.d * X[k] + X[k - 1]);
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
    public static double euclidianDistance(double lat1, double lon1, double lat2, double lon2) {

        double d = Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
        return d;
    }
    
    public static double getDate(String file, String strTime, int index) throws IOException {

        if (!new File(file).isFile()) {
            throw new FileNotFoundException(file);
        }
      
        // Date formatter to extract NetCDF time
        DateTimeFormatter dateFormatter;
        
        // Output date
        LocalDateTime finalDate;

        // Open the NetCDF file
        // Recover the time variable and units
        NetcdfFile nc = NetcdfDataset.openDataset(file);
        Variable timeVar = nc.findVariable(strTime);
        Attribute attrUnits = timeVar.findAttributeIgnoreCase("units");

        // Recover the time values for the corresponding index
        Array timeArr = nc.findVariable(strTime).read();
        long time = (long) timeArr.getDouble(timeArr.getIndex().set(index));

        // Converts string into lower case.
        String units = attrUnits.getStringValue().toLowerCase();

        // Extract the NetCDF reference date by removing the
        // prefix (day(s), month(s) or day(s)) and the seconds values
        int beginIndex = units.indexOf("since") + 5;
        int endIndex = units.lastIndexOf(":");
        String subUnits = units.substring(beginIndex, endIndex).trim();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateUnit = LocalDateTime.parse(subUnits, dateFormatter);

        if (units.contains("second")) {
            finalDate = dateUnit.plusSeconds(time);
        } else if (units.contains("hour")) {
            finalDate = dateUnit.plusHours(time);
        } else {
            finalDate = dateUnit.plusDays(time);
        }

        double output = Duration.between(TimeManager.DATE_REF, finalDate).toSeconds();
        return output;

    }
        
}
