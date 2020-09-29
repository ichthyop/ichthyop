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
