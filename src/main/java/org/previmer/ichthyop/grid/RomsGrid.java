/*
 *ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 *http://www.ichthyop.org
 *
 *Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-today
 *http://www.ird.fr
 *
 *Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 *Contributors (alphabetically sorted):
 *Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 *Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 *Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 *Stephane POUS, Nathan PUTMAN.
 *
 *Ichthyop is a free Java tool designed to study the effects of physical and
 *biological factors on ichthyoplankton dynamics. It incorporates the most
 *important processes involved in fish early life: spawning, movement, growth,
 *mortality and recruitment. The tool uses as input time series of velocity,
 *temperature and salinity fields archived from oceanic models such as NEMO,
 *ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 *generates output files that can be post-processed easily using graphic and
 *statistical software.
 *
 *To cite Ichthyop, please refer to Lett et al. 2008
 *A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 *Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 *doi:10.1016/j.envsoft.2008.02.005
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation (version 3 of the License). For a full
 *description, see the LICENSE file.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.previmer.ichthyop.grid;

import java.io.IOException;
import java.util.logging.Level;

import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Class to manage Roms grid.
 * 
 * 
 */
public class RomsGrid extends AbstractGrid {

    /**
     * Mask: water = 1, cost = 0
     */
    private byte[][] maskRho;

    private NetcdfFile ncIn;
    /**
     * Name of the Dimension in NetCDF file
     */
    private String strXiDim, strEtaDim, strSigDim;

    private String strHc, strCs_r, strCs_w;

    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat, strMask, strBathy;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strPn, strPm;

    /**
     * Longitude at rho point.
     */
    private double[][] lonRho;
    /**
     * Latitude at rho point.
     */
    private double[][] latRho;
    /**
     * Bathymetry
     */
    private double[][] hRho;

    private double[][][] depth, depthW;
    private String strSig, strSig_W;
    /**
     *
     */
    private double[][] pm, pn;

    private VerticalMode verticalMode;
    
    private String meshFile;

    private enum VerticalMode {
        UCLA, STANDARD;
    }

