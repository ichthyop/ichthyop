package org.previmer.ichthyop;

import java.util.ArrayList;

/** import AWT */
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
public class Zone extends SimulationManagerAccessor {

    private ArrayList<RhoPoint> polygon = new ArrayList(3);
    private TypeZone type;
    /**
     * Lower bathymetric line [meter]
     */
    private int inshoreLine;
    /**
     * Higher bathymetric line [meter]
     */
    private int offshoreLine;
    /**
     *  [meter]
     */
    private int lowerDepth;
    /**
     *  [meter]
     */
    private int upperDepth;
    int index;

    public Zone(TypeZone type, int index) {
        this.type = type;
        this.index = index;
    }

    public void setInshoreLine(int inshoreLine) {
        this.inshoreLine = inshoreLine;
    }

    public void setOffshoreLine(int offshoreLine) {
        this.offshoreLine = offshoreLine;
    }

    public void setLowerDepth(int lowerDepth) {
        this.lowerDepth = lowerDepth;
    }

    public int getUpperDepth() {
        return upperDepth;
    }

    public int getLowerDepth() {
        return lowerDepth;
    }

    public void setUpperDepth(int upperDepth) {
        this.upperDepth = upperDepth;
    }

    public void addPoint(RhoPoint point) {
        polygon.add(point);
    }

    public void init() {

        for (RhoPoint rhoPoint : polygon) {
            rhoPoint.geo2Grid();
        }
        polygon.add((RhoPoint) polygon.get(0).clone());
    }

    public boolean isPointInZone(RhoPoint point) {

        boolean isInZone = true;
        if (point.is3D()) {
            isInZone = isDepthInLayer(Math.abs(point.getDepth()));
            //System.out.println("depth in layer " + (float)Math.abs(point.getDepth()) + " " + isDepthInLayer(Math.abs(point.getDepth())));
        }
        isInZone = isInZone && isXYBetweenBathyLines(point.getX(), point.getY()) && isXYInPolygon(point.getX(), point.getY());

        return isInZone;
    }

    private boolean isDepthInLayer(double depth) {
        return depth <= lowerDepth & depth > upperDepth;
    }

    private boolean isXYBetweenBathyLines(double x, double y) {
        return (getSimulationManager().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) >
                inshoreLine &
                getSimulationManager().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) <
                offshoreLine);
    }

    private boolean isXYInPolygon(double x, double y) {

        boolean isInBox;
        int inc, crossings;
        double dx1, dx2, dxy;

        isInBox = true;
        crossings = 0;

        for (int k = 0; k < polygon.size() - 1; k++) {
            if (polygon.get(k).getX() != polygon.get(k + 1).getX()) {
                dx1 = x - polygon.get(k).getX();
                dx2 = polygon.get(k + 1).getX() - x;
                dxy = dx2 * (y - polygon.get(k).getY()) - dx1 * (polygon.get(k + 1).getY() - y);
                inc = 0;
                if ((polygon.get(k).getX() == x) & (polygon.get(k).getY() == y)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (y >= polygon.get(k).getY())) |
                        ((dx2 == 0.) & (y >= polygon.get(k + 1).getY()))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) &
                        ((polygon.get(k + 1).getX() - polygon.get(k).getX()) * dxy >= 0.)) {
                    inc = 2;
                }
                if (polygon.get(k + 1).getX() > polygon.get(k).getX()) {
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
            isInBox = (getSimulationManager().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) >
                    inshoreLine &
                    getSimulationManager().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) <
                    offshoreLine);
        }

        return (isInBox);
    }

    /**
     * Gets the type of zone, release or recruitment.
     *
     * @return an int characterizing the type of zone.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of zone.
     */
    public TypeZone getType() {
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
     * Gets the smallest x-coordinate of the demarcation points.
     *
     * @return a double, the x-coordinate of the demarcation point closest to
     * the grid origin.
     */
    public double getXmin() {

        double xmin = polygon.get(0).getX();
        for (int k = 0; k < polygon.size(); k++) {
            xmin = Math.min(xmin, polygon.get(k).getX());
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

        double ymin = polygon.get(0).getY();
        for (int i = 1; i < 4; i++) {
            ymin = Math.min(ymin, polygon.get(i).getY());
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
        double xmax = polygon.get(0).getX();
        for (int i = 1; i < 4; i++) {
            xmax = Math.max(xmax, polygon.get(i).getX());
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
        double ymax = polygon.get(0).getY();
        for (int i = 1; i < 4; i++) {
            ymax = Math.max(ymax, polygon.get(i).getY());
        }
        return ymax;
    }

    @Override
    public String toString() {
        StringBuffer zoneStr = new StringBuffer(getType().toString());
        zoneStr.append(' ');
        zoneStr.append("zone ");
        zoneStr.append(getIndex());
        zoneStr.append('\n');
        zoneStr.append("Polygon [");
        for (RhoPoint point : polygon) {
            zoneStr.append(point.toString());
            zoneStr.append(" ");
        }
        zoneStr.append(']');
        zoneStr.append('\n');
        zoneStr.append("shore-lines (");
        zoneStr.append(inshoreLine);
        zoneStr.append("m, ");
        zoneStr.append(offshoreLine);
        zoneStr.append("m) depth-lines (");
        zoneStr.append(upperDepth);
        zoneStr.append("m, ");
        zoneStr.append(lowerDepth);
        zoneStr.append("m)");

        return zoneStr.toString();
    }
    //---------- End of class
}
