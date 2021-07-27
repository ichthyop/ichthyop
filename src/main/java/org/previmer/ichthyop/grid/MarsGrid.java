package org.previmer.ichthyop.grid;

import java.io.IOException;
import java.util.logging.Level;

import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Class to manage Mars grid.
 * 
 * 
 * @author Nicolas Barrier
 */
public class MarsGrid extends AbstractGrid {

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
    /**
     * Mask: water = 1, cost = 0
     */
    private byte[][] maskRho;

    /**
     *
     */
    double[][] dyu, dx;
    /**
     *
     */
    double[][] dxv, dy;
    /**
     *
     */

    double[][][] depth, depthW;

    private double[] sigma, sigmaW;

    private String strLonDim;
    private String strLatDim;
    private String strZDim;
    private String strLon;
    private String strLat;
    private String strBathy;
    private NetcdfFile ncIn;
    private String strDx, strDy;
    private String strDxV, strDyU;
    private String strLevW, strLev;
    private VerticalMode verticalMode;

    private enum VerticalMode {
        GENERALIZED, STANDARD;
    }

    public MarsGrid(String filename) {
        super(filename);
    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setUp() throws Exception {
        loadParameters();
        openDataset();
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
    }

    /**
     * Determines the geographical boundaries of the domain in longitude, latitude
     * and depth.
     */
    public void getDimGeogArea() {

        int nx = this.get_nx();
        int ny = this.get_ny();

        // --------------------------------------
        // Calculate the Physical Space extrema

        this.setLonMin(Double.MAX_VALUE);
        this.setLonMax(-this.getLonMin());
        this.setLatMin(Double.MAX_VALUE);
        this.setLatMax(-this.getLatMin());
        this.setDepthMax(0.d);
        int i = nx;

        while (i-- > 0) {
            int j = ny;
            while (j-- > 0) {
                if (lonRho[j][i] >= this.getLonMax()) {
                    this.setLonMax(lonRho[j][i]);
                }
                if (lonRho[j][i] <= this.getLonMin()) {
                    this.setLonMin(lonRho[j][i]);
                }
                if (latRho[j][i] >= this.getLatMax()) {
                    this.setLatMax(latRho[j][i]);
                }
                if (latRho[j][i] <= this.getLatMin()) {
                    this.setLatMin(latRho[j][i]);
                }
                if (hRho[j][i] >= this.getDepthMax()) {
                    this.setDepthMax(hRho[j][i]);
                }
            }
        }
        // System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " +
        // latMin + " latmax " + latMax);
        // System.out.println("depth max " + depthMax);

        double double_tmp;
        if (getLonMin() > getLonMax()) {
            double_tmp = getLonMin();
            this.setLonMin(getLonMax());
            this.setLonMax(double_tmp);
        }

        if (getLatMin() > getLatMax()) {
            double_tmp = getLatMin();
            this.setLatMin(getLatMax());
            this.setLatMax(double_tmp);
        }
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     * 
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws IOException {

        try {
            set_nx(ncIn.findDimension(strLonDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset longitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            set_ny(ncIn.findDimension(strLatDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset latitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            set_ny(ncIn.findDimension(strZDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset latitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        set_ipo(0);
        set_jpo(0);
    }

    private void openDataset() throws IOException {
        ncIn = NetcdfDataset.openDataset(this.getFilename());
    }

    @Override
    public boolean is3D() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getBathy(int i, int j) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInWater(int i, int j) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void loadParameters() {
        strLonDim = getParameter("field_dim_lon");
        strLatDim = getParameter("field_dim_lat");
        strZDim = getParameter("field_dim_z");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strLevW = getParameter("field_var_sigw");
        strLev = getParameter("field_var_sig");
        strDx = getParameter("field_var_dx");
        strDy = getParameter("field_var_dy");
        strDxV = getParameter("field_var_dxv");
        strDyU = getParameter("field_var_dyv");
        verticalMode = VerticalMode.valueOf(getParameter("depth_mode").toUpperCase());
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCloseToCost(double[] pGrid) {
        // TODO Auto-generated method stub
        return false;
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
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Method to interpolate a U variable. On NEMO, U points are on the eastern face
     * of the cell.
     * 
     */
    public double interpolate2dU(double[] pGrid, double[][][] variable, int kIndex) {

        double ix = pGrid[0];
        double jy = pGrid[1];

        int i = (int) Math.round(ix);
        int j = (int) Math.floor(jy);

        double output = 0;
        double weight = 0;
        for (int jj = 0; jj < 1; jj++) {
            for (int ii = 0; ii < 1; ii++) {
                double cox = Math.abs(ix - i + 0.5 - ii);
                double coy = Math.abs(jy - j - 1 + jj);
                double co = cox * coy;
                output += variable[kIndex][i - ii][j + jj] * co;
                weight += co;
            }
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /**
     * Method to interpolate a U variable. On NEMO, U points are on the eastern face
     * of the cell.
     * 
     */
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

    /**
     * Method to interpolate a V variable.
     * 
     * V points are locate in the northern faces
     * 
     */
    public double interpolate2dV(double[] pGrid, double[][][] variable, int kIndex) {

        double ix = pGrid[0];
        double jy = pGrid[1];

        int i = (int) Math.floor(ix);
        int j = (int) Math.round(jy);
        double output = 0;
        double weight = 0;

        // blue case:
        for (int jj = 0; jj < 1; jj++) {
            for (int ii = 0; ii < 1; ii++) {
                double coy = Math.abs(jy - j + 0.5 - jj);
                double cox = Math.abs(ix - i - 1 + ii);
                double co = cox * coy;
                output += variable[kIndex][i + ii][j - jj] * co;
                weight += co;
            }
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }

    /**
     * Method to interpolate a V variable.
     * 
     * V points are locate in the northern faces
     * 
     */
    public double interpolate3dV(double[] pGrid, double[][][] variable) {

        double kz = pGrid[2];

        int k = (int) Math.floor(kz);
        double output = 0;
        double weight = 0;

        // blue case:
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

    /**
     * Method to interpolate a T variable. On NEMO, T points are in the centerof the
     * cell.
     * 
     */
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

    /**
     * Method to interpolate a T variable. On NEMO, T points are in the centerof the
     * cell.
     * 
     */
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
                getLogger().log(Level.WARNING, "Failed to resize domain. " + ex.toString(), ex);
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

        readLonLat();

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
    void readLonLat() throws IOException {
        Array arrLon = null, arrLat = null;
        try {
            if (ncIn.findVariable(strLon).getShape().length > 1) {
                arrLon = ncIn.findVariable(strLon).read(new int[] { get_jpo(), get_ipo() },
                        new int[] { get_ny(), get_nx() });
            } else {
                arrLon = ncIn.findVariable(strLon).read(new int[] { get_ipo() }, new int[] { get_nx() });
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            if (ncIn.findVariable(strLat).getShape().length > 1) {
                arrLat = ncIn.findVariable(strLat).read(new int[] { get_jpo(), get_ipo() },
                        new int[] { get_ny(), get_nx() });
            } else {
                arrLat = ncIn.findVariable(strLat).read(new int[] { get_jpo() }, new int[] { get_ny() });
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset latitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        lonRho = new double[get_ny()][get_nx()];
        latRho = new double[get_ny()][get_nx()];
        if (arrLon.getShape().length > 1) {
            Index index = arrLon.getIndex();
            for (int j = 0; j < get_ny(); j++) {
                for (int i = 0; i < get_nx(); i++) {
                    index.set(j, i);
                    lonRho[j][i] = arrLon.getDouble(index);
                    if (Math.abs(lonRho[j][i]) > 360) {
                        lonRho[j][i] = Double.NaN;
                    }
                    latRho[j][i] = arrLat.getDouble(index);
                    if (Math.abs(latRho[j][i]) > 90) {
                        latRho[j][i] = Double.NaN;
                    }
                }
            }
        } else {
            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < get_ny(); j++) {
                indexLat.set(j);
                for (int i = 0; i < get_nx(); i++) {
                    latRho[j][i] = arrLat.getDouble(indexLat);
                    indexLon.set(i);
                    lonRho[j][i] = arrLon.getDouble(indexLon);
                }
            }
        }
    }

    void readConstantField() throws Exception {

        Array arrH = null;
        Index index;
        int nx = this.get_nx();
        int ny = this.get_ny();
        int ipo = this.get_ipo();
        int jpo = this.get_jpo();
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        maskRho = new byte[ny][nx];
        dx = new double[ny][nx];
        dy = new double[ny][nx];
        this.dxv = new double[ny][nx];
        this.dyu = new double[ny][nx];

        /* Read longitude & latitude */
        readLonLat();

        /* Read bathymetry */
        try {
            arrH = ncIn.findVariable(strBathy).read(new int[] { jpo, ipo }, new int[] { ny, nx });
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("{Dataset} Error reading bathymetry variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        hRho = new double[ny][nx];
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                hRho[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        /* Compute mask */
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                maskRho[j][i] = (Double.isNaN(hRho[j][i]) || (hRho[j][i] <= 0)) ? (byte) 0 : (byte) 1;
            }
        }

        // Reads width of T cells
        arrH = ncIn.findVariable(strDx).read(new int[] { jpo, ipo }, new int[] { ny, nx });
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                dx[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        // Reads width of V cell
        arrH = ncIn.findVariable(strDxV).read(new int[] { jpo, ipo }, new int[] { ny, nx });
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                dxv[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        // Reads height of T cells
        arrH = ncIn.findVariable(strDy).read(new int[] { jpo, ipo }, new int[] { ny, nx });
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                dy[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        // Reads height of U cell
        arrH = ncIn.findVariable(strDyU).read(new int[] { jpo, ipo }, new int[] { ny, nx });
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                dyu[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        this.reconstructDepth();

    }

    private void reconstructDepth() throws IOException, InvalidRangeException {

        // Reading the Sigma levels at T and W depths
        this.sigma = (double[]) ncIn.findVariable(this.strLev).read().copyTo1DJavaArray();
        this.sigmaW = (double[]) ncIn.findVariable(this.strLevW).read().copyTo1DJavaArray();

        this.depth = new double[get_nz()][get_ny()][get_ny()];
        this.depthW = new double[get_nz() + 1][get_ny()][get_ny()];

        if (verticalMode.equals(VerticalMode.STANDARD)) {

            // If standard mode
            for (int k = 0; k < this.get_nz(); k++) {
                for (int j = 0; j < this.get_ny(); j++) {
                    for (int i = 0; i < this.get_nx(); i++) {
                        this.depth[k][j][i] = this.sigma[k] * hRho[j][i];
                        this.depthW[k][j][i] = this.sigmaW[k] * hRho[j][i];
                    }
                }
            }

            // reconstruct of W depth at the surface
            int k = get_nz();
            for (int j = 0; j < this.get_ny(); j++) {
                for (int i = 0; i < this.get_nx(); i++) {
                    this.depthW[k][j][i] = this.sigmaW[k] * hRho[j][i];
                }
            }

        } else {

            // Using generalized mode.
            // First, the C(sigma) values at T and W points are extracted
            String strCs = getParameter("field_var_cs");
            String strCsW = getParameter("field_var_csw");
            double[] Cs = (double[]) ncIn.findVariable(strCs).read().copyTo1DJavaArray();
            double[] CsW = (double[]) ncIn.findVariable(strCsW).read().copyTo1DJavaArray();

            // Reading the Hc value
            String strHc = getParameter("field_var_hc");
            Array arrHc = ncIn.findVariable(strHc).read(new int[] { get_jpo(), get_ipo() },
                    new int[] { get_ny(), get_nx() });
            Index indexHc = arrHc.getIndex();
            for (int k = 0; k < this.get_nz(); k++) {
                for (int j = 0; j < this.get_ny(); j++) {
                    for (int i = 0; i < this.get_nx(); i++) {
                        indexHc.set(k, j, i);
                        double hc = arrHc.getDouble(indexHc);
                        this.depth[k][j][i] = hc * (this.sigma[k] - Cs[k]) + hRho[j][i] * Cs[k];
                        this.depthW[k][j][i] = hc * (this.sigmaW[k] - CsW[k]) + hRho[j][i] * CsW[k];
                    }
                }
            }

            // Reconstructing the last ocean grid (ocean surface).
            int k = get_nz();
            for (int j = 0; j < this.get_ny(); j++) {
                for (int i = 0; i < this.get_nx(); i++) {
                    indexHc.set(k, j, i);
                    double hc = arrHc.getDouble(indexHc);
                    this.depthW[k][j][i] = hc * (this.sigmaW[k] - CsW[k]) + hRho[j][i] * CsW[k];
                }
            }
        }
    }

}