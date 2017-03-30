package org.previmer.ichthyop.util;

/**
 * imports
 */
import java.io.IOException;
import java.util.Comparator;
import ucar.ma2.InvalidRangeException;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.NetcdfFile;

/**
 * A comparison function of two netcdf files for chronological order. It
 * compares the first value of the time variable of the two files.
 *
 * <p>
 * Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class NCComparator implements Comparator<String> {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * The name of the time variable to be read in the NetCDF files.
     */
    private final String strTime;
    /**
     * Time arrow, 1 forward, -1 backward.
     */
    private final int timeArrow;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new NetCDF file comparator.
     *
     * @param strTime the name of the time variable to be read in the NetCDF
     * files.
     * @param timeArrow, 1 = forward, -1 = backward.
     */
    public NCComparator(String strTime, int timeArrow) {
        this.strTime = strTime;
        this.timeArrow = timeArrow;
    }

    public NCComparator(String strTime) {
        this(strTime, 1);
    }

///////////////////////////
// Definition of the method
///////////////////////////
    /**
     * Compares two NetCDF files for chronological order.
     *
     * @param nc1 the pathname of the first NetCDF file.
     * @param nc2 the pathname of the second NetCDF file.
     * @return a negative integer, zero, or a positive integer as the first
     * value of the time variable of the first file is less than, equal to, or
     * greater than the first value of the time variable of the second file.<p>
     *
     */
    @Override
    public int compare(String nc1, String nc2) {

        Double n1;
        Double n2;
        NetcdfFile ncdf;
        try {
            ncdf = NetcdfDataset.openFile(nc1, null);
            n1 = ncdf.findVariable(strTime).read(new int[]{0}, new int[]{1}).getDouble(0);
            ncdf.close();
            ncdf = NetcdfDataset.openFile(nc2, null);
            n2 = ncdf.findVariable(strTime).read(new int[]{0}, new int[]{1}).getDouble(0);
            ncdf.close();
        } catch (IOException | InvalidRangeException e) {
            return 0;
        }

        return timeArrow > 0
                ? n1.compareTo(n2)
                : n2.compareTo(n1);
    }
    //---------- End of class
}