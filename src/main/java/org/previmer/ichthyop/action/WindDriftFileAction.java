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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.previmer.ichthyop.dataset.DatasetCenteringBasin;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
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
    
    /** Start index for shrinking */
    int ipo = 0;
    int jpo = 0;
    
    /**
     * U wind variable
     */
    RequiredExternalVariable U_variable;
    /**
     * V wind variable
     */
    RequiredExternalVariable V_variable;
    
    DatasetCenteringBasin basin = null; 

    @Override
    public void loadParameters() throws Exception {
        strTime = getParameter("field_time");
        time_current = getSimulationManager().getTimeManager().getTime();
        openLocation(getParameter("input_path"));

        wind_factor = Double.valueOf(getParameter("wind_factor"));
        depth_application = Float.valueOf(getParameter("depth_application"));
        //angle = Math.PI / 2.0 - Double.valueOf(getParameter("angle")) * Math.PI / 180.0;
        angle = Double.valueOf(getParameter("angle")) * Math.PI / 180.0;
        strUW = getParameter("wind_u");
        strVW = getParameter("wind_v");
        strLon = getParameter("longitude");
        strLat = getParameter("latitude");
        convention = getParameter("wind_convention").equals("wind to") ? 1 : -1;
        
        if(!this.isNull("basin_center")) {
            String basinCenter = getParameter("basin_center").toLowerCase();
            if(basinCenter == "pacific") {
                basin = DatasetCenteringBasin.PACIFIC;
            } else {
                basin =  DatasetCenteringBasin.ATLANTIC;
            }
        }

        getDimNC();
        
        this.shrinkGrid();
        
        setOnFirstTime();
        //setAllFieldsTp1AtTime();
        this.setAllFieldsTpAtInit();
        readLonLat();

        U_variable = new RequiredExternalVariable(latRho, lonRho, uw_tp0, uw_tp1, getSimulationManager().getDataset(), basin);
        V_variable = new RequiredExternalVariable(latRho, lonRho, vw_tp0, vw_tp1, getSimulationManager().getDataset(), basin);

    }

    public Array readVariable(String name) throws Exception {
        try {
            Variable variable = ncIn.findVariable(name);
            int[] origin = null, shape = null;
            switch (variable.getShape().length) {
                case 4:
                    origin = new int[]{rank, 0, jpo, ipo};
                    shape = new int[]{1, 1, ny, nx};
                    break;
                case 3:
                    origin = new int[]{rank, jpo, ipo};
                    shape = new int[]{1, ny, nx};
                    break;
            }
            
            getLogger().log(Level.INFO, "Read wind variable {0} at time index {1}", new Object[] {name, rank});
            
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
        String fileMask = getParameter("file_filter");
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
    
    private void setAllFieldsTpAtInit() throws Exception {
        
        // read the fields on the left, i.e. the values that are valid for the first step
        double time_tp0 = DatasetUtil.getDate(ncIn.getLocation(), strTime, rank);
        uw_tp0 = readVariable(strUW);
        vw_tp0 = readVariable(strVW);
        
        // Now increments the value for the rank value and eventually update the file to read.
        int time_arrow = timeArrow();
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncIn.close();
            open(getNextFile(time_arrow));
            nbTimeRecords = ncIn.findDimension(strTime).getLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        
        // computation of the right fields
        uw_tp1 = readVariable(strUW);
        vw_tp1 = readVariable(strVW);
        time_tp1 = DatasetUtil.getDate(ncIn.getLocation(), strTime, rank);
        
        dt_wind = Math.abs(time_tp1 - time_tp0);

    }

    private void nextStepTriggered() throws Exception {
        double time = getSimulationManager().getTimeManager().getTime();
        time_current = time;

        int time_arrow = timeArrow();

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
        setAllFieldsTp1AtTime();

    }

    private void setOnFirstTime() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        this.indexFile = DatasetUtil.index(listInputFiles, t0, timeArrow(), strTime);
        open(getFile(this.indexFile));
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
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

    private String getFile(int fileRank) throws Exception {
        return listInputFiles.get(fileRank);
    }

    

    void readLonLat() throws IOException {
        
        int[] originLon = new int[] {ipo};
        int[] shapeLon = new int[] {nx};
        
              
        int[] originLat = new int[] {jpo};
        int[] shapeLat = new int[] {ny};
        
        Array arrLon = null, arrLat = null;
        try {
            arrLon = ncIn.findVariable(strLon).read(originLon, shapeLon);

        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wind dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncIn.findVariable(strLat).read(originLat, shapeLat);
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

    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("{Wind dataset} Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    private void setAllFieldsTp1AtTime() throws Exception {

        double time_tp0 = time_tp1;

        uw_tp1 = readVariable(strUW);
        vw_tp1 = readVariable(strVW);

        try {
            time_tp1 = DatasetUtil.getDate(ncIn.getLocation(), strTime, rank);
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
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] windincr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(windincr);

    }

    public double[] getDLonLat(double[] pgrid, double depth, double time, double dt) {
        double[] dWi = new double[2];
        if (getSimulationManager().getDataset().is3D()) {
            if (depth > depth_application) {
                dWi[0] = 0;
                dWi[1] = 0;
                return dWi;
            }
        }
        double dx, dy;
        // Recover the lon/lat in Dataset System (Pacific-center for Oscar).
        double[] latlon = getSimulationManager().getDataset().xy2latlon(pgrid[0], pgrid[1]);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        
        // Recovers the displacement for the particule, given the wind-speed.
        // Converts from "m" to delta in longitude/latitude.
        dx = dt * U_variable.getVariable(pgrid, time) / one_deg_lon_meter;
        dy = dt * V_variable.getVariable(pgrid, time) / ONE_DEG_LATITUDE_IN_METER;
        dWi[0] = convention * wind_factor * (dx * Math.cos(angle) - dy * Math.sin(angle));
        dWi[1] = convention * wind_factor * (dx * Math.sin(angle) + dy * Math.cos(angle));
        return dWi;
    }
    
    /**
     * Resizes the domain and determines the range of the grid indexes taht will
     * be used in the simulation. The new domain is limited by the Northwest and
     * the Southeast corners.
     *
     * @param pGeog1 a float[], the geodesic coordinates of the domain Northwest
     * corner
     * @param pGeog2 a float[], the geodesic coordinates of the domain Southeast
     * corner
     * @throws an IOException if the new domain is not strictly nested within
     * the NetCDF dataset domain.
     */
    private void range(double lat1, double lon1, double lat2, double lon2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        readLonLat();

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            throw new IOException("Impossible to proportion the simulation area : points out of domain");
        }
        lonRho = null;
        latRho = null;

        //System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " + (float)pGrid2[0] + " " + (float)pGrid2[1]);
        ipo = (int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0]));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        jpo = (int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1]));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        nx = Math.min(nx, ipn - ipo + 1);
        ny = Math.min(ny, jpn - jpo + 1);
        //System.out.println("ipo " + ipo + " nx " + nx + " jpo " + jpo + " ny " + ny);
    }
    
    private double[] latlon2xy(double lat1, double lon1) {

        // Init output
        double[] output = new double[2];
        
        // Init dimensions
        int nLat = lonRho.length;
        int nLon = lonRho[0].length;
        
        // Init distance as a maximum value
        double distOut = Double.MAX_VALUE;
        for (int j = 0; j < nLat; j++) {
            for (int i = 0; i < nLon; i++) {
                double tempDist = Math.pow(lonRho[j][i] - lon1, 2) + Math.pow(latRho[j][i] - lat1, 2);
                if (tempDist < distOut) {
                    output[0] = i;
                    output[1] = j;
                    distOut = tempDist;
                }
            }
        }
        
        return output;
        
    }
    
    public void shrinkGrid() {

        if (findParameter("shrink_domain") && Boolean.valueOf(getParameter("shrink_domain"))) {
            try {
                float lon1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lon"), LonLatFormat.DecimalDeg));
                float lat1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lat"), LonLatFormat.DecimalDeg));
                float lon2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lon"), LonLatFormat.DecimalDeg));
                float lat2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lat"), LonLatFormat.DecimalDeg));
                range(lat1, lon1, lat2, lon2);
            } catch (IOException | NumberFormatException ex) {
                getLogger().log(Level.WARNING, "Failed to resize domain. " + ex.toString(), ex);
            }
        }
    }
    
    public boolean findParameter(String key) {
        // Check whether the parameter can be retrieved
        try {
            getParameter(key);
        } catch (NullPointerException ex) {
            // Tue parameter does not exist
            return false;
        }
        // The parameter does exist
        return true;
    }

}
