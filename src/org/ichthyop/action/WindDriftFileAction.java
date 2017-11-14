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

package org.ichthyop.action;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ichthyop.calendar.InterannualCalendar;
import org.ichthyop.dataset.RequiredExternalVariable;
import org.ichthyop.util.IOTools;
import org.ichthyop.particle.IParticle;
import org.ichthyop.util.MetaFilenameFilter;
import org.ichthyop.util.NCComparator;
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
    static double time_current;
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
    public String getKey() {
        return "action.wind_drift_file";
    }

    @Override
    public void loadParameters() throws Exception {
        strTime = getConfiguration().getString("action.wind_drift_file.field_time");
        time_current = getSimulationManager().getTimeManager().getTime();
        openLocation(getConfiguration().getString("action.wind_drift_file.input_path"));

        wind_factor = getConfiguration().getDouble("action.wind_drift_file.wind_factor");
        depth_application = getConfiguration().getFloat("action.wind_drift_file.depth_application");
        //angle = Math.PI / 2.0 - getConfiguration().getDouble("angle")) * Math.PI / 180.0;
        angle = getConfiguration().getDouble("action.wind_drift_file.angle") * Math.PI / 180.0;
        strUW = getConfiguration().getString("action.wind_drift_file.wind_u");
        strVW = getConfiguration().getString("action.wind_drift_file.wind_v");
        strLon = getConfiguration().getString("action.wind_drift_file.longitude");
        strLat = getConfiguration().getString("action.wind_drift_file.latitude");
        convention = getConfiguration().getString("action.wind_drift_file.wind_convention").equals("wind to") ? 1 : -1;

        getDimNC();
        setOnFirstTime();
        setAllFieldsTp1AtTime(rank);
        readLonLat();

        U_variable = new RequiredExternalVariable(latRho, lonRho, uw_tp0, uw_tp1, getSimulationManager().getDataset().getGrid());
        V_variable = new RequiredExternalVariable(latRho, lonRho, vw_tp0, vw_tp1, getSimulationManager().getDataset().getGrid());

    }

    public Array readVariable(String name) throws Exception {
        try {
            Variable variable = ncIn.findVariable(name);
            int[] origin = null, shape = null;
            switch (variable.getShape().length) {
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

        if (IOTools.isDirectory(path)) {
            listInputFiles = getInputList(path);
            open(listInputFiles.get(0));
        }
    }

    private ArrayList<String> getInputList(String path) throws IOException {

        ArrayList<String> list;

        File inputPath = new File(path);
        String fileMask = getConfiguration().getString("file_filter");
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList<>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            boolean skipSorting;
            try {
                skipSorting = Boolean.valueOf(getConfiguration().getString("skip_sorting"));
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
                nbTimeRecords = ncIn.findDimension(strTime).getLength();
            } catch (Exception ex) {
                IOException ioex = new IOException("Error dataset time dimension ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }
        info("Opened wind dataset {0}", filename);
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

    private String getFile(double time) throws Exception {

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
        StringBuilder msg = new StringBuilder();
        msg.append("{Wind dataset} Time value ");
        msg.append(getSimulationManager().getTimeManager().timeToString());
        msg.append(" (");
        msg.append(time);
        msg.append(" seconds) not contained among NetCDF files.");
        msg.append(" Ckeck if time units and origin of time (hidden parameters) are the good ones. They are usually defined as attributes of the time variable in wind dataset.");
        throw new IndexOutOfBoundsException(msg.toString());
    }

    private boolean isTimeIntoFile(double time, int index) throws Exception {

        String filename = listInputFiles.get(index);
        NetcdfFile nc = NetcdfDataset.openDataset(filename);
        Array timeArr = nc.findVariable(strTime).read();
        double time_r0 = skipSeconds(conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(0))));
        double time_rf = skipSeconds(conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(
                timeArr.getShape()[0] - 1))));
        nc.close();

        return (time >= time_r0 && time < time_rf);
    }

    private boolean isTimeBetweenFile(double time, int index) throws Exception {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        double[] time_nc = new double[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listInputFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = skipSeconds(conversion2seconds(
                        timeArr.getDouble(timeArr.getIndex().set(0))));
                nc.close();
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

    double conversion2seconds(double time) throws Exception {
        String units = getConfiguration().getString("time_unit");
        String time_origin = getConfiguration().getString("time_origin");
        double origin = 0;
        try {
            origin = getSimulationManager().getTimeManager().date2seconds(time_origin);
            //System.out.println("origin : " + origin);
        } catch (ParseException ex) {
            IOException pex = new IOException("Error converting initial time of wind dataset into seconds ==> " + ex.toString());
            pex.setStackTrace(ex.getStackTrace());
            throw pex;
        }
        if (origin == 0) {
            Calendar calendartmp;
            SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
            Calendar calendar_o = Calendar.getInstance();
            calendar_o.setTime(INPUT_DATE_FORMAT.parse(time_origin));
            int year_o = calendar_o.get(Calendar.YEAR);
            int month_o = calendar_o.get(Calendar.MONTH);
            int day_o = calendar_o.get(Calendar.DAY_OF_MONTH);
            int hour_o = calendar_o.get(Calendar.HOUR_OF_DAY);
            int min_o = calendar_o.get(Calendar.MINUTE);
            calendartmp = new InterannualCalendar(year_o, month_o, day_o, hour_o, min_o);
            INPUT_DATE_FORMAT.setCalendar(calendartmp);
            String origin_hydro = getConfiguration().getString("app.time.time_origin");
            calendartmp.setTime(INPUT_DATE_FORMAT.parse(origin_hydro));
            origin = -calendartmp.getTimeInMillis() / 1000L;
        }
        switch (units) {
            case "seconds":
                return time + origin;
            case "minutes":
                return time * 60.0 + origin;
            case "hours":
                return time * 3600.0 + origin;
            case "days":
                return time * 3600L * 24L + origin;
            default:
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

    int findCurrentRank(double time) throws Exception {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        double time_rank;
        try {
            Array timeArr = ncIn.findVariable(strTime).read();
            time_rank = skipSeconds(conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(lrank))));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = skipSeconds(conversion2seconds(timeArr.getDouble(timeArr.getIndex().set(lrank))));
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

    @Override
    public void execute(IParticle particle) {
        if (time_current != getSimulationManager().getTimeManager().getTime()) {
            try {
                nextStepTriggered();
            } catch (Exception ex) {
                Logger.getLogger(WindDriftAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        U_variable.nextStep(uw_tp1, time_tp1, dt_wind);
        V_variable.nextStep(vw_tp1, time_tp1, dt_wind);

        double[] mvt = getDLonLat(particle.getGridCoordinates(), -particle.getDepth(), getSimulationManager().getTimeManager().getTime(), getSimulationManager().getTimeManager().get_dt());
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().getGrid().latlon2xy(newLat, newLon);
        double[] windincr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(windincr);

    }

    public double[] getDLonLat(double[] pgrid, double depth, double time, double dt) {
        double[] dWi = new double[2];
        if (getSimulationManager().getDataset().getGrid().is3D()) {
            if (depth > depth_application) {
                dWi[0] = 0;
                dWi[1] = 0;
                return dWi;
            }
        }
        double dx, dy;
        double[] latlon = getSimulationManager().getDataset().getGrid().xy2latlon(pgrid[0], pgrid[1]);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        dx = dt * U_variable.getVariable(pgrid, time) / one_deg_lon_meter;
        dy = dt * V_variable.getVariable(pgrid, time) / ONE_DEG_LATITUDE_IN_METER;
        dWi[0] = convention * wind_factor * (dx * Math.cos(angle) - dy * Math.sin(angle));
        dWi[1] = convention * wind_factor * (dx * Math.sin(angle) + dy * Math.cos(angle));
        return dWi;
    }

}
