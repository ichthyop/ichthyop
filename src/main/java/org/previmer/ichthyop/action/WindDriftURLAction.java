/* <p>Copyright: Copyright (c) 2007-2011. Free software under GNU GPL</p>
 * 
 * @author G.Andres
 */

package org.previmer.ichthyop.action;

import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

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
            ncIn = NetcdfDataset.openDataset(opendapURL);
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
        rank = findCurrentRank(t0);
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
        double time0 = skipSeconds(conversion2seconds(timeArr.getLong(timeArr.getIndex().set(0))));
        double timeN = skipSeconds(conversion2seconds(timeArr.getLong(timeArr.getIndex().set(ntime - 1))));
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

        try {
            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = conversion2seconds(xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank)));
            time_tp1 -= time_tp1 % 100;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        
        dt_wind = Math.abs(time_tp1 - time_tp0);
    }
 
}
