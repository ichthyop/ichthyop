/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.TimeManager;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 *
 * @author gwendo
 */
public class WaveDriftFileAction extends AbstractAction {

    private double wave_factor;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strUW, strVW, strTime;
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
     * Time between 2 wave files
     */
    static double dt_wave;
    /**
     * List on NetCDF input files in which wave dataset is read.
     */
    ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    int indexFile;

    /**
     * wave NetcdfFile
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
    static double time_current;
    /**
     * latitude and longitude arrays
     */
    static double[][] lonRho, latRho;
    /**
     * Wave variables to compute depth effect
     */
    private String str_wave_period, str_wave_speed_u, str_wave_speed_v;
    /**
     * Zonal component of the stokes drift field at current time
     */
    static Array uw_tp0;
    /**
     * Zonal component of the stokes drift field at time t + dt
     */
    static Array uw_tp1;
    /**
     * Meridional component of the stokes drift field at current time
     */
    static Array vw_tp0;
    /**
     * Meridional component of the stokes drift field at time t + dt
     */
    static Array vw_tp1;
    /**
     * U stokes drift variable
     */
    RequiredExternalVariable U_variable;
    /**
     * V stokes drift variable
     */
    RequiredExternalVariable V_variable;
    /**
     * Wave period
     */
    RequiredExternalVariable wave_period;
    /**
     * Wave speed
     */
    RequiredExternalVariable wave_speed_u, wave_speed_v;
    /**
     * Zonal component of the wave speed field at current time
     */
    static Array wave_speed_u_tp0;
    /**
     * Zonal component of the wave speed field at time t + dt
     */
    static Array wave_speed_u_tp1;
    /**
     * Meridional component of the wave speed field at current time
     */
    static Array wave_speed_v_tp0;
    /**
     * Meridional component of the wave speed field at time t + dt
     */
    static Array wave_speed_v_tp1;
    /**
     * Wave period field at current time
     */
    static Array wave_period_tp0;
    /**
     * Wave period field at time t + dt
     */
    static Array wave_period_tp1;

    public void loadParameters() throws Exception {

        strTime = getParameter("field_time");
        time_current = getSimulationManager().getTimeManager().getTime();
        openLocation(getParameter("input_path"));

        wave_factor = Double.valueOf(getParameter("wave_factor"));
        str_wave_period = getParameter("wave_period");
        str_wave_speed_u = getParameter("wave_u");
        str_wave_speed_v = getParameter("wave_v");
        strUW = getParameter("stokes_u");
        strVW = getParameter("stokes_v");
        strLon = getParameter("longitude");
        strLat = getParameter("latitude");
        getDimNC();
        setOnFirstTime();
        setAllFieldsTp1AtTime(rank);
        readLonLat();

        U_variable = new RequiredExternalVariable(latRho, lonRho, uw_tp0, uw_tp1, getSimulationManager().getDataset());
        V_variable = new RequiredExternalVariable(latRho, lonRho, vw_tp0, vw_tp1, getSimulationManager().getDataset());
        wave_period = new RequiredExternalVariable(latRho, lonRho, wave_period_tp0, wave_period_tp1,
                getSimulationManager().getDataset());
        wave_speed_u = new RequiredExternalVariable(latRho, lonRho, wave_speed_u_tp0, wave_speed_u_tp1,
                getSimulationManager().getDataset());
        wave_speed_v = new RequiredExternalVariable(latRho, lonRho, wave_speed_v_tp0, wave_speed_v_tp1,
                getSimulationManager().getDataset());

    }

