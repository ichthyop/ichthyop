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
    void open() throws Exception {
        // List uv files
        uvFiles = DatasetUtil.list(getConfiguration().getString("dataset.hycom_3d.input_path"),
                getConfiguration().getString("dataset.hycom_3d.uv_file_pattern"));
        index = 0;
    }

    @Override
    public void init() throws Exception {
        int time_arrow = timeArrow();
        DatasetUtil.sort(uvFiles, "time", time_arrow);
        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(uvFiles, t0, time_arrow, "time");
        NetcdfFile nc = DatasetUtil.openFile(uvFiles.get(index), true);
        String name = DatasetUtil.findVariable(nc, "time");
        if (null == name) {
            throw new IOException("Time variable not found in HYCOM dataset");
        }
        int ntime = nc.findVariable(name).getShape()[0];
        int rank = DatasetUtil.rank(t0, nc, "time", time_arrow);
        double time_tp1 = t0;

        int nx = getGrid().get_nx();
        int ny = getGrid().get_ny();
        int nz = getGrid().get_nz();
        int i0 = getGrid().get_i0();
        int j0 = getGrid().get_j0();

        // t+1
        u[1] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index), true), "eastward_sea_water_velocity", getGrid(), rank, time_tp1, tilingh, tilingv);
        v[1] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index), true), "northward_sea_water_velocity", getGrid(), rank, time_tp1, tilingh, tilingv);
        w[1] = new WTiledVariable(DatasetUtil.openFile(uvFiles.get(index), true), getGrid(), rank, time_tp1, tilinghw);

        // t+2
        rank += time_arrow;
        int index2 = index;
        if (rank > (ntime - 1) || rank < 0) {
            nc.close();
            index2 = DatasetUtil.next(uvFiles, index, time_arrow);
            nc = DatasetUtil.openFile(uvFiles.get(index2), true);
            ntime = nc.findVariable(name).getShape()[0];
            rank = (1 - time_arrow) / 2 * (ntime - 1);
        }
        double time_tp2 = DatasetUtil.timeAtRank(nc, "time", rank);
        nc.close();
        u[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "eastward_sea_water_velocity", getGrid(), rank, time_tp2, tilingh, tilingv);
        v[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "northward_sea_water_velocity", getGrid(), rank, time_tp2, tilingh, tilingv);
        w[2] = new WTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), getGrid(), rank, time_tp2, tilinghw);

        //checkRequiredVariable(nc);
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        int time_arrow = timeArrow();

        if (time_arrow * time < time_arrow * u[1].getTimeStamp()) {
            return;
        }

        if (null != u[0]) {
            u[0].clear();
        }
        u[0] = u[1];
        u[1] = u[2];

        if (null != v[0]) {
            v[0].clear();
        }
        v[0] = v[1];
        v[1] = v[2];

        if (null != w[0]) {
            w[0].clear();
        }
        w[0] = w[1];
        w[1] = w[2];

        // t+1
        NetcdfFile nc = DatasetUtil.openFile(uvFiles.get(index), true);
        int ntime = nc.findVariable(DatasetUtil.findVariable(nc, "time")).getShape()[0];
        int rank = DatasetUtil.rank(time, nc, "time", time_arrow) + time_arrow;
        if (rank > (ntime - 1) || rank < 0) {
            nc.close();
            index = DatasetUtil.next(uvFiles, index, time_arrow);
            nc = DatasetUtil.openFile(uvFiles.get(index), true);
            ntime = nc.findVariable(DatasetUtil.findVariable(nc, "time")).getShape()[0];
            rank = (1 - time_arrow) / 2 * (ntime - 1);
        }

        // t+2
        rank += time_arrow;
        int index2 = index;
        if (rank > (ntime - 1) || rank < 0) {
            nc.close();
            try {
                index2 = DatasetUtil.next(uvFiles, index, time_arrow);
            } catch (IOException ex) {
                return;
            }
            nc = DatasetUtil.openFile(uvFiles.get(index2), true);
            ntime = nc.findVariable(DatasetUtil.findVariable(nc, "time")).getShape()[0];
            rank = (1 - time_arrow) / 2 * (ntime - 1);
        }
        double time_tp2 = DatasetUtil.timeAtRank(nc, "time", rank);
        nc.close();
        int nx = getGrid().get_nx();
        int ny = getGrid().get_ny();
        int nz = getGrid().get_nz();
        int i0 = getGrid().get_i0();
        int j0 = getGrid().get_j0();
        u[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "eastward_sea_water_velocity", getGrid(), rank, time_tp2, tilingh, tilingv);
        v[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "northward_sea_water_velocity", getGrid(), rank, time_tp2, tilingh, tilingv);
        w[2] = new WTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), getGrid(), rank, time_tp2, tilinghw);
        // pre-load tiles
        u[2].loadTiles(u[0].getTilesIndex());
        v[2].loadTiles(v[0].getTilesIndex());
        w[2].loadTiles(w[0].getTilesIndex());

//        for (RequiredVariable variable : requiredVariables.values()) {
//            variable.nextStep(readVariable(nc, variable.getName(), rank), time_tp1, dt_HyMo);
//        }
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
