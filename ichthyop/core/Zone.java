package ichthyop.core;

/** import AWT */
import java.awt.Color;

/** local import */
import ichthyop.io.Dataset;

/**
 * <p>
 * This class defines a geographical area used by the program to locate
 * the release areas or the recruitment areas. It provides a tool to determine
 * whether any grid point belongs to the Zone.
 * </p>
 *
 * The geographical area is defined as followed :
 * Four geodesics demarcation points {lon, lat} P1, P2, P3 & P4 and
 * two depths depth1 & depth2.
 * <p>
 * Longitude is expressed in East degree and latitude in North degree.
 * Depths are positive Integers.
 * </p>
 * The area is first delimited by the polygon (P1 P2 P3 P4).
 * The points dont have to be in the water, but make sure they belong to the
 * geographical grid of the simulation.
 * The four demarcation points P1(plon1, plat1) to P4(plon4, plat4) must be
 * recorded in the clockwise or anticlockwise direction.
 * Then, another routine isolates the area of the polygone that is contained
 * between the bathymetric lines depth1 & depth2.
 * At last the user can choose the color
 * (Red[0, 255], Green[0, 255], Blue[0, 255]) of the area.

 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class Zone {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Type of zone,  release zone or recruitment.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of zone.
     */
    final private int type;
    /**
     * Longitudes (east) of the demarcation points.
     */
    final private double[] lon;
    /**
     * Latitudes (north) of the demarcation points.
     */
    final private double[] lat;
    /**
     * Lower bathymetric line [meter]
     */
    private int bathyLineMin;
    /**
     * Higher bathymetric line [meter]
     */
    private int bathyLineMax;
    /**
     * Color RGB of the zone.
     */
    private Color color;
    /**
     * Index of the zone (positive integer)
     */
    private int index;
    /**
     * x-coordinates of the demarcation points.
     */
    private double[] xGrid;
    /**
     * y-coordinates of the demarcation points.
     */
    private double[] yGrid;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new Zone with the specified arguments. Remind the four
     * points P1(plon1, plat1) to P4(plon4, plat4) demarcating the geographical
     * area must be recorded in the clockwise or anticlockwise direction.
     *
     * @param type an integer characterizing the type of zone, release or
     * recruitment.
     * @param index the index of the zone
     * @param lon1 a double, the longitude (east) of the 1st demarcation point
     * @param lat1 a double, the latitude (north of the 1st demarcation point
     * @param lon2 a double, the longitude (east) of the 2nd demarcation point
     * @param lat2 a double, the latitude (north of the 2nd demarcation point
     * @param lon3 a double, the longitude (east) of the 3rd demarcation point
     * @param lat3 a double, the latitude (north of the 3rd demarcation point
     * @param lon4 a double, the longitude (east) of the 4th demarcation point
     * @param lat4 a double, the latitude (north of the 4th demarcation point
     * @param depth1 an int, the lower bathymetric line
     * @param depth2 an int, the higher bathymetric line
     * @param color the Color of the zone
     */
    public Zone(int type,
                int index,
                double lon1, double lat1,
                double lon2, double lat2,
                double lon3, double lat3,
                double lon4, double lat4,
                int depth1, int depth2,
                Color color) {

        lon = new double[4];
        lat = new double[4];

        this.type = type;
        this.index = index;
        this.lon[0] = lon1;
        this.lon[1] = lon2;
        this.lon[2] = lon3;
        this.lon[3] = lon4;
        this.lat[0] = lat1;
        this.lat[1] = lat2;
        this.lat[2] = lat3;
        this.lat[3] = lat4;
        this.bathyLineMin = depth1;
        this.bathyLineMax = depth2;
        this.color = color;

        xGrid = new double[5];
        yGrid = new double[5];

    }

    /**
     * Constructs an empty white zone of the specified type and index.
     *
     * @param type an integer characterizing the type of zone, release or
     * recruitment.
     * @param index the index of the zone
     */
    public Zone(int type, int index) {

        this(type, index, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12000, Color.WHITE);
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Transforms longitude and latitude of the demarcation points into
     * grid coordinates
     */
    public void geo2Grid() {

        for (int i = 0; i < 4; i++) {
            double[] po = Dataset.getInstance().geo2Grid(lon[i], lat[i]);
            xGrid[i] = po[0];
            yGrid[i] = po[1];
        }
        xGrid[4] = xGrid[0];
        yGrid[4] = yGrid[0];
    }

    /**
     * Determines whether the specified grid point (x, y) belongs to the zone.
     * It first ensures the grid point (x, y) is inside the polygon
     * (P1 P2 P3 P4) and then checks whether (x, y) is in between bathymetric
     * line depth1 and depth2.
     *
     * <p>
     * The algorithm to determine whether a point (X0, Y0) is inside a polygon
     * has been adapted from a function in ROMS/UCLA code, originally written by
     * Alexander F. Shchepetkin and Hernan G. Arango. Please find below an
     * extract of the ROMS/UCLA documention.
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
     * @param x a double, the x-coordinate of the grid point.
     * @param y a double, the x-coordinate of the grid point.
     * @return <code>true</code> if (x, y) belongs to the zone,
     *         <code>false</code>otherwise.
     */
    public boolean isXYInZone(double x, double y) {

        boolean isInBox;
        int inc, crossings;
        double dx1, dx2, dxy;

        isInBox = true;
        crossings = 0;

        for (int k = 0; k < 4; k++) {
            if (xGrid[k] != xGrid[k + 1]) {
                dx1 = x - xGrid[k];
                dx2 = xGrid[k + 1] - x;
                dxy = dx2 * (y - yGrid[k]) - dx1 * (yGrid[k + 1] - y);
                inc = 0;
                if ((xGrid[k] == x) & (yGrid[k] == y)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (y >= yGrid[k])) |
                           ((dx2 == 0.) & (y >= yGrid[k + 1]))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) &
                           ((xGrid[k + 1] - xGrid[k]) * dxy >= 0.)) {
                    inc = 2;
                }
                if (xGrid[k + 1] > xGrid[k]) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }
        if (crossings == 0) {
            isInBox = false;
        }

        if (isInBox) {
            isInBox = (Dataset.getInstance().getBathy((int) Math.round(x), (int) Math.round(y)) >
                       bathyLineMin &
                       Dataset.getInstance().getBathy((int) Math.round(x), (int) Math.round(y)) <
                       bathyLineMax);
        }

        return (isInBox);
    }

////////////////////
// Getters & setters
////////////////////

    /**
     * Gets the longitude of the specified demarcation point.
     *
     * @param i the index of the demarcation point
     * @return a double, the longitude (east) of the ith demarcation point.
     */
    public double getLon(int i) {
        return lon[i];
    }

    /**
     * Sets the longitude of the specified demarcation point.
     *
     * @param i the index of the demarcation point
     * @param lon a double, the longitude (east) of the ith demarcation point.
     */
    public void setLon(int i, double lon) {
        this.lon[i] = lon;
    }

    /**
     * Gets the latitude of the specified demarcation point.
     *
     * @param i the index of the demarcation point
     * @return a double, the latitude (north) of the ith demarcation point.
     */
    public double getLat(int i) {
        return lat[i];
    }

    /**
     * Sets the latitude of the specified demarcation point.
     *
     * @param i the index of the demarcation point
     * @param lon a double, the latitude (north) of the ith demarcation point.
     */
    public void setLat(int i, double lat) {
        this.lat[i] = lat;
    }

    /**
     * Gets the lower bathymetric line
     *
     * @return an int, the depth [meter] of the lower bathymetric line.
     */
    public int getBathyMin() {
        return bathyLineMin;
    }

    /**
     * Sets the lower bathymetric line
     * @param bathy an int, the depth [meter] of the lower bathymetric line.
     */
    public void setBathyMin(int bathy) {
        this.bathyLineMin = bathy;
    }

    /**
     * Gets the higher bathymetric line
     *
     * @return an int, the depth [meter] of the higher bathymetric line.
     */
    public int getBathyMax() {
        return bathyLineMax;
    }

    /**
     * Sets the higher bathymetric line
     * @param bathy an int, the depth [meter] of the higher bathymetric line.
     */
    public void setBathyMax(int bathy) {
        this.bathyLineMax = bathy;
    }

    /**
     * Gets the color of the zone
     * @return the Color of the zone.
     * @see java.awt.Color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the zone.
     *
     * @param color the Color of the zone
     * @see java.awt.Color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets the type of zone, release or recruitment.
     *
     * @return an int characterizing the type of zone.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of zone.
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the index of the zone.
     *
     * @return a positive integer, the index of the zone.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the zone.
     *
     * @param index a positive integer, index of the zone.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets the smallest x-coordinate of the demarcation points.
     *
     * @return a double, the x-coordinate of the demarcation point closest to
     * the grid origin.
     */
    public double getXmin() {

        double xmin = xGrid[0];
        for (int i = 1; i < 4; i++) {
            xmin = Math.min(xmin, xGrid[i]);
        }
        return xmin;
    }

    /**
     * Gets the smallest y-coordinate of the demarcation points.
     *
     * @return a double, the y-coordinate of the demarcation point closest to
     * the grid origin.
     */
    public double getYmin() {

        double ymin = yGrid[0];
        for (int i = 1; i < 4; i++) {
            ymin = Math.min(ymin, yGrid[i]);
        }
        return ymin;
    }

    /**
     * Gets the biggest x-coordinate of the demarcation points.
     *
     * @return a double, the x-coordinate of the demarcation point farthest from
     * the grid origin.
     */
    public double getXmax() {
        double xmax = xGrid[0];
        for (int i = 1; i < 4; i++) {
            xmax = Math.max(xmax, xGrid[i]);
        }
        return xmax;
    }

    /**
     * Gets the biggest y-coordinate of the demarcation points.
     *
     * @return a double, the y-coordinate of the demarcation point farthest from
     * the grid origin.
     */
    public double getYmax() {
        double ymax = yGrid[0];
        for (int i = 1; i < 4; i++) {
            ymax = Math.max(ymax, yGrid[i]);
        }
        return ymax;
    }

    //---------- End of class
}
