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

import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

public class RomsGrid extends AbstractGrid {
    
    /**
     * Mask: water = 1, cost = 0
     */
    private byte[][] maskRho;
    
    private NetcdfFile ncIn;
    /**
     * Name of the Dimension in NetCDF file
     */
    private String strXiDim, strEtaDim, strTimeDim;

    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat, strMask, strBathy;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strPn, strPm;
    
    /**
     * Determines whether or not the turbulent diffusivity should be read in the
     * NetCDF file, function of the user's options.
     */
    String gridFile;
    
    public RomsGrid(String filename) {
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
        //openLocation(getParameter("input_path"));
        getDimNC();
        //shrinkGrid();
        //readConstantField(gridFile);
        getDimGeogArea();


    }
    
    void openDataset() throws IOException {
       ncIn = NetcdfDataset.openDataset(gridFile);
    }
    
    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws Exception {

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
        set_ipo(0);
        set_jpo(0);
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
        try {
            return (maskRho[j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public void loadParameters() {
        strXiDim = getParameter("field_dim_xi");
        strEtaDim = getParameter("field_dim_eta");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strMask = getParameter("field_var_mask");
        this.strPn = getParameter("field_var_pn");
        this.strPm = getParameter("field_var_pm");
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
        return ((pGrid[0] > (get_nx() - 2.0f))
        || (pGrid[0] < 1.0f)
        || (pGrid[1] > (get_ny() - 2.0f))
        || (pGrid[1] < 1.0f));
    }
    
    
    /** Interpolation of tracer field on T grid */
    public double interpolateT(double[] pGrid, double[][][] variable) { 
        
        double ix = pGrid[0];
        double jy = pGrid[1];
        double kz = pGrid[2];
        
        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);
        int k = (int) Math.floor(kz);
        
        double output = 0;
        double weight = 0;
       
        for (int kk = 0; kk < 1; kk++) {
            for (int jj = 0; jj < 1; jj++) {
                for (int ii = 0; ii < 1; ii++) {
                    double cox = Math.abs(ix - i - 1 - ii);
                    double coy = Math.abs(jy - j - 1 + jj);
                    double coz = Math.abs(kz - k - 1 + kk);
                    double co = cox * coy * coz;
                    output += variable[k + kk][i + ii][j + jj] * co;
                    weight += co;
                }
            }
        }
        
        if(weight != 0) { 
            output /= weight;
        }
        
        return output;
        
    }
    
    /** Interpolation of tracer field on T grid */
    public double interpolateU(double[] pGrid, double[][][] variable) { 
        
        double ix = pGrid[0];
        double jy = pGrid[1];
        double kz = pGrid[2];
        
        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);
        int k = (int) Math.floor(kz);
        
        double output = 0;
        double weight = 0;
        
        for (int kk = 0; kk < 1; kk++) {
            for (int jj = 0; jj < 1; jj++) {
                for (int ii = 0; ii < 1; ii++) {
                    double cox = Math.abs(ix - i - 0.5 + ii);
                    double coy = Math.abs(jy - j - 1 + jj);
                    double coz = Math.abs(kz - k - 1 + kk);
                    double co = cox * coy * coz;
                    output += variable[k + kk][i + ii][j + jj] * co;
                    weight += co;
                }
            }
        }
        
        if(weight != 0) { 
            output /= weight;
        }
        
        return output;
        
    }
    
     /** Interpolation of tracer field on T grid */
     public double interpolateV(double[] pGrid, double[][][] variable) { 
        
        double ix = pGrid[0];
        double jy = pGrid[1];
        double kz = pGrid[2];
        
        int i = (int) Math.floor(ix);
        int j = (int) Math.floor(jy);
        int k = (int) Math.floor(kz);
        
        double output = 0;
        double weight = 0;
        
        for (int kk = 0; kk < 1; kk++) {
            for (int jj = 0; jj < 1; jj++) {
                for (int ii = 0; ii < 1; ii++) {
                    double cox = Math.abs(ix - i - 1 + ii);
                    double coy = Math.abs(jy - j - 0.5 + jj);
                    double coz = Math.abs(kz - k - 1 + kk);
                    double co = cox * coy * coz;
                    output += variable[k + kk][i + ii][j + jj] * co;
                    weight += co;
                }
            }
        }
        
        if(weight != 0) { 
            output /= weight;
        }
        
        return output;
        
    }
    
}
