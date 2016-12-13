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
import java.util.logging.Level;
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
    void open() throws Exception {
        // List uv files
        uvFiles = DatasetUtil.list(getParameter("input_path"), getParameter("uv_file_pattern"));
        index = 0;
    }

    @Override
    public void init() throws Exception {
        int time_arrow = timeArrow();
        DatasetUtil.sort(uvFiles, "time", time_arrow);
        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(uvFiles, t0, time_arrow, "time");
        NetcdfFile nc = DatasetUtil.openFile(uvFiles.get(index), true);
        nbTimeRecords = nc.findDimension("time").getLength();
        rank = DatasetUtil.rank(t0, nc, "time", time_arrow);
        time_tp1 = t0;

        // t+1
        u[1] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index), true), "eastward_sea_water_velocity", nx, ny, nz, i0, j0, rank, tilingh, tilingv);
        v[1] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index), true), "northward_sea_water_velocity", nx, ny, nz, i0, j0, rank, tilingh, tilingv);
        w[1] = new WTiledVariable(DatasetUtil.openFile(uvFiles.get(index), true), nx, ny, nz, i0, j0, tilinghw, rank);

        // t+2
        int rank2 = rank + time_arrow;
        int index2 = index;
        if (rank2 > (nbTimeRecords - 1) || rank2 < 0) {
            index2 = DatasetUtil.next(uvFiles, index, time_arrow);
            nc = DatasetUtil.openFile(uvFiles.get(index2), true);
            int nbTimeRecords2 = nc.findDimension("time").getLength();
            nc.close();
            rank2 = (1 - time_arrow) / 2 * (nbTimeRecords2 - 1);
        }
        u[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "eastward_sea_water_velocity", nx, ny, nz, i0, j0, rank2, tilingh, tilingv);
        v[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "northward_sea_water_velocity", nx, ny, nz, i0, j0, rank2, tilingh, tilingv);
        w[2] = new WTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), nx, ny, nz, i0, j0, tilinghw, rank2);

        //checkRequiredVariable(nc);
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        int time_arrow = timeArrow();

        if (time_arrow * time < time_arrow * time_tp1) {
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
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            index = DatasetUtil.next(uvFiles, index, time_arrow);
            NetcdfFile nc = DatasetUtil.openFile(uvFiles.get(index), true);
            nbTimeRecords = nc.findDimension("time").getLength();
            nc.close();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        double time_tp0 = time_tp1;
        NetcdfFile nc = DatasetUtil.openFile(uvFiles.get(index), true);
        time_tp1 = DatasetUtil.timeAtRank(nc, "time", rank);
        nc.close();
        dt_HyMo = Math.abs(time_tp1 - time_tp0);

        // t+2
        int rank2 = rank + time_arrow;
        int index2 = index;
        if (rank2 > (nbTimeRecords - 1) || rank2 < 0) {
            try {
            index2 = DatasetUtil.next(uvFiles, index, time_arrow);
            } catch(IOException ex) {
                return;
            }
            nc = DatasetUtil.openFile(uvFiles.get(index2), true);
            int nbTimeRecords2 = nc.findDimension("time").getLength();
            nc.close();
            rank2 = (1 - time_arrow) / 2 * (nbTimeRecords2 - 1);
        }
        u[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "eastward_sea_water_velocity", nx, ny, nz, i0, j0, rank2, tilingh, tilingv);
        v[2] = new NetcdfTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), "northward_sea_water_velocity", nx, ny, nz, i0, j0, rank2, tilingh, tilingv);
        w[2] = new WTiledVariable(DatasetUtil.openFile(uvFiles.get(index2), true), nx, ny, nz, i0, j0, tilinghw, rank2);
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
            getLogger().log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
