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

package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.functors.NullIsTruePredicate;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.manager.TimeManager;
import org.previmer.ichthyop.particle.IParticle;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.units.RaiseException;

/**
 *
 * @author pverley
 */
public class FvcomDataset extends AbstractDataset {

    /**
     * NetCDF file object
     */
    NetcdfFile ncIn;

    /** List of input files */
    private List<String> files;

    /** Index of the file to process. */
    private int index;

    /**
     * Time arrow, 1 forward, -1 backward
     */
    int timeArrow;

    private String strEleDim;
    private String strNodesDim;
    private String strNodes;
    private String strXVarName;
    private String strYVarName;
    private String strLonVarName;
    private String strLatVarName;
    private String strTime;
    private String strTimeDim;
    private String strA1U, strA2U;
    private String strAW0, strAWX, strAWY;
    private int nLayer;

    int nbTimeRecords;
    int rank;
    double time_tp0;
    double time_tp1;

    /** Number of nodes */
    private int nNodes;

    /** Number of elements (i.e. triangles) */
    private int nTriangles;

    /** Index of the triangle nodes. Dimension = (nTriangles, 3) */
    private int[][] triangleNodes;

    /** Index of the neighbouring neighbours */
    private int[][] neighbouringTriangles;

    /** Longitude of the nodes */
    private double[] lonNodes;

    /** Latitude of the nodes */
    private double[] latNodes;

    /** X coordinates of the nodes */
    private double[] xNodes;

    /** Y coordinates of the nodes */
    private double[] yNodes;
    
    /**
     * Scale factors used for interpolation of tracer/velocities. Dimensions are
     * [nEle, 4 or 3] depending on the variables. They are transposed compared to
     * the way they are stored in the NetCDF.
     */
    private float[][] a1u, a2u, aw0, awx, awy;

