/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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

package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.List;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import static org.previmer.ichthyop.io.IOTools.isDirectory;
import static org.previmer.ichthyop.io.IOTools.isFile;
/**
 *
 * @author pverley
 */
public class Roms3dDataset extends Roms3dCommon {
    
    private List<String> ncfiles;
    private int ncindex;

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
        zeta_tp0 = zeta_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncindex = DatasetUtil.next(ncfiles, ncindex, time_arrow);
            ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
            readTimeLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    void openDataset() throws Exception {

        ncfiles = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(ncfiles, strTime, timeArrow());
        }
        ncIn = DatasetUtil.openFile(ncfiles.get(0), true);
        readTimeLength();
        
        try {
            if (!getParameter("grid_file").isEmpty()) {
                String path = IOTools.resolveFile(getParameter("grid_file"));  // barrier.n
                if (!isFile(path)) {
                    throw new IOException("{Dataset} " + getParameter("grid_file") + " is not a valid file.");
                }
                gridFile = path;
            
            } else {
                gridFile = ncIn.getLocation();
            }
        } catch (NullPointerException ex) {
            gridFile = ncIn.getLocation();
        }      
    }

    @Override
    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        ncindex = DatasetUtil.index(ncfiles, t0, timeArrow(), strTime);
        ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }
}