    public RomsGrid() {
        super();
    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setUp() throws Exception {
        loadParameters();
        openDataset();
        // openLocation(getParameter("input_path"));
        getDimNC();
        shrinkGrid();
        readConstantField(this.meshFile);
        getDimGeogArea();

    }

    public void openDataset() throws IOException {
        ncIn = NetcdfDataset.openDataset(this.meshFile);
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    public void getDimNC() throws Exception {

        try {
            this.set_nx(ncIn.findDimension(strXiDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset grid dimensions XI. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            this.set_ny(ncIn.findDimension(strEtaDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset grid dimensions ETA. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            this.set_nz(ncIn.findDimension(strSigDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset grid dimensions SIGMA. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        
        set_ipo(0);
        set_jpo(0);
    }

    @Override
    public boolean is3D() {
        return true;
    }

    @Override
    public double getBathy(int i, int j) {
        if (isInWater(i, j)) {
            return hRho[j][i];
        }
        return Double.NaN;
    }

    @Override
    public boolean isInWater(int i, int j) {
        try {
            return (maskRho[j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public void loadParameters() {
        this.strXiDim = getParameter("field_dim_xi");
        this.strEtaDim = getParameter("field_dim_eta");
        this.strSigDim = getParameter("field_dim_sig");
        this.strLon = getParameter("field_var_lon");
        this.strLat = getParameter("field_var_lat");
        this.strBathy = getParameter("field_var_bathy");
        this.strMask = getParameter("field_var_mask");
        this.strPn = getParameter("field_var_pn");
        this.strPm = getParameter("field_var_pm");
        this.meshFile = getParameter("grid_file");
        this.strSig = getParameter("field_var_sig");
        this.strSig_W = getParameter("field_var_sigw");
        this.verticalMode = VerticalMode.valueOf(getParameter("depth_mode").toUpperCase());
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public boolean isCloseToCost(double[] pGrid) {
        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) && isInWater(i, j + jj));
    }

    @Override
    public double depth2z(double x, double y, double depth) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double z2depth(double x, double y, double z) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double[] xy2latlon(double xRho, double yRho) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double[] latlon2xy(double lat, double lon) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (get_nx() - 2.0f)) || (pGrid[0] < 1.0f) || (pGrid[1] > (get_ny() - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    /** Interpolation of tracer field on T grid */
    public double interpolate2dT(double[] pGrid, double[][][] variable, int kIndex) {

        double ix = pGrid[0];
        double jy = pGrid[1];

        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);

        double output = 0;
        double weight = 0;

        for (int jj = 0; jj < 1; jj++) {
            for (int ii = 0; ii < 1; ii++) {
                double cox = Math.abs(ix - i - 1 - ii);
                double coy = Math.abs(jy - j - 1 + jj);
                double co = cox * coy;
                output += variable[kIndex][i + ii][j + jj] * co;
                weight += co;
            }
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /** Interpolation of tracer field on T grid */
    public double interpolate3dT(double[] pGrid, double[][][] variable) {

        double kz = pGrid[2];

        int k = (int) Math.floor(kz);

        double output = 0;
        double weight = 0;

        for (int kk = 0; kk < 1; kk++) {
            double coz = Math.abs(kz - k - 1 + kk);
            output += this.interpolate2dT(pGrid, variable, k + kk) * coz;
            weight += coz;
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /** Interpolation of tracer field on U grid */
    public double interpolate2dU(double[] pGrid, double[][][] variable, int kIndex) {

        double ix = pGrid[0];
        double jy = pGrid[1];

        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);

        double output = 0;
        double weight = 0;

        for (int jj = 0; jj < 1; jj++) {
            for (int ii = 0; ii < 1; ii++) {
                double cox = Math.abs(ix - i - 0.5 + ii);
                double coy = Math.abs(jy - j - 1 + jj);
                double co = cox * coy;
                output += variable[kIndex][i + ii][j + jj] * co;
                weight += co;
            }
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /** Interpolation of tracer field on U grid */
    public double interpolate3dU(double[] pGrid, double[][][] variable) {

        double kz = pGrid[2];

        int k = (int) Math.floor(kz);

        double output = 0;
        double weight = 0;

        for (int kk = 0; kk < 1; kk++) {
            double coz = Math.abs(kz - k - 1 + kk);
            output += this.interpolate2dU(pGrid, variable, k + kk) * coz;
            weight += coz;
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /** Interpolation of tracer field on V grid */
    public double interpolate2dV(double[] pGrid, double[][][] variable, int kIndex) {

        double ix = pGrid[0];
        double jy = pGrid[1];

        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);

        double output = 0;
        double weight = 0;

        for (int jj = 0; jj < 1; jj++) {
            for (int ii = 0; ii < 1; ii++) {
                double cox = Math.abs(ix - i - 1 + ii);
                double coy = Math.abs(jy - j - 0.5 + jj);
                double co = cox * coy;
                output += variable[kIndex][i + ii][j + jj] * co;
                weight += co;
            }
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /** Interpolation of tracer field on V grid */
    public double interpolate3dV(double[] pGrid, double[][][] variable) {

        double kz = pGrid[2];

        int k = (int) Math.floor(kz);

        double output = 0;
        double weight = 0;

        for (int kk = 0; kk < 1; kk++) {
            double coz = Math.abs(kz - k - 1 + kk);
            output += this.interpolate2dV(pGrid, variable, k + kk) * coz;
            weight += coz;
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    void readConstantField(String gridFile) throws IOException {

        int[] origin = new int[] { get_jpo(), get_ipo() };
        int[] size = new int[] { get_ny(), get_nx() };
        Array arrLon, arrLat, arrMask, arrH, arrPm, arrPn;
        Index index;

        NetcdfFile ncGrid = NetcdfDataset.openDataset(gridFile);
        try {
            arrLon = ncGrid.findVariable(strLon).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset longitude. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncGrid.findVariable(strLat).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset latitude. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrMask = ncGrid.findVariable(strMask).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset mask. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrH = ncGrid.findVariable(strBathy).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset bathymetry. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrPm = ncGrid.findVariable(strPm).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset pm metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        try {
            arrPn = ncGrid.findVariable(strPn).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset pn metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        ncGrid.close();

        lonRho = new double[get_ny()][get_nx()];
        latRho = new double[get_ny()][get_nx()];
        index = arrLon.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                index.set(j, i);
                lonRho[j][i] = arrLon.getDouble(index);
                latRho[j][i] = arrLat.getDouble(index);
            }
        }

        maskRho = new byte[get_ny()][get_nx()];
        index = arrMask.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                maskRho[j][i] = arrMask.getByte(index.set(j, i));
            }
        }

        pm = new double[get_ny()][get_nx()];
        pn = new double[get_ny()][get_nx()];
        index = arrPm.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                index.set(j, i);
                pm[j][i] = arrPm.getDouble(index);
                pn[j][i] = arrPn.getDouble(index);
            }
        }

        hRho = new double[get_ny()][get_nx()];
        index = arrH.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                hRho[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        this.reconstructDepth();

    }

    public void shrinkGrid() {
        boolean isParamDefined;
        try {
            Boolean.valueOf(getParameter("shrink_domain"));
            isParamDefined = true;
        } catch (NullPointerException ex) {
            isParamDefined = false;
        }

        if (isParamDefined && Boolean.valueOf(getParameter("shrink_domain"))) {
            try {
                float lon1 = Float.valueOf(
                        LonLatConverter.convert(getParameter("north-west-corner.lon"), LonLatFormat.DecimalDeg));
                float lat1 = Float.valueOf(
                        LonLatConverter.convert(getParameter("north-west-corner.lat"), LonLatFormat.DecimalDeg));
                float lon2 = Float.valueOf(
                        LonLatConverter.convert(getParameter("south-east-corner.lon"), LonLatFormat.DecimalDeg));
                float lat2 = Float.valueOf(
                        LonLatConverter.convert(getParameter("south-east-corner.lat"), LonLatFormat.DecimalDeg));
                range(lat1, lon1, lat2, lon2);
            } catch (IOException | NumberFormatException ex) {
                getLogger().log(Level.WARNING, "Failed to resize domain", ex);
            }
        }
    }

    /**
     * Resizes the domain and determines the range of the grid indexes taht will be
     * used in the simulation. The new domain is limited by the Northwest and the
     * Southeast corners.
     *
     * @param pGeog1 a float[], the geodesic coordinates of the domain Northwest
     *               corner
     * @param pGeog2 a float[], the geodesic coordinates of the domain Southeast
     *               corner
     * @throws an IOException if the new domain is not strictly nested within the
     *            NetCDF dataset domain.
     */
    private void range(double lat1, double lon1, double lat2, double lon2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        readLonLat(this.meshFile);

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            throw new IOException("Impossible to proportion the simulation area : points out of domain");
        }
        lonRho = null;
        latRho = null;

        // System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " +
        // (float)pGrid2[0] + " " + (float)pGrid2[1]);
        set_ipo((int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0])));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        set_jpo((int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1])));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        set_nx(Math.min(get_nx(), ipn - get_ipo() + 1));
        set_ny(Math.min(get_ny(), jpn - get_jpo() + 1));
        // System.out.println("ipo " + ipo + " nx " + nx + " jpo " + jpo + " ny " + ny);
    }

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat(String gridFile) throws IOException {

        Array arrLon, arrLat;
        NetcdfFile ncGrid = NetcdfDataset.openDataset(gridFile);
        try {
            arrLon = ncIn.findVariable(strLon).read();
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncIn.findVariable(strLat).read();
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading dataset latitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ncGrid.close();

        latRho = new double[get_ny()][get_nx()];
        lonRho = new double[get_ny()][get_nx()];
        Index index = arrLon.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                index.set(j, i);
                lonRho[j][i] = arrLon.getDouble(index);
                latRho[j][i] = arrLat.getDouble(index);
            }
        }
    }

    private double getHc() throws IOException {

        if (null != ncIn.findGlobalAttribute(strHc)) {
            /* supposedly UCLA */
            return ncIn.findGlobalAttribute(strHc).getNumericValue().floatValue();
        } else if (null != ncIn.findVariable(strHc)) {
            /* supposedly Rutgers */
            return ncIn.findVariable(strHc).readScalarFloat();
        } else {
            /* hc not found */
            throw new IOException(
                    "S-coordinate critical depth (hc) could not be found, neither among variables nor global attributes");
        }
    }

    private double[] getCs_r() throws IOException {
        if (null != ncIn.findGlobalAttribute(strCs_r)) {
            /* supposedly UCLA */
            Attribute attrib_cs_r = ncIn.findGlobalAttribute(strCs_r);
            double[] Cs_r = new double[attrib_cs_r.getLength()];
            for (int k = 0; k < Cs_r.length - 1; k++) {
                Cs_r[k] = attrib_cs_r.getNumericValue(k).floatValue();
            }
            return Cs_r;
        } else if (null != ncIn.findVariable(strCs_r)) {
            /* supposedly Rutgers */
            Array arr_cs_r = ncIn.findVariable(strCs_r).read();
            double[] Cs_r = new double[arr_cs_r.getShape()[0]];
            for (int k = 0; k < Cs_r.length - 1; k++) {
                Cs_r[k] = arr_cs_r.getDouble(k);
            }
            return Cs_r;
        } else {
            /* Cs_w not found */
            throw new IOException(
                    "S-coordinate stretching curves at Rho-points (Cs_r) could not be found, neither among variables nor global attributes");
        }
    }

    private double[] getCs_w() throws IOException {
        if (null != ncIn.findGlobalAttribute(strCs_w)) {
            /* supposedly UCLA */
            Attribute attrib_cs_w = ncIn.findGlobalAttribute(strCs_w);
            double[] Cs_w = new double[attrib_cs_w.getLength()];
            for (int k = 0; k < Cs_w.length - 1; k++) {
                Cs_w[k] = attrib_cs_w.getNumericValue(k).floatValue();
            }
            return Cs_w;
        } else if (null != ncIn.findVariable(strCs_w)) {
            /* supposedly Rutgers */
            Array arr_cs_w = ncIn.findVariable(strCs_w).read();
            double[] Cs_w = new double[arr_cs_w.getShape()[0]];
            for (int k = 0; k < Cs_w.length - 1; k++) {
                Cs_w[k] = arr_cs_w.getDouble(k);
            }
            return Cs_w;
        } else {
            /* Cs_w not found */
            throw new IOException(
                    "S-coordinate stretching curves at W-points (Cs_w) could not be found, neither among variables nor global attributes");
        }
    }

    private void reconstructDepth() throws IOException {

        int k;
        double hc = getHc();

        double[] sigma = new double[get_nz()];
        double[] Cs_r;
        double[] sigmaW = new double[get_nz() + 1];
        double[] Cs_w;

        Cs_r = this.getCs_r();
        Cs_w = this.getCs_w();

        sigma = (double[]) ncIn.findVariable(strSig_W).read().copyTo1DJavaArray();
        sigmaW = (double[]) ncIn.findVariable(strSig).read().copyTo1DJavaArray();

        switch (verticalMode) {
            case UCLA:
                for (int i = 0; i < get_nx(); i++) {
                    for (int j = 0; j < get_ny(); j++) {
                        for (k = 0; k < get_nz(); k++) {
                            depth[k][j][i] = hRho[j][i] * (sigma[k] * hc + Cs_r[k] * hRho[j][i]) / (hc + hRho[j][i]);
                            depthW[k + 1][j][i] = hRho[j][i] * (sigmaW[k + 1] * hc + Cs_w[k + 1] * hRho[j][i])
                                    / (hc + hRho[j][i]);
                        }
                    }
                }

                k = get_nz();
                for (int i = 0; i < get_nx(); i++) {
                    for (int j = 0; j < get_ny(); j++) {
                        depthW[k + 1][j][i] = hRho[j][i] * (sigmaW[k + 1] * hc + Cs_w[k + 1] * hRho[j][i])
                                / (hc + hRho[j][i]);
                    }
                }
                break;

            case STANDARD:
                for (int i = 0; i < get_nx(); i++) {
                    for (int j = 0; j < get_ny(); j++) {
                        for (k = 0; k < get_nz(); k++) {
                            depth[k][j][i] = hc * (sigma[k] - Cs_r[k]) + Cs_r[k] * hRho[j][i];
                            depthW[k][j][i] = hc * (sigmaW[k] - Cs_w[k]) + Cs_w[k] * hRho[j][i];
                        }
                    }
                }

                k = get_nz();
                for (int i = 0; i < get_nx(); i++) {
                    for (int j = 0; j < get_ny(); j++) {
                        depthW[k][j][i] = hc * (sigmaW[k] - Cs_w[k]) + Cs_w[k] * hRho[j][i];
                    }
                }
                break;

        }

    }

}