    /*
     * Sets up the {@code Dataset}. The method first sets the appropriate variable
     * names, loads the first NetCDF dataset and extract the time non-dependant
     * information, such as grid dimensions, geographical boundaries, depth at sigma
     * levels.
     *
     * @throws an IOException if an error occurs while setting up the {@code
     * Dataset}
     */
    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        openDataset();
        getDimNC();
        readConstantField();
        findNeighbouringTriangles();
        System.exit(0);
        
    }

    @Override
    public double[] latlon2xy(double lat, double lon) {
        return new double[] {lon, lat};
    }

    @Override
    public double[] xy2latlon(double xRho, double yRho) {
        return new double[] {yRho, xRho};
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
    public double get_dUx(double[] pGrid, double time) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        return (this.findTriangle(pGrid) >= 0);
    }

    @Override
    public boolean isInWater(int i, int j) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCloseToCost(double[] pGrid) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnEdge(double[] pGrid) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getBathy(int i, int j) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int get_nx() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int get_ny() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int get_nz() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getdxi(int j, int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getdeta(int j, int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public double getLatMin() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLatMax() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLonMin() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLonMax() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLon(int igrid, int jgrid) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLat(int igrid, int jgrid) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getDepthMax() {
        return 0;
    }

    @Override
    public boolean is3D() {
        return false;
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double xTore(double x) {
        return 0;
    }

    @Override
    public double yTore(double y) {
        return 0;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    void loadParameters() {
        strEleDim = getParameter("field_dim_elements");
        strNodesDim = getParameter("field_dim_nodes");
        strTimeDim = getParameter("field_dim_time");
        strTime = getParameter("field_var_time");
        
        strXVarName = getParameter("field_var_x");
        strYVarName = getParameter("field_var_y");
        strLonVarName = getParameter("field_var_lon");
        strLatVarName = getParameter("field_var_lat");
        
        strA1U = getParameter("field_var_a1u");
        strA2U = getParameter("field_var_a2u");      
        strAW0 = getParameter("field_var_aw0");
        strAWX = getParameter("field_var_awx");
        strAWY = getParameter("field_var_awy");
        
        strNodes = getParameter("field_var_nodes");

        timeArrow = timeArrow();

    }

    int timeArrow() {
        return getSimulationManager().getParameterManager().getParameter("app.time", "time_arrow")
                .equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 : -1;
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an
     *             IOException if an error occurs while reading the dimensions.
     *
     *             pverley pour chourdin: Pour ROMS ou MARS je lisais les dimensions
     *             à partir de la variable ncIn qui est le premier fichier sortie
     *             qui me tombe sous la main. Avec OPA, les dimensions se lisent
     *             dans un fichier particulier *byte*mask*. A déterminer si toujours
     *             vrai ?
     */
    private void getDimNC() throws IOException {

        try {
            this.nTriangles = ncIn.findDimension(this.strEleDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset nele dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            this.nNodes = ncIn.findDimension(this.strNodesDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Y dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(files, t0, timeArrow, strTime);
        ncIn = DatasetUtil.openFile(files.get(index), true);
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow);
        time_tp1 = t0;
    }

    void openDataset() throws Exception {
        files = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(files, strTime, timeArrow());
        }
        ncIn = DatasetUtil.openFile(files.get(0), true);
        readTimeLength();
    }

    void readTimeLength() throws IOException {
        try {
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read dataset time dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    /**
     * Read a scale factor
     */
    float[][] read_variable(String varname, int n) throws IOException {

        Index index;
        Array variable = ncIn.findVariable(varname).read();
        index = variable.getIndex();
        float[][] array = new float[this.nTriangles][];
        for (int i = 0; i < nTriangles; i++) {
            array[i] = new float[n];
            for (int j = 0; j < n; j++) {
                index.set(j, i);
                array[i][j] = variable.getFloat(index);
            }
        }

        return array;

    }
    
    /**
     * Read a scale factor
     */
    double[] read_coordinates(String varname) throws IOException {

        Index index;
        Array variable = ncIn.findVariable(varname).read();
        index = variable.getIndex();
        double[] array = new double[this.nNodes];
        for (int i = 0; i < nNodes; i++) {
            index.set(i);
            array[i] = variable.getDouble(index);
        }

        return array;

    }
    
    void readConstantField() throws IOException {

        // Convert the variable of node index from float to int.
        float[][] temp = this.read_variable(strNodes, 3);
        this.triangleNodes = new int[nTriangles][3];
        for (int i = 0; i < nTriangles; i++) {
            for (int j = 0; j < 3; j++) {
                this.triangleNodes[i][j] = (int) temp[i][j];
            }
        }

        a1u = this.read_variable(this.strA1U, 4);
        a2u = this.read_variable(this.strA2U, 4);
        aw0 = this.read_variable(this.strAW0, 3);
        awx = this.read_variable(this.strAWY, 3);
        awy = this.read_variable(this.strAWX, 3);
        
        xNodes = this.read_coordinates(this.strXVarName);
        yNodes = this.read_coordinates(this.strYVarName);

    }

    void findNeighbouringTriangles() {

        this.neighbouringTriangles = new int[nTriangles][];

        for (int iele = 0; iele < nTriangles; iele++) {

            // get the nodes of the target triangle
            int[] target = this.triangleNodes[iele];

            // Init the list of neighbouring triangles
            List<Integer> tempOutput = new ArrayList<>();
            
            // Loop over the three edges, jeeping the order (clockwise)
            for (int k = 0; k < 3; k++) {

                // coordinates of the nodes
                int x1 = target[k];
                int x2 = target[(k + 1) % 3];

                // Loop over all the triangles to find the neighbours
                for (int i = 0; i < nTriangles; i++) {

                    // discard the target triangle
                    if (i == iele) {
                        continue;
                    }

                    // get the nodes of the loop triangle
                    int[] temp = this.triangleNodes[i];

                    for (int p = 0; p < 3; p++) {

                        int p1 = temp[p];
                        int p2 = temp[(p + 1) % 3];

                        if ((p1 == x1) & (p2 == x2)) {
                            tempOutput.add(i);
                            break;
                        }
                        
                        if ((p1 == x2) & (p2 == x1)) {
                            tempOutput.add(i);
                            break;
                        }
                    }  // end of loop over test edges
                }  // end of loop over triangles
            }  // end of loop over target edges

            int nNeighbour = tempOutput.size();
            neighbouringTriangles[iele] = new int[nNeighbour];
            for (int p = 0; p < nNeighbour; p++) {
                neighbouringTriangles[iele][p] = tempOutput.get(p);
            }

        } // end of loop over target triangles
    } // end of method
    
    /**
     * Return the index of the triangle containing the given particle
     * 
     * @throws Exception
     */
    private int findTriangle(double[] pGrid) {
           
        int i = -999;
        double[] xpol = new double[3];
        double[] ypol = new double[3];
        double x = pGrid[0];
        double y = pGrid[1];

        for (i = 0; i < nTriangles; i++) {

            // get the index of the nodes for the given triangle
            int[] nodes = this.triangleNodes[i];

            // get the coordinates of the polygon
            for (int k = 0; k < 3; k++) {
                xpol[k] = xNodes[nodes[k]];
                ypol[k] = yNodes[nodes[k]];
            }

            if (isInsidePolygone(xpol, ypol, x, y)) {
                return i;
            }
        }

        return -999;

    }

    /**
     * Determines whether the specified geographical point (lon, lat) belongs to the
     * is inside the polygon defined by (imin, jmin) & (imin, jmax) & (imax, jmax) &
     * (imax, jmin). 
     * 
     * Detail description here: http://forge.ipsl.jussieu.fr/roms_locean/browser/Roms_tools/Roms_Agrif/init_floats.F?rev=2
     */
    private boolean isInsidePolygone(double[] xpol, double ypol[], double x, double y) {

        // --------------------------------------------------------------
        // Return true if (lon, lat) is insidide the polygon defined by
        // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)
        // -----------------------------------------
        // Build the polygone
        boolean isInPolygone = true;

        // Close the polygon
        double[] xpol4 = new double[4];
        for (int i = 0; i < 3; i++) {
            xpol4[i] = xpol[i];
        }
        xpol4[3] = xpol[0];

        double[] ypol4 = new double[4];
        for (int i = 0; i < 3; i++) {
            ypol4[i] = ypol[i];
        }
        ypol4[3] = ypol[0];

        // ---------------------------------------------
        // Check if {lon, lat} is inside polygone
        int inc, crossings;
        double dx1, dx2, dxy;
        crossings = 0;
        int nb = 4;

        for (int k = 0; k < nb - 1; k++) {
            if (xpol4[k] != xpol4[k + 1]) {
                dx1 = x - xpol4[k]; // x0 - x1
                dx2 = xpol4[k + 1] - x; // x2 - x0
                dxy = dx2 * (y - ypol4[k]) + dx1 * (y - ypol4[k + 1]);
                // (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) >= 0, if x2-x1 > 0
                inc = 0;
                if ((xpol4[k] == x) & (ypol4[k] == y)) {
                    return (true);
                } else if (((dx1 == 0.) & (y >= ypol4[k])) | ((dx2 == 0.) & (y >= ypol4[k + 1]))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) & ((xpol4[k + 1] - xpol4[k]) * dxy >= 0.)) {
                    // the first condition checks that x0 is between x1 and x2
                    inc = 2;
                }
                if (xpol4[k + 1] > xpol4[k]) {
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
    
    
    private double[][] compute_du_dx(double[][] u) {
        
        double[][] du_dx = new double[this.nTriangles][this.nLayer];
        for(int i =0; i < nTriangles; i++) {
            for(int l = 0; l < this.nLayer; l++) {
                du_dx[i][l] = a1u[i][0] * u[i][l];
                
                // TODO Verify that the neighbours index are consistent.
                // i.e
                int cpt = 0;
                for(int n : this.neighbouringTriangles[i]) { 
                    du_dx[i][l] += a1u[i][cpt] * u[n][l];
                    cpt += 1;
                }
                
            }   
        }        
        
        return du_dx;
        
        
        
    }
        

}