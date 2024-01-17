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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

package org.previmer.ichthyop.util;

/**
 * imports
 */
import java.io.IOException;
import java.util.Comparator;
import ucar.ma2.InvalidRangeException;

import ucar.nc2.dataset.NetcdfDatasets;
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
            ncdf = NetcdfDatasets.openFile(nc1, null);
            n1 = ncdf.findVariable(strTime).read(new int[]{0}, new int[]{1}).getDouble(0);
            ncdf.close();
            ncdf = NetcdfDatasets.openFile(nc2, null);
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
