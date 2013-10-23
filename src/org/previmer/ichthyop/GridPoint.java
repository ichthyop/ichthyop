package org.previmer.ichthyop;

/**
 * Both ROMS and MARS use an Arakawa C grid. The Rho point represents a 2D or 3D
 * point within the C grid. It is referenced by two sets of coordinates:
 * <ul>
 * <li>grid coordinates (x, y, z) if 3D, (x, y) if 2D
 * <li>geographical coordinates (longitude, latitude, depth) if 3D, (longitude,
 * latitude) if 2D
 * </ul>
 * The class provides methods to switch from grid coordinates (x, y, z) to
 * geographical coordinates (lon, lat, depth) and reciprocally.
 *
 * @author P.Verley
 */
public class GridPoint extends SimulationManagerAccessor {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Grid coordinate
     */
    private double x, y, z;
    private double dx, dy, dz;
    /**
     * Geographical coordinate
     */
    private double lat, lon, depth;
    /**
     * <code>true</code> if 3 dimensions point false otherwise
     */
    private static boolean is3D;
    private boolean latlonHaveChanged, depthHasChanged;
    private boolean xyHaveChanged, zHasChanged;
    private boolean exclusivityH, exclusivityV;
    private static int nz;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new 2D or 3D point.
     *
     * @param bln3D a boolean, <code>true</code> if 3 dimensions point, false
     * otherwise
     */
    public GridPoint(boolean bln3D) {
        is3D = bln3D;
        latlonHaveChanged = false;
        depthHasChanged = false;
        xyHaveChanged = false;
        zHasChanged = false;
        dx = dy = dz = 0.d;
        x = y = z = -1;
        lon = lat = depth = Double.NaN;
        try {
            nz = getSimulationManager().getDataset().get_nz();
        } catch (Exception ex) {
            nz = -1;
        }
    }

    public GridPoint() {
        this(true);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Transforms geographical coordinates into grid coordinates. (lon, lat,
     * depth) ==> (x, y, z)
     */
    public void geo2Grid() {

        if (latlonHaveChanged) {
            double[] pGrid = getSimulationManager().getDataset().latlon2xy(lat, lon);
            x = pGrid[0];
            y = pGrid[1];
            latlonHaveChanged = false;
        }
        if (is3D && depthHasChanged) {
            z = getSimulationManager().getDataset().depth2z(x, y, depth);
            depthHasChanged = false;
        }
    }

    /**
     * Transforms grid coordinates into geographical coordinates. (x, y, z) ==>
     * (lon, lat, depth)
     *
     * @see ichthyop.io.Dataset#grid2Geo()
     */
    public void grid2Geo() {

        if (xyHaveChanged) {
            double[] pGeog = getSimulationManager().getDataset().xy2latlon(x, y);
            lat = pGeog[0];
            lon = pGeog[1];
            xyHaveChanged = false;
        }
        if (is3D && zHasChanged) {
            depth = getSimulationManager().getDataset().z2depth(x, y, z);
            zHasChanged = false;
        }
    }

    public void applyMove() {

        setX(x + dx);
        dx = 0.d;
        setY(y + dy);
        dy = 0.d;
        if (is3D) {
            setZ(z + dz);
            dz = 0.d;
        }
        exclusivityH = false;
        exclusivityV = false;
    }

    public double[] getMove() {
        return is3D
                ? new double[]{dx, dy, dz}
                : new double[]{dx, dy};
    }

    /**
     * Increments (x, y, z) with (dx, dy, dz) if 3 dimensions move. Increments
     * (x, y) with (dx, dy) if 2 dimensions move.
     *
     * @param move a double[] array {dx, dy, dz} or {dx, dy}
     */
    public void increment(double[] move) {
        increment(move, false, false);
    }

    public void increment(double[] move, boolean exclusivityH, boolean exclusivityV) {

        if (this.exclusivityH & exclusivityH) {
            throw new UnsupportedOperationException("Two actions are requesting exclusivity on horizontal transport");
        }
        if (!this.exclusivityH) {
            if (exclusivityH) {
                dx = move[0];
                dy = move[1];
                this.exclusivityH = true;
            } else {
                dx += move[0];
                dy += move[1];
            }
        }

        if (this.exclusivityV & exclusivityV) {
            throw new UnsupportedOperationException("Two actions are requesting exclusivity on vertical transport");
        }
        if (!this.exclusivityV) {
            if (move.length > 2) {
                if (exclusivityV) {
                    dz = move[2];
                    this.exclusivityV = true;
                } else {
                    dz += move[2];
                }
            }
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
     *
     * @return double x, the x coordinate of the grid point
     */
    public double getX() {
        return x;
    }

    /**
     * Gets y coordinate
     *
     * @return double y, the y coordinate of the grid point
     */
    public double getY() {
        return y;
    }

    /**
     * Gets z coordinate
     *
     * @return double z, the z coordinate of the grid point
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets longitude
     *
     * @return double lon, the longitude of the point [degree East]
     */
    public double getLon() {
        return lon;
    }

    /**
     * Gets latitude
     *
     * @return double lat, the latitude of the point [degree North]
     */
    public double getLat() {
        return lat;
    }

    /**
     * Gets depth
     *
     * @return double depth, the depth of the point [meter]
     */
    public double getDepth() {
        return depth;
    }

    /**
     * Sets the depth
     *
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
            if (x < -0.5d) {
                this.x = getSimulationManager().getDataset().get_nx() + x;
            }
            if (x > getSimulationManager().getDataset().get_nx() - 0.5d) {
                this.x = x - getSimulationManager().getDataset().get_nx();
            }
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
     *
     * @param z a double, z coordinate of the grid point
     */
    public void setZ(double z) {
        if (this.z != z) {
            this.z = bound(z);
            zHasChanged = true;
        }
    }

    private double bound(double z) {

        return Math.max(Math.min(nz - 1, z), 0.f);
    }

    public double[] getGridCoordinates() {
        return is3D
                ? new double[]{x, y, z}
                : new double[]{x, y};
    }

    public void setLon(double lon) {
        if (this.lon != lon) {
            this.lon = lon;
            latlonHaveChanged = true;
        }
    }

    public void setLat(double lat) {
        if (this.lat != lat) {
            this.lat = lat;
            latlonHaveChanged = true;
        }
    }

    @Override
    public Object clone() {

        GridPoint point = new GridPoint(is3D);
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
                ? "(" + lat + ", " + lon + ", " + depth + ")"
                : "(" + lat + ", " + lon + ")";
    }

    public boolean isInWater() {
        return getSimulationManager().getDataset().isInWater(getGridCoordinates());
    }

    public boolean isOnEdge() {
        return getSimulationManager().getDataset().isOnEdge(getGridCoordinates());
    }
}
