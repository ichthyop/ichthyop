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

import java.util.List;
import org.previmer.ichthyop.event.NextStepEvent;

/**
 *
 * @author pverley
 */
public class Mars3dDataset extends Mars3dCommon {

    private List<String> files;
    private int index;

    @Override
    void openDataset() throws Exception {

        files = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(files, strTime, timeArrow());
        }
        ncIn = DatasetUtil.openFile(files.get(0), true);
        readTimeLength();
    }

    @Override
    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(files, t0, timeArrow, strTime);
        ncIn = DatasetUtil.openFile(files.get(index), true);
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow);
        time_tp1 = t0;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        if (timeArrow * time < timeArrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        
        // if(this.readKz()) { 
        //     kz_tp0 = kz_tp1;   
        // }
        kz_tp0 = kz_tp1 ; 
        zeta_tp0 = zeta_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += timeArrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncIn.close();
            index = DatasetUtil.next(files, index, timeArrow);
            ncIn = DatasetUtil.openFile(files.get(index), true);
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            rank = (1 - timeArrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }
}
