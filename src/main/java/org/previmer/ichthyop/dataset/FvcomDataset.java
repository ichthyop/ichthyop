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
    private String xVarName;
    private String yVarName;
    private String lonVarName;
    private String latVarName;
    private String strTime;
    private String strTimeDim;
    private String strA1U, strA2U;
    private String strAW0, strAWX, strAWY;

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
    private float[] lonNodes;

    /** Latitude of the nodes */
    private float[] latNodes;

    /** X coordinates of the nodes */
    private float[] xNodes;

    /** Y coordinates of the nodes */
    private float[] yNodes;

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
        getDimNC();
        readConstantField();

    }

    @Override
    public double[] latlon2xy(double lat, double lon) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double[] xy2latlon(double xRho, double yRho) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return false;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean is3D() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double xTore(double x) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double yTore(double y) {
        // TODO Auto-generated method stub
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
        xVarName = getParameter("field_var_x");
        yVarName = getParameter("field_var_y");
        lonVarName = getParameter("field_var_lon");
        latVarName = getParameter("field_var_lat");
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

    void readConstantField() throws IOException {

        // Convert the variable of node index from float to int.
        float[][] temp = this.read_variable(strNodes, 3);
        this.triangleNodes = new int[nTriangles][3];
        for (int i = 0; i < nTriangles; i++) {
            for (int j = 0; j < 3; j++) {
                this.triangleNodes[i][j] = (int) temp[i][j];
            }
        }

        a1u = this.read_variable(this.strA1U, 3);
        a2u = this.read_variable(this.strA2U, 3);
        aw0 = this.read_variable(this.strAW0, 4);
        awx = this.read_variable(this.strAWY, 4);
        awy = this.read_variable(this.strAWX, 4);

    }

    void findNeighbouringTriangles() {

        this.neighbouringTriangles = new int[nTriangles][];

        for (int iele = 0; iele < nTriangles; iele++) {

            // get the nodes of the target triangle
            int[] target = this.triangleNodes[iele];

            // Init the list of neighbouring triangles
            List<Integer> tempOutput = new ArrayList<>();

            // Loop over all the triangles to find the neighbours
            for (int i = 0; i < nTriangles; i++) {

                // discard the target triangle
                if (i == iele) {
                    continue;
                }

                // get the nodes of the loop triangle
                int[] temp = this.triangleNodes[i];

                // init number of common nodes.
                int cpt = 0;

                // loop the nodes of the loop triangle
                for (int j = 0; j < 3; j++) {
                    int p1 = temp[j];
                    // loop over the nodes of the target triangle
                    for (int k = 0; k < 3; k++) {
                        int n1 = target[k];
                        if (n1 == p1) {
                            // if node1 = node2: iterate
                            cpt += 1;
                        }
                    }
                }

                // neighbouring triangles are triangles for which cpt=2, i.e
                // two nodes (one edge) are shared.
                // in this case, we add the i index in the temporary list
                if (cpt == 2) {
                    tempOutput.add(i);
                }
            } // end of inner triangle list

            //
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
    private int findTriangle(IParticle particle) throws Exception {

        int i = -1;
        float[] xpol = new float[3];
        float[] ypol = new float[3];
        float x = (float) particle.getX();
        float y = (float) particle.getY();

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

        if (i == -1) {
            Exception ex = new Exception("Particle triangle cannot be determined");
            throw ex;
        }

        return i;

    }

    /**
     * Determines whether the specified geographical point (lon, lat) belongs to the
     * is inside the polygon defined by (imin, jmin) & (imin, jmax) & (imax, jmax) &
     * (imax, jmin).
     *
     * <p>
     * The algorithm has been adapted from a function in ROMS/UCLA code, originally
     * written by Alexander F. Shchepetkin and Hernan G. Arango. Please find below
     * an extract of the ROMS/UCLA documention.
     * </p>
     * 
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
     * @param imin
     *            an int, i-coordinate of the area left corners
     * @param imax
     *            an int, i-coordinate of the area right corners
     * @param jmin
     *            an int, j-coordinate of the area left corners
     * @param jmax
     *            an int, j-coordinate of the area right corners
     * @param lon
     *            a double, the longitude of the geographical point
     * @param lat
     *            a double, the latitude of the geographical point
     * @return <code>true</code> if (lon, lat) belongs to the polygon,
     *         <code>false</code>otherwise.
     */
    private boolean isInsidePolygone(float[] xpol, float ypol[], float x, float y) {

        // --------------------------------------------------------------
        // Return true if (lon, lat) is insidide the polygon defined by
        // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)
        // -----------------------------------------
        // Build the polygone
        boolean isInPolygone = true;

        // Close the polygon
        float[] xpol4 = new float[4];
        for (int i = 0; i < 3; i++) {
            xpol4[i] = xpol[i];
        }
        xpol4[3] = xpol[0];

        float[] ypol4 = new float[4];
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

}