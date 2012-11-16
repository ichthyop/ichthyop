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
import org.previmer.ichthyop.calendar.InterannualCalendar;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;


public class WindDriftFileAction extends WindDriftAction {

    /**
     * Name of the Dimension in NetCDF file
     */
    static String strLon, strLat;
    /**
     * Number of time records in current NetCDF file
     */
    static int nbTimeRecords;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /*
     * Time between 2 wind files
     */
    static double dt_wind;
    /**
     * List on NetCDF input files in which wind dataset is read.
     */
    ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    int indexFile;

    /**
     * wind NetcdfFile
     */
    static NetcdfFile ncIn;
    
   
    /**
     * Grid dimension
     */
    int nx, ny;
    /**
     * Current rank in NetCDF dataset
     */
    static int rank;
    /**
     * Current time in NetCDF dataset
     */
    static long time_current;
    /**
     * latitude and longitude arrays 
     */
    static double[][] lonRho, latRho;
    /**
     * Zonal component of the wind velocity field at current time
     */
    static Array uw_tp0;
    /**
     * Zonal component of the wind velocity field at time t + dt
     */
    static Array uw_tp1;
    /**
     * Meridional component of the wind velocity field at current time
     */
    static Array vw_tp0;
    /**
     * Meridional component of the wind velocity field at time t + dt
     */
    static Array vw_tp1;
    /**
     * U wind variable 
     */
    RequiredExternalVariable U_variable;
    /**
     * V wind variable 
     */
    RequiredExternalVariable V_variable;
    
    @Override
    public void loadParameters() throws Exception {
        System.out.println("*****************LOAD PARAMMMMMM WIND FILE ***********");
        strTime = getParameter("field_time");
        time_current=getSimulationManager().getTimeManager().getTime();
        openLocation(getParameter("input_path"));

        wind_factor=Double.valueOf(getParameter("wind_factor"));
        depth_application = Float.valueOf(getParameter("depth_application"));
        angle = Math.PI/2.0-Double.valueOf(getParameter("angle"))*Math.PI/180.0;
        strUW=getParameter("wind_u");
        strVW=getParameter("wind_v");
        strLon=getParameter("longitude");
        strLat=getParameter("latitude");
        getDimNC();
        setOnFirstTime();
        setAllFieldsTp1AtTime(rank);
        readLonLat();

        U_variable=new RequiredExternalVariable(latRho,lonRho,uw_tp0,uw_tp1,getSimulationManager().getDataset());
        V_variable=new RequiredExternalVariable(latRho,lonRho,vw_tp0,vw_tp1,getSimulationManager().getDataset());
        
    }
    
    public Array readVariable(String name) throws Exception {
        try {
        Variable variable = ncIn.findVariable(name);
        int[] origin = null, shape = null;
        switch (variable.getShape().length){
            case 4:
                origin = new int[]{rank, 0, 0, 0};
                shape = new int[]{1, 1, ny, nx};
                break;
            case 3:
                origin = new int[]{rank, 0, 0};
                shape = new int[]{1, ny, nx};
                break;
        }
        return variable.read(origin, shape).reduce();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading UW wind velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }   

    
    private void openLocation(String rawPath) throws IOException {

        String path = IOTools.resolvePath(rawPath);

        if (isDirectory(path)) {
            listInputFiles = getInputList(path);
            open(listInputFiles.get(0));
        }
    }
    
    
    private boolean isDirectory(String location) throws IOException {

        File f = new File(location);
        if (!f.isDirectory()) {
            throw new IOException(location + " is not a valid directory.");
        }
        return f.isDirectory();
    }
    
    private ArrayList<String> getInputList(String path) throws IOException {

        ArrayList<String> list = null;

        File inputPath = new File(path);
        String fileMask = getParameter("file_filter");
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList<String>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            boolean skipSorting = false;
            try {
                skipSorting = Boolean.valueOf(getParameter("skip_sorting"));
            } catch (Exception ex) {
                skipSorting = false;
            }
            if (skipSorting) {
                Collections.sort(list);
            } else {
                Collections.sort(list, new NCComparator(strTime));
            }
        }
        return list;
    }
    
