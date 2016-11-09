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

package org.ichthyop.util;

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
