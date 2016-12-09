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

import java.util.List;
import org.ichthyop.event.NextStepEvent;

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
        nc = DatasetUtil.openFile(uvFiles.get(0), true);
    }

    @Override
    public void init() throws Exception {
        DatasetUtil.sort(uvFiles, "time", timeArrow());
        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(uvFiles, t0, timeArrow(), "time");
        nc.close();
        nc = DatasetUtil.openFile(uvFiles.get(index), true);
        nbTimeRecords = nc.findDimension("time").getLength();
        rank = DatasetUtil.rank(t0, nc, "time", timeArrow());
        time_tp1 = t0;
        setAllFieldsTp1AtTime(rank);
        checkRequiredVariable(nc);
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
        if (null != v[0]) {
            v[0].clear();
        }
        v[0] = v[1];

        if (null != uw[0]) {
            uw[0].clear();
        }
        uw[0] = uw[1];
        if (null != vw[0]) {
            vw[0].clear();
        }
        vw[0] = vw[1];
        if (null != wmap[0]) {
            wmap[0].clear();
        }
        wmap[0] = wmap[1];

        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            //nc.close();
            index = DatasetUtil.next(uvFiles, index, time_arrow);
            nc = DatasetUtil.openFile(uvFiles.get(index), true);
            nbTimeRecords = nc.findDimension("time").getLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }

        setAllFieldsTp1AtTime(rank);
    }

}