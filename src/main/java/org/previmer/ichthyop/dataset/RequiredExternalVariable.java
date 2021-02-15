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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author G.Andres
 */
public class RequiredExternalVariable {

    private final IDataset dataset;
    private Array array_tp0, array_tp1;
    private double time_tp1, dt_dataset;
    private final int nx_file;
    private final int ny_file;
    private final double[][] latRho;
    private final double[][] lonRho;

    public RequiredExternalVariable(double[][] lat, double[][] lon, Array variable0, Array variable1, IDataset dataset_hydro) throws IOException {
        this.dataset = dataset_hydro;
        latRho = lat;
        lonRho = lon;
        array_tp0 = variable0;
        array_tp1 = variable1;
        nx_file = lonRho[0].length;
        ny_file = lonRho.length;
        if (!isInto(dataset)) {
            throw new IOException("!! WARNING : please use option Dataset/Shrink Domain "
                    + "to ensure that your ichthyop domain is contained within the new grid. \n"
                    + "Min and max longitude : " + lonRho[0][0] + " " + lonRho[0][nx_file - 1]
                    + " Min an max latitude : " + latRho[0][0] + " " + latRho[ny_file - 1][0]
                    + ". \nActual domain (longitude latitude) : " + dataset.getLonMin() + " "
                    + dataset.getLonMax() + "     " + dataset.getLatMin() + " " + dataset.getLatMax());
        }
    }

    private boolean isInto(IDataset dataset) {
        boolean isInto = true;

        /** Coordinates for the required dataset, i.e. 
         * the wind coordinates. */
        double lat_min_meteo = latRho[0][0];
        double lat_max_meteo = latRho[ny_file - 1][0];
        double lon_min_meteo = lonRho[0][0];
        double lon_max_meteo = lonRho[0][nx_file - 1];

        /** Coordinates for the input dataset, i.e. OSCAR */
        double lon_max = dataset.getLonMax();
        double lat_max = dataset.getLatMax();
        double lon_min = dataset.getLonMin();
        double lat_min = dataset.getLatMin();

        if (this.dataset.getClass() == OscarDataset.class) {
            // If Oscar Dataset, we assume that all longitudes are represented.
            // only latitudes are compared. 
            if (lat_max > lat_max_meteo) {
                isInto = false;
            } else {
                if (lat_min < lat_min_meteo) {
                    isInto = false;
                }
            }
        } else {
            // for the other datasets, longitudes are also compared.
            if (lon_max > lon_max_meteo) {
                isInto = false;
            } else {
                if (lat_max > lat_max_meteo) {
                    isInto = false;
                } else {
                    if (lat_min < lat_min_meteo) {
                        isInto = false;
                    } else {
                        if (lon_min < lon_min_meteo) {
                            isInto = false;
                        }
                    }
                }
            }
        }
        

        return isInto;
    }

    /*
     * computeVariable used to compute the entire grid of th variable without interpolate time
     */
    /*
    private double computeVariable(double[] pGrid_hydro) {

        double[] latlon = dataset.xy2latlon(pGrid_hydro[0], pGrid_hydro[1]);
        double[] pGrid = latlon2xy(latlon[0], latlon[1]);

        int n = dataset.isCloseToCost(pGrid_hydro) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;

        int[] shape = new int[]{2, 2};
        int[] origin = new int[]{j, i};
        try {
            double value_t0 = interp2D(array_tp0.section(origin, shape), dx, dy, n);
            //double value_t1 = interp2D(array_tp1.section(origin, shape), dx, dy, n);
            return value_t0; //interpTime(value_t0, value_t1, time);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
            return Float.NaN;
        }
    }
    */

    /*
     * getVariable to compute the value of variable in a grid_hydro point with time interpolating
     */
    public double getVariable(double[] pGrid_hydro, double time) {

        double[] latlon = dataset.xy2latlon(pGrid_hydro[0], pGrid_hydro[1]);
        double[] pGrid = latlon2xy(latlon[0], latlon[1]);
        int n = dataset.isCloseToCost(pGrid_hydro) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;

        int[] shape = new int[]{2, 2};
        int[] origin = new int[]{j, i};
        try {
            double value_t0 = interp2D(array_tp0.section(origin, shape), dx, dy, n);
            double value_t1 = interp2D(array_tp1.section(origin, shape), dx, dy, n);
            return interpTime(value_t0, value_t1, time);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
            return Float.NaN;
        }
    }

    private double interp2D(Array array, double dx, double dy, int n) {
        double value = 0.d;
        double CO = 0.d;

        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                double co = Math.abs((1.d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                value += array.getFloat(array.getIndex().set(jj, ii)) * co;
            }
        }
        if (CO != 0) {
            value /= CO;
        }
        return value;
    }

