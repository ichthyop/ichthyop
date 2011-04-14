package ichthyop.core;

/** import netcdf data */
import ichthyop.io.Dataset;

/**
 * Both ROMS and MARS use an Arakawa C grid.
 * The Rho point represents a 2D or 3D point within the C grid. It is referenced
 * by two sets of coordinates:
 * <ul>
 * <li>grid coordinates (x, y, z) if 3D, (x, y) if 2D
 * <li>geographical coordinates (longitude, latitude) if 3D,
 * (longitude, latitude) if 2D
 * </ul>
 * The class provides methods to switch from grid coordinates (x, y, z)
 * to geographical coordinates (lon, lat, depth) and reciprocally.
 *
 * @author P.Verley
 */
public class RhoPoint {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /** Grid coordinate */
    private double x, y, z;

    /** Geographical coordinate */
    private double lon, lat, depth;

    /** <code>true</code> if 3 dimensions point false otherwise */
    private static boolean bln3D;
    
    /** Bouncy coastline */
    public static boolean FLAG_BOUNCY_COASTLINE;

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
        this.bln3D = bln3D;
    }

////////////////////////////
// Definition of the methods
////////////////////////////


    /**
     * Transforms geographical coordinates into grid coordinates.
     * (lon, lat, depth) ==> (x, y, z)
     *
     * @see ichthyop.io.Dataset#geo2Grid()
     */
    public void geog2Grid() {

        double[] pGrid = Dataset.geo2Grid(lon, lat);
        x = pGrid[0];
        y = pGrid[1];
        if (bln3D) {
            z = Dataset.depth2z(x, y, depth);
        }
    }

    /**
     * Transforms grid coordinates into geographical coordinates.
     * (x, y, z) ==> (lon, lat, depth)
     *
     * @see ichthyop.io.Dataset#grid2Geo()
     */
    public void grid2Geog() {
        double[] pGeog = bln3D ? Dataset.grid2Geo(x, y, z) :
                         Dataset.grid2Geo(x, y);
        lon = pGeog[1];
        lat = pGeog[0];
        if (bln3D) {
            depth = pGeog[2];
        }
    }

    /**
     * Checks wether the point is on the edge of the grid.
     *
     * @param nx an int, the grid length in the x direction
     * @param ny an int, the grid length in the y direction
     * @return a boolean, <code>true</code> if the point is on edge
     */
    public boolean isOnEdge(int nx, int ny) {
        return ((x > (nx - 2.0f)) ||
                (x < 1.0f) ||
                (y > (ny - 2.0f)) ||
                (y < 1.0f));
    }

    private double[] bounceCostline(double x, double y, double dx, double dy) {
        return bounceCostline(x, y, dx, dy, 0);
    }

    private double[] bounceCostline(double x, double y, double dx, double dy, int iter) {

        double newdx = dx;
        double newdy = dy;
        iter += 1;
        if (!Dataset.isInWater(new double[]{x + dx, y + dy})) {
            double s = x;
            double ds = dx;
            double signum = 1.d;
            double ys = y;
            boolean bounceMeridional = false;
            boolean bounceZonal = false;
            int n = 0;
            /* Iterative process to estimate the point of impact with the
             * costline.
             */
            while (n < 1000 && !(bounceMeridional | bounceZonal)) {
                ds *= 0.5d;
                s = s + signum * ds;
                ys = dy / dx * (s - x) + y;
                signum = (Dataset.isInWater(new double[]{s, ys}))
                        ? 1.d
                        : -1.d;
                bounceMeridional = Math.abs(Math.round(s + 0.5d) - (s + 0.5d)) < 1e-8;
                bounceZonal = Math.abs(Math.round(ys + 0.5d) - (ys + 0.5d)) < 1e-8;
                n++;
            }
            /* Compute dx1 such as dx = dx1 + dx2, dx1 in water, dx2 on land
             * or dy1 such as dy = dy1 + dy2, dy1 in water, dy2 on land
             */
            double dx1 = (Math.round(x) + Math.signum(dx) * 0.5d - x);
            double dy1 = (Math.round(y) + Math.signum(dy) * 0.5d - y);
            if (bounceMeridional & bounceZonal) {
                /* case1 : particle hits the cost on a corner */
                newdx = 2.d * dx1 - dx;
                newdy = 2.d * dy1 - dy;
            } else if (bounceMeridional) {
                /* case2: particle hits the meridional cost */
                newdx = 2.d * dx1 - dx;
                newdy = dy;
            } else if (bounceZonal) {
                /* case3: particle hits the zonal cost */
                newdy = 2.d * dy1 - dy;
                newdx = dx;
            }
            /* Ensure the new point is in water and repeat the process otherwise */
            if (!Dataset.isInWater(new double[]{x + newdx, y + newdy})) {
                if (iter < 10) {
                    return bounceCostline(x, y, newdx, newdy, iter);
                }
            }
        }
        return new double[]{newdx, newdy};
    }

    /**
     * Increments (x, y, z) with (dx, dy, dz) if 3 dimensions move.
     * Increments (x, y) with (dx, dy) if 2 dimensions move.
     *
     * @param move a double[] array {dx, dy, dz} or {dx, dy}
     */
    public void increment(double[] move) {

        if (FLAG_BOUNCY_COASTLINE) {
            double[] bounce = bounceCostline(x, y, move[0], move[1]);
            x += bounce[0];
            y += bounce[1];

        } else {
            x += move[0];
            y += move[1];
        }
        if (move.length > 2) {
            z += move[2];
            z = Math.max(0.d, Math.min(z, (double) Dataset.get_nz() - 1.00001f));
        }
    }

//////////
// Setters
//////////

    /**
     * Sets the horizontal coordinates.
     *
     * @param x a double, x coordinate of the grid point
     * @param y a double, y coordinate of the grid point
     */
    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the vertical coordinate
     * @param z a double, z coordinate of the grid point
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Sets the depth
     * @param depth a double, depth of the geographical point [meter]
     */
    public void setDepth(double depth) {
        this.depth = depth;
    }

    /**
     * Sets the horizontal grid coordinates, the depth and then transforms the
     * depth into the z vertical grid coordinate.
     *
     * @param x a double, x coordinate of the grid point
     * @param y a double, y coordinate of the grid point
     * @param depth a double, depth of the geographical point [meter]
     */
    public void setXYD(double x, double y, double depth) {

        this.x = x;
        this.y = y;
        this.z = bln3D
                 ? Math.min(Dataset.depth2z(x, y, depth), Dataset.get_nz() - 1)
                 : 0;
    }

    /**
     * Sets the geographical coordinates.
     *
     * @param lon a double, longitude of the point [degree East]
     * @param lat a double, latitude of the point [degree North]
     * @param depth a double, depth of the point [meter]
     */
    public void setLLD(double lon, double lat, double depth) {

        this.lon = lon;
        this.lat = lat;
        this.depth = depth;
    }

//////////
// Getters
//////////

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
     * Gets the grid coordinates
     *
     * @return double[] {x, y, z} for 3D point and {x, y} for 2D point
     */
    public double[] getPGrid() {
        return bln3D ? new double[] {x, y, z}
                : new double[] {x, y};
    }

    //----------- End of class
}