    public Array readVariable(String name) throws Exception {
        try {
            Variable variable = ncIn.findVariable(name);
            int[] origin = null, shape = null;
            switch (variable.getShape().length) {
                case 4:
                    origin = new int[] { rank, 0, 0, 0 };
                    shape = new int[] { 1, 1, ny, nx };
                    break;
                case 3:
                    origin = new int[] { rank, 0, 0 };
                    shape = new int[] { 1, ny, nx };
                    break;
            }
            return variable.read(origin, shape).reduce();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading UW wave velocity variable. " + ex.toString());
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

                ncIn = NetcdfDatasets.openDataset(filename);
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
        getLogger().log(Level.INFO, "Opened wave dataset {0}", filename);
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
        double time = getSimulationManager().getTimeManager().getTime();
        time_current = time;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        uw_tp0 = uw_tp1;
        vw_tp0 = vw_tp1;
        wave_period_tp0 = wave_period_tp1;
        wave_speed_u_tp0 = wave_speed_u_tp1;
        wave_speed_v_tp0 = wave_speed_v_tp1;

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

        double t0 = getSimulationManager().getTimeManager().get_tO();
        int fileRank = DatasetUtil.index(listInputFiles, t0, timeArrow(), strTime);
        
        open(getFile(fileRank));
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }

    void readTimeLength() throws IOException {
        try {
            nbTimeRecords = ncIn.findDimension(strTime).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read wave dataset time dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private String getFile(int fileRank) throws Exception {
        return listInputFiles.get(fileRank);
    }

    void readLonLat() throws IOException {
        Array arrLon = null, arrLat = null;
        try {
            arrLon = ncIn.findVariable(strLon).read();

        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wave dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncIn.findVariable(strLat).read();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wave dataset latitude. " + ex.toString());
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

    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("{Wave dataset} Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    private void setAllFieldsTp1AtTime(int i_time) throws Exception {

        double time_tp0 = time_tp1;

        uw_tp1 = readVariable(strUW);
        vw_tp1 = readVariable(strVW);
        wave_period_tp1 = readVariable(str_wave_period);
        wave_speed_u_tp1 = readVariable(str_wave_speed_u);
        wave_speed_v_tp1 = readVariable(str_wave_speed_v);

        try {
            time_tp1 = DatasetUtil.getDate(ncIn.getLocation(), strTime, rank);
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_wave = Math.abs(time_tp1 - time_tp0);

    }

    public void execute(IParticle particle) {
        if (time_current != getSimulationManager().getTimeManager().getTime()) {
            try {
                nextStepTriggered();
            } catch (Exception ex) {
                Logger.getLogger(WaveDriftFileAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        U_variable.nextStep(uw_tp1, time_tp1, dt_wave);
        V_variable.nextStep(vw_tp1, time_tp1, dt_wave);
        wave_period.nextStep(wave_period_tp1, time_tp1, dt_wave);
        wave_speed_u.nextStep(wave_speed_u_tp1, time_tp1, dt_wave);
        wave_speed_v.nextStep(wave_speed_v_tp1, time_tp1, dt_wave);

        double[] mvt = getDLonLat(particle.getGridCoordinates(), particle.getDepth(),
                getSimulationManager().getTimeManager().getTime(), getSimulationManager().getTimeManager().get_dt());
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] waveincr = new double[] { newPos[0] - particle.getX(), newPos[1] - particle.getY() };
        particle.increment(waveincr);

    }

    public double[] getDLonLat(double[] pgrid, double depth, double time, double dt) {
        double[] dWi = new double[2];
        double dx, dy;
        double[] latlon = getSimulationManager().getDataset().xy2latlon(pgrid[0], pgrid[1]);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        double wave_speed = Math.pow(
                Math.pow(wave_speed_u.getVariable(pgrid, time), 2) + Math.pow(wave_speed_v.getVariable(pgrid, time), 2),
                0.5);
        double wave_length = wave_speed * wave_period.getVariable(pgrid, time);
        double wave_number = 2 * Math.PI / wave_length;
        dx = dt * U_variable.getVariable(pgrid, time) / one_deg_lon_meter;
        dy = dt * V_variable.getVariable(pgrid, time) / ONE_DEG_LATITUDE_IN_METER;
        dWi[0] = wave_factor * dx * Math.exp(2 * wave_number * depth);
        dWi[1] = wave_factor * dy * Math.exp(2 * wave_number * depth);

        /*
         * double tmp; for(double i=0;i<10;i++){
         * System.out.println(" wave number length speed : " + wave_number + " " +
         * wave_length + " " + wave_speed); tmp = Math.exp(-2.0*wave_number*i);
         * System.out.println(" prof atténuation : " + i + " " + tmp ); }
         */
        return dWi;
    }

    @Override
    public void init(IParticle particle) {
    }

    double skipSeconds(double time) {
        return 100.d * Math.floor(time / 100.d);
    }
    
    int timeArrow() {
        return getSimulationManager().getParameterManager().getParameter("app.time", "time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 :-1;
    }
    
}