    private double interpTime(double value_t0, double value_t1, double time) {
        double frac = (dt_dataset - Math.abs(time_tp1 - time)) / dt_dataset;
        return (1.d - frac) * value_t0 + frac * value_t1;
    }

    public void nextStep(Array array_tp1, double time_tp1, double dt_dataset) {

        this.time_tp1 = time_tp1;
        this.dt_dataset = dt_dataset;
        array_tp0 = this.array_tp1;
        this.array_tp1 = array_tp1;
    }

    public double[] latlon2xy(double lat, double lon) {

        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)
        boolean found;
        int imin, imax, jmin, jmax, i0, j0;
        double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

        xgrid = -1.;
        ygrid = -1.;
        found = isInsidePolygone(0, nx_file - 1, 0, ny_file - 1, lon, lat);

        //-------------------------------------------
        // Research surrounding grid-points
        if (found) {
            imin = 0;
            imax = nx_file - 1;
            jmin = 0;
            jmax = ny_file - 1;
            while (((imax - imin) > 1) | ((jmax - jmin) > 1)) {
                if ((imax - imin) > 1) {
                    i0 = (imin + imax) / 2;
                    found = isInsidePolygone(imin, i0, jmin, jmax, lon, lat);
                    if (found) {
                        imax = i0;
                    } else {
                        imin = i0;
                    }
                }
                if ((jmax - jmin) > 1) {
                    j0 = (jmax + jmin) / 2;
                    found = isInsidePolygone(imin, imax, jmin, j0, lon, lat);
                    if (found) {
                        jmax = j0;
                    } else {
                        jmin = j0;
                    }
                }
            }

            //--------------------------------------------
            // Trilinear interpolation
            dy1 = latRho[jmin + 1][imin] - latRho[jmin][imin];
            dx1 = lonRho[jmin + 1][imin] - lonRho[jmin][imin];
            dy2 = latRho[jmin][imin + 1] - latRho[jmin][imin];
            dx2 = lonRho[jmin][imin + 1] - lonRho[jmin][imin];

            c1 = lon * dy1 - lat * dx1;
            c2 = lonRho[jmin][imin] * dy2 - latRho[jmin][imin] * dx2;
            deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
            deltax = (deltax - lonRho[jmin][imin]) / dx2;
            xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

            c1 = lonRho[jmin][imin] * dy1 - latRho[jmin][imin] * dx1;
            c2 = lon * dy2 - lat * dx2;
            deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
            deltay = (deltay - latRho[jmin][imin]) / dy1;
            ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
        }
        return (new double[]{xgrid, ygrid});
    }

    boolean isInsidePolygone(int imin, int imax, int jmin, int jmax, double lon, double lat) {

        //--------------------------------------------------------------
        // Return true if (lon, lat) is insidide the polygon defined by
        // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)
        //-----------------------------------------
        // Build the polygone
        int nb, shft;
        double[] xb, yb;
        boolean isInPolygone = true;

        nb = 2 * (jmax - jmin + imax - imin);
        xb = new double[nb + 1];
        yb = new double[nb + 1];
        shft = 0 - imin;
        for (int i = imin; i <= (imax - 1); i++) {
            xb[i + shft] = lonRho[jmin][i];
            yb[i + shft] = latRho[jmin][i];
        }
        shft = 0 - jmin + imax - imin;
        for (int j = jmin; j <= (jmax - 1); j++) {
            xb[j + shft] = lonRho[j][imax];
            yb[j + shft] = latRho[j][imax];
        }
        shft = jmax - jmin + 2 * imax - imin;
        for (int i = imax; i >= (imin + 1); i--) {
            xb[shft - i] = lonRho[jmax][i];
            yb[shft - i] = latRho[jmax][i];
        }
        shft = 2 * jmax - jmin + 2 * (imax - imin);
        for (int j = jmax; j >= (jmin + 1); j--) {
            xb[shft - j] = lonRho[j][imin];
            yb[shft - j] = latRho[j][imin];
        }
        xb[nb] = xb[0];
        yb[nb] = yb[0];

        //---------------------------------------------
        //Check if {lon, lat} is inside polygone
        int inc, crossings;
        double dx1, dx2, dxy;
        crossings = 0;

        for (int k = 0; k < nb; k++) {
            if (xb[k] != xb[k + 1]) {
                dx1 = lon - xb[k];
                dx2 = xb[k + 1] - lon;
                dxy = dx2 * (lat - yb[k]) - dx1 * (yb[k + 1] - lat);
                inc = 0;
                if ((xb[k] == lon) & (yb[k] == lat)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (lat >= yb[k]))
                        | ((dx2 == 0.) & (lat >= yb[k + 1]))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) & ((xb[k + 1] - xb[k]) * dxy >= 0.)) {
                    inc = 2;
                }
                if (xb[k + 1] > xb[k]) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }
        if (crossings == 0) {
            isInPolygone = false;
        }
        return (isInPolygone);
    }

}
