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
import java.util.List;
import java.util.logging.Level;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.util.IOTools;
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
     * @param path, a String the directory
     * @param pattern, the file pattern with either * or ? metacharacters.
     * @return the list of files matching the pattern, alphabetically sorted.
     * @throws IOException if no file match the pattern
     */
    public static List<String> list(String path, String pattern) throws IOException {
        List<String> list = new ArrayList();

        File inputPath = new File(IOTools.resolvePath(path));
        if (!inputPath.isDirectory()) {
            throw new IOException("{Dataset} " + inputPath + " is not a valid directory.");
        }
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(pattern));
        if (listFile.length > 0) {
            list = new ArrayList(listFile.length);
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
        if (!new File(file).isFile()) {
            throw new FileNotFoundException(file);
        }
        NetcdfFile nc = NetcdfDataset.openDataset(file);
        Array timeArr = nc.findVariable(findVariable(nc, strTime)).read();
        double convert = guessTimeConversion(nc.findVariable(findVariable(nc, strTime)));
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
        if (!new File(file).isFile()) {
            throw new FileNotFoundException(file);
        }
        NetcdfFile nc = NetcdfDataset.openDataset(file);
        Array timeArr = nc.findVariable(findVariable(nc, strTime)).read();
        double convert = guessTimeConversion(nc.findVariable(findVariable(nc, strTime)));
        nc.close();
        return convert == 1.d
                ? skipSeconds(timeArr.getDouble(timeArr.getIndex().set(timeArr.getShape()[0] - 1)))
                : convert * timeArr.getDouble(timeArr.getIndex().set(timeArr.getShape()[0] - 1));
    }

    public static double timeAtRank(NetcdfFile nc, String strTime, int rank) throws IOException {
        Array timeArr = nc.findVariable(findVariable(nc, strTime)).read();
        double convert = guessTimeConversion(nc.findVariable(findVariable(nc, strTime)));
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
            throw new IOException("{Dataset} Unable to find any file following " + list.get(index));
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
        msg.append("{Dataset} Time value ");
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
    static int rank(double time, NetcdfFile nc, String strTime, int timeArrow) throws ArrayIndexOutOfBoundsException, IOException {

        int lrank = 0;
        double nctime;
        Array timeArr = null;
        try {
            Variable vtime = nc.findVariable(findVariable(nc, strTime));
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

    public static NetcdfFile openFile(String filename, boolean enhanced) throws IOException {
        NetcdfFile nc;
        nc = NetcdfDataset.openDataset(filename, enhanced, null);
        getLogger().log(Level.FINE, "'{'Dataset'}' Open {0}", filename);
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
}
