package org.previmer.ichthyop;

import org.previmer.ichthyop.arch.IRhoPoint;

/**
 * Both ROMS and MARS use an Arakawa C grid.
 * The Rho point represents a 2D or 3D point within the C grid. It is referenced
 * by two sets of coordinates:
 * <ul>
 * <li>grid coordinates (x, y, z) if 3D, (x, y) if 2D
 * <li>geographical coordinates (longitude, latitude, depth) if 3D,
 * (longitude, latitude) if 2D
 * </ul>
 * The class provides methods to switch from grid coordinates (x, y, z)
 * to geographical coordinates (lon, lat, depth) and reciprocally.
 *
 * @author P.Verley
 */
public class RhoPoint extends SimulationManagerAccessor implements IRhoPoint {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /** Grid coordinate */
    private double x, y, z;
    /** Geographical coordinate */
    private double lon, lat, depth;
    /** <code>true</code> if 3 dimensions point false otherwise */
    private static boolean is3D;
    private boolean lonlatHaveChanged,  depthHasChanged;
    private boolean xyHaveChanged, zHasChanged;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new 2D or 3D point.
     *
     * @param bln3D a boolean, <code>true</code> if 3 dimensions point,
     * false otherwise
     */
    public RhoPoint(boolean bln3D) {
        is3D = bln3D;
        lonlatHaveChanged = false;
        depthHasChanged = false;
        xyHaveChanged = false;
        zHasChanged = false;
    }

    public RhoPoint() {
        this(true);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Transforms geographical coordinates into grid coordinates.
     * (lon, lat, depth) ==> (x, y, z)
     */
    public void geo2Grid() {

        if (lonlatHaveChanged) {
            double[] pGrid = getSimulationManager().getDataset().lonlat2xy(lon, lat);
            x = pGrid[0];
            y = pGrid[1];
            lonlatHaveChanged = false;
        }
        if (is3D && depthHasChanged) {
            z = getSimulationManager().getDataset().depth2z(x, y, depth);
            depthHasChanged = false;
        }
    }

    /**
     * Transforms grid coordinates into geographical coordinates.
     * (x, y, z) ==> (lon, lat, depth)
     *
     * @see ichthyop.io.Dataset#grid2Geo()
     */
    public void grid2Geo() {

        if (xyHaveChanged) {
            double[] pGeog = getSimulationManager().getDataset().xy2lonlat(x, y);
            lon = pGeog[1];
            lat = pGeog[0];
            xyHaveChanged = false;
        }
        if (is3D && zHasChanged) {
            depth = getSimulationManager().getDataset().z2depth(x, y, z);
            zHasChanged = false;
        }
    }

    /**
     * Increments (x, y, z) with (dx, dy, dz) if 3 dimensions move.
     * Increments (x, y) with (dx, dy) if 2 dimensions move.
     *
     * @param move a double[] array {dx, dy, dz} or {dx, dy}
     */
    public void increment(double[] move) {

        x += move[0];
        y += move[1];
        xyHaveChanged = true;
        if (move.length > 2) {
            z += move[2];
            z = Math.max(0.d, Math.min(z, (double) getSimulationManager().getDataset().get_nz() - 1.00001f));
            zHasChanged = true;
        }
    }

//////////
// Setters
//////////
    public void make2D() {
        is3D = false;
    }
//////////
// Getters
//////////

    public boolean is3D() {
        return is3D;
    }

    /**
     * Gets x coordinate
     * @return double x, the x coordinate of the grid point
     */
    public double getX() {
        return x;
    }

    /**
     * Gets y coordinate
     * @return double y, the y coordinate of the grid point
     */
    public double getY() {
        return y;
    }

    /**
     * Gets z coordinate
     * @return double z, the z coordinate of the grid point
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets longitude
     * @return double lon, the longitude of the point [degree East]
     */
    public double getLon() {
        return lon;
    }

    /**
     * Gets latitude
     * @return double lat, the latitude of the point [degree North]
     */
    public double getLat() {
        return lat;
    }

    /**
     * Gets depth
     * @return double depth, the depth of the point [meter]
     */
    public double getDepth() {
        return depth;
    }

    /**
     * Sets the depth
     * @param depth a double, depth of the geographical point [meter]
     */
    public void setDepth(double depth) {
        if (this.depth != depth) {
            this.depth = depth;
            depthHasChanged = true;
        }
    }

    public void setX(double x) {
        if (this.x != x) {
            this.x = x;
            xyHaveChanged = true;
        }
    }

    public void setY(double y) {
        if (this.y != y) {
            this.y = y;
            xyHaveChanged = true;
        }
    }

    /**
     * Sets the vertical coordinate
     * @param z a double, z coordinate of the grid point
     */
    public void setZ(double z) {
        if (this.z != z) {
            this.z = z;
            zHasChanged = true;
        }
    }

    public double[] getGridPoint() {
        return is3D
                ? new double[]{x, y, z}
                : new double[]{x, y};
    }

    public void setLon(double lon) {
        if (this.lon != lon) {
            this.lon = lon;
            lonlatHaveChanged = true;
        }
    }

    public void setLat(double lat) {
        if (this.lat != lat) {
            this.lat = lat;
            lonlatHaveChanged = true;
        }
    }

    public double[] getGeoPoint() {
        return is3D
                ? new double[]{lon, lat, depth}
                : new double[]{lon, lat};
    }

    @Override
    public Object clone() {

        RhoPoint point = new RhoPoint(is3D);
        point.setX(x);
        point.setY(y);
        point.setLon(lon);
        point.setLat(lat);
        if (is3D) {
            point.setZ(z);
            point.setDepth(depth);
        }
        return point;
    }
    //----------- End of class

    @Override
    public String toString() {
        return is3D
                ? "(" + lon + ", " + lat + ", " + depth + ")"
                : "(" + lon + ", " + lat + ")";
    }

    public boolean isInWater() {
        return getSimulationManager().getDataset().isInWater(getGridPoint());
    }

    public boolean isOnEdge() {
        return getSimulationManager().getDataset().isOnEdge(getGridPoint());
    }
}
