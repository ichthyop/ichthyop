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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static org.ichthyop.SimulationManagerAccessor.getLogger;
import org.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public class Hycom3dOpendapDataset extends Hycom3dCommon {

    private NetcdfFile nc;
    private int nbTimeRecords;

    @Override
    void loadParameters() {
    }

    @Override
    public void setUp() throws Exception {

        nc = DatasetUtil.openURL("http://tds.hycom.org/thredds/dodsC/GLBu0.08/expt_91.2", true);
        longitude = (double[]) nc.findVariableByAttribute(null, "standard_name", "longitude").read().copyTo1DJavaArray();
        latitude = (double[]) nc.findVariableByAttribute(null, "standard_name", "latitude").read().copyTo1DJavaArray();
        depthLevel = (double[]) nc.findVariableByAttribute(null, "standard_name", "depth").read().copyTo1DJavaArray();
        nx = longitude.length;
        ny = latitude.length;
        nz = depthLevel.length;
        nbTimeRecords = nc.findDimension("time").getLength();
        getDimGeogArea();
        // read velocity at t=0 for an initial mask
        setAllFieldsTp1AtTime(0);
        // bathy missing @TODO
    }

    @Override
    public void init() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        rank = DatasetUtil.rank(t0, nc, "time", timeArrow());
        time_tp1 = t0;
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {
        double time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            throw new IndexOutOfBoundsException("Time out of dataset range");
        }
        setAllFieldsTp1AtTime(rank);
    }

    void setAllFieldsTp1AtTime(int rank) throws Exception {

        getLogger().info("Reading NetCDF variables...");

        int[] origin = new int[]{rank, 0, 0, 0};
        double time_tp0 = time_tp1;
        Array arr;
        Index index;

        try {
            arr = nc.findVariable("water_u").read(origin, new int[]{1, nz, ny, nx}).reduce();
            u_tp1 = new double[nz][ny][nx];
            index = arr.getIndex();
            for (int k = 0; k < nz; k++) {
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        u_tp1[k][j][i] = arr.getDouble(index.set(k, j, i));
                    }
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arr = nc.findVariable("water_v").read(origin, new int[]{1, nz, ny, nx}).reduce();
            v_tp1 = new double[nz][ny][nx];
            index = arr.getIndex();
            for (int k = 0; k < nz; k++) {
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        v_tp1[k][j][i] = arr.getDouble(index.set(k, j, i));
                    }
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            time_tp1 = DatasetUtil.timeAtRank(nc, "time", rank);
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading dataset time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);

        w_tp1 = computeW();
    }

}
