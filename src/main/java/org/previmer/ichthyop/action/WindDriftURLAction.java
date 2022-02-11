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

package org.previmer.ichthyop.action;

import java.io.IOException;
import java.util.logging.Level;

import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDatasets;

public class WindDriftURLAction extends WindDriftFileAction {


    @Override
    public void loadParameters() throws Exception {
        strTime = getParameter("field_time");
        time_current=getSimulationManager().getTimeManager().getTime();
        openURL(getParameter("wind_url"));
        wind_factor=Double.valueOf(getParameter("wind_factor"));
        angle = Math.PI/2.0-Double.valueOf(getParameter("angle"))*Math.PI/180.0;
        strUW=getParameter("wind_u");
        strVW=getParameter("wind_v");
        strLon=getParameter("longitude");
        strLat=getParameter("latitude");
        convention = "wind to".equals(getParameter("wind_convention"))? 1 : -1;

        getDimNC();
        setOnFirstTime();
        setAllFieldsTp1AtTime(rank);
        readLonLat();

        U_variable=new RequiredExternalVariable(latRho,lonRho,uw_tp0,uw_tp1,getSimulationManager().getDataset());
        V_variable=new RequiredExternalVariable(latRho,lonRho,vw_tp0,vw_tp1,getSimulationManager().getDataset());

    }

    static void openURL(String opendapURL) throws IOException {
        try {
            ncIn = NetcdfDatasets.openDataset(opendapURL);
            getLogger().log(Level.INFO, "'{'Wind Dataset'}' Open remote {0}", opendapURL);
        } catch (Exception e) {
            IOException ioex = new IOException("{Wind Dataset} Problem opening " + opendapURL + " ==> " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
    }

    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        readTimeLength();
        checkInitTime(ncIn, strTime);
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }

    public void checkInitTime(NetcdfFile nc, String strTime) throws IOException, IndexOutOfBoundsException, Exception{

        double time = getSimulationManager().getTimeManager().get_tO();
        Array timeArr = null;
        try {
            timeArr = nc.findVariable(strTime).read();
        } catch (Exception ex) {
            IOException ioex = new IOException("{Wind dataset} Failed to read time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        int ntime = timeArr.getShape()[0];
        double time0 = DatasetUtil.getDate(nc.getLocation(), strTime, 0);
        double timeN = DatasetUtil.getDate(nc.getLocation(), strTime, ntime - 1);
        if (time < time0 || time > timeN) {
            StringBuilder msg = new StringBuilder();
            msg.append("{Wind dataset} Time value ");
            msg.append(getSimulationManager().getTimeManager().timeToString());
            msg.append(" (");
            msg.append(time);
            msg.append(" seconds) not contained in dataset ");
            msg.append(nc.getLocation());
            throw new IndexOutOfBoundsException(msg.toString());
        }
    }

    public void nextStepTriggered() throws Exception {
        double time = getSimulationManager().getTimeManager().getTime();
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        uw_tp0 = uw_tp1;
        vw_tp0 = vw_tp1;

        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            throw new IndexOutOfBoundsException("Time out of wind dataset range");
        }
        setAllFieldsTp1AtTime(rank);
    }

    void setAllFieldsTp1AtTime(int i_time) throws Exception {

        double time_tp0 = time_tp1;
        uw_tp1 = readVariable(strUW);
        vw_tp1 = readVariable(strVW);

        time_tp1 = DatasetUtil.getDate(ncIn.getLocation(), strTime, rank);

        dt_wind = Math.abs(time_tp1 - time_tp0);
    }

}
