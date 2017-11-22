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

import java.io.IOException;
import java.util.List;
import org.ichthyop.event.NextStepEvent;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public class Hycom3dDataset extends Hycom3dCommon {

    private List<String> uvFiles;
    private int index;

    @Override
    String getKey() {
        return "dataset.hycom_3d";
    }

    @Override
    void loadParameters() {
        try {
            // List uv files
            uvFiles = DatasetUtil.list(
                    getConfiguration().getString("dataset.hycom_3d.input_path"),
                    getConfiguration().getString("dataset.hycom_3d.uv_file_pattern"));
        } catch (IOException ex) {
            error("[dataset] Failed to list HYCOM NetCDF files in folder " + getConfiguration().getString("dataset.hycom_3d.input_path"), ex);
        }
        index = 0;
    }

    @Override
    public void init() throws Exception {

        int time_arrow = timeArrow();
        double time = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(uvFiles, time, time_arrow, "time");
        NetcdfFile nc = getNC();
        int rank = DatasetUtil.rank(time, nc, "time", time_arrow) - time_arrow;
        for (int istack = 0; istack < NLAYER - 1; istack++) {
            rank += time_arrow;
            int ntime = nc.findVariable(DatasetUtil.findVariable(nc, "time")).getShape()[0];
            if (rank > (ntime - 1) || rank < 0) {
                nc.close();
                index = DatasetUtil.next(uvFiles, index, time_arrow);
                nc = DatasetUtil.openFile(uvFiles.get(index), true);
                ntime = nc.findVariable(DatasetUtil.findVariable(nc, "time")).getShape()[0];
                rank = (1 - time_arrow) / 2 * (ntime - 1);
                time = DatasetUtil.timeAtRank(nc, "time", rank);
            } else {
                time = DatasetUtil.timeAtRank(nc, "time", rank);
            }
            u.update(new NetcdfTiledVariable(getNC(), "eastward_sea_water_velocity", getGrid(), rank, time, TILING_H, TILING_V));
            v.update(new NetcdfTiledVariable(getNC(), "northward_sea_water_velocity", getGrid(), rank, time, TILING_H, TILING_V));
            w.update(new WTiledVariable(getNC(), getGrid(), rank, time, WTILING_H));
        }
        nc.close();
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        int time_arrow = timeArrow();
        if (u.updateNeeded(time, time_arrow)) {
            int rank;
            try (NetcdfFile nc = getNC()) {
                int ntime = nc.findVariable(DatasetUtil.findVariable(nc, "time")).getShape()[0];
                rank = DatasetUtil.rank(time, nc, "time", time_arrow);
                rank += (NLAYER - 1) * time_arrow;
                if (rank > (ntime - 1) || rank < 0) {
                    try {
                        index = DatasetUtil.next(uvFiles, index, time_arrow);
                    } catch (IOException ex) {
                        return;
                    }
                    try (NetcdfFile nc2 = DatasetUtil.openFile(uvFiles.get(index), true)) {
                        ntime = nc2.findVariable(DatasetUtil.findVariable(nc2, "time")).getShape()[0];
                        rank = (1 - time_arrow) / 2 * (ntime - 1);
                        time = DatasetUtil.timeAtRank(nc2, "time", rank);
                    }
                } else {
                    time = DatasetUtil.timeAtRank(nc, "time", rank);
                }
            }
            // update variables
            u.update(new NetcdfTiledVariable(getNC(), "eastward_sea_water_velocity", getGrid(), rank, time, TILING_H, TILING_V));
            v.update(new NetcdfTiledVariable(getNC(), "northward_sea_water_velocity", getGrid(), rank, time, TILING_H, TILING_V));
            w.update(new WTiledVariable(getNC(), getGrid(), rank, time, WTILING_H));
        }
    }

    @Override
    NetcdfFile getNC() {
        try {
            return DatasetUtil.openFile(uvFiles.get(index), true);
        } catch (IOException ex) {
            error("Failed to open NetCDF file " + uvFiles.get(index), ex);
        }
        return null;
    }

}