    private void open(String filename) throws IOException {
        if (ncIn == null || (new File(ncIn.getLocation()).compareTo(new File(filename)) != 0)) {
            if (ncIn != null) {
                ncIn.close();
            }
            try {
                ncIn = NetcdfDataset.openDataset(filename);
            } catch (Exception ex) {
                IOException ioex = new IOException("Error opening dataset " + filename + " ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
            try {
                System.out.println("*** time : " + strTime);
                nbTimeRecords = ncIn.findDimension(strTime).getLength();
            } catch (Exception ex) {
                IOException ioex = new IOException("Error dataset time dimension ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }
        getLogger().log(Level.INFO, "Opened wind dataset {0}", filename);
    }
    
    void getDimNC() throws IOException {

        try {
            nx = ncIn.findDimension(strLon).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset longitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            ny = ncIn.findDimension(strLat).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset latitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }
    
    private void nextStepTriggered() throws Exception {
        long time = getSimulationManager().getTimeManager().getTime();
        time_current=time;

        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }
        
        uw_tp0 = uw_tp1;
        vw_tp0 = vw_tp1;
        
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncIn.close();
            open(getNextFile(time_arrow));
            nbTimeRecords = ncIn.findDimension(strTime).getLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);

    }
    
    private void setOnFirstTime() throws Exception {

        long t0 = getSimulationManager().getTimeManager().get_tO();
        open(getFile(t0));
        readTimeLength();
        rank = findCurrentRank(t0);
        time_tp1 = t0;
    }
    
    void readTimeLength() throws IOException {
        try {
            nbTimeRecords = ncIn.findDimension(strTime).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read wind dataset time dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }
    
    private String getFile(long time) throws Exception {

        int indexLast = listInputFiles.size() - 1;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());

        for (int i = 0; i < indexLast; i++) {
            if (isTimeIntoFile(time, i)) {
                indexFile = i;
                return listInputFiles.get(i);
            } else if (isTimeBetweenFile(time, i)) {
                indexFile = i - (time_arrow - 1) / 2;
                return listInputFiles.get(indexFile);
            }
        }

        if (isTimeIntoFile(time, indexLast)) {
            indexFile = indexLast;
            return listInputFiles.get(indexLast);
        }
        StringBuffer msg = new StringBuffer();
        msg.append("{Wind dataset} Time value ");
        msg.append(getSimulationManager().getTimeManager().timeToString());
        msg.append(" (");
        msg.append(time);
        msg.append(" seconds) not contained among NetCDF files.");
        msg.append(" Ckeck if time units and origin of time (hidden parameters) are the good ones. They are usually defined as attributes of the time variable in wind dataset.");
        throw new IndexOutOfBoundsException(msg.toString());
    }
    
    private boolean isTimeIntoFile(long time, int index) throws Exception {

        String filename = "";
        NetcdfFile nc;
        Array timeArr;
        long time_r0, time_rf;

        filename = listInputFiles.get(index);
        System.out.println("....... " + filename);
        nc = NetcdfDataset.openDataset(filename);
        timeArr = nc.findVariable(strTime).read();
        time_r0 = DatasetUtil.skipSeconds((long) conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(0))));
        time_rf = DatasetUtil.skipSeconds((long) conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(
                timeArr.getShape()[0] - 1))));
        System.out.println("temps en jours :" + timeArr.getFloat(timeArr.getIndex().set(0)));
        System.out.println("time dep et time fin : " + time_r0 + " " + time_rf);
        nc.close();
        timeArr = null;
        nc = null;

        return (time >= time_r0 && time < time_rf);
    }

    private boolean isTimeBetweenFile(long time, int index) throws Exception {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        long[] time_nc = new long[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listInputFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = DatasetUtil.skipSeconds((long) conversion2seconds(
                        timeArr.getDouble(timeArr.getIndex().set(0))));
                timeArr = null;
                nc.close();
                nc = null;
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
        } catch (NullPointerException e) {
            throw new IOException("{Wind dataset} Unable to read " + strTime
                    + " variable in file " + filename + " : " + e.getCause());
        }
        return false;
    }
    
