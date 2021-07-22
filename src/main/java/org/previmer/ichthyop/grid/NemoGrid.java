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

package org.previmer.ichthyop.grid;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/** Class to manage Nemo grid. 
 * 
 * The Grid layout in Roms is as follows:
 * 
 * +----------+------------+
 * |          |            |     
 * |          |            |      
 * |          |            |       
 * |----------+--V(i,j)----|        
 * |          |            |  
 * |      U(i-1,j) T(i,j) U(i,j) 
 * |          |            |       
 * +----------+--V(i,j-1)--+       
 * 
 * @author Nicolas Barrier
 */
public class NemoGrid extends AbstractGrid {

    /**
     * Mask: water = 1, cost = 0
     */
    private int[][][] maskRho;//, masku, maskv;
    
    /**
     * Depth at rho point
     */
    private double[][][] gdepT;
    
    /**
     * Depth at w point. The free surface elevation is disregarded. For index k,
     * gdepW[k] is the depth of the W point below the center of the cell.
     */
    private double[][][] gdepW;

    /**
     * Name of the Dimension in NetCDF file
     */
    private String strXDim, strYDim, strZDim;
    
    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat, strMask;
    /**
     *
     */
    private double[][][] e3t, e3u, e3v;
    private double[][] e1t, e2t, e1v, e2u;
    private String stre1t, stre2t, stre3t, stre1v, stre2u, stre3u, stre3v;
    private String str_gdepT, str_gdepW;
    private String file_hgr, file_zgr, file_mask;
    private boolean isGridInfoInOneFile;
    
    public NemoGrid(String filename) {
        super(filename);   
    }

