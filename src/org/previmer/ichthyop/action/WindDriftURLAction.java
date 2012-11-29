/* <p>Copyright: Copyright (c) 2007-2011. Free software under GNU GPL</p>
 * 
 * @author G.Andres
 */

package org.previmer.ichthyop.action;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.calendar.InterannualCalendar;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
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
        convention = getParameter("wind_convention")=="wind to"? 1 : -1;
        
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
            getLogger().info("{Wind Dataset} Open remote " + opendapURL);
        } catch (Exception e) {
            IOException ioex = new IOException("{Wind Dataset} Problem opening " + opendapURL + " ==> " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
    }
    
    void setOnFirstTime() throws Exception {
        long t0 = getSimulationManager().getTimeManager().get_tO();
        readTimeLength();
        checkInitTime(ncIn, strTime);
        rank = findCurrentRank(t0);
        time_tp1 = t0;
    }

    public void checkInitTime(NetcdfFile nc, String strTime) throws IOException, IndexOutOfBoundsException, Exception{

        long time = getSimulationManager().getTimeManager().get_tO();
        Array timeArr = null;
        try {
            timeArr = nc.findVariable(strTime).read();
        } catch (Exception ex) {
            IOException ioex = new IOException("{Wind dataset} Failed to read time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        int ntime = timeArr.getShape()[0];
        long time0 = DatasetUtil.skipSeconds((long) conversion2seconds(timeArr.getLong(timeArr.getIndex().set(0))));
        long timeN = DatasetUtil.skipSeconds((long) conversion2seconds(timeArr.getLong(timeArr.getIndex().set(ntime - 1))));
        if (time < time0 || time > timeN) {
            StringBuffer msg = new StringBuffer();
            msg.append("{Wind dataset} Time value ");
            msg.append(getSimulationManager().getTimeManager().timeToString());
            msg.append(" (");
            msg.append(time);
            msg.append(" seconds) not contained in dataset " + nc.getLocation());
            throw new IndexOutOfBoundsException(msg.toString());
        }
    }
    
    public void nextStepTriggered() throws Exception {
        long time = getSimulationManager().getTimeManager().getTime();
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

        int[] origin = new int[]{i_time, 0, 0};
        double time_tp0 = time_tp1;


        
        uw_tp1 = readVariable(strUW);
        vw_tp1 = readVariable(strVW);

        try {
            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = conversion2seconds(xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank)));
            time_tp1 -= time_tp1 % 100;
            xTimeTp1 = null;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        
        dt_wind = Math.abs(time_tp1 - time_tp0);

    }
 
}