    double conversion2seconds(double time) throws Exception{
        String units = getParameter("time_unit");
        String time_origin = getParameter("time_origin");
        double origin=0;
        try {
            origin = getSimulationManager().getTimeManager().date2seconds(time_origin);
            System.out.println("origin : " + origin);
        } catch (ParseException ex) {
            IOException pex = new IOException("Error converting initial time of wind dataset into seconds ==> " + ex.toString());
            pex.setStackTrace(ex.getStackTrace());
            throw pex;
        }
        if(origin==0){
            Calendar calendartmp;
            SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
            calendartmp = new InterannualCalendar(time_origin, INPUT_DATE_FORMAT);
            INPUT_DATE_FORMAT.setCalendar(calendartmp);
            String origin_hydro=getSimulationManager().getParameterManager().getParameter("app.time", "time_origin");
            calendartmp.setTime(INPUT_DATE_FORMAT.parse(origin_hydro));
            origin= - calendartmp.getTimeInMillis() / 1000L;      
        }
        switch(units) {
            case "seconds":
                return time + origin;
            case "minutes" : 
                return time*60.0 + origin;
            case "hours" : 
                return time*3600.0 + origin;
            case "days" :
                return time*3600L*24L + origin;
            default :
                throw new UnsupportedOperationException("{Wind Dataset} Unknown time unit");
        }
    }
    
    void readLonLat() throws IOException {
        Array arrLon = null, arrLat = null;
        try {
            arrLon = ncIn.findVariable(strLon).read();

        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wind dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncIn.findVariable(strLat).read();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wind dataset latitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        Index indexLon = arrLon.getIndex();
        Index indexLat = arrLat.getIndex();
        for (int j = 0; j < ny; j++) {
            indexLat.set(j);
            for (int i = 0; i < nx; i++) {
                latRho[j][i] = arrLat.getDouble(indexLat);
                indexLon.set(i);
                lonRho[j][i] = arrLon.getDouble(indexLon);
            }
        }
    }
    
    int findCurrentRank(long time) throws Exception {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long time_rank;
        Array timeArr = null;
        try {
            timeArr = ncIn.findVariable(strTime).read();
            time_rank = DatasetUtil.skipSeconds((long) conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(lrank))));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = DatasetUtil.skipSeconds((long) conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(lrank))));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
        }
        lrank = lrank - (time_arrow + 1) / 2;

        return lrank;
    }
    
    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("{Wind dataset} Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }
    
    private void setAllFieldsTp1AtTime(int i_time) throws Exception {

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
    
    @Override
    public void execute(IBasicParticle particle) {
        if(time_current!=getSimulationManager().getTimeManager().getTime()){
            try {
                nextStepTriggered();
            } catch (Exception ex) {
                Logger.getLogger(WindDriftAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        U_variable.nextStep(uw_tp1, time_tp1, dt_wind);
        V_variable.nextStep(vw_tp1, time_tp1, dt_wind);
       

        double[] mvt = getDLonLat(particle.getGridCoordinates(),-particle.getDepth(),getSimulationManager().getTimeManager().getTime(),getSimulationManager().getTimeManager().get_dt());
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] windincr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(windincr);
        
    }
   
   public double[] getDLonLat(double[] pgrid, double depth, double time, double dt){
        double[] dWi = new double[2];
        if (depth > depth_application) {
            dWi[0]=0;
            dWi[1]=0;
            return dWi;
        }
        double dx,dy;
        double[] latlon = getSimulationManager().getDataset().xy2latlon(pgrid[0], pgrid[1]);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        dx = dt*U_variable.getVariable(pgrid, time) /one_deg_lon_meter;
        dy = dt*V_variable.getVariable(pgrid, time) / ONE_DEG_LATITUDE_IN_METER;
        dWi[0] = wind_factor*(dx*Math.cos(angle)-dy*Math.sin(angle));
        dWi[1] = wind_factor*(dx*Math.sin(angle)+dy*Math.cos(angle));
        return dWi;
    } 
    
}
