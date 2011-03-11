package org.previmer.ichthyop;

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
public class GridPoint extends SimulationManagerAccessor {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /** Grid coordinate */
    private double x, y, z;
    private double dx, dy, dz;
    /** Geographical coordinate */
    private double lon, lat, depth;
    /** <code>true</code> if 3 dimensions point false otherwise */
    private static boolean is3D;
    private boolean lonlatHaveChanged, depthHasChanged;
    private boolean xyHaveChanged, zHasChanged;
    private boolean exclusivityH, exclusivityV;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new 2D or 3D point.
     *
     * @param bln3D a boolean, <code>true</code> if 3 dimensions point,
     * false otherwise
     */
    public GridPoint(boolean bln3D) {
        is3D = bln3D;
        lonlatHaveChanged = false;
        depthHasChanged = false;
        xyHaveChanged = false;
        zHasChanged = false;
        dx = dy = dz = 0.d;
        x = y = z = -1;
        lon = lat = depth = Double.NaN;
    }

    public GridPoint() {
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

    private double[] bounceCostline(double x, double y, double dx, double dy) {
        return bounceCostline(x, y, dx, dy, 0);
    }

    private double[] bounceCostline(double x, double y, double dx, double dy, int iter) {

        double newdx = dx;
        double newdy = dy;
        iter += 1;
        if (!getSimulationManager().getDataset().isInWater(new double[]{x + dx, y + dy})) {
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
                signum = (getSimulationManager().getDataset().isInWater(new double[]{s, ys}))
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
            if (!getSimulationManager().getDataset().isInWater(new double[]{x + newdx, y + newdy})) {
                if (iter < 10) {
                    return bounceCostline(x, y, newdx, newdy, iter);
                }
            }

            /*if ((Math.abs(dx) > Math.abs(dx1)) && (Math.abs(dy) > Math.abs(dy1))) {
                boolean bumpX = getSimulationManager().getDataset().isInWater(new double[]{x + dx1, y + dx1 / dx * dy});
                boolean bumpY = getSimulationManager().getDataset().isInWater(new double[]{x + dy1 / dy * dx, y + dy1});
                System.out.println(bumpX + " " + bumpY);
                if (Math.abs(dx1) > Math.abs(dy1)) {
                    //dx1 = dy1 / dy * dx;
                    //System.out.println("x: " + (float) (x + dx1) + " y: " + (y + dy1));
                    System.out.println("bounce 1.1");
                    newdy = 2.d * dy1 - dy;
                    newdx = dx;
                } else {
                    //dy1 = dx1 / dx * dy;
                    //System.out.println("x: " + (x + dx1) + " y: " + (float) (y + dy1));
                    System.out.println("bounce 1.2");
                    newdx = 2.d * dx1 - dx;
                    newdy = dy;
                }
            } else if ((Math.abs(dx) > Math.abs(dx1))) {
                System.out.println("bounce 2");
                newdx = 2.d * dx1 - dx;
                newdy = dy;
            } else if ((Math.abs(dy) > Math.abs(dy1))) {
                System.out.println("bounce 3");
                newdy = 2.d * dy1 - dy;
                newdx = dx;
            }
            //System.out.println("bounce (" + (x + dx) + ":" + (y + dy) + ") ==> (" + (x + newdx) + ":" + (y + newdy) + ")");
            /*if (!getSimulationManager().getDataset().isInWater(new double[]{x + dx, y})) {
            newdx = 2.d * (Math.round(x) + Math.signum(dx) * 0.5d - x) - dx;
            }
            if (!getSimulationManager().getDataset().isInWater(new double[]{x, y + dy})) {
            newdy = 2.d * (Math.round(y) + Math.signum(dy) * 0.5d - y) - dy;
            }
            if (!getSimulationManager().getDataset().isInWater(new double[]{x + newdx, y + newdy})) {
            if (iter < 10) {
            return bounceCostline(x, y, newdx, newdy, iter);
            }
            }*/
        }
        return new double[]{newdx, newdy};
    }

    public void applyMove(boolean bouncyCostline) {

        if (bouncyCostline) {
            double[] bounce = bounceCostline(x, y, dx, dy);
            dx = bounce[0];
            dy = bounce[1];
        }
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

    /**
     * Increments (x, y, z) with (dx, dy, dz) if 3 dimensions move.
     * Increments (x, y) with (dx, dy) if 2 dimensions move.
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

    public double[] getGridCoordinates() {
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

    public double[] getGeoCoordinates() {
        return is3D
                ? new double[]{lon, lat, depth}
                : new double[]{lon, lat};
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
                ? "(" + lon + ", " + lat + ", " + depth + ")"
                : "(" + lon + ", " + lat + ")";
    }

    public boolean isInWater() {
        return getSimulationManager().getDataset().isInWater(getGridCoordinates());
    }

    public boolean isOnEdge() {
        return getSimulationManager().getDataset().isOnEdge(getGridCoordinates());
    }
}
