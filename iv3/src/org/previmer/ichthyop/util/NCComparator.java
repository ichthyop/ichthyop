package org.previmer.ichthyop.util;

/** imports */
import java.util.Comparator;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.NetcdfFile;

/**
 * A comparison function of two netcdf files for chronological order.
 * It compares the first value of the time variable of the two files.

 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class NCComparator implements Comparator<String> {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * The name of the time variable to be read in the netcdf files.
     */
    private String strTime;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new netcdf file comparator.
     *
     * @param strTime the name of the time variable to be read in the
     * netcdf files.
     */
    public NCComparator(String strTime) {
        this.strTime = strTime;
    }

///////////////////////////
// Definition of the method
///////////////////////////
    /**
     * Compares two netcdf files for chronological order.
     *
     * @param nc1 the pathname of the first netcdf file.
     * @param nc2 the pathname of the second netcdf file.
     * @return a negative integer,
     * zero, or a positive integer as the first value of the time variable of
     * the first file is less than, equal to, or greater than the first
     * value of the time variable of the second file.<p>

     */
    public int compare(String nc1, String nc2) {

        Double n1 = new Double(0);
        Double n2 = new Double(0);
        NetcdfFile ncdf;
        try {
            ncdf = NetcdfDataset.openFile(nc1, null);
            n1 = ncdf.findVariable(strTime).read(new int[]{0}, new int[]{1}).getDouble(0);
            ncdf.close();
            ncdf = NetcdfDataset.openFile(nc2, null);
            n2 = ncdf.findVariable(strTime).read(new int[]{0}, new int[]{1}).getDouble(0);
            ncdf.close();
        } catch (Exception e) {
            //e.printStackTrace();
            return 0;
        }

        return n1.compareTo(n2);
    }
    //---------- End of class
}
