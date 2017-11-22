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

package org.ichthyop.dataset;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.util.IOTools;
import org.ichthyop.ui.LonLatConverter;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class SymphonieDataset extends AbstractDataset {

    /**
     * Grid dimension
     */
    private int ni, nj, nk;
    /**
     * Origin for grid index
     */
    private int ipo, jpo;
    /**
     * Number of time records in current NetCDF file
     */
    private static int nbTimeRecords;
    /**
     * Longitude at rho point.
     */
    private double[][] longitude_t;
    /**
     * Latitude at rho point.
     */
    private double[][] latitude_t;
    /**
     * Bathymetry
     */
    private float[][] hm_w;
    /**
     * Mask: water = 1, cost = 0
     */
    private float[][] mask_t;
    /**
     * Cell x size at v location
     */
    private float[][] dx_v;
    /**
     * Cell y size at u location
     */
    private float[][] dy_u;
    /**
     * Cell are at T location
     */
    private float[][] dxdy_t;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    private double dt_HyMo;
    /**
     * List on NetCDF input files in which dataset is read.
     */
    private List<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;
    /**
     * Time t + dt expressed in seconds
     */
    private double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    private int rank;
    /**
     * Current NetcdfFile
     */
    private NetcdfFile ncIn;
    /**
     * Grid file
     */
    private NetcdfFile ncGrid;
    /**
     * Name of NetCDF dimensions
     */
    private String strDim_nj_t, strDim_ni_t, strDim_nk_t, strDim_time;
    /**
     * Name of NetCDF variables
     */
    private String strVar_longitude_t, strVar_latitude_t, strVar_mask_t,
            strVar_hm_w, strVar_dx_v, strVar_dy_u, strVar_time, strVar_ssh_ib,
            strVar_depth_t, strVar_depth_w, strVar_u, strVar_v, strVar_dxdy_t;
    /*
     * Sea surface height
     */
    private float[][] ssh_tp0, ssh_tp1;
    /**
     * Geographical boundary of the domain
     */
    private double latMin, lonMin, latMax, lonMax, depthMax;
    /**
     * Depth at T point.
     */
    private float[][][] depth_t;
    /**
     * Depth at W point at current time. Takes account of free surface
     * elevation.
     */
    private float[][][] depth_w_tp0;
    /**
     * Depth at W point at time t + dt Takes account of free surface elevation.
     */
    private float[][][] depth_w_tp1;
    /**
     * Depth at W point. The free surface elevation is disregarded.
     */
    private float[][][] depth_w;
    /**
     * Zonal component of the velocity field at current time
     */
    private float[][][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    private float[][][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    private float[][][] v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    private float[][][] v_tp1;
    /**
     * Vertical component of the velocity field at current time
     */
    private float[][][] w_tp0;
    /**
     * Vertical component of the velocity field at time t + dt
     */
    private float[][][] w_tp1;
    
    @Override
    String getKey() {
        return "dataset.symphonie";
    }

    @Override
    void loadParameters() {

        // Dimensions
        strDim_nj_t = getConfiguration().getString("dataset.symphonie.field_dim_nj");
        strDim_ni_t = getConfiguration().getString("dataset.symphonie.field_dim_ni");
        strDim_nk_t = getConfiguration().getString("dataset.symphonie.field_dim_nk");
        strDim_time = getConfiguration().getString("dataset.symphonie.field_dim_time");
        // Variables
        strVar_longitude_t = getConfiguration().getString("dataset.symphonie.field_var_lon");
        strVar_latitude_t = getConfiguration().getString("dataset.symphonie.field_var_lat");
        strVar_mask_t = getConfiguration().getString("dataset.symphonie.field_var_mask");
        strVar_hm_w = getConfiguration().getString("dataset.symphonie.field_var_bathy");
        strVar_dx_v = getConfiguration().getString("dataset.symphonie.field_var_dx");
        strVar_dy_u = getConfiguration().getString("dataset.symphonie.field_var_dy");
        strVar_dxdy_t = getConfiguration().getString("dataset.symphonie.field_var_dxdy");
        strVar_time = getConfiguration().getString("dataset.symphonie.field_var_time");
        strVar_ssh_ib = getConfiguration().getString("dataset.symphonie.field_var_ssh");
        strVar_depth_t = getConfiguration().getString("dataset.symphonie.field_var_depth_t");
        strVar_depth_w = getConfiguration().getString("dataset.symphonie.field_var_depth_w");
        strVar_u = getConfiguration().getString("dataset.symphonie.field_var_u");
        strVar_v = getConfiguration().getString("dataset.symphonie.field_var_v");
    }

    @Override
    public void setUp() throws Exception {
        loadParameters();
        clearRequiredVariables();
        listInputFiles = DatasetUtil.list(getConfiguration().getString("dataset.symphonie.input_path"),
                getConfiguration().getString("dataset.symphonie.file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(listInputFiles, strVar_time, timeArrow());
        }
        open(listInputFiles.get(0));
        openGridFile(getConfiguration().getString("dataset.symphonie.grid_file"));
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
    }

    @Override
    public void init() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        indexFile = DatasetUtil.index(listInputFiles, t0, timeArrow(), strDim_time);
        open(listInputFiles.get(indexFile));
        checkRequiredVariable(ncIn);
        setAllFieldsTp1AtTime(rank = DatasetUtil.index(listInputFiles, t0, timeArrow(), strDim_time));
        time_tp1 = t0;
    }
    
    public void shrinkGrid() {
        boolean isParamDefined;
        try {
            Boolean.valueOf(getConfiguration().getString("dataset.symphonie.shrink_domain"));
            isParamDefined = true;
        } catch (NullPointerException ex) {
            isParamDefined = false;
        }

        if (isParamDefined && Boolean.valueOf(getConfiguration().getString("dataset.symphonie.shrink_domain"))) {
            try {
                float lon1 = Float.valueOf(LonLatConverter.convert(getConfiguration().getString("dataset.symphonie.north-west-corner.lon"), LonLatConverter.LonLatFormat.DecimalDeg));
                float lat1 = Float.valueOf(LonLatConverter.convert(getConfiguration().getString("dataset.symphonie.north-west-corner.lat"), LonLatConverter.LonLatFormat.DecimalDeg));
                float lon2 = Float.valueOf(LonLatConverter.convert(getConfiguration().getString("dataset.symphonie.south-east-corner.lon"), LonLatConverter.LonLatFormat.DecimalDeg));
                float lat2 = Float.valueOf(LonLatConverter.convert(getConfiguration().getString("dataset.symphonie.south-east-corner.lat"), LonLatConverter.LonLatFormat.DecimalDeg));
                range(lat1, lon1, lat2, lon2);
            } catch (Exception ex) {
                warning("Failed to resize domain", ex);
            }
        }
    }
    
    /**
     * Resizes the domain and determines the range of the grid indexes
     * taht will be used in the simulation.
     * The new domain is limited by the Northwest and the Southeast corners.
     * @param pGeog1 a float[], the geodesic coordinates of the domain
     * Northwest corner
     * @param pGeog2  a float[], the geodesic coordinates of the domain
     * Southeast corner
     * @throws an IOException if the new domain is not strictly nested
     * within the NetCDF dataset domain.
     */
    private void range(double lat1, double lon1, double lat2, double lon2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        try {
            longitude_t = (double[][]) ncGrid.findVariable(strVar_longitude_t).read().copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset longitude_t. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            latitude_t = (double[][]) ncGrid.findVariable(strVar_latitude_t).read().copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset latitude_t. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            throw new IOException("Impossible to proportion the simulation area : points out of domain");
        }
        longitude_t = null;
        latitude_t = null;

        //System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " + (float)pGrid2[0] + " " + (float)pGrid2[1]);
        ipo = (int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0]));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        jpo = (int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1]));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        ni = Math.min(ni, ipn - ipo + 1);
        nj = Math.min(nj, jpn - jpo + 1);
        //System.out.println("ipo " + ipo + " nx " + nx + " jpo " + jpo + " ny " + ny);
    }

    void setAllFieldsTp1AtTime(int rank) throws Exception {

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][][]) ncIn.findVariable(strVar_u).read(origin, new int[]{1, nk, nj, (ni - 1)}).reduce().copyToNDJavaArray();

        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            v_tp1 = (float[][][]) ncIn.findVariable(strVar_v).read(origin,
                    new int[]{1, nk, (nj - 1), ni}).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
           time_tp1 = DatasetUtil.timeAtRank(ncIn, strVar_time, rank);
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }


        try {
            ssh_tp1 = (float[][]) ncIn.findVariable(strVar_ssh_ib).read(
                    new int[]{rank, jpo, ipo},
                    new int[]{1, nj, ni}).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset ssh-ib variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        depth_w_tp1 = computeSigLevels();
        w_tp1 = computeW();
    }

    float[][][] computeW() {



        double[][][] Huon = new double[nk][nj][ni];
        double[][][] Hvom = new double[nk][nj][ni];

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = 0; k < nk; k++) {
            for (int i = 1; i < ni; i++) {
                for (int j = 0; j < nj; j++) {
                    Huon[k][j][i] = .5d * ((depth_w_tp1[k + 1][j][i] - depth_w_tp1[k][j][i])
                            + (depth_w_tp1[k + 1][j][i - 1] - depth_w_tp1[k][j][i - 1]))
                            * dy_u[j][i - 1] * u_tp1[k][j][i - 1];
                    if (Double.isNaN(Huon[k][j][i])) {
                        Huon[k][j][i] = 0.d;

                    }
                }
            }
            for (int i = 0; i < ni; i++) {
                for (int j = 1; j < nj; j++) {
                    Hvom[k][j][i] = .5d * (((depth_w_tp1[k + 1][j][i] - depth_w_tp1[k][j][i])
                            + (depth_w_tp1[k + 1][j - 1][i] - depth_w_tp1[k][j - 1][i]))
                            * dx_v[j - 1][i]) * v_tp1[k][j - 1][i];
                    if (Double.isNaN(Hvom[k][j][i])) {
                        Hvom[k][j][i] = 0.d;

                    }
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[][][] w_double = new double[nk + 1][nj][ni];

        for (int j = 0; j < nj - 1; j++) {
            for (int i = 0; i < ni; i++) {
                w_double[0][j][i] = 0.f;
            }
            for (int k = 1; k < nk + 1; k++) {
                for (int i = 0; i < ni - 1; i++) {
                    w_double[k][j][i] = w_double[k - 1][j][i]
                            + (Huon[k - 1][j][i] - Huon[k - 1][j][i + 1]
                            + Hvom[k - 1][j][i] - Hvom[k - 1][j + 1][i]);
                }
            }
           
            for (int k = 1; k < nk; k++) {
                for (int i = 0; i < ni; i++) {
                    w_double[k][j][i] -= w_double[nk][j][i]
                            * (depth_w_tp1[k][j][i] - depth_w_tp1[0][j][i])
                            / (depth_w_tp1[nk][j][i] - depth_w_tp1[0][j][i]);
                }
            }

            for (int i = ni; i-- > 0;) {
                w_double[nk][j][i] = 0.f;
            }
        }

        //---------------------------------------------------
        // Boundary Conditions
        for (int k = 0; k < nk + 1; k++) {
            for (int j = 0; j < nj; j++) {
                w_double[k][j][0] = w_double[k][j][1];
                w_double[k][j][ni - 1] = w_double[k][j][ni - 2];
            }
        }
        for (int k = 0; k < nk + 1; k++) {
            for (int i = 0; i < ni; i++) {
                w_double[k][0][i] = w_double[k][1][i];
                w_double[k][nj - 1][i] = w_double[k][nj - 2][i];
            }
        }

        //---------------------------------------------------
        // w / (dx_v * dy_u)
        float[][][] w = new float[nk + 1][nj][ni];
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                for (int k = 0; k < nk + 1; k++) {
                    w[k][j][i] = isInWater(i, j)
                            ? (float) (w_double[k][j][i] / dxdy_t[j][i])
                            : 0.f;
                }
            }
        }

        //---------------------------------------------------
        // Return w
        return w;

    }

    private float[][][] computeSigLevels() {

        //-----------------------------------------------------
        // Daily recalculation of depth_w with ssh
        float[][][] depth_w_tmp = new float[nk + 1][nj][ni];

        for (int i = ni; i-- > 0;) {
            for (int j = nj; j-- > 0;) {
                if (ssh_tp1[j][i] == 999.f) {
                    ssh_tp1[j][i] = 0.f;
                }
                for (int k = 0; k < nk + 1; k++) {
                    depth_w_tmp[k][j][i] = (float) (Float.isNaN(ssh_tp1[j][i])
                            ? depth_w[k][j][i]
                            : depth_w[k][j][i] + ssh_tp1[j][i] * (1.f + depth_w[k][j][i] / hm_w[j][i]));
                }
            }
        }

        return depth_w_tmp;
    }

    /**
     * Determines the geographical boundaries of the domain in longitude,
     * latitude and depth.
     */
    private void getDimGeogArea() {

        //--------------------------------------
        // Calculate the Physical Space extrema

        lonMin = Double.MAX_VALUE;
        lonMax = -lonMin;
        latMin = Double.MAX_VALUE;
        latMax = -latMin;
        depthMax = 0.d;
        int i = ni;
        int j;

        while (i-- > 0) {
            j = nj;
            while (j-- > 0) {
                if (longitude_t[j][i] >= lonMax) {
                    lonMax = longitude_t[j][i];
                }
                if (longitude_t[j][i] <= lonMin) {
                    lonMin = longitude_t[j][i];
                }
                if (latitude_t[j][i] >= latMax) {
                    latMax = latitude_t[j][i];
                }
                if (latitude_t[j][i] <= latMin) {
                    latMin = latitude_t[j][i];
                }
                if (hm_w[j][i] >= depthMax) {
                    depthMax = hm_w[j][i];
                }
            }
        }
        //System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " + latMin + " latmax " + latMax);
        //System.out.println("depth max " + depthMax);

        double double_tmp;
        if (lonMin > lonMax) {
            double_tmp = lonMin;
            lonMin = lonMax;
            lonMax = double_tmp;
        }

        if (latMin > latMax) {
            double_tmp = latMin;
            latMin = latMax;
            latMax = double_tmp;
        }
    }

    void readConstantField() throws IOException {

        int[] origin2d = new int[]{jpo, ipo};
        int[] size2d = new int[]{nj, ni};

        try {
            longitude_t = (double[][]) ncGrid.findVariable(strVar_longitude_t).read(origin2d, size2d).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset longitude_t. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            latitude_t = (double[][]) ncGrid.findVariable(strVar_latitude_t).read(origin2d, size2d).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset latitude_t. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            mask_t = (float[][]) ncGrid.findVariable(strVar_mask_t).read(new int[]{nk - 1, jpo, ipo}, new int[]{1, nj, ni}).reduce().copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset mask_t. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            hm_w = (float[][]) ncGrid.findVariable(strVar_hm_w).read(origin2d, size2d).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset hm_w. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            dx_v = (float[][]) ncGrid.findVariable(strVar_dx_v).read(origin2d, new int[]{nj - 1, ni}).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset dx_v metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        try {
            dy_u = (float[][]) ncGrid.findVariable(strVar_dy_u).read(origin2d, new int[]{nj, ni - 1}).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset dy_u metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        try {
            dxdy_t = (float[][]) ncGrid.findVariable(strVar_dxdy_t).read(origin2d, size2d).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset dxdy_t metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        try {
            depth_t = (float[][][]) ncGrid.findVariable(strVar_depth_t).read(new int[]{0, jpo, ipo}, new int[]{nk, nj, ni}).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset depth_t variable. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        try {
            depth_w = (float[][][]) ncGrid.findVariable(strVar_depth_w).read(new int[]{0, jpo, ipo}, new int[]{nk + 1, nj, ni}).copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset depth_t variable. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        // Close grid file
        ncGrid.close();

        try {
            ssh_tp0 = (float[][]) ncIn.findVariable(strVar_ssh_ib).read(new int[]{0, jpo, ipo}, new int[]{1, nj, ni}).reduce().copyToNDJavaArray();
        } catch (Exception e) {
            IOException ioex = new IOException("Problem reading dataset ssh-ib variable. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        ssh_tp1 = ssh_tp0;
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws IOException {

        try {
            ni = ncIn.findDimension(strDim_ni_t).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset ni_t dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            nj = ncIn.findDimension(strDim_nj_t).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset nj_t dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ipo = jpo = 0;

        /* Vertical dimension */
        try {
            nk = ncIn.findDimension(strDim_nk_t).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset nk_t dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private void openGridFile(String rawFile) throws IOException {

        File filename = new File(IOTools.resolveFile(rawFile));
        if (!filename.exists()) {
            throw new IOException("Grid file " + filename + " does not exist");
        }
        ncGrid = NetcdfDataset.openDataset(filename.toString());
    }

    void open(String filename) throws IOException {
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
                nbTimeRecords = ncIn.findDimension(strDim_time).getLength();
            } catch (Exception ex) {
                IOException ioex = new IOException("Error dataset time dimension ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }
        info("Opened dataset {0}", filename);
    }

    public double[] latlon2xy(double lat, double lon) {
        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)

        boolean found;
        int imin, imax, jmin, jmax, i0, j0;
        double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

        xgrid = -1.;
        ygrid = -1.;
        found = isInsidePolygone(0, ni - 1, 0, nj - 1, lon, lat);

        //-------------------------------------------
        // Research surrounding grid-points
        if (found) {
            imin = 0;
            imax = ni - 1;
            jmin = 0;
            jmax = nj - 1;
            while (((imax - imin) > 1) | ((jmax - jmin) > 1)) {
                if ((imax - imin) > 1) {
                    i0 = (imin + imax) / 2;
                    found = isInsidePolygone(imin, i0, jmin, jmax, lon, lat);
                    if (found) {
                        imax = i0;
                    } else {
                        imin = i0;
                    }
                }
                if ((jmax - jmin) > 1) {
                    j0 = (jmax + jmin) / 2;
                    found = isInsidePolygone(imin, imax, jmin, j0, lon, lat);
                    if (found) {
                        jmax = j0;
                    } else {
                        jmin = j0;
                    }
                }
            }

            //--------------------------------------------
            // Trilinear interpolation
            dy1 = latitude_t[jmin + 1][imin] - latitude_t[jmin][imin];
            dx1 = longitude_t[jmin + 1][imin] - longitude_t[jmin][imin];
            dy2 = latitude_t[jmin][imin + 1] - latitude_t[jmin][imin];
            dx2 = longitude_t[jmin][imin + 1] - longitude_t[jmin][imin];

            c1 = lon * dy1 - lat * dx1;
            c2 = longitude_t[jmin][imin] * dy2 - latitude_t[jmin][imin] * dx2;
            deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
            deltax = (deltax - longitude_t[jmin][imin]) / dx2;
            xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

            c1 = longitude_t[jmin][imin] * dy1 - latitude_t[jmin][imin] * dx1;
            c2 = lon * dy2 - lat * dx2;
            deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
            deltay = (deltay - latitude_t[jmin][imin]) / dy1;
            ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
        }
        return (new double[]{xgrid, ygrid});
    }

    public double[] xy2latlon(double xRho, double yRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)

        final double ix = Math.max(0.00001f, Math.min(xRho, (double) ni - 1.00001f));
        final double jy = Math.max(0.00001f, Math.min(yRho, (double) nj - 1.00001f));

        final int i = (int) Math.floor(ix);
        final int j = (int) Math.floor(jy);
        double latitude = 0.d;
        double longitude = 0.d;
        final double dx = ix - (double) i;
        final double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                latitude += co * latitude_t[j + jj][i + ii];
                longitude += co * longitude_t[j + jj][i + ii];
            }
        }
        return (new double[]{latitude, longitude});
    }

    public double depth2z(double x, double y, double depth) {
        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double z;
        int lk = nk - 1;
        while ((lk > 0) && (getDepth(x, y, lk) > depth)) {
            lk--;
        }
        if (lk == (nk - 1)) {
            z = (double) lk;
        } else {
            double pr = getDepth(x, y, lk);
            z = Math.max(0.d,
                    (double) lk
                    + (depth - pr) / (getDepth(x, y, lk + 1) - pr));
        }
        return (z);
    }

    private double getDepth(double xRho, double yRho, int k) {

        final int i = (int) xRho;
        final int j = (int) yRho;
        double hh = 0.d;
        final double dx = (xRho - i);
        final double dy = (yRho - j);
        double co;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                if (isInWater(i + ii, j + jj)) {
                    co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                    double z_r = depth_t[k][j + jj][i + ii] + (double) ssh_tp0[j + jj][i + ii]
                            * (1.d + depth_t[k][j + jj][i + ii] / hm_w[j + jj][i + ii]);
                    hh += co * z_r;
                }
            }
        }
        return (hh);
    }
    
    public double getDepthMax(double x, double y) {
        return getDepth(x, y, nk - 1);
    }

    public double z2depth(double x, double y, double z) {

        final double kz = Math.max(0.d, Math.min(z, (double) nk - 1.00001f));
        final int i = (int) Math.floor(x);
        final int j = (int) Math.floor(y);
        final int k = (int) Math.floor(kz);
        double depth = 0.d;
        final double dx = x - (double) i;
        final double dy = y - (double) j;
        final double dz = kz - (double) k;
        double co;
        double z_r;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    if (isInWater(i + ii, j + jj)) {
                        z_r = depth_t[k + kk][j + jj][i + ii] + (double) ssh_tp0[j + jj][i + ii]
                                * (1.d + depth_t[k + kk][j + jj][i + ii] / hm_w[j
                                + jj][i + ii]);
                        depth += co * z_r;
                    }
                }
            }
        }
        return depth;
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        double du = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nk - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        int k = (int) kz;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    double co = Math.abs((.5d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    if (!(Float.isNaN(u_tp0[k + kk][j + jj][i + ii - 1]) || Float.isNaN(u_tp1[k + kk][j + jj][i + ii - 1]))) {
                        double x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1] + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                        du += 2.d * x * co;
                    }
                }
            }
        }
        du /= dx_v[j][i];
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nk - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (int) Math.round(jy);
        int k = (int) kz;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;

        for (int jj = 0; jj < 2; jj++) {
            for (int ii = 0; ii < n; ii++) {
                for (int kk = 0; kk < 2; kk++) {
                    double co = Math.abs((1.d - (double) ii - dx)
                            * (.5d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    if (!(Float.isNaN(v_tp0[k + kk][j + jj - 1][i + ii]) || Float.isNaN(v_tp1[k + kk][j + jj - 1][i + ii]))) {
                        double x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i + ii] + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                        dv += 2.d * x * co;
                    }
                }
            }
        }
        dv /= dy_u[j][i];
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        double dw = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nk - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        int k = (int) Math.round(kz);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    double co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (.5d - (double) kk - dz));
                    CO += co;
                    if (isInWater(i + ii, j + jj)) {
                        double x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii] + x_euler * w_tp1[k + kk][j + jj][i + ii];
                        dw += 2.d * x * co / (depth_w_tp0[Math.min(k + kk + 1, nk)][j + jj][i + ii] - depth_w_tp0[Math.max(k + kk - 1, 0)][j + jj][i + ii]);
                    }
                }
            }
        }
        if (CO != 0) {
            dw /= CO;
        }
        return dw;
    }

    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    public boolean isInWater(int i, int j) {
        try {
            return (mask_t[j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean isCloseToCost(double[] pGrid) {
        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) && isInWater(i, j + jj));
    }

    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (ni - 2.0f))
                || (pGrid[0] < 1.0f)
                || (pGrid[1] > (nj - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    public double getBathy(int i, int j) {
        if (isInWater(i, j)) {
            return hm_w[j][i];
        }
        return Double.NaN;
    }

    public int get_nx() {
        return ni;
    }

    public int get_ny() {
        return nj;
    }

    public int get_nz() {
        return nk;
    }

    public double get_dx(int j, int i) {
        return dx_v[j][i];
    }

    public double get_dy(int j, int i) {
        return dy_u[j][i];
    }

    /**
     * Gets domain minimum latitude.
     *
     * @return a double, the domain minimum latitude [north degree]
     */
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    public double getLatMax() {
        return latMax;
    }

    /**
     * Gets domain minimum longitude.
     *
     * @return a double, the domain minimum longitude [east degree]
     */
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets domain maximum longitude.
     *
     * @return a double, the domain maximum longitude [east degree]
     */
    public double getLonMax() {
        return lonMax;
    }

    /**
     * Gets domain maximum depth.
     *
     * @return a float, the domain maximum depth [meter]
     */
    public double getDepthMax() {
        return depthMax;
    }

    public double getLon(int igrid, int jgrid) {
        return longitude_t[jgrid][igrid];
    }

    public double getLat(int igrid, int jgrid) {
        return latitude_t[jgrid][igrid];
    }

    public boolean is3D() {
        return true;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {
        double time = e.getSource().getTime();
        //Logger.getAnonjmousLogger().info("set fields at time " + time);
        int time_arrow = timeArrow();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        ssh_tp0 = ssh_tp1;
        if (depth_w_tp1 != null) {
            depth_w_tp0 = depth_w_tp1;
        }
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            indexFile = DatasetUtil.next(listInputFiles, indexFile, time_arrow);
            open(listInputFiles.get(indexFile));
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    private boolean isInsidePolygone(int imin, int imax, int jmin, int jmax, double lon, double lat) {

        //--------------------------------------------------------------
        // Return true if (lon, lat) is insidide the polygon defined by
        // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)

        //-----------------------------------------
        // Build the polygone
        int nb, shft;
        double[] xb, yb;
        boolean isInPolygone = true;

        nb = 2 * (jmax - jmin + imax - imin);
        xb = new double[nb + 1];
        yb = new double[nb + 1];
        shft = 0 - imin;
        for (int i = imin; i <= (imax - 1); i++) {
            xb[i + shft] = longitude_t[jmin][i];
            yb[i + shft] = latitude_t[jmin][i];
        }
        shft = 0 - jmin + imax - imin;
        for (int j = jmin; j <= (jmax - 1); j++) {
            xb[j + shft] = longitude_t[j][imax];
            yb[j + shft] = latitude_t[j][imax];
        }
        shft = jmax - jmin + 2 * imax - imin;
        for (int i = imax; i >= (imin + 1); i--) {
            xb[shft - i] = longitude_t[jmax][i];
            yb[shft - i] = latitude_t[jmax][i];
        }
        shft = 2 * jmax - jmin + 2 * (imax - imin);
        for (int j = jmax; j >= (jmin + 1); j--) {
            xb[shft - j] = longitude_t[j][imin];
            yb[shft - j] = latitude_t[j][imin];
        }
        xb[nb] = xb[0];
        yb[nb] = yb[0];

        //---------------------------------------------
        //Check if {lon, lat} is inside polygone
        int inc, crossings;
        double dx1, dx2, dxy;
        crossings = 0;

        for (int k = 0; k < nb; k++) {
            if (xb[k] != xb[k + 1]) {
                dx1 = lon - xb[k];
                dx2 = xb[k + 1] - lon;
                dxy = dx2 * (lat - yb[k]) - dx1 * (yb[k + 1] - lat);
                inc = 0;
                if ((xb[k] == lon) & (yb[k] == lat)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (lat >= yb[k]))
                        | ((dx2 == 0.) & (lat >= yb[k + 1]))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) & ((xb[k + 1] - xb[k]) * dxy >= 0.)) {
                    inc = 2;
                }
                if (xb[k + 1] > xb[k]) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }
        if (crossings == 0) {
            isInPolygone = false;
        }
        return (isInPolygone);
    }
    
    public double xTore(double x) {
        return x;
    }

    public double yTore(double y) {
        return y;
    }
}