    @Override
    public boolean is3D() {
        return true;
    }
    
    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    private void readConstantField() throws Exception {

        NetcdfFile nc;
        nc = NetcdfDataset.openDataset(file_hgr, enhanced(), null);
        getLogger().log(Level.INFO, "read lon, lat & mask from {0}", nc.getLocation());
        lonRho = new double[get_ny()][get_nx()];
        latRho = new double[get_ny()][get_nx()];
        Array arrLon = nc.findVariable(strLon).read().reduce();
        Array arrLat = nc.findVariable(strLat).read().reduce();
        Index indexLon = arrLon.getIndex();
        Index indexLat = arrLat.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                lonRho[j][i] = arrLon.getFloat(indexLon.set(get_jpo() + j, get_ipo() + i));
                latRho[j][i] = arrLat.getFloat(indexLat.set(get_jpo() + j, get_ipo() + i));
            }
        }

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDataset.openDataset(file_mask, enhanced(), null);
        }
        
        maskRho = new int[get_nz()][get_ny()][get_nx()];
        Array arrMask = nc.findVariable(strMask).read().reduce().flip(0);
        Index indexMask = arrMask.getIndex();
        for (int k = 0; k < get_nz(); k++) {
            for (int j = 0; j < get_ny(); j++) {
                for (int i = 0; i < get_nx(); i++) {
                    maskRho[k][j][i] = arrMask.getInt(indexMask.set(k, j + get_jpo(), i + get_ipo()));
                }
            }
        }

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDataset.openDataset(file_zgr, enhanced(), null);
        }
        
        get_gdep_fields(nc);

        // phv 20150319 - patch for e3t that can be found in NEMO output spread
        // into three variables e3t_0, e3t_ps and mbathy
        e3t = read_e3_field(nc, stre3t);
        
        if (stre3u.equals(stre3t)) {
            e3u = e3t;
        } else {
            e3u = read_e3_field(nc, stre3u);
        }
        
        if (stre3v.equals(stre3t)) {
            e3v = e3t;
        } else {
            e3v = read_e3_field(nc, stre3v);
        }

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDataset.openDataset(file_hgr, enhanced(), null);
        }
        
        //System.out.println("read e1t e2t " + nc.getLocation());
        // fichier *mesh*h*
        e1t = read_e1_e2_field(nc, stre1t);
        e2t = read_e1_e2_field(nc, stre2t);
        e1v = read_e1_e2_field(nc, stre1v);
        e2u = read_e1_e2_field(nc, stre2u);
        
        nc.close();
    }

    private double[][][] read_e3_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Array array = nc.findVariable(varname).read().reduce().flip(0);
        Index index = array.getIndex();
        double[][][] field = new double[get_nz()][get_ny()][get_nx()];
        boolean depth3d;

        // If depthT array is 3D, then use 3d depth array
        // if depthT is 1D, use 1D array to reconstruct 3D one
        if (array.getShape().length == 3) {
            depth3d = true;
        } else {
            depth3d = false;
        }

        if (depth3d) {

            for (int k = 0; k < get_nz(); k++) {
                for (int j = 0; j < get_ny(); j++) {
                    for (int i = 0; i < get_nx(); i++) {
                        index.set(k, j + get_jpo(), i + get_ipo());
                        field[k][j][i] = Double.isNaN(array.getDouble(index)) ? 0.d : array.getDouble(index);
                    }
                }
            }

        } else {

            for (int k = 0; k < get_nz(); k++) {
                index.set(k);
                double value = Double.isNaN(array.getDouble(index)) ? 0.d : array.getDouble(index);
                for (int j = 0; j < get_ny(); j++) {
                    for (int i = 0; i < get_nx(); i++) {
                        index.set(k, j + get_jpo(), i + get_ipo());
                        field[k][j][i] = value;
                    }
                }
            }
        }
        
        return field;
    }

    private void get_gdep_fields(NetcdfFile nc) throws InvalidRangeException, IOException {

        Index indexT, indexW;
        Array arrayT, arrayW;
        boolean depth3d;

        arrayT = nc.findVariable(str_gdepT).read().reduce().flip(0);
        arrayW = nc.findVariable(str_gdepW).read().reduce().flip(0);

        // If depthT array is 3D, then use 3d depth array
        // if depthT is 1D, use 1D array to reconstruct 3D one
        if (arrayT.getShape().length == 3) {
            depth3d = true;
        } else {
            depth3d = false;
        }

        indexT = arrayT.getIndex();
        indexW = arrayW.getIndex();
        gdepT = new double[get_nz()][get_ny()][get_nx()];
        gdepW = new double[get_nz()][get_ny()][get_nx()];

        if (!depth3d) {

            getLogger().log(Level.INFO, "Depth array are 1D arrays (z dimension)");

            // Extraction of depth at T points
            for (int k = 0; k < get_nz(); k++) {
                indexT.set(k);
                indexW.set(k);
                for (int j = 0; j < get_ny(); j++) {
                    for (int i = 0; i < get_nx(); i++) {
                        gdepT[k][j][i] = arrayT.getDouble(indexT);
                        gdepW[k][j][i] = arrayW.getDouble(indexW);
                    }
                }
            }

        } else {

            getLogger().log(Level.INFO, "Depth array are 3D arrays (z, y, z dimensions)");

            for (int k = 0; k < get_nz(); k++) {
                for (int j = 0; j < get_ny(); j++) {
                    for (int i = 0; i < get_nx(); i++) {
                        indexT.set(k, j + get_jpo(), i + get_ipo());
                        indexW.set(k, j + get_jpo(), i + get_ipo());
                        gdepT[k][j][i] = arrayT.getDouble(indexT);
                        gdepW[k][j][i] = arrayT.getDouble(indexT);
                    }
                }
            }

        }
    }

    private double[][] read_e1_e2_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Array array = nc.findVariable(varname).read().reduce();
        double[][] field = new double[get_ny()][get_nx()];
        Index index = array.getIndex();
        for (int j = 0; j < get_ny(); j++) {
            for (int i = 0; i < get_nx(); i++) {
                index.set(j + get_jpo(), i + get_ipo());
                field[j][i] = array.getDouble(index);
            }
        }
        return field;
    }

    
    /**
     * Reads longitude and latitude fields in NetCDF dataset
     *
     * pverley pour chourdin: même remarque que chaque fois. Les infos dans OPA
     * se trouvent dans différents fichiers, donc selon la méthode appelée, je
     * dois recharger le fichier NetCDF correspondant. Au lieu d'utiliser la
     * variable ncIn globale.
     */
    private void readLonLat() throws IOException {

        try (NetcdfFile nc = NetcdfDataset.openDataset(file_hgr, enhanced(), null)) {
            lonRho = new double[get_ny()][get_nx()];
            latRho = new double[get_ny()][get_nx()];
            Array arrLon = nc.findVariable(strLon).read().reduce();
            Array arrLat = nc.findVariable(strLat).read().reduce();
            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < get_ny(); j++) {
                for (int i = 0; i < get_nx(); i++) {
                    lonRho[j][i] = arrLon.getFloat(indexLon.set(j, i));
                    latRho[j][i] = arrLat.getFloat(indexLat.set(j, i));
                }
            }
        }
    }

    /*
     * Gets cell dimension [meter] in the XI-direction.
     *
     * pverley pour chourdin: vérifier avec Steph que je ne me trompe pas dans
     * la définition de e1t et e2t
     */
    // @Override
    // public double getdxi(int j, int i) {
    //     return e1t[j][i];
    // }

    /*
     * Gets cell dimension [meter] in the ETA-direction.
     */
    // @Override
    // public double getdeta(int j, int i) {
    //     return e2t[j][i];
    // }

    /*
     * Sets up the {@code Dataset}. The method first sets the appropriate
     * variable names, loads the first NetCDF dataset and extract the time
     * non-dependant information, such as grid dimensions, geographical
     * boundaries, depth at sigma levels.
     *
     * @throws an IOException if an error occurs while setting up the
     * {@code Dataset}
     */
    @Override
    public void setUp() throws Exception {

        loadParameters();
        sortInputFiles();
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
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

    /*
     * Gets the names of the NetCDF variables from the configuration file.
     *
     * pverley pour chourdin : "Configuration" c'est la classe qui lit le
     * fichier de configuration. Tu vois que normallement, le nom des variables
     * est lu dans le fichier cfg. Temporairement je courcircuite cette
     * opération et je renseigne à la main le nom des variables des sorties OPA.
     */
    @Override
    public void loadParameters() {

        strXDim = getParameter("field_dim_x");
        strYDim = getParameter("field_dim_y");
        strZDim = getParameter("field_dim_z");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strMask = getParameter("field_var_mask");
        stre3t = getParameter("field_var_e3t");
        stre3u = getParameter("field_var_e3u");
        stre3v = getParameter("field_var_e3v");
        str_gdepT = getParameter("field_var_gdept"); // z_rho
        str_gdepW = getParameter("field_var_gdepw"); // z_w
        stre1t = getParameter("field_var_e1t");
        stre2t = getParameter("field_var_e2t");
        stre1v = getParameter("field_var_e1v");
        stre2u = getParameter("field_var_e2u");
        if (!findParameter("enhanced_mode")) {
            getLogger().warning("Ichthyop assumes that by default the NEMO NetCDF files must be opened in enhanced mode (with scale, offset and missing attributes).");
        }
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     *
     * pverley pour chourdin: Pour ROMS ou MARS je lisais les dimensions à
     * partir de la variable ncIn qui est le premier fichier sortie qui me tombe
     * sous la main. Avec OPA, les dimensions se lisent dans un fichier
     * particulier *byte*mask*. A déterminer si toujours vrai ?
     */
    private void getDimNC() throws IOException {

        NetcdfFile nc = NetcdfDataset.openDataset(file_mask, enhanced(), null);
        try {
            this.set_nx(nc.findDimension(strXDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset X dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        
        try {
            this.set_ny(nc.findDimension(strYDim).getLength());
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Y dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        
        try {
            this.set_nz(nc.findDimension(strZDim).getLength());  // real number of Z values (including sea-bed)
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Z dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        
        this.set_ipo(0);
        this.set_jpo(0);
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
            throw new IOException(
                    "Impossible to proportion the simulation area : points out of domain");
        }
        lonRho = null;
        latRho = null;

        //System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " + (float)pGrid2[0] + " " + (float)pGrid2[1]);
        set_ipo((int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0])));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        
        set_jpo((int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1])));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        set_nx(Math.min(get_nx(), ipn - get_ipo() + 1));
        set_ny(Math.min(get_ny(), jpn - get_jpo() + 1));
        //System.out.println("get_ipo() " + get_ipo() + " get_nx() " + get_nx() + " get_jpo() " + get_jpo() + " get_ny() " + get_ny());
    }

    /*
     * Initializes the {@code Dataset}. Opens the file holding the first time of
     * the simulation. Checks out the existence of the fields required by the
     * current simulation. Sets all fields at time for the first time step.
     *
     * @throws an IOException if a required field cannot be found in the NetCDF
     * dataset.
     */
    @Override
    public void init() throws Exception {

    }

   

    /**
     * Determines whether or not the specified grid cell(i, j) is in water.
     *
     * @param i an int, i-coordinate of the cell
     * @param j an intn the j-coordinate of the cell
     * @return <code>true</code> if cell(i, j) is in water, <code>false</code>
     * otherwise.
     */
    private boolean isInWater(int i, int j, int k) {
        //System.out.println(i + " " + j + " " + k + " - "  + (maskRho[k][j][i] > 0));
        try {
            return (maskRho[k][j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    @Override
    public boolean isInWater(int i, int j) {
        return isInWater(i, j, get_nz() - 1);
    }

    /*
     * Determines whether the specified {@code RohPoint} is in water.
     *
     * @param ptRho the RhoPoint
     * @return <code>true</code> if the {@code RohPoint} is in water,
     * <code>false</code> otherwise.
     * @see #isInWater(int i, int j)
     */
    @Override
    public boolean isInWater(double[] pGrid) {
        if (pGrid.length > 2) {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
        } else {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
        }
    }

    /**
     * Determines whether or not the specified grid point is close to cost line.
     * The method first determines in which quater of the cell the grid point is
     * located, and then checks wether or not its cell and the three adjacent
     * cells to the quater are in water.
     *
     * @param pGrid a double[] the coordinates of the grid point
     * @return <code>true</code> if the grid point is close to cost,
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isCloseToCost(double[] pGrid) {
        int i, j, k, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        k = (int) (Math.round(pGrid[2]));
        ii = (i - (int) Math.floor(pGrid[0])) == 0 ? 1 : -1;
        jj = (j - (int) Math.floor(pGrid[1])) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j, k) && isInWater(i + ii, j + jj, k) && isInWater(i, j + jj, k));
    }

    /*
     * Transforms the depth at specified x-y particle location into z coordinate
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @param depth a double, the depth of the particle
     * @return a double, the z-coordinate corresponding to the depth
     *
     * pverley pour chourdin: méthode à tester.
     */
    @Override
    public double depth2z(double x, double y, double depth) {

        int j = (int) Math.round(y);
        int i = (int) Math.round(x);

        if (depth > 0) {
            depth = 0.d;
        }
        depth = Math.abs(depth);
        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double zRho = 0.d;

        /* case particle is going straight up to surface, due to strong
         * buoyancy for instance.
         */
        if (depth < gdepT[get_nz() - 1][j][i]) {
            //System.out.println("depth: " + depth + " ==> z: " + (get_nz() - 1) + " gdepT[get_nz() - 1]: " + gdepT[get_nz() - 1]);
            return (get_nz() - 1);
        }
        for (int k = get_nz() - 1; k > 0; k--) {
            //System.out.println("t1 " + z_w[k] + " " + (float)depth + " " + z_rho[k]);
            if (depth <= gdepW[k][j][i] && depth > gdepT[k][j][i]) {
                zRho = k + 0.d - 0.5d * Math.abs((gdepT[k][j][i] - depth) / (gdepT[k][j][i] - gdepW[k][j][i]));
                //System.out.println("depth: " + depth + " ==> z: " + zRho + " - k: " + k + " gdepW[k]: " + gdepW[k] + " gdepT[k]: " + gdepT[k]);
                return zRho;
            }
            //System.out.println("t2 " + z_rho[k] + " " + (float)depth + " " + z_w[k + 1]);
            if (depth <= gdepT[k][j][i] && depth > gdepW[k + 1][j][i]) {
                zRho = k + 0.d
                        + 0.5d
                        * Math.abs((gdepT[k][j][i] - depth)
                                / (gdepW[k + 1][j][i] - gdepT[k][j][i]));
                //System.out.println("depth: " + depth + " ==> z: " + zRho + " - k: " + k + " gdepW[k + 1]: " + gdepW[k + 1] + " gdepT[k]: " + gdepT[k]);
                return zRho;
            }
        }
        //System.out.println("depth: " + depth + " ==> z: " + zRho);
        return zRho;
    }

    /*
     * Transforms the specified z-location into depth
     *
     * pverley pour chourdin: j'ai testé z2depth et depth2z, ça à l'air de
     * marcher mais il faudra faire une validation plus sérieuse.
     *
     * @param x double
     * @param y double
     * @param z double
     * @return double
     */
    @Override
    public double z2depth(double x, double y, double z) {

        double depth;
        double dz;

        double kz = Math.max(0.d, Math.min(z, (double) get_nz() - 1.00001f));
        int k = (int) Math.round(kz);
        int j = (int) Math.round(y);
        int i = (int) Math.round(x);
        dz = z - k;

        if (dz < 0) {
            // The particule is located below the closest T point
            // depth regarding T grid needs to be increased (in NEMO, depths are > 0).
            // The 2 occurs since a chage of dz of 0.5 induces a change in depth of gdepT - gdepW.
            // Cross product induces a division by 0.5
            depth = gdepT[k][j][i] + 2 * Math.abs(dz * (gdepT[k][j][i] - gdepW[k - 1][j][i]));
        } else {
            // The particule is located above the closest T point
            // depth needs to be decreased
            depth = gdepT[k][j][i] - 2 * Math.abs(dz * (gdepT[k][j][i] - gdepW[k][j][i]));
        }
        
        return -depth;
        
    }

    /*
     * * Transforms the specified 2D grid coordinates into geographical
     * coordinates. It merely does a bilinear spatial interpolation of the
     * surrounding grid nods geographical coordinates.
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @return a double[], the corresponding geographical coordinates (latitude,
     * longitude)
     *
     * @param xRho double
     * @param yRho double
     * @return double[]
     */
    @Override
    public double[] xy2latlon(double xRho, double yRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        final double ix = Math.max(0.00001f,
                Math.min(xRho, (double) get_nx() - 1.00001f));
        final double jy = Math.max(0.00001f,
                Math.min(yRho, (double) get_ny() - 1.00001f));

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
                latitude += co * latRho[j + jj][i + ii];
                longitude += co * lonRho[j + jj][i + ii];
            }
        }
        return (new double[]{latitude, longitude});
    }

    /**
     * Transforms the specified 2D geographical coordinates into a grid
     * coordinates.
     *
     * The algorithme has been adapted from a function in ROMS/UCLA code,
     * originally written by Alexander F. Shchepetkin and Hernan G. Arango.
     * Please find below an extract of the ROMS/UCLA documention.
     *
     * <pre>
     *  Checks the position to find if it falls inside the whole domain.
     *  Once it is established that it is inside, find the exact cell to which
     *  it belongs by successively dividing the domain by a half (binary
     *  search).
     * </pre>
     *
     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return a double[], the corresponding grid coordinates (x, y)
     * @see #isInsidePolygone
     */
    @Override
    public double[] latlon2xy(double lat, double lon) {

        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)
        boolean found;
        int imin, imax, jmin, jmax, i0, j0;
        double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

        xgrid = -1.;
        ygrid = -1.;
        found = isInsidePolygone(0, get_nx() - 1, 0, get_ny() - 1, lon, lat);

        //-------------------------------------------
        // Research surrounding grid-points
        if (found) {
            imin = 0;
            imax = get_nx() - 1;
            jmin = 0;
            jmax = get_ny() - 1;
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
            dy1 = latRho[jmin + 1][imin] - latRho[jmin][imin];
            dx1 = lonRho[jmin + 1][imin] - lonRho[jmin][imin];
            dy2 = latRho[jmin][imin + 1] - latRho[jmin][imin];
            dx2 = lonRho[jmin][imin + 1] - lonRho[jmin][imin];

            c1 = lon * dy1 - lat * dx1;
            c2 = lonRho[jmin][imin] * dy2 - latRho[jmin][imin] * dx2;
            deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
            deltax = (deltax - lonRho[jmin][imin]) / dx2;
            xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

            c1 = lonRho[jmin][imin] * dy1 - latRho[jmin][imin] * dx1;
            c2 = lon * dy2 - lat * dx2;
            deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
            deltay = (deltay - latRho[jmin][imin]) / dy1;
            ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
        }
        return (new double[]{xgrid, ygrid});
    }

    /**
     * Determines whether the specified geographical point (lon, lat) belongs to
     * the is inside the polygon defined by (imin, jmin) & (imin, jmax) & (imax,
     * jmax) & (imax, jmin).
     *
     * <p>
     * The algorithm has been adapted from a function in ROMS/UCLA code,
     * originally written by Alexander F. Shchepetkin and Hernan G. Arango.
     * Please find below an extract of the ROMS/UCLA documention.
     * </p>
     * <pre>
     * Given the vectors Xb and Yb of size Nb, defining the coordinates
     * of a closed polygon,  this function find if the point (Xo,Yo) is
     * inside the polygon.  If the point  (Xo,Yo)  falls exactly on the
     * boundary of the polygon, it still considered inside.
     * This algorithm does not rely on the setting of  Xb(Nb)=Xb(1) and
     * Yb(Nb)=Yb(1).  Instead, it assumes that the last closing segment
     * is (Xb(Nb),Yb(Nb)) --> (Xb(1),Yb(1)).
     *
     * Reference:
     * Reid, C., 1969: A long way from Euclid. Oceanography EMR,
     * page 174.
     *
     * Algorithm:
     *
     * The decision whether the point is  inside or outside the polygon
     * is done by counting the number of crossings from the ray (Xo,Yo)
     * to (Xo,-infinity), hereafter called meridian, by the boundary of
     * the polygon.  In this counting procedure,  a crossing is counted
     * as +2 if the crossing happens from "left to right" or -2 if from
     * "right to left". If the counting adds up to zero, then the point
     * is outside.  Otherwise,  it is either inside or on the boundary.
     *
     * This routine is a modified version of the Reid (1969) algorithm,
     * where all crossings were counted as positive and the decision is
     * made  based on  whether the  number of crossings is even or odd.
     * This new algorithm may produce different results  in cases where
     * Xo accidentally coinsides with one of the (Xb(k),k=1:Nb) points.
     * In this case, the crossing is counted here as +1 or -1 depending
     * of the sign of (Xb(k+1)-Xb(k)).  Crossings  are  not  counted if
     * Xo=Xb(k)=Xb(k+1).  Therefore, if Xo=Xb(k0) and Yo>Yb(k0), and if
     * Xb(k0-1) < Xb(k0) < Xb(k0+1),  the crossing is counted twice but
     * with weight +1 (for segments with k=k0-1 and k=k0). Similarly if
     * Xb(k0-1) > Xb(k0) > Xb(k0+1), the crossing is counted twice with
     * weight -1 each time.  If,  on the other hand,  the meridian only
     * touches the boundary, that is, for example, Xb(k0-1) < Xb(k0)=Xo
     * and Xb(k0+1) < Xb(k0)=Xo, then the crossing is counted as +1 for
     * segment k=k0-1 and -1 for segment k=k0, resulting in no crossing.
     *
     * Note 1: (Explanation of the logical condition)
     *
     * Suppose  that there exist two points  (x1,y1)=(Xb(k),Yb(k))  and
     * (x2,y2)=(Xb(k+1),Yb(k+1)),  such that,  either (x1 < Xo < x2) or
     * (x1 > Xo > x2).  Therefore, meridian x=Xo intersects the segment
     * (x1,y1) -> (x2,x2) and the ordinate of the point of intersection
     * is:
     *                y1*(x2-Xo) + y2*(Xo-x1)
     *            y = -----------------------
     *                         x2-x1
     * The mathematical statement that point  (Xo,Yo)  either coinsides
     * with the point of intersection or lies to the north (Yo>=y) from
     * it is, therefore, equivalent to the statement:
     *
     *      Yo*(x2-x1) >= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 > 0
     * or
     *      Yo*(x2-x1) <= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 < 0
     *
     * which, after noting that  Yo*(x2-x1) = Yo*(x2-Xo + Xo-x1) may be
     * rewritten as:
     *
     *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) >= 0,   if   x2-x1 > 0
     * or
     *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) <= 0,   if   x2-x1 < 0
     *
     * and both versions can be merged into  essentially  the condition
     * that (Yo-y1)*(x2-Xo)+(Yo-y2)*(Xo-x1) has the same sign as x2-x1.
     * That is, the product of these two must be positive or zero.
     * </pre>
     *
     * @param imin an int, i-coordinate of the area left corners
     * @param imax an int, i-coordinate of the area right corners
     * @param jmin an int, j-coordinate of the area left corners
     * @param jmax an int, j-coordinate of the area right corners
     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return <code>true</code> if (lon, lat) belongs to the polygon,
     * <code>false</code>otherwise.
     */
    private boolean isInsidePolygone(int imin, int imax, int jmin,
            int jmax, double lon, double lat) {

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
            xb[i + shft] = lonRho[jmin][i];
            yb[i + shft] = latRho[jmin][i];
        }
        shft = 0 - jmin + imax - imin;
        for (int j = jmin; j <= (jmax - 1); j++) {
            xb[j + shft] = lonRho[j][imax];
            yb[j + shft] = latRho[j][imax];
        }
        shft = jmax - jmin + 2 * imax - imin;
        for (int i = imax; i >= (imin + 1); i--) {
            xb[shft - i] = lonRho[jmax][i];
            yb[shft - i] = latRho[jmax][i];
        }
        shft = 2 * jmax - jmin + 2 * (imax - imin);
        for (int j = jmax; j >= (jmin + 1); j--) {
            xb[shft - j] = lonRho[j][imin];
            yb[shft - j] = latRho[j][imin];
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

    /**
     * Sort OPA input files. First make sure that there is at least and only one
     * file matching the hgr, zgr and byte mask patterns. Then list the gridU,
     * gridV and gridT files.
     *
     * @param path
     * @throws java.io.IOException
     */
    private void sortInputFiles() throws IOException {

        String path = IOTools.resolvePath(getParameter("input_path"));
        File file = new File(path);

        file_mask = checkExistenceAndUnicity(file, getParameter("byte_mask_pattern"));
        file_hgr = checkExistenceAndUnicity(file, getParameter("hgr_pattern"));
        file_zgr = checkExistenceAndUnicity(file, getParameter("zgr_pattern"));

        isGridInfoInOneFile = (new File(file_mask).equals(new File(file_hgr)))
                && (new File(file_mask).equals(new File(file_zgr)));

    }

    private String checkExistenceAndUnicity(File file, String pattern) throws IOException {

        File[] listFiles = file.listFiles(new MetaFilenameFilter(pattern));
        int nbFiles = listFiles.length;

        if (nbFiles == 0) {
            throw new IOException("No file matching pattern " + pattern);
        } else if (nbFiles > 1) {
            throw new IOException("More than one file matching pattern " + pattern);
        }

        return listFiles[0].toString();
    }

    /*
     * Determines whether or not the x-y particle location is on edge of the
     * domain.
     *
     * @param x a double, the x-coordinate
     * @param y a double, the y-coordinate
     * @return <code>true</code> if the particle is on edge of the domain
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (get_nx() - 3.0f))
                || (pGrid[0] < 2.0f)
                || (pGrid[1] > (get_ny() - 3.0f))
                || (pGrid[1] < 2.0f));
    }


    /**
     * Gets the latitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the latitude [north degree] at (i, j) grid point.
     */
    @Override
    public double getLat(int i, int j) {
        return latRho[j][i];
    }

    /**
     * Gets the longitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the longitude [east degree] at (i, j) grid point.
     */
    @Override
    public double getLon(int i, int j) {
        return lonRho[j][i];
    }

    /**
     * Gets the bathymetry at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the bathymetry [meter] at (i, j) grid point if is in
     * water, return NaN otherwise.
     */
    @Override
    public double getBathy(int i, int j) {

        double bathy = 0.d;
        if (isInWater(i, j, get_nz() - 1)) {
            for (int k = 0; k < get_nz(); k++) {
                bathy += Double.isNaN(maskRho[k][j][i] * e3t[k][j][i])
                        ? 0.d
                        : maskRho[k][j][i] * e3t[k][j][i];
                //System.out.println("k: " + k + " " + maskRho[k][j][i] + " " + e3t[k][j][i] + " " + bathy);
            }
            return bathy;
        }
        return Double.NaN;
    }
    
    
    /*
    @Override
    public double xTore(double x) {
        return x;
    }

    @Override
    public double yTore(double y) {
        return y;
    }
    */
    
    /** Method to interpolate a U variable. 
     * On NEMO, U points are on the eastern face of the cell.
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
            double coy = 1 - Math.abs(jy - (j + jj));
            for (int ii = 0; ii < 1; ii++) {
                double cox = 1 - Math.abs(ix - (i - ii + 0.5));
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
    
    /** Interpolates in 3D current velocities */
    public double interpolate3dU(double[] pGrid, double[][][] variable) {

        double kz = pGrid[2];
        int k = (int) Math.floor(kz);
        
        double output = 0;
        double weight = 0;
        
        for (int kk = 0; kk < 1; kk++) {
            double coz = 1 - Math.abs(kz - (k + kk));
            output += this.interpolate2dU(pGrid, variable, k + kk) * coz;
            weight += coz;
        }
        
        if (weight != 0) {
            output /= weight;
        }

        return output;

    }
    
    
    /** Method to interpolate a V variable. 
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
        for (int kk = 0; kk < 1; kk++) {
            for (int jj = 0; jj < 1; jj++) {
                double coy = 1 - Math.abs(jy - (j - jj + 0.5));
                for (int ii = 0; ii < 1; ii++) {
                    double cox = 1 - Math.abs(ix - (i + ii));
                    double co = cox * coy;
                    output += variable[kIndex][i + ii][j - jj] * co;
                    weight += co;
                }
            }
        }

        if (weight != 0) {
            output /= weight;
        }

        return output;

    }
    
     /** Method to interpolate a V variable. 
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
            double coz = 1 - Math.abs(kz - (k + kk));
            output += this.interpolate2dV(pGrid, variable, k + kk) * coz;
            weight += coz;
        }
  
        if (weight != 0) {
            output /= weight;
        }

        return output;

    }
    
         
    /** Method to interpolate a T variable. 
     * On NEMO, T points are in the centerof the cell.
     * 
    */
    public double interpolate2dT(double[] pGrid, double[][][] variable, int kIndex) {
        
        double ix = pGrid[0];
        double jy = pGrid[1];
       
        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);
    
        double output = 0;
        double weight = 0;
       
        for (int kk = 0; kk < 1; kk++) {
            for (int jj = 0; jj < 1; jj++) {
                double coy = 1 - Math.abs(jy - (j + jj));
                for (int ii = 0; ii < 1; ii++) {
                    double cox = 1 - Math.abs(ix - (i + ii));
                    double co = cox * coy;
                    output += variable[kIndex][i + ii][j + jj] * co;
                    weight += co;
                }
            }
        }
        
        if(weight != 0) { 
            output /= weight;
        }
        
        return output;
        
    }
    
    /** Method to interpolate a T variable. 
     * On NEMO, T points are in the centerof the cell.
     * 
    */
    public double interpolate3dT(double[] pGrid, double[][][] variable) {
        
        double kz = pGrid[2];

        int k = (int) Math.floor(kz);
        
        double output = 0;
        double weight = 0;

        for (int kk = 0; kk < 1; kk++) {
            double coz = 1 - Math.abs(kz - (k + kk));
            output += this.interpolate2dT(pGrid, variable, k + kk) * coz;
            weight += coz;
        }
      
        
        if(weight != 0) { 
            output /= weight;
        }
        
        return output;
        
    }

}